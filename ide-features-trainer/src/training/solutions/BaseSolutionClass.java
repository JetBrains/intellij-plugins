package training.solutions;

import com.intellij.openapi.editor.LogicalPosition;
import training.editor.EduEditor;

/**
 * Created by karashevich on 10/11/15.
 */
public class BaseSolutionClass {

    public static void gotoOffset(EduEditor eduEditor, int offset){
        eduEditor.getEditor().getCaretModel().moveToOffset(offset);
    }

    public static void gotoLogicalPosition(EduEditor eduEditor, int line, int column) {
        eduEditor.getEditor().getCaretModel().moveToLogicalPosition(new LogicalPosition(line - 1, column - 1));
    }
}
