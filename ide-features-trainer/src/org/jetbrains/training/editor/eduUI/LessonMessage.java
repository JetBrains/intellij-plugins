package org.jetbrains.training.editor.eduUI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created by karashevich on 30/06/15.
 */
public class LessonMessage {

    private Message[] myMessages;
    private int start;
    private int end;

    LessonMessage(String text, int start, int end){
        myMessages = new Message[1];
        myMessages[0] = new Message(text, Message.MessageType.TEXT_REGULAR);
        this.start = start;
        this.end = end;
    }

    LessonMessage(Message[] messages, int start, int end){
        myMessages =  messages.clone();
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
