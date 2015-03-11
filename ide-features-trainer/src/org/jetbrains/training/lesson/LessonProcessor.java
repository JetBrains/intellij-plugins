package org.jetbrains.training.lesson;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.CommandFactory;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by karashevich on 30/01/15.
 */
public class LessonProcessor {

//    RECORDING FOR DISPOSABLE
//    private static boolean isRecording = false;

//    public static void processLesson(final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException {
//        if (lesson.getScn().equals(null)) {
//            System.err.println("Scenario is empty or cannot be read!");
//            return;
//        }
//        if (lesson.getScn().getRoot().equals(null)) {
//            System.err.println("Scenario is empty or cannot be read!");
//            return;
//        }
//
//        for (final Element element : lesson.getScn().getRoot().getChildren()) {
//
//            Command cmd = CommandFactory.buildCommand(element);
//            cmd.execute(element, lesson, editor, e, document, target, infoPanel);
//
//        }
//    }

    public static void process(final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target) throws InterruptedException, ExecutionException {

        Queue<Element> elements = new LinkedBlockingQueue<Element>();
        if (lesson.getScn().equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        if (lesson.getScn().getRoot().equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        //Create queue of Actions
        for (final Element el : lesson.getScn().getRoot().getChildren()) {
            //if element is MouseBlocked (blocks all mouse events) than add all children inside it.
            if(isMouseBlock(el)) {
                if (el.getChildren() != null) {
                    elements.add(el); //add block element
                    for(Element el1 : el.getChildren()){
                        elements.add(el1); //add inner elements
                    }
                    elements.add(new Element(Command.CommandType.MOUSEUNBLOCK.toString())); //add unblock element
                }
            } else {
                elements.add(el);
            }
        }

        //Perform first action, all next perform like a chain reaction
        MouseListenerHolder mouseListenerHolder = new MouseListenerHolder();


        Command cmd = CommandFactory.buildCommand(elements.peek());
        cmd.execute(elements, lesson, editor, e, document, target, lesson.getInfoPanel(), mouseListenerHolder);

    }

    private static boolean isMouseBlock(Element el){
        return el.getName().toUpperCase().equals(Command.CommandType.MOUSEBLOCK.toString());
    }


}
