package training.editor.eduUI;

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

}
