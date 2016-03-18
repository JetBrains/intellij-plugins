package training.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Condition;

/**
 * Created by jetbrains on 17/03/16.
 */
public class LearnCondition implements Condition, DumbAware{

    @Override
    public boolean value(Object o) {
        return false;
    }
}
