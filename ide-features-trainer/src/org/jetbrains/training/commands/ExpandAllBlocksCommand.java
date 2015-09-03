package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.IdeActions;
import org.jetbrains.training.commandsEx.util.PerformActionUtil;

import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class ExpandAllBlocksCommand extends Command {

    public ExpandAllBlocksCommand(){
        super(CommandType.EXPANDALLBLOCKS);
    }

    @Override
    public void execute(final ExecutionList executionList) {
        //Block caret and perform next command
//        ActionManager.getInstance().getAction()
        executionList.getElements().poll();
        executionList.getEditor().getSettings().setAutoCodeFoldingEnabled(false);
        executionList.getEditor().getSettings().setAllowSingleLogicalLineFolding(false);
        startNextCommand(executionList);

    }
}
