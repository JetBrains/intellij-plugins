package org.jetbrains.training.commandsEx;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.LessonIsOpenedException;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.lesson.Lesson;

import java.awt.*;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class WinCommandEx extends CommandEx {

    public WinCommandEx(){
        super(CommandType.WIN);
    }

    @Override
    public void execute(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel, final MouseListenerHolder mouseListenerHolder) throws InterruptedException {

        Element element = elements.poll();

        String description = "";

        if (element.getAttribute("description") != null) description = element.getAttribute("description").getValue();
        infoPanel.setText(description);
        infoPanel.greenalize();
        lesson.setPassed(true);

        if (lesson.getCourse().hasNotPassedLesson()) {
            infoPanel.setButtonText("Next lesson");
            try {
                infoPanel.addButtonAction(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            infoPanel.hideButton();
                            infoPanel.dispose();
                            if (lesson.hintPanel != null) lesson.hintPanel.dispose();
                            lesson.onNextLesson();
                        } catch (BadLessonException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (FontFormatException e1) {
                            e1.printStackTrace();
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (BadCourseException e1) {
                            e1.printStackTrace();
                        } catch (LessonIsOpenedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            infoPanel.showButton();
        }

    }
}
