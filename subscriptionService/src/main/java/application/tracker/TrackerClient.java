package application.tracker;

import application.http.RemoteHttpClient;
import application.link.LinkRequest;

import org.springframework.stereotype.Component;

@Component
public class TrackerClient {
    private final TrackerRouter router;
    private final RemoteHttpClient httpClient;


    public TrackerClient(TrackerRouter router, RemoteHttpClient httpClient) {
        this.router = router;
        this.httpClient = httpClient;
    }


    public void send(LinkRequest request) {
        TrackerInstance tracker = router.route(request.link().domain());
        httpClient.post(tracker.baseUrl(), "/api/v1/links/requests", request);
    }
}
