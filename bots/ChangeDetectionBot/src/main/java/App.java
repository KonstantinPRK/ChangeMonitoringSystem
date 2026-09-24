import application.ChangeDetectionBot;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("application")
public class App {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(App.class);

        ChangeDetectionBot bot = context.getBean(ChangeDetectionBot.class);

        Runtime.getRuntime().addShutdownHook(
            new Thread(
                () -> {
                    bot.stop();
                    context.close();
                },
                "changedetection-shutdown"
            )
        );

        try {
            bot.start();
            bot.awaitTermination();

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            bot.stop();
            context.close();
        }
    }
}
