package org.jetbrains.training;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
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

    public static void process(final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException, ExecutionException {

        Queue<Element> elements = new LinkedBlockingQueue<Element>();
        if (lesson.getScn().equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }
        if (lesson.getScn().getRoot().equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        //Create queue
        for (final Element el : lesson.getScn().getRoot().getChildren()) {
            elements.add(el);
        }

        //Perform first action

        Command cmd = CommandFactory.buildCommand(elements.peek());
        cmd.execute(elements, lesson, editor, e, document, target, infoPanel);

    }



}
