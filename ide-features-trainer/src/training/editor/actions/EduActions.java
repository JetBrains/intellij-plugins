package training.editor.actions;

import org.jetbrains.annotations.NonNls;

/**
 * Created by karashevich on 19/08/15.
 */
public interface EduActions {
    @NonNls
    String EDU_NEXT_LESSON_ACTION = "EduNextLessonAction";
    String EDU_BLOCK_EDITOR_CARET_ACTION = "EduBlockEditorCaretAction";
    String EDU_BLOCK_MOUSE_ACTION = "EduBlockMouseAction";

    void unregisterAction();
}
