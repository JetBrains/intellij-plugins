package training.commands;

import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.find.FindResult;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class MoveCaretCommand extends Command {

    final public static String MOVECARET_OFFSET = "offset";
    final public static String MOVECARET_POSITION = "position";
    final public static String MOVECARET_STRING = "string";

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
        } else if (element.getAttribute(MOVECARET_STRING) != null) {
            final Editor editor = executionList.getEditor();
            final Document document = editor.getDocument();
            final Project project = executionList.getProject();

            final FindManager findManager = FindManager.getInstance(project);
            final FindModel model = findManager.getFindInFileModel().clone();
            model.setGlobal(false);
            model.setReplaceState(false);

            final String value_start = element.getAttribute(MOVECARET_STRING).getValue();
            model.setStringToFind(value_start);
            final FindResult start = FindManager.getInstance(project).findString(document.getCharsSequence(), 0, model);

            executionList.getEditor().getCaretModel().moveToOffset(start.getStartOffset());
        }

        startNextCommand(executionList);
    }
}
