package training.learn;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import training.editor.actions.BlockCaretAction;
import training.editor.actions.EduActions;
import training.editor.eduUI.EduBalloonBuilder;
import training.editor.eduUI.EduPanel;
import training.editor.eduUI.Message;
import training.learn.exceptons.BadLessonException;
import training.learn.exceptons.BadModuleException;
import training.learn.exceptons.LessonIsOpenedException;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

/**
 * Created by jetbrains on 18/03/16.
 */
public class LessonManager {

    Lesson myCurrentLesson;

    static ArrayList<EduActions> myEduActions;
    private static MouseListener myMouseDummyListener;
    private final EduBalloonBuilder eduBalloonBuilder;
    private static boolean mouseBlocked = false;
    private static MouseListener[] myMouseListeners;
    private static MouseMotionListener[] myMouseMotionListeners;
    private static HashSet<ActionsRecorder> actionsRecorders = new HashSet<ActionsRecorder>();
    private static HashMap<Lesson, LessonManager> lessonManagers = new HashMap<Lesson, LessonManager>();
    private static Editor lastEditor;

    public final int balloonDelay = 3000;

    public LessonManager(Lesson lesson, Editor editor) {
        myCurrentLesson = lesson;
        mouseBlocked = false;
        if (myEduActions == null) {
            myEduActions = new ArrayList<EduActions>();
        }
        eduBalloonBuilder = new EduBalloonBuilder(editor, balloonDelay, "Caret is blocked in this lesson");
        lessonManagers.put(lesson, this);
        lastEditor = editor;
    }

    private static LessonManager getInstance() {
        return ServiceManager.getService(LessonManager.class);
    }

    public static LessonManager getInstance(Lesson lesson){
        if (lessonManagers == null) return null;
        return lessonManagers.get(lesson);
    }

