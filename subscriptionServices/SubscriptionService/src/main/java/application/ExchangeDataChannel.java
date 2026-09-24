package application;

import java.util.concurrent.ExecutorService;

public class ExchangeDataChannel {
    TrackerSubscriptionBridge trackerSubscriptionBridge;
    BotSubscriptionBridge botSubscriptionBridge;

    public void start(){
        trackerSubscriptionBridge.start();
        botSubscriptionBridge.start();
    }

    public void stop(){
        trackerSubscriptionBridge.stop();
        botSubscriptionBridge.stop();
    }
}
