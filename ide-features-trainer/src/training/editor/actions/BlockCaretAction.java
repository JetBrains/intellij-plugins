package training.editor.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by karashevich on 19/08/15.
 */
public class BlockCaretAction extends DumbAwareAction implements LearnActions {

    private ArrayList<Runnable> actionHandlers;
    private Editor editor;

    private final static String actionId = "LearnBlockCaretAction";


    public BlockCaretAction(@NotNull Editor editor){
        super(LEARN_BLOCK_EDITOR_CARET_ACTION);
        actionHandlers = new ArrayList<>();

        this.editor = editor;

        //collect all shortcuts for caret actions
        ArrayList<Shortcut> superShortcut = new ArrayList<>();
        HashSet<String> caretActionIds = new HashSet<>();
        caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN);
        caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN_WITH_SELECTION);
        caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT);
        caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT_WITH_SELECTION);
        caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_UP);
        caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_UP_WITH_SELECTION);
        caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT);
        caretActionIds.add(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT_WITH_SELECTION);

        //block clone caret
        caretActionIds.add(IdeActions.ACTION_EDITOR_CLONE_CARET_ABOVE);
        caretActionIds.add(IdeActions.ACTION_EDITOR_CLONE_CARET_BELOW);

        //tab
        caretActionIds.add(IdeActions.ACTION_EDITOR_TAB);
        caretActionIds.add(IdeActions.ACTION_EDITOR_EMACS_TAB);

        for (String caretActionId : caretActionIds) {
            Shortcut[] shortcuts = ActionManager.getInstance().getAction(caretActionId).getShortcutSet().getShortcuts();
            Collections.addAll(superShortcut, shortcuts);
        }

        Shortcut[] shortcuts = new Shortcut[superShortcut.size()];
        shortcuts = superShortcut.toArray(shortcuts);
        ShortcutSet shortcutSet = new CustomShortcutSet(shortcuts);
        this.registerCustomShortcutSet(shortcutSet, editor.getComponent());
    }

    @Override
    public void unregisterAction(){
        this.unregisterCustomShortcutSet(editor.getComponent());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        if(actionHandlers != null && actionHandlers.size() > 0) {
            for (Runnable actionHandler : actionHandlers) {
                actionHandler.run();
            }
        }
    }



    public void addActionHandler(Runnable runnable) {
        if (actionHandlers == null) actionHandlers = new ArrayList<>();
        actionHandlers.add(runnable);
    }

    public void removeActionHandler(Runnable runnable) {
        if (actionHandlers.contains(runnable)) {
            actionHandlers.remove(runnable);
        }
    }

    public void removeAllActionHandlers(){
        actionHandlers = null;
    }

    @Override
    public String getActionId() {
        return actionId;
    }
}
