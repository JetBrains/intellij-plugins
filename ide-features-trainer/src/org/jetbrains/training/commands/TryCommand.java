package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.ActionsRecorder;
import org.jetbrains.training.MyClassLoader;
import org.jetbrains.training.commandsEx.ActionCommandEx;
import org.jetbrains.training.commandsEx.CommandEx;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.graphics.HintPanel;
import org.jetbrains.training.keymap.KeymapUtil;
import org.jetbrains.training.keymap.SubKeymapUtil;
import org.jetbrains.training.lesson.Lesson;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.Scanner;

/**
 * Created by karashevich on 30/01/15.
 */
public class TryCommand extends Command {

    public TryCommand(){
        super(CommandType.TRY);
    }

    @Override
    public void execute(ExecutionList executionList) throws InterruptedException {


        Element element = executionList.getElements().poll();
//        updateDescription(element, infoPanel, editor);

        String myTarget = executionList.getTarget();
        if (element.getAttribute("target") != null)
            try {
                myTarget = getFromTarget(executionList.getLesson(), element.getAttribute("target").getValue());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        String htmlText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        if (htmlText.isEmpty()) htmlText = element.getAttribute("description").getValue();

        if (htmlText.equals("")) {
            updateDescription(htmlText, executionList.getEduEditor());
        } else {
            updateHTMLDescription(htmlText, executionList.getEduEditor());
        }

        //Show button "again"
//        updateButton(element, elements, lesson, editor, e, document, myTarget, infoPanel, mouseListenerHolder);

        final ActionsRecorder recorder = new ActionsRecorder(executionList.getEditor().getProject(), executionList.getEditor().getDocument(), executionList.getTarget());
        //TODO: Make recorder disposable

        if (element.getAttribute("trigger") != null) {
            String actionId = element.getAttribute("trigger").getValue();
            startRecord(executionList, recorder, actionId);
        } else {
            startRecord(executionList, recorder, null);
        }
    }

    private void startRecord(final ExecutionList executionList, ActionsRecorder recorder, String actionId) {
        recorder.startRecording(new Runnable() {        //do when done
            @Override
            public void run() {
                System.out.println("Lesson Completed");
            }
        }, actionId);
    }

    private String getFromTarget(Lesson lesson, String targetPath) throws IOException {
        InputStream is = MyClassLoader.getInstance().getResourceAsStream(lesson.getCourse().getAnswersPath() + targetPath);
        if(is == null) throw new IOException("Unable to get checkfile for \"" + lesson.getId() + "\" lesson");
        return new Scanner(is).useDelimiter("\\Z").next();
    }

    private String resolveShortcut(String text, String actionId){
        final KeyStroke shortcutByActionId = KeymapUtil.getShortcutByActionId(actionId);
        final String shortcutText = SubKeymapUtil.getKeyStrokeTextSub(shortcutByActionId);
        return substitution(text, shortcutText);
    }

    public static String substitution(String text, String shortcutString){
        if (text.contains(ActionCommandEx.SHORTCUT)) {
            return text.replace(ActionCommandEx.SHORTCUT, shortcutString);
        } else {
            return text;
        }
    }

}
