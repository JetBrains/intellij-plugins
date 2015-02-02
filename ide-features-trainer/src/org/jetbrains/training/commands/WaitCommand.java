package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.Alarm;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.CommandFactory;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.Queue;

/**
 * Created by karashevich on 30/01/15.
 */
public class WaitCommand extends Command {

    public WaitCommand(){
        super(CommandType.WAIT);
    }

    @Override
    public void execute(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel) throws InterruptedException {

        Element element = elements.poll();
        updateDescription(element, infoPanel, editor);
        int delay = 1000;

        if (element.getAttribute("delay") != null) {
            delay = Integer.parseInt(element.getAttribute("delay").getValue());
        }

        final int finalDelay = delay;
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                (new Alarm()).addRequest(new Runnable() {

                    @Override
                    public void run() {
                        synchronized (editor) {
                            try {
                                CommandFactory.buildCommand(elements.peek()).execute(elements, lesson, editor, e, document, target, infoPanel);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }, finalDelay);
            }

        });
    }

}
