import application.SubscriptionService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(SubscriptionService.class);

        SubscriptionService subscriptionService =
            context.getBean(SubscriptionService.class);

        subscriptionService.start();
    }
}
