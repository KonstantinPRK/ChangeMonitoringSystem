package application.command;

import java.util.Optional;

public interface CommandCatalog {
    Optional<CommandDefinition> findByCode(String code);
}

