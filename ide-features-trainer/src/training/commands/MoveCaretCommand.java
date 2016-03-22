package training.commands;

import com.intellij.openapi.editor.LogicalPosition;
import org.jdom.Element;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class MoveCaretCommand extends Command {

    final public static String MOVECARET_OFFSET = "offset";
    final public static String MOVECARET_POSITION = "position";

    public MoveCaretCommand(){
        super(CommandType.MOVECARET);
    }

    @Override
    public void execute(ExecutionList executionList) throws InterruptedException, ExecutionException {
        Element element = executionList.getElements().poll();

        if (element.getAttribute(MOVECARET_OFFSET) != null) {
            final String offsetString = (element.getAttribute(MOVECARET_OFFSET).getValue());
            final int offset = Integer.parseInt(offsetString);

            executionList.getEditor().getCaretModel().moveToOffset(offset);
        } else if (element.getAttribute(MOVECARET_POSITION) != null){
            final String positionString = (element.getAttribute(MOVECARET_POSITION).getValue());
            String[] splitStrings = positionString.split(":");
            assert(splitStrings.length == 2);

            final int line = Integer.parseInt(splitStrings[0]);
            final int column = Integer.parseInt(splitStrings[1]);

            executionList.getEditor().getCaretModel().moveToLogicalPosition(new LogicalPosition(line - 1, column - 1));
        }

        startNextCommand(executionList);
    }
}
