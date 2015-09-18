package training.commands;

import com.intellij.codeInsight.editorActions.SelectWordUtil;
import com.intellij.find.impl.livePreview.SelectionManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import org.jdom.Element;

import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class SetSelectionCommand extends Command {

    final public static String START_SELECT_POSITION = "start-position";
    final public static String END_SELECT_POSITION = "end-position";


    public SetSelectionCommand(){
        super(CommandType.SETSELECTION);
    }

    @Override
    public void execute(ExecutionList executionList) throws InterruptedException, ExecutionException, BadCommandException {

        int start_line;
        int start_column;
        int end_line;
        int end_column;

        Element element = executionList.getElements().poll();

        if (element.getAttribute(START_SELECT_POSITION) != null){
            String positionString = (element.getAttribute(START_SELECT_POSITION).getValue());
            String[] splitStrings = positionString.split(":");
            assert(splitStrings.length == 2);

            start_line = Integer.parseInt(splitStrings[0]);
            start_column = Integer.parseInt(splitStrings[1]);

            if (element.getAttribute(END_SELECT_POSITION) != null) {
                positionString = (element.getAttribute(END_SELECT_POSITION).getValue());
                splitStrings = positionString.split(":");
                assert(splitStrings.length == 2);

                end_line = Integer.parseInt(splitStrings[0]);
                end_column = Integer.parseInt(splitStrings[1]);
            } else {
                throw new BadCommandException(this);
            }
        } else {
            throw new BadCommandException(this);
        }

        //decrease all parameters;
        start_line--;
        start_column--;
        end_line--;
        end_column--;

        selectInDocument(executionList, start_line, start_column, end_line, end_column);

        startNextCommand(executionList);
    }

    private void selectInDocument(ExecutionList executionList, int start_line, int start_column, int end_line, int end_column) {
        final Editor editor = executionList.getEditor();
        final LogicalPosition blockStart = new LogicalPosition(start_line, start_column);
        final LogicalPosition blockEnd = new LogicalPosition(end_line, end_column);

        editor.getSelectionModel().setSelection(editor.logicalPositionToOffset(blockStart), editor.logicalPositionToOffset(blockEnd));
    }
}
