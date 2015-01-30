package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

/**
 * Created by karashevich on 30/01/15.
 */
public class MoveCaretCommand extends Command {

    public MoveCaretCommand(){
        super(CommandType.MOVECARET);
    }

    @Override
    public void execute(Element element, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException {

        final String offsetString =(element.getAttribute("offset").getValue().toString());
        final int offset = Integer.parseInt(offsetString);


        WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
            @Override
            public void run() {
                editor.getCaretModel().moveToOffset(offset);
            }
        });

    }


}
