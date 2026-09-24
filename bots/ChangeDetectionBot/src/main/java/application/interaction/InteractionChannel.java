package application.interaction;

import application.command.Command;
import application.command.CommandHandler;
import application.command.CommandResult;
import application.command.CommandType;
import application.telegram.MessengerDispatcher;
import application.telegram.TelegramMessage;
import application.user.User;

import java.util.concurrent.Executor;

public class InteractionChannel {
    MessengerDispatcher messengerDispatcher;
    CommandHandler commandHandler;
    Executor executor;

    private User user;
    private Command activeCommand;

    private volatile InteractionStatus status = InteractionStatus.CLOSED;
    private volatile ConversationState conversationState = ConversationState.NEW;


    private InteractionChannel(User user) {
        this.user = user;
    }


    public static InteractionChannel openNewInteractionChannel(User user) {
        return new InteractionChannel(user);
    }


    public void start() {
        status = InteractionStatus.OPEN;
        executor.execute(this::working);
    }


    public void stop() {
        status = InteractionStatus.CLOSED;
    }


    public boolean isOpen() {
        return status == InteractionStatus.OPEN;
    }


    public void notify(String message) {
        messengerDispatcher.sendMessage(message);
    }


    private void working() {
        try {
            while (isOpen()) {
                TelegramMessage message =
                    messengerDispatcher.takeMessage();

                processMessage(message.text());
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        }
    }


    private void processMessage(String message) {
        CommandType commandType = CommandType.fromText(message);

        if (commandType != null) {
            startCommand(commandType);
            return;
        }

        continueActiveCommand(message);
    }


    private void startCommand(CommandType commandType) {
        activeCommand = commandHandler.openCommand(commandType);

        CommandResult result = activeCommand.start(user);

        applyCommandResult(result);
    }


    private void continueActiveCommand(String message) {
        if (activeCommand == null) {
            messengerDispatcher.sendMessage(
                "Неизвестная команда. Используйте /help."
            );

            return;
        }

        CommandResult result = activeCommand.process(
            user,
            message
        );

        applyCommandResult(result);
    }


    private void applyCommandResult(CommandResult result) {
        conversationState = activeCommand.getConversationState();

        if (result.replyRequired()) {
            messengerDispatcher.sendMessage(result.text());
        }

        if (activeCommand.isCompleted()) {
            activeCommand = null;
            conversationState = ConversationState.WAITING_FOR_USER_COMMAND;
        }
    }
}
