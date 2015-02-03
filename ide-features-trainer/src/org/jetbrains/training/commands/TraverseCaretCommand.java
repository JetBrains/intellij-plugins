package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.CommandFactory;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.commands.util.PerformActionUtil;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.Queue;
import java.util.concurrent.ExecutionException;

import static org.jetbrains.training.commands.util.PerformActionUtil.sleepHere;

/**
 * Created by karashevich on 30/01/15.
 */
public class TraverseCaretCommand extends Command {

    public TraverseCaretCommand(){
        super(CommandType.TRAVERSECARET);
    }

    @Override
    public void execute(Queue<Element> elements, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException, ExecutionException {

        Element element = elements.poll();
        updateDescription(element, infoPanel, editor);

        boolean isTraversing = true;
        int delay = 20;

        final String stopString = (element.getAttribute("stop").getValue());
        final int stop = Integer.parseInt(stopString);

        if (element.getAttribute("delay") != null) {
            delay = Integer.parseInt(element.getAttribute("delay").getValue());
        }

        while (isTraversing) {
            isTraversing = !(editor.getCaretModel().getOffset() == stop);

//            sleepHere(editor, 20);
            //If caret stay on different line than move down (or up)
            //Move caret down
            if (editor.getCaretModel().getVisualLineEnd() < stop) {
                PerformActionUtil.performAction("EditorDown", editor, e);
            }  else if (editor.getCaretModel().getVisualLineStart() > stop) {
                //Move caret up
                PerformActionUtil.performAction("EditorUp", editor, e);
            } else {
                final int j = editor.getCaretModel().getOffset();
                //traverse caret inside
                if (j > stop) {
                            editor.getCaretModel().moveToOffset(j - 1);

                } else if (j < stop) {
                            editor.getCaretModel().moveToOffset(j + 1);
                }
            }
        }

        //execute next
        startNextCommand(elements, lesson, editor, e, document, target ,infoPanel);
    }
}
