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

        if (element.getAttribute("hint") != null) {
            String hintText = element.getAttribute("hint").getValue();
            ShowHint.showHintPanel(lesson, editor, hintText);
        }

        String htmlText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        if (htmlText.isEmpty()) htmlText = element.getAttribute("description").getValue();

        if (htmlText.equals("")) {
            updateDescription(element, infoPanel, editor);
        } else {
            updateHTMLDescription(element, infoPanel, editor, htmlText);
        }

        //Show button "again"

        updateButton(element, elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);

        final ActionsRecorder recorder = new ActionsRecorder(e.getProject(), document, target);
        //TODO: Make recorder disposable

        if (element.getAttribute("trigger") != null) {
            String actionId = element.getAttribute("trigger").getValue();
            startRecord(elements, lesson, editor, e, document, target, mouseListenerHolder, recorder, actionId);
        } else {
            startRecord(elements, lesson, editor, e, document, target, mouseListenerHolder, recorder, null);
        }
    }

    private void startRecord(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent anActionEvent, final Document document, final String target, final MouseListenerHolder mouseListenerHolder, ActionsRecorder recorder, @Nullable String actionId) {
        recorder.startRecording(new Runnable() {        //do when done
            @Override
            public void run() {
                startNextCommand(elements, lesson, editor, anActionEvent, document, target, lesson.getInfoPanel(), mouseListenerHolder);
            }
        }, actionId);
    }

}
