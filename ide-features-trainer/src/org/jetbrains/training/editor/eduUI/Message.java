package org.jetbrains.training.editor.eduUI;

import org.jetbrains.annotations.NotNull;

/**
 * Created by karashevich on 01/09/15.
 */
public class Message {



    public enum MessageType {TEXT_REGULAR, TEXT_BOLD, SHORTCUT, CODE;}
    @NotNull
    private String messageText;

    @NotNull
    private MessageType messageType;
    public Message(@NotNull String messageText, @NotNull MessageType messageType) {
        this.messageText = messageText;
        this.messageType = messageType;
    }

    public String getText() {
        return messageText;
    }

    public MessageType getType() {
        return messageType;
    }

    public boolean isText() {
        return messageType == MessageType.TEXT_REGULAR || messageType == MessageType.TEXT_BOLD;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageText='" + messageText + '\'' +
                ", messageType=" + messageType +
                '}';
    }
}
