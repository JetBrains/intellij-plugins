package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.lesson.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.Queue;

/**
 * Created by karashevich on 30/01/15.
 */
public class StartCommand extends Command {

    public StartCommand(){
        super(CommandType.START);
    }

    @Override
    public void execute(Queue<Element> elements, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel, MouseListenerHolder mouseListenerHolder) throws InterruptedException {

        Element element = elements.poll();
        updateDescription(element, infoPanel, editor);
        updateButton(element, elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);
    }


}