    public void initLesson(Editor editor) {
        cleanEditor(); //remove mouse blocks and action recorders from last editor
        EduPanel eduPanel = CourseManager.getInstance().getEduPanel();
        eduPanel.setLessonName(myCurrentLesson.getName());
        String moduleName = myCurrentLesson.getModule().getName();
        if (moduleName != null)
            eduPanel.setModuleName(moduleName);
        eduPanel.getModulePanel().init(myCurrentLesson);
        clearEditor(editor);
        clearLessonPanel();
        removeActionsRecorders();
        if (isMouseBlocked()) restoreMouseActions(editor);
        if (myEduActions != null) {
            for (EduActions myLearnAction : myEduActions) {
                myLearnAction.unregisterAction();
            }
            myEduActions.clear();
        }

        Runnable runnable = null;
        String buttonText = null;
        Lesson lesson = CourseManager.getInstance().giveNextLesson(myCurrentLesson);
        if (lesson != null) {
//            buttonText = lesson.getName();
            runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        CourseManager.getInstance().openLesson(editor.getProject(), lesson);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        } else {
            Module nextModule = CourseManager.getInstance().giveNextModule(myCurrentLesson);
            if (nextModule != null) {
                buttonText = nextModule.getName();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        Lesson notPassedLesson = nextModule.giveNotPassedLesson();
                        if (notPassedLesson == null) {
                            try {
                                CourseManager.getInstance().openLesson(editor.getProject(), nextModule.getLessons().get(0));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                CourseManager.getInstance().openLesson(editor.getProject(), notPassedLesson);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
            }
        }

        if (runnable != null) {
            CourseManager.getInstance().getEduPanel().setButtonSkipAction(runnable, buttonText, true);
        } else {
            CourseManager.getInstance().getEduPanel().setButtonSkipAction(runnable, buttonText, false);
        }
    }

    public void addMessage(String message){
        CourseManager.getInstance().getEduPanel().addMessage(message);
    }

    public void addMessage(Message[] messages) {
        CourseManager.getInstance().getEduPanel().addMessage(messages);
    }

    public void passExercise() {
        CourseManager.getInstance().getEduPanel().setPreviousMessagesPassed();
    }

    public void passLesson(Project project, Editor editor) {
        EduPanel eduPanel = CourseManager.getInstance().getEduPanel();
        eduPanel.setLessonPassed();
        if(myCurrentLesson.getModule()!=null && myCurrentLesson.getModule().hasNotPassedLesson()){
            final Lesson notPassedLesson = myCurrentLesson.getModule().giveNotPassedLesson();
            eduPanel.setButtonNextAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        CourseManager.getInstance().openLesson(project, notPassedLesson);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, notPassedLesson);
        } else {
            eduPanel.hideNextButton();
        }
//        eduPanel.updateLessonPanel(myCurrentLesson);
        eduPanel.getModulePanel().updateLessons(myCurrentLesson);
    }


    public void clearEditor(Editor editor) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                if (editor != null) {
                    final Document document = editor.getDocument();
                    if (document != null) {
                        try {
                            document.setText("");
                        } catch (Exception e) {
                            System.err.println("Unable to update text in EduEdutor!");
                        }
                    }
                }
            }
        });
    }

    public void clearLessonPanel() {
        CourseManager.getInstance().getEduPanel().clearLessonPanel();
    }

    private void hideButtons() {
        CourseManager.getInstance().getEduPanel().hideButtons();
    }

    public void removeActionsRecorders(){
        for (ActionsRecorder actionsRecorder : actionsRecorders) {
            actionsRecorder.dispose();
        }
        actionsRecorders.clear();
    }

    public boolean isMouseBlocked() {
        return mouseBlocked;
    }

    public void restoreMouseActions(Editor editor){
        if (getMyMouseListeners() != null) {
            for (MouseListener myMouseListener : getMyMouseListeners()) {
                editor.getContentComponent().addMouseListener(myMouseListener);
            }
        }

        if (getMyMouseMotionListeners() != null) {
            for (MouseMotionListener myMouseMotionListener : getMyMouseMotionListeners()) {
                editor.getContentComponent().addMouseMotionListener(myMouseMotionListener);
            }
        }

        if(myMouseDummyListener != null) editor.getContentComponent().removeMouseListener(myMouseDummyListener);

        setMyMouseListeners(null);
        setMyMouseMotionListeners(null);
        setMouseBlocked(false);
    }

    public MouseListener[] getMyMouseListeners() {
        return myMouseListeners;
    }

    public MouseMotionListener[] getMyMouseMotionListeners() {
        return myMouseMotionListeners;
    }

    public void setMyMouseListeners(MouseListener[] myMouseListeners) {
        this.myMouseListeners = myMouseListeners;
    }

    public void setMyMouseMotionListeners(MouseMotionListener[] myMouseMotionListeners) {
        this.myMouseMotionListeners = myMouseMotionListeners;
    }

    public void setMouseBlocked(boolean mouseBlocked) {
        this.mouseBlocked = mouseBlocked;
    }

    public void unblockCaret() {
        ArrayList<BlockCaretAction> myBlockActions = new ArrayList<BlockCaretAction>();

        for (EduActions myLearnAction : myEduActions) {
            if(myLearnAction instanceof BlockCaretAction) {
                myBlockActions.add((BlockCaretAction) myLearnAction);
                ((BlockCaretAction) myLearnAction).unregisterAction();
            }
        }

        myEduActions.removeAll(myBlockActions);
    }

    public void grabMouseActions(Editor editor){
        MouseListener[] mouseListeners = editor.getContentComponent().getMouseListeners();
        setMyMouseListeners(editor.getContentComponent().getMouseListeners());


        for (MouseListener mouseListener : mouseListeners) {
            editor.getContentComponent().removeMouseListener(mouseListener);
        }

        //kill all mouse (motion) listeners
        MouseMotionListener[] mouseMotionListeners = editor.getContentComponent().getMouseMotionListeners();
        setMyMouseMotionListeners(editor.getContentComponent().getMouseMotionListeners());

        for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
            editor.getContentComponent().removeMouseMotionListener(mouseMotionListener);
        }

        myMouseDummyListener  = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    showCaretBlockedBalloon();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                try {
                    showCaretBlockedBalloon();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                try {
                    showCaretBlockedBalloon();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        editor.getContentComponent().addMouseListener(myMouseDummyListener);

        setMouseBlocked(true);
    }

    public void blockCaret(Editor editor) {

//        try {
//            LearnUiUtil.getInstance().drawIcon(myProject, getEditor());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        for (EduActions myLearnAction : myEduActions) {
            if(myLearnAction instanceof BlockCaretAction) return;
        }

        BlockCaretAction blockCaretAction = new BlockCaretAction(editor);
        blockCaretAction.addActionHandler(new Runnable() {
            @Override
            public void run() {
                try {
                    showCaretBlockedBalloon();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        myEduActions.add(blockCaretAction);
    }

    public void registerActionsRecorder(ActionsRecorder recorder){
        actionsRecorders.add(recorder);
    }

    private void showCaretBlockedBalloon() throws InterruptedException {
        eduBalloonBuilder.showBalloon();
    }

    private void cleanEditor(){
        restoreMouseActions(lastEditor);
        removeActionsRecorders();
        unblockCaret();
    }

}
