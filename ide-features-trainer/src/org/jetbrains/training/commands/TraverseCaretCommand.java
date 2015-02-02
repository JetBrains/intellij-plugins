package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.impl.CaretModelImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.Command;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.List;

/**
 * Created by karashevich on 30/01/15.
 */
public class TraverseCaretCommand extends Command {

    public TraverseCaretCommand(){
        super(CommandType.TRAVERSECARET);
    }

    @Override
    public void execute(Element element, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException {

        boolean isTraversing = true;
        int delay = 20;

        final String stopString = (element.getAttribute("stop").getValue());
        final int stop = Integer.parseInt(stopString);

        if (element.getAttribute("delay") != null) {
            delay = Integer.parseInt(element.getAttribute("delay").getValue());
        }

        int i = 0;

        Element elementDown = new Element("Action").setAttribute("action", "EditorDown").setAttribute("delay", Integer.toString(delay));
        Element elementUp = new Element("Action").setAttribute("action", "EditorUp").setAttribute("delay", Integer.toString(delay));

        while (isTraversing) {
            isTraversing = !(editor.getCaretModel().getOffset() == stop);
            Thread.sleep(delay);
            //If caret stay on different line than move down (or up)
            //Move caret down
            if (editor.getCaretModel().getVisualLineEnd() < stop) {
                (new ActionCommand()).execute(elementDown, lesson, editor, e, document, target, infoPanel);
            }  else if (editor.getCaretModel().getVisualLineStart() > stop) {
                //Move caret up
                (new ActionCommand()).execute(elementUp, lesson, editor, e, document, target, infoPanel);
            } else {
                final int j = editor.getCaretModel().getOffset();
                //traverse caret inside
                if (j > stop) {
                    WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
                        @Override
                        public void run() {
                            editor.getCaretModel().moveToOffset(j - 1);
                        }
                    });
                } else if (j < stop) {
                    WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
                        @Override
                        public void run() {
                            editor.getCaretModel().moveToOffset(j + 1);
                        }
                    });
                }
            }
        }
    }
}
