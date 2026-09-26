package application.subscription.operation;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Реализует ответственность компонента {@code SubscriptionOperationExecutors}.
 */
@Component
public class SubscriptionOperationExecutors {
    private final Map<SubscriptionOperationType, SubscriptionOperationExecutor> executors =
            new EnumMap<>(SubscriptionOperationType.class);


    public SubscriptionOperationExecutors(List<SubscriptionOperationExecutor> operationExecutors) {
        operationExecutors.forEach(executor -> executors.put(executor.type(), executor));
    }


    public SubscriptionOperationExecutor get(SubscriptionOperationType type) {
        return executors.get(type);
    }
}
