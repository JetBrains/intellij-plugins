package org.jetbrains.training.editor.eduUI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by karashevich on 30/06/15.
 */
public class LessonMessage {

    private ArrayList<Message> myMessages;
    private int start;
    private int end;

    LessonMessage(String text, int start, int end){
        if (myMessages == null) myMessages = new ArrayList<Message>();
        myMessages.add(new Message(text, Message.MessageType.TEXT_REGULAR));
        this.start = start;
        this.end = end;
    }

    LessonMessage(Message[] messages, int start, int end){
        if (myMessages == null) myMessages = new ArrayList<Message>();
        myMessages.addAll(Arrays.asList(messages));
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void appendMacCheck() {
        final Message message = new Message(" ✓", Message.MessageType.TEXT_BOLD);
        myMessages.add(message);
        this.end += 2;
    }

    public void appendWinCheck() {
        final Message message = new Message(" ", Message.MessageType.CHECK);
        myMessages.add(message);
        this.end += 2;
    }
}
