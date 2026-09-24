package application;

import java.util.concurrent.ExecutorService;

public class TrackerSubscriptionBridge {
    private ExecutorService executor;

    SubscriptionManager subscriptionManager;
    TrackerManager trackerManager;


    public void start(){
        executor.submit(this::forwardLinkNotifications);
        executor.submit(this::frowardLinkRequests);
    }

    public void stop(){
        executor.shutdown();
    }


    private void frowardLinkRequests(){
   //от сервиса к трекеру - получение запроса на действие со ссылкой от сервиса (добавление, удаление)
        try {
            while (!Thread.currentThread().isInterrupted()) {
                LinkRequest[] linkRequests = subscriptionManager.takeLinkRequests();

                trackerManager.putLinkRequests(linkRequests);
            }

        } catch (Exception exception) {
            Thread.currentThread().interrupt();

        }
    }

    private void forwardLinkNotifications(){
        //от трекера к сервису - передача от субскрайбера трекеру действий по подпискам - добавление или удаление
        try {
            while (!Thread.currentThread().isInterrupted()) {
                LinkNotification[] linkNotifications = trackerManager.takeLinkNotifications();

                subscriptionManager.putLinkNotifications(linkNotifications);
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