package training.commands;

import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import training.learn.ActionsRecorder;
import training.check.Check;
import training.learn.LessonManager;
import training.learn.log.LessonLog;
import training.keymap.KeymapUtil;
import training.keymap.SubKeymapUtil;
import training.learn.Lesson;
import training.util.MyClassLoader;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by karashevich on 30/01/15.
 */
public class TryCommand extends Command {

    public TryCommand(){
        super(CommandType.TRY);
    }

    @Override
    public void execute(ExecutionList executionList) throws Exception {


        Element element = executionList.getElements().poll();
        Check check = null;
//        updateDescription(element, infoPanel, editor);

        String target = executionList.getTarget();
        Lesson lesson = executionList.getLesson();
        Editor editor = executionList.getEditor();

        if (element.getAttribute("target") != null)
            try {
                target = getFromTarget(lesson, element.getAttribute("target").getValue());
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        String htmlText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        if (htmlText.isEmpty()) htmlText = element.getAttribute("description").getValue();

        if (htmlText.equals("")) {
            updateDescription(htmlText, lesson);
        } else {
            updateHTMLDescription(htmlText, lesson);
        }

        //Show button "again"
//        updateButton(element, elements, lesson, editor, e, document, myTarget, infoPanel, mouseListenerHolder);

        final ActionsRecorder recorder = new ActionsRecorder(editor.getProject(), editor.getDocument(), target, editor);
        LessonManager.getInstance(lesson).registerActionsRecorder(recorder);

        if (element.getAttribute("check") != null) {
            String checkClassString = element.getAttribute("check").getValue();
            try {
                Class myCheck = Class.forName(checkClassString);
                check = (Check) myCheck.newInstance();
                check.set(executionList.getProject(), editor);
                check.before();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (element.getAttribute("trigger") != null) {
            String actionId = element.getAttribute("trigger").getValue();
            startRecord(executionList, recorder, actionId, check);
        } else if(element.getAttribute("triggers") != null) {
            String actionIds = element.getAttribute("triggers").getValue();
            String[] actionIdArray = actionIds.split(";");
            startRecord(executionList, recorder, actionIdArray, check);
        } else {
            startRecord(executionList, recorder, check);
        }

    }

    private void startRecord(final ExecutionList executionList, ActionsRecorder recorder, @Nullable Check check) throws Exception {
        recorder.startRecording(new Runnable() {        //do when done
            @Override
            public void run() {
                pass(executionList);
            }
        }, (String[]) null, check);
    }

    private void startRecord(final ExecutionList executionList, ActionsRecorder recorder, String actionId, @Nullable Check check) throws Exception {
        recorder.startRecording(new Runnable() {        //do when done
            @Override
            public void run() {
                pass(executionList);
            }
        }, actionId, check);
    }

    private void startRecord(final ExecutionList executionList, ActionsRecorder recorder, String[] actionIdArray, @Nullable Check check ) throws Exception {
        recorder.startRecording(new Runnable() {        //do when done
            @Override
            public void run() {
                pass(executionList);
            }
        }, actionIdArray, check);
    }

    private void pass(ExecutionList executionList) {
        Lesson lesson = executionList.getLesson();
        LessonManager.getInstance(lesson).passExercise();
        final LessonLog lessonLog = lesson.getLessonLog();
        lessonLog.log("Passed exercise. Exercise #" + lessonLog.getMyLesson().getExerciseCount());
        lesson.passItem();
        startNextCommand(executionList);
    }


    private String getFromTarget(Lesson lesson, String targetPath) throws IOException {
        InputStream is = MyClassLoader.getInstance().getResourceAsStream(lesson.getModule().getAnswersPath() + targetPath);
        if(is == null) throw new IOException("Unable to get checkfile for \"" + lesson.getName() + "\" lesson");
        return new Scanner(is).useDelimiter("\\Z").next();
    }

    private String resolveShortcut(String text, String actionId){
        final KeyStroke shortcutByActionId = KeymapUtil.getShortcutByActionId(actionId);
        final String shortcutText = SubKeymapUtil.getKeyStrokeTextSub(shortcutByActionId);
        return substitution(text, shortcutText);
    }

    public static String substitution(String text, String shortcutString){
        if (text.contains(ActionCommand.SHORTCUT)) {
            return text.replace(ActionCommand.SHORTCUT, shortcutString);
        } else {
            return text;
        }
    }

}
