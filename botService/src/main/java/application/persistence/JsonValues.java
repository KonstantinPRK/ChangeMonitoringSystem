package application.persistence;

import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Читает и преобразует входные данные для {@code JsonValues}.
 */
@Component
public class JsonValues {
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };
    private final ObjectMapper objectMapper;


    public JsonValues(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public String writeStrings(List<String> values) {
        return objectMapper.writeValueAsString(values);
    }


    public List<String> readStrings(String json) {
        return objectMapper.readValue(json, STRING_LIST);
    }
}
