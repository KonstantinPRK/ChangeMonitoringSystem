package application;

import org.springframework.stereotype.Component;

@Component
public class SubscriptionService {
    ExchangeDataChannel exchangeDataChannel;

    public SubscriptionService(ExchangeDataChannel exchangeDataChannel){
        this.exchangeDataChannel = exchangeDataChannel;
    }

    public void start() {
        exchangeDataChannel.start();
    }

    public void stop() {
        exchangeDataChannel.stop();
    }
}
