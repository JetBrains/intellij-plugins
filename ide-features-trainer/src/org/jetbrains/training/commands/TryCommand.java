package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.*;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.HintPanel;
import org.jetbrains.training.graphics.ShowHint;
import org.jetbrains.training.lesson.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.Scanner;
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

        String myTarget = target;
        if (element.getAttribute("target") != null)
            try {
                myTarget = getFromTarget(lesson, element.getAttribute("target").getValue());
            } catch (IOException e1) {
                e1.printStackTrace();
            }


        if(lesson.hintPanel == null) {
            if (element.getAttribute("hint") != null) {
                String hintText = element.getAttribute("hint").getValue();
                ShowHint.showHintPanel(lesson, editor, hintText);
            }
        }

        String htmlText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        if (htmlText.isEmpty()) htmlText = element.getAttribute("description").getValue();

        if (htmlText.equals("")) {
            updateDescription(element, infoPanel, editor);
        } else {
            updateHTMLDescription(element, infoPanel, editor, htmlText);
        }

        //Show button "again"
        updateButton(element, elements, lesson, editor, e, document, myTarget, infoPanel, mouseListenerHolder);

        final ActionsRecorder recorder = new ActionsRecorder(e.getProject(), document, myTarget);
        //TODO: Make recorder disposable

        if (element.getAttribute("trigger") != null) {
            String actionId = element.getAttribute("trigger").getValue();
            startRecord(elements, element, lesson, editor, e, document, myTarget, mouseListenerHolder, recorder, actionId);
        } else {
            startRecord(elements, element, lesson, editor, e, document, myTarget, mouseListenerHolder, recorder, null);
        }
    }

    private void startRecord(final Queue<Element> elements, final Element element, final Lesson lesson, final Editor editor, final AnActionEvent anActionEvent, final Document document, final String target, final MouseListenerHolder mouseListenerHolder, ActionsRecorder recorder, @Nullable String actionId) {
        recorder.startRecording(new Runnable() {        //do when done
            @Override
            public void run() {
                if(lesson.hintPanel != null) {
                    if (element.getAttribute("hint") != null) {
                        String hintText = element.getAttribute("hint").getValue();
                        lesson.hintPanel.setCheck(true, hintText);
                    }
                }

                startNextCommand(elements, lesson, editor, anActionEvent, document, target, lesson.getInfoPanel(), mouseListenerHolder);
            }
        }, actionId);
    }

    private String getFromTarget(Lesson lesson, String targetPath) throws IOException {
        InputStream is = MyClassLoader.getInstance().getResourceAsStream(lesson.getParentCourse().getAnswersPath() + targetPath);
        if(is == null) throw new IOException("Unable to get checkfile for \"" + lesson.getId() + "\" lesson");
        return new Scanner(is).useDelimiter("\\Z").next();
    }

}
