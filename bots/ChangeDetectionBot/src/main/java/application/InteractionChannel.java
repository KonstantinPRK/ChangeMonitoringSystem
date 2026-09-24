package application;


import application.commandHandler.CommandHandler;
import application.commandHandler.CommandType;

import java.util.concurrent.Executor;


public final class InteractionChannel {
    MessengerDispatcher messengerDispatcher;
    CommandHandler commandHandler;
    Executor executor;

    private User user;

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

        //
    }

    public void stop() {
        //

        status = InteractionStatus.CLOSED;
    }

   public boolean isOpen(){
       return status == InteractionStatus.OPEN;
   }


   private void working(){
        while(isOpen()){
            CommandType commandType = messengerDispatcher.getCommand(user);
            conversationState = commandHandler.getConversationState(commandType);
            String botRespond = commandHandler.process(commandType, user);

            messengerDispatcher.sendMessage(botRespond);
        }

   }


    public void notify(String message) {
        conversationState = ConversationState.WAITING_FOR_SUBSCRIPTION_NOTIFICATION;
        messengerDispatcher.sendMessage(message);
    }
}