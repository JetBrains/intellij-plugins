package org.jetbrains.training.lesson;

import com.intellij.ide.ui.laf.darcula.ui.DarculaEditorTextFieldBorder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.LessonIsOpenedException;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.graphics.HintPanel;

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
    private DetailPanel infoPanel;
    public HintPanel hintPanel;
    private boolean isPassed;
    private boolean isOpen;

    public Lesson(Scenario scenario, boolean passed, @Nullable Course course) throws BadLessonException {

        super(scenario.getName());
            scn = scenario;
            name = scn.getName();

            isPassed = passed;
            targetPath = scn.getTarget();
            lessonListeners = new ArrayList<LessonListener>();
            parentCourse = course;

            isOpen = false;


    }

    public void open(Dimension infoPanelDimension) throws IOException, FontFormatException, LessonIsOpenedException {
        //init infoPanel, check that Lesson has not opened yet
        if (isOpen) throw new LessonIsOpenedException(this.getId() + "is opened");
        onStart();

        infoPanel = new DetailPanel(infoPanelDimension);
        isOpen = true;
    }

    public void close(){
        //destroy infoPanel (infoPanel = null)
        isOpen = false;
        onClose();

        infoPanel = null;
    }

    /**
     *
     * @return null if DetailPanel still not initialized
     */
    @Nullable
    public DetailPanel getInfoPanel() {
        return infoPanel;
    }

    public String getId() {
        return name;
    }

    public boolean isPassed(){
        return isPassed;
    }

    public boolean isOpen() {return isOpen;}

    public void setPassed(boolean passed){
        hintPanel.setAllAsDone();
        isPassed = passed;
    }

    public Scenario getScn(){
        return scn;
    }

    public String getTargetPath() {
        return targetPath;
    }

    @Nullable
    public Course getParentCourse() {return parentCourse;}

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

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            CourseManager.getInstance().openLesson(anActionEvent, this);
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
        e.getPresentation().setEnabled(!isPassed());
    }
}
