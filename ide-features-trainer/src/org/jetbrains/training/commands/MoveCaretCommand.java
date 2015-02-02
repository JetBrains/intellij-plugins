package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.CommandFactory;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.Queue;

/**
 * Created by karashevich on 30/01/15.
 */
public class MoveCaretCommand extends Command {

    public MoveCaretCommand(){
        super(CommandType.MOVECARET);
    }

    @Override
    public void execute(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel) throws InterruptedException {

        Element element = elements.poll();
        updateDescription(element, infoPanel, editor);

        final String offsetString =(element.getAttribute("offset").getValue());
        final int offset = Integer.parseInt(offsetString);

        WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
            @Override
            public void run() {
                editor.getCaretModel().moveToOffset(offset);
                try {
                    CommandFactory.buildCommand(elements.peek()).execute(elements, lesson, editor, e, document, target, infoPanel);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });

    }


}
