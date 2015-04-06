package org.jetbrains.training;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.training.sandbox.LessonAction;

/**
 * Created by karashevich on 06/04/15.
 */
public class MenuUpdater extends ActionGroup {

    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {

        AnAction[] actions = new AnAction[3];
        actions[0] = new LessonAction("Course 1");
        actions[1] = new LessonAction("Course 2");
        actions[2] = new SubLessons("Course 3", true);

        return actions;

    }

    private class SubLessons extends ActionGroup{

        public SubLessons(String shortName, boolean popup) {
            super(shortName, popup);
        }

        @NotNull
        @Override
        public AnAction[] getChildren(AnActionEvent anActionEvent) {

            AnAction[] actions = new AnAction[2];
            actions[0] = new LessonAction("Lesson 1");
            actions[1] = new LessonAction("Lesson 2");
            return actions;
        }
    }

}
