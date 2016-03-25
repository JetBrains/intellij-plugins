package training.editor.actions;

import org.jetbrains.annotations.NonNls;

/**
 * Created by karashevich on 19/08/15.
 */
public interface LearnActions {
    @NonNls
    String LEARN_NEXT_LESSON_ACTION = "LearnNextLessonAction";
    String LEARN_BLOCK_EDITOR_CARET_ACTION = "LearnBlockEditorCaretAction";
    String LEARN_BLOCK_MOUSE_ACTION = "LearnBlockMouseAction";

    String getActionId();

    void unregisterAction();
}
