package training.lesson;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nullable;
import training.editor.eduUI.EduIcons;
import training.lesson.exceptons.BadCourseException;
import training.lesson.exceptons.BadLessonException;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 29/01/15.
 */
public class Lesson extends AnAction {

    private Scenario scn;
    private String name;
    private String targetPath;
    private ArrayList<LessonListener> lessonListeners;
    private Course parentCourse;

    @Nullable
    private boolean isPassed;
    private boolean isOpen;

    public Lesson(Scenario scenario, boolean passed, @Nullable Course course) throws BadLessonException {

        super(scenario.getName());
            scn = scenario;
            name = scn.getName();

            isPassed = passed;
            if (!scn.getSubtype().equals("aimless")) {
                targetPath = scn.getTarget();
            } else {
                targetPath = null;
            }
            lessonListeners = new ArrayList<LessonListener>();
            parentCourse = course;

            isOpen = false;


    }

    public void open(Dimension infoPanelDimension) throws IOException, FontFormatException, LessonIsOpenedException {
        //init infoPanel, check that Lesson has not opened yet
        if (isOpen) throw new LessonIsOpenedException(this.getId() + "is opened");
        onStart();

        isOpen = true;
    }

    public void close(){
        //destroy infoPanel (infoPanel = null)
        isOpen = false;
        onClose();
    }

    /**
     *
     * @return null if DetailPanel still not initialized
     */
    @Nullable

    public String getId() {
        return name;
    }

    public boolean isPassed(){
        return isPassed;
    }

    public boolean isOpen() {return isOpen;}

    public void setPassed(boolean passed){
        isPassed = passed;
    }

    public Scenario getScn(){
        return scn;
    }

    @Nullable
    public String getTargetPath() {
        return targetPath;
    }

    @Nullable
    public Course getCourse() {return parentCourse;}

    //Listeners
    public void addLessonListener(LessonListener lessonListener){
        if (lessonListeners == null) lessonListeners = new ArrayList<LessonListener>();

        lessonListeners.add(lessonListener);
    }

    public void removeLessonListener(LessonListener lessonListener) {
        if (lessonListeners.contains(lessonListener)) {
            lessonListeners.remove(lessonListener);
        }
    }

    public void onStart(){
        if (lessonListeners == null) lessonListeners = new ArrayList<LessonListener>();

        for (LessonListener lessonListener : lessonListeners) {
            lessonListener.lessonStarted(this);
        }
    }

    public void onClose(){
        for (LessonListener lessonListener : lessonListeners) {
            lessonListener.lessonClosed(this);
        }

        lessonListeners = null;

    }

    //call onPass handlers in lessonListeners
    public void onPass(){
        if(isPassed) return;
        scn.getRoot().getAttribute("passed").setValue("true");
        try {
            scn.saveState();
            for (LessonListener lessonListener : lessonListeners) {
                lessonListener.lessonPassed(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        isPassed = true;
    }

    public void onNextLesson() throws BadLessonException, ExecutionException, IOException, FontFormatException, InterruptedException, BadCourseException, LessonIsOpenedException {
        for (LessonListener lessonListener : lessonListeners) {
            lessonListener.lessonNext(this);
        }
    }


//    @Override
//    public boolean isSelected(AnActionEvent anActionEvent) {
//        return isPassed;
//    }
//
//    @Override
//    public void (AnActionEvent anActionEvent, boolean b) {
//        try {
//            CourseManager.getInstance().openLesson(anActionEvent.getProject(), this);
//        } catch (BadCourseException e) {
//            e.printStackTrace();
//        } catch (BadLessonException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (FontFormatException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (LessonIsOpenedException e) {
//            e.printStackTrace();
//        }
//
//    }


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            CourseManager.getInstance().openLesson(anActionEvent.getProject(), this);
        } catch (BadCourseException e) {
            e.printStackTrace();
        } catch (BadLessonException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (LessonIsOpenedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        if (isPassed())
            e.getPresentation().setIcon(IconLoader.getIcon(EduIcons.CHECKMARK_DARK_GRAY));

    }
}
