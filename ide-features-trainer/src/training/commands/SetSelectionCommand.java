package training.commands;

import com.intellij.codeInsight.editorActions.SelectWordUtil;
import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.find.FindResult;
import com.intellij.find.FindUtil;
import com.intellij.find.impl.livePreview.SelectionManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.DocumentUtil;
import org.jdom.Element;

import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class SetSelectionCommand extends Command {

    final public static String START_SELECT_POSITION = "start-position";
    final public static String END_SELECT_POSITION = "end-position";

    final public static String START_SELECT_STRING = "start-string";
    final public static String END_SELECT_STRING = "end-string";


    public SetSelectionCommand(){
        super(CommandType.SETSELECTION);
    }

    //always put the caret at the end of the selection
    @Override
    public void execute(ExecutionList executionList) throws InterruptedException, ExecutionException, BadCommandException {

        int start_line = 0;
        int start_column = 0;
        int end_line = 0;
        int end_column = 0;

        int park_caret = 0;

        Element element = executionList.getElements().poll();
        final Editor editor = executionList.getEditor();

        if (element.getAttribute(START_SELECT_POSITION) != null) {
            String positionString = (element.getAttribute(START_SELECT_POSITION).getValue());
            String[] splitStrings = positionString.split(":");
            assert (splitStrings.length == 2);

            start_line = Integer.parseInt(splitStrings[0]);
            start_column = Integer.parseInt(splitStrings[1]);

            if (element.getAttribute(END_SELECT_POSITION) != null) {
                positionString = (element.getAttribute(END_SELECT_POSITION).getValue());
                splitStrings = positionString.split(":");
                assert (splitStrings.length == 2);

                end_line = Integer.parseInt(splitStrings[0]);
                end_column = Integer.parseInt(splitStrings[1]);

                start_line--;
                start_column--;
                end_line--;
                end_column--;

                final LogicalPosition blockStart = new LogicalPosition(start_line, start_column);
                final LogicalPosition blockEnd = new LogicalPosition(end_line, end_column);

                int start_position = editor.logicalPositionToOffset(blockStart);
                int end_position = editor.logicalPositionToOffset(blockStart);

                editor.getSelectionModel().setSelection(start_position, end_position);
                park_caret = end_position;
            } else {
                throw new BadCommandException(this);
            }
        } else if (element.getAttribute(START_SELECT_STRING) != null && element.getAttribute(END_SELECT_STRING) != null) {
            final Document document = editor.getDocument();
            final Project project = executionList.getProject();

            final FindManager findManager = FindManager.getInstance(project);
            final FindModel model = findManager.getFindInFileModel().clone();
            model.setGlobal(false);
            model.setReplaceState(false);

            final String value_start = element.getAttribute(START_SELECT_STRING).getValue();
            model.setStringToFind(value_start);
            final FindResult start = FindManager.getInstance(project).findString(document.getCharsSequence(), 0, model);

            final String value_end = element.getAttribute(END_SELECT_STRING).getValue();
            model.setStringToFind(value_end);
            final FindResult end = FindManager.getInstance(project).findString(document.getCharsSequence(), 0, model);

            selectInDocument(executionList, start.getStartOffset(), end.getEndOffset());
            park_caret = end.getEndOffset();
        } else {
            throw new BadCommandException(this);
        }

        //move caret to the end of the selection
        executionList.getEditor().getCaretModel().moveToOffset(park_caret);
        startNextCommand(executionList);
    }

    private void selectInDocument(ExecutionList executionList, int startOffset, int endOffset){
        final Editor editor = executionList.getEditor();
        editor.getSelectionModel().setSelection(startOffset, endOffset);
    }

}
