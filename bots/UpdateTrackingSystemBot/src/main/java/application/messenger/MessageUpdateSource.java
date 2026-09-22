package application.messenger;

import java.util.List;

public interface MessageUpdateSource {
    List<IncomingMessage> poll();
}

