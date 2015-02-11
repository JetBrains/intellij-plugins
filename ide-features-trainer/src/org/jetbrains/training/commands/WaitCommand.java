package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.Alarm;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.lesson.Lesson;
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
        (new Alarm()).addRequest(new Runnable() {
            @Override
            public void run() {
                startNextCommand(elements, lesson, editor, e, document, target, infoPanel);
            }
        }, finalDelay);

    }
}
