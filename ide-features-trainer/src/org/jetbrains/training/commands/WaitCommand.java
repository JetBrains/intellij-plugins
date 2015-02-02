package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.Alarm;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

/**
 * Created by karashevich on 30/01/15.
 */
public class WaitCommand extends Command {

    public WaitCommand(){
        super(CommandType.WAIT);
    }

    @Override
    public void execute(Element element, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException {

        int delay = 1000;

        if (element.getAttribute("delay") != null) {
            delay = Integer.parseInt(element.getAttribute("delay").getValue());
        }

        final int finalDelay = delay;
        System.err.println("Delay is " + finalDelay);
        try {
                System.err.println("----Invokation");
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        System.err.println("Alarm recorded!");
                        (new Alarm()).addRequest(new Runnable() {
                            int k = 0;

                            @Override
                            public void run() {
                                k++;
                                System.err.println("Alarm trigerred! " + k);
                                synchronized (editor) {
                                    editor.notify();
                                }
                            }
                        }, finalDelay);
                    }

                });
                synchronized (editor) {
                    System.err.println(">>>>Start waiting");
                    editor.wait();
                    System.err.println("<<<<Finish waiting");
                }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
                }
    }

}
