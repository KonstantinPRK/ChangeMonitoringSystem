package application;

import application.inbox.IncomingMessageDispatcher;
import application.messenger.MessengerDataBridge;
import application.outbox.OutgoingMessageDispatcher;
import application.registration.BotRegistrationManager;
import application.subscription.operation.SubscriptionOperationDispatcher;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Управляет жизненным циклом сервиса ботов.
 */
@Component
public class BotService implements SmartLifecycle {
    private final MessengerDataBridge messengerDataBridge;
    private final BotRegistrationManager registrationManager;
    private final IncomingMessageDispatcher incomingMessageDispatcher;
    private final SubscriptionOperationDispatcher subscriptionOperationDispatcher;
    private final OutgoingMessageDispatcher outgoingMessageDispatcher;
    private final AtomicReference<BotLifecycleStatus> status =
            new AtomicReference<>(BotLifecycleStatus.NEW);


    public BotService(
            MessengerDataBridge messengerDataBridge,
            BotRegistrationManager registrationManager,
            IncomingMessageDispatcher incomingMessageDispatcher,
            SubscriptionOperationDispatcher subscriptionOperationDispatcher,
            OutgoingMessageDispatcher outgoingMessageDispatcher
    ) {
        this.messengerDataBridge = messengerDataBridge;
        this.registrationManager = registrationManager;
        this.incomingMessageDispatcher = incomingMessageDispatcher;
        this.subscriptionOperationDispatcher = subscriptionOperationDispatcher;
        this.outgoingMessageDispatcher = outgoingMessageDispatcher;
    }


    @Override
    public void start() {
        if (!status.compareAndSet(BotLifecycleStatus.NEW, BotLifecycleStatus.STARTING)
                && !status.compareAndSet(BotLifecycleStatus.STOPPED, BotLifecycleStatus.STARTING)) return;

        try {
            startDispatchers();
            registrationManager.start();
            messengerDataBridge.start();
            status.set(BotLifecycleStatus.RUNNING);

        } catch (RuntimeException exception) {
            stopStartedComponents();
            status.set(BotLifecycleStatus.STOPPED);
            throw exception;

        }
    }


    @Override
    public void stop() {
        stop(() -> { });
    }


    @Override
    public void stop(Runnable callback) {
        if (!status.compareAndSet(BotLifecycleStatus.RUNNING, BotLifecycleStatus.STOPPING)) {
            callback.run();
            return;
        }

        stopStartedComponents();
        registrationManager.stop().whenComplete((ignored, failure) -> finishStop(callback));
    }


    @Override
    public boolean isRunning() {
        return status.get() == BotLifecycleStatus.RUNNING;
    }


    @Override
    public boolean isAutoStartup() {
        return true;
    }


    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1024;
    }


    private void startDispatchers() {
        incomingMessageDispatcher.start();
        subscriptionOperationDispatcher.start();
        outgoingMessageDispatcher.start();
    }


    private void stopStartedComponents() {
        messengerDataBridge.stop();
        incomingMessageDispatcher.stop();
        subscriptionOperationDispatcher.stop();
        outgoingMessageDispatcher.stop();
    }


    private void finishStop(Runnable callback) {
        status.set(BotLifecycleStatus.STOPPED);
        callback.run();
    }
}
