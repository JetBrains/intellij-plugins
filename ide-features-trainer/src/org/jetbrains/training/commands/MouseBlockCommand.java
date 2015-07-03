package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.commandsEx.CommandEx;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.lesson.Lesson;

import java.util.Queue;

/**
 * Created by karashevich on 30/01/15.
 */
public class MouseBlockCommand extends Command {

    public MouseBlockCommand(){
        super(CommandType.MOUSEBLOCK);
    }

    @Override
    public void execute(ExecutionList executionList) {
        //Block mouse and perform next
        executionList.getMouseListenerHolderl().grabListeners(executionList.getEditor());

        executionList.getElements().poll();
        startNextCommand(executionList);

    }
}
