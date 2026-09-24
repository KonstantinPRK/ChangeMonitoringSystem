package application;

import java.util.concurrent.ExecutorService;

public class BotSubscriptionBridge {
    private ExecutorService executor;

    SubscriptionManager subscriptionManager;
    BotManager botManager;

    public void start(){
        executor.submit(this::forwardSubscriptionNotifications);
        executor.submit(this::forwardSubscriptionRequests);
    }

    public void stop(){
        executor.shutdown();
    }

    private void forwardSubscriptionRequests(){
        //от бота к сервису
        try {
            while (!Thread.currentThread().isInterrupted()) {
                SubscriptionRequest[] requests = botManager.takeSubscriptionRequests();

                subscriptionManager.putSubscriptionRequests(requests);
            }

        } catch (Exception exception) {
            Thread.currentThread().interrupt();

        }
    }

    private void forwardSubscriptionNotifications(){
        //от сервиса к боту
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Notification[] notifications = subscriptionManager.takeBotsNotifications();

                botManager.putNotifications(notifications);
            }

        } catch (Exception exception) {
            Thread.currentThread().interrupt();

        }
    }
}

/*
бот у нас
получает ссылку
передает менеджеру подписок

менеджер подписок
получает ссылку и пользователя - связывает их

увеличивает количество пользователей по ссылке или сначала добавляет ее
если добавляет то сначала передает нужному трекеру

уменьшает количество пользователей по ссылке или удаляет ее (при 0)
если удаляет то сначала запрашивает все уведомления и отправляет запрос на удаление если никто больше не следит


трекер у нас
добавляет подписки на ссылку
уведомпляет об изменении на ссылку
удаляет подписки на ссылку
 */