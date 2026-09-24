package application;

import application.commandHandler.Command;
import application.commandHandler.CommandHandler;
import application.commandHandler.CommandType;

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

        String response = activeCommand.start(user);

        applyCommandResult(response);
    }


    private void continueActiveCommand(String message) {
        if (activeCommand == null) {
            messengerDispatcher.sendMessage(
                "Неизвестная команда. Используйте /help."
            );

            return;
        }

        String response = activeCommand.process(
            user,
            message
        );

        applyCommandResult(response);
    }


    private void applyCommandResult(String response) {
        conversationState = activeCommand.getConversationState();

        messengerDispatcher.sendMessage(response);

        if (activeCommand.isCompleted()) {
            activeCommand = null;
            conversationState = ConversationState.WAITING_FOR_USER_COMMAND;
        }
    }
}
