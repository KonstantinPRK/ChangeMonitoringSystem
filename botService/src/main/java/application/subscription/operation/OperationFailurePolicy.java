package application.subscription.operation;

import application.config.WorkerProperties;
import application.subscription.api.SubscriptionServiceException;
import application.subscription.api.SubscriptionRequestFailedException;

import org.springframework.stereotype.Component;

@Component
public class OperationFailurePolicy {
    private final WorkerProperties workerProperties;


    public OperationFailurePolicy(WorkerProperties workerProperties) {
        this.workerProperties = workerProperties;
    }


    public OperationFailureDecision decide(StoredSubscriptionOperation operation, Throwable failure) {
        if (operation.attempts() >= workerProperties.maximumAttempts()) {
            return new OperationFailureDecision(false, workerProperties.retryDelay());
        }
        if (failure instanceof SubscriptionRequestFailedException) {
            return new OperationFailureDecision(false, workerProperties.retryDelay());
        }
        if (failure instanceof SubscriptionServiceException serviceFailure
                && isPermanent(serviceFailure.statusCode())) {
            return new OperationFailureDecision(false, workerProperties.retryDelay());
        }

        return new OperationFailureDecision(true, workerProperties.retryDelay());
    }


    private boolean isPermanent(int statusCode) {
        return statusCode >= 400 && statusCode < 500 && statusCode != 408 && statusCode != 429;
    }
}
