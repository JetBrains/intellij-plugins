package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.actionSystem.EditorAction;

/**
 * Created by karashevich on 30/01/15.
 */
public class ShowLineNumberCommand extends Command {

    public ShowLineNumberCommand(){
        super(CommandType.SHOWLINENUMBER);
    }

    @Override
    public void execute(ExecutionList executionList) {
        //Block caret and perform next command
//        ActionManager.getInstance().getAction()
        executionList.getEditor().getSettings().setLineNumbersShown(true);
        executionList.getElements().poll();
        startNextCommand(executionList);
    }
}
