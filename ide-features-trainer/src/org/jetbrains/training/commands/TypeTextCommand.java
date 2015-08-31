package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.commandsEx.CommandEx;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.lesson.Lesson;

import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class TypeTextCommand extends Command {

    public TypeTextCommand(){
        super(CommandType.TYPETEXT);
    }

    @Override
    public void execute(final ExecutionList executionList) throws InterruptedException, ExecutionException {

        Element element = executionList.getElements().poll();

        final String finalText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        boolean isTyping = true;
        final int[] i = {0};
        final int initialOffset = executionList.getEditor().getCaretModel().getOffset();

        while (isTyping) {
//            Thread.sleep(20);
            final int finalI = i[0];
            WriteCommandAction.runWriteCommandAction(executionList.getProject(), new Runnable() {
                @Override
                public void run() {
                    executionList.getEditor().getDocument().insertString(finalI + initialOffset, finalText.subSequence(i[0], i[0] + 1));
                    executionList.getEditor().getCaretModel().moveToOffset(finalI + 1 + initialOffset);
                }
            });
            isTyping = (++i[0] < finalText.length());
        }

        //execute next
        startNextCommand(executionList);

    }
}
