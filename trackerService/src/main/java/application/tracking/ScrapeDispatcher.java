package application.tracking;

import application.catalog.model.TrackedResource;
import application.config.WorkerProperties;
import application.metrics.ScrapeMetrics;
import application.tracking.persistence.ScrapeTaskRepository;
import application.work.ScrapeWorkSignal;
import application.work.SerializedWorker;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Передаёт ожидающие задачи подходящим обработчикам через {@code ScrapeDispatcher}.
 */
@Component
public class ScrapeDispatcher {
    private final ScrapeTaskRepository scrapeTaskRepository;
    private final LinkScrapeProcessor scrapeProcessor;
    private final ScrapeCompletion scrapeCompletion;
    private final ScrapeFailureCompletion failureCompletion;
    private final ScrapeMetrics scrapeMetrics;
    private final List<SerializedWorker> workers;
    private final AtomicBoolean running = new AtomicBoolean();


    public ScrapeDispatcher(
            ScrapeTaskRepository scrapeTaskRepository,
            LinkScrapeProcessor scrapeProcessor,
            ScrapeCompletion scrapeCompletion,
            ScrapeFailureCompletion failureCompletion,
            ScrapeMetrics scrapeMetrics,
            WorkerProperties workerProperties,
            ScrapeWorkSignal workSignal,
            @Qualifier("trackerTaskExecutor") Executor executor
    ) {
        this.scrapeTaskRepository = scrapeTaskRepository;
        this.scrapeProcessor = scrapeProcessor;
        this.scrapeCompletion = scrapeCompletion;
        this.failureCompletion = failureCompletion;
        this.scrapeMetrics = scrapeMetrics;
        workers = createWorkers(workerProperties.maximumConcurrency(), executor);
        workSignal.connect(this::signalWorkers);
    }


    public void start() {
        running.set(true);
        signalWorkers();
    }


    public void stop() {
        running.set(false);
    }


    @Scheduled(fixedDelayString = "${app.workers.recovery-interval}")
    public void recover() {
        if (running.get()) signalWorkers();
    }


    private List<SerializedWorker> createWorkers(int workerCount, Executor executor) {
        List<SerializedWorker> createdWorkers = new ArrayList<>();
        for (int index = 0; index < workerCount; index++) {
            createdWorkers.add(new SerializedWorker(executor, this::scrapeNext));
        }
        return List.copyOf(createdWorkers);
    }


    private void signalWorkers() {
        workers.forEach(SerializedWorker::signal);
    }


    private CompletionStage<Boolean> scrapeNext() {
        if (!running.get()) return CompletableFuture.completedFuture(false);
        Optional<TrackedResource> claimedResource = scrapeTaskRepository.claimNext();
        if (claimedResource.isEmpty()) return CompletableFuture.completedFuture(false);

        TrackedResource resource = claimedResource.get();
        long startedAt = System.nanoTime();
        return scrapeProcessor.inspect(resource)
                .handle((inspection, failure) -> complete(resource, inspection, failure, startedAt));
    }


    private boolean complete(
            TrackedResource resource,
            ScrapeInspection inspection,
            Throwable failure,
            long startedAt
    ) {
        Duration duration = Duration.ofNanos(System.nanoTime() - startedAt);
        if (failure != null) {
            failureCompletion.complete(resource, failure);
            scrapeMetrics.record(resource.providerKey(), resource.resourceKind(), "failed", duration);
            return true;
        }

        boolean changed = scrapeCompletion.complete(resource, inspection);
        String outcome = inspection.deferred() ? "deferred" : "completed";
        scrapeMetrics.record(resource.providerKey(), resource.resourceKind(), outcome, duration);
        if (changed) scrapeMetrics.changeDetected(resource.providerKey(), resource.resourceKind());
        return true;
    }
}
