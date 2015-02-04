package org.jetbrains.training.sandbox;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.CommandFactory;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.commands.util.PerformActionUtil;
import org.jetbrains.training.commands.util.TraverseProcessor;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by karashevich on 03/02/15.
 */
public class TestProcessor {

    public static void process(final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException, ExecutionException {


        final String finalText="public class JavaLessonExample {\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "\n" +
                "        int a = 7;\n" +
                "        double x = 0.34;\n" +
                "\n" +
                "        //Let's look an absolute value for a and x.\n" +
                "        System.out.println(\"|\" + a + \"| is \" + Math.abs(a));\n" +
                "    }\n" +
                "}";

        //Perform first action
        WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
            @Override
            public void run() {
                editor.getDocument().insertString(0, finalText);

            }
        });

        Thread.sleep(200);

        TraverseProcessor traverseProcessor = new TraverseProcessor(editor, 178, e);
        traverseProcessor.process();


    }

}
