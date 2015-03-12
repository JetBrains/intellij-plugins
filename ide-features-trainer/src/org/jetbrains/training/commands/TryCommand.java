package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.ActionsRecorder;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.Command;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.lesson.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

import java.awt.*;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class TryCommand extends Command {

    public TryCommand(){
        super(CommandType.TRY);
    }

    @Override
    public void execute(Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel, MouseListenerHolder mouseListenerHolder) throws InterruptedException {

        Element element = elements.poll();
//        updateDescription(element, infoPanel, editor);
        String htmlText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        if (htmlText.isEmpty()) htmlText = element.getAttribute("description").getValue();

        if (htmlText.equals("")) {
            updateDescription(element, infoPanel, editor);
        } else {
            updateHTMLDescription(element, infoPanel, editor, htmlText);
        }
        updateButton(element, elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);

        final String winMessage = element.getAttribute("win-message").getValue();

        final ActionsRecorder recorder = new ActionsRecorder(e.getProject(), document, target);
        //TODO: Make recorder disposable

        if (element.getAttribute("trigger") != null) {
            String actionId = element.getAttribute("trigger").getValue();
            startRecord(lesson, infoPanel, winMessage, recorder, actionId);
        } else {

            startRecord(lesson, infoPanel, winMessage, recorder, null);
        }

    }

    private void startRecord(final Lesson lesson, final DetailPanel infoPanel, final String winMessage, ActionsRecorder recorder, @Nullable String actionId) {
        recorder.startRecording(new Runnable() {        //do when done
            @Override
            public void run() {
                infoPanel.setText(winMessage);
                infoPanel.greenalize();
                lesson.setPassed(true);

                if (lesson.getParentCourse().hasNotPassedLesson()) {
                    infoPanel.setButtonText("Next lesson");
                    try {
                        infoPanel.addButtonAction(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    infoPanel.hideButton();
                                    infoPanel.dispose();
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
                                }
                            }
                        });
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    infoPanel.showButton();
                }

            }
        }, actionId);
    }


}
