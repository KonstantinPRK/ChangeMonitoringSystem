package application.catalog.api;

import application.catalog.synchronization.LinkSynchronizationScheduler;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Предоставляет HTTP-эндпоинты компонента {@code LinkRequestController}.
 */
@RestController
@RequestMapping("/api/v1/links/requests")
public class LinkRequestController {
    private final LinkSynchronizationScheduler synchronizationScheduler;


    public LinkRequestController(LinkSynchronizationScheduler synchronizationScheduler) {
        this.synchronizationScheduler = synchronizationScheduler;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void accept(@Valid @RequestBody LinkChangeRequest request) {
        synchronizationScheduler.requestSynchronization();
    }
}
