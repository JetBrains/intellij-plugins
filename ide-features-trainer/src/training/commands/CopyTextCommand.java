package training.commands;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.util.DocumentUtil;
import org.jdom.Element;

/**
 * Created by karashevich on 30/01/15.
 */
public class CopyTextCommand extends Command {

    public CopyTextCommand(){
        super(CommandType.COPYTEXT);
    }

    @Override
    public void execute(final ExecutionList executionList) throws InterruptedException {

        Element element = executionList.getElements().poll();
//        updateDescription(element, infoPanel, editor);

        final String finalText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        DocumentUtil.writeInRunUndoTransparentAction(new Runnable() {
            @Override
            public void run() {
                executionList.getEditor().getDocument().insertString(0, finalText);

            }
        });

        startNextCommand(executionList);

    }

}
