package training.editor.actions;

import com.intellij.openapi.actionSystem.ActionPromoter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karashevich on 17/08/15.
 */
public class EduActionPromoter implements ActionPromoter {

    @Override
    public List<AnAction> promote(List<AnAction> actions, DataContext context) {
        for (AnAction action : actions) {
            if (action instanceof EduActions) {
                return  (new ArrayList<AnAction>(actions));
            }
        }

        return (new ArrayList<AnAction>(actions));
    }
}
