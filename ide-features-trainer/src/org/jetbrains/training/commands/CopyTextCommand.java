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
public class CopyTextCommand extends Command {

    public CopyTextCommand(){
        super(CommandType.COPYTEXT);
    }

    @Override
    public void execute(Element element, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException {

        updateDescription(element, infoPanel, editor);
        if (updateButton(element, infoPanel, editor)) {
            synchronized (editor) {
                editor.wait();
            }
        }

        final String finalText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        WriteCommandAction.runWriteCommandAction(e.getProject(), new Runnable() {
            @Override
            public void run() {
                editor.getDocument().insertString(0, finalText);
            }
        });


    }


}
