package application.interaction;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Читает и преобразует входные данные для {@code TextListParser}.
 */
@Component
public class TextListParser {
    public List<String> parse(String text) {
        String value = text.trim();
        if (value.isEmpty() || "-".equals(value)) return List.of();

        return Arrays.stream(value.split("\\s+"))
                .distinct()
                .toList();
    }
}
