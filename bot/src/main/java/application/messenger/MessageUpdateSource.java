package application.messenger;

import java.util.List;

@FunctionalInterface
public interface MessageUpdateSource {
    List<IncomingMessage> poll();
}
