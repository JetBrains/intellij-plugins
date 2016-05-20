package training.solutions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;

/**
 * Created by karashevich on 10/11/15.
 */
public class BaseSolutionClass {

    public static void gotoOffset(Editor editor, int offset){
        editor.getCaretModel().moveToOffset(offset);
    }

    public static void gotoLogicalPosition(Editor editor, int line, int column) {
        editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(line - 1, column - 1));
    }
}
