package training.learn;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import training.editor.actions.BlockCaretAction;
import training.editor.actions.LearnActions;
import training.ui.LearnBalloonBuilder;
import training.ui.LearnPanel;
import training.ui.Message;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by karashevich on 18/03/16.
 */
public class LessonManager {

    private Lesson myCurrentLesson;

    private static ArrayList<LearnActions> myLearnActions;
    private static MouseListener myMouseDummyListener;
    private final LearnBalloonBuilder learnBalloonBuilder;
    private static boolean mouseBlocked = false;
    private static MouseListener[] myMouseListeners;
    private static MouseMotionListener[] myMouseMotionListeners;
    private static HashSet<ActionsRecorder> actionsRecorders = new HashSet<>();
    private static HashMap<Lesson, LessonManager> lessonManagers = new HashMap<>();
    private static Editor lastEditor;

    private final int balloonDelay = 3000;

    public LessonManager(Lesson lesson, Editor editor) {
        myCurrentLesson = lesson;
        mouseBlocked = false;
        if (myLearnActions == null) {
            myLearnActions = new ArrayList<>();
        }
        learnBalloonBuilder = new LearnBalloonBuilder(editor, balloonDelay, "Caret is blocked in this lesson");
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

    void initLesson(Editor editor) throws Exception {
        cleanEditor(); //remove mouse blocks and action recorders from last editor
        LearnPanel learnPanel = CourseManager.getInstance().getLearnPanel();
        learnPanel.setLessonName(myCurrentLesson.getName());
        Module module = myCurrentLesson.getModule();
        if (module == null) throw new Exception("Unable to find module for lesson: " + myCurrentLesson);
        String moduleName = module.getName();
        learnPanel.setModuleName(moduleName);
        learnPanel.getModulePanel().init(myCurrentLesson);
        clearEditor(editor);
        clearLessonPanel();
        removeActionsRecorders();
        if (isMouseBlocked()) restoreMouseActions(editor);
        if (myLearnActions != null) {
            for (LearnActions myLearnAction : myLearnActions) {
                myLearnAction.unregisterAction();
            }
            myLearnActions.clear();
        }

        Runnable runnable = null;
        String buttonText = null;
        Lesson lesson = CourseManager.getInstance().giveNextLesson(myCurrentLesson);
        if (lesson != null) {
//            buttonText = lesson.getName();
            runnable = () -> {
                try {
                    CourseManager.getInstance().openLesson(editor.getProject(), lesson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        } else {
            Module nextModule = CourseManager.getInstance().giveNextModule(myCurrentLesson);
            if (nextModule != null) {
                buttonText = nextModule.getName();
                runnable = () -> {
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
                };
            }
        }

        if (runnable != null) {
            CourseManager.getInstance().getLearnPanel().setButtonSkipAction(runnable, buttonText, true);
        } else {
            CourseManager.getInstance().getLearnPanel().setButtonSkipAction(null, null, false);
        }
    }

    public void addMessage(String message){
        CourseManager.getInstance().getLearnPanel().addMessage(message);
    }

    public void addMessage(Message[] messages) {
        CourseManager.getInstance().getLearnPanel().addMessage(messages);
    }

    public void passExercise() {
        CourseManager.getInstance().getLearnPanel().setPreviousMessagesPassed();
    }

    public void passLesson(Project project, Editor editor) {
        LearnPanel learnPanel = CourseManager.getInstance().getLearnPanel();
        learnPanel.setLessonPassed();
        if(myCurrentLesson.getModule() !=null && myCurrentLesson.getModule().hasNotPassedLesson()){
            final Lesson notPassedLesson = myCurrentLesson.getModule().giveNotPassedLesson();
            learnPanel.setButtonNextAction(() -> {
                try {
                    CourseManager.getInstance().openLesson(project, notPassedLesson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, notPassedLesson);
        } else {
            Module module = CourseManager.getInstance().giveNextModule(myCurrentLesson);
            if (module == null) hideButtons();
            else {
                Lesson lesson = module.giveNotPassedLesson();
                if (lesson == null) lesson = module.getLessons().get(0);

                Lesson lessonFromNextModule = lesson;
                Module nextModule = lessonFromNextModule.getModule();
                if (nextModule == null) return;
                learnPanel.setButtonNextAction(() -> {
                    try {
                        CourseManager.getInstance().openLesson(project, lessonFromNextModule);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, lesson, LearnBundle.message("learn.ui.button.next.module") + ": " + nextModule.getName());
            }
        }
//        learnPanel.updateLessonPanel(myCurrentLesson);
        learnPanel.getModulePanel().updateLessons(myCurrentLesson);
    }


    public void clearEditor(Editor editor) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                if (editor != null) {
                    final Document document = editor.getDocument();
                    try {
                        document.setText("");
                    } catch (Exception e) {
                        System.err.println("Unable to update text in editor!");
                    }
                }
            }
        });
    }

    private void clearLessonPanel() {
        CourseManager.getInstance().getLearnPanel().clearLessonPanel();
    }

    private void hideButtons() {
        CourseManager.getInstance().getLearnPanel().hideButtons();
    }

    private void removeActionsRecorders(){
        for (ActionsRecorder actionsRecorder : actionsRecorders) {
            actionsRecorder.dispose();
        }
        actionsRecorders.clear();
    }

    private boolean isMouseBlocked() {
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

    private MouseListener[] getMyMouseListeners() {
        return myMouseListeners;
    }

    private MouseMotionListener[] getMyMouseMotionListeners() {
        return myMouseMotionListeners;
    }

    private void setMyMouseListeners(MouseListener[] myMouseListeners) {
        LessonManager.myMouseListeners = myMouseListeners;
    }

    private void setMyMouseMotionListeners(MouseMotionListener[] myMouseMotionListeners) {
        LessonManager.myMouseMotionListeners = myMouseMotionListeners;
    }

    private void setMouseBlocked(boolean mouseBlocked) {
        LessonManager.mouseBlocked = mouseBlocked;
    }

    public void unblockCaret() {
        ArrayList<BlockCaretAction> myBlockActions = new ArrayList<BlockCaretAction>();

        for (LearnActions myLearnAction : myLearnActions) {
            if(myLearnAction instanceof BlockCaretAction) {
                myBlockActions.add((BlockCaretAction) myLearnAction);
                myLearnAction.unregisterAction();
            }
        }

        myLearnActions.removeAll(myBlockActions);
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

        for (LearnActions myLearnAction : myLearnActions) {
            if(myLearnAction instanceof BlockCaretAction) return;
        }

        BlockCaretAction blockCaretAction = new BlockCaretAction(editor);
        blockCaretAction.addActionHandler(() -> {
            try {
                showCaretBlockedBalloon();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        myLearnActions.add(blockCaretAction);
    }

    public void registerActionsRecorder(ActionsRecorder recorder){
        actionsRecorders.add(recorder);
    }

    private void showCaretBlockedBalloon() throws InterruptedException {
        learnBalloonBuilder.showBalloon();
    }

    private void cleanEditor(){
        restoreMouseActions(lastEditor);
        removeActionsRecorders();
        unblockCaret();
    }

}
