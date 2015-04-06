package org.jetbrains.training.lesson;

import com.intellij.ide.ui.laf.darcula.ui.DarculaEditorTextFieldBorder;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.LessonIsOpenedException;
import org.jetbrains.training.graphics.DetailPanel;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 29/01/15.
 */
public class Lesson {

    private Scenario scn;
    private String name;
    private String targetPath;
    private ArrayList<LessonListener> lessonListeners;
    private Course parentCourse;

    @Nullable
    private DetailPanel infoPanel;
    private boolean isPassed;
    private boolean isOpen;

    public Lesson(String pathToScenario, boolean passed, @Nullable Course course) throws BadLessonException {
        try {
            scn = new Scenario(pathToScenario);
            name = scn.getName();
            isPassed = passed;
            targetPath = scn.getTarget();
            lessonListeners = new ArrayList<LessonListener>();
            parentCourse = course;

            isOpen = false;

        } catch (JDOMException e) {
            //Scenario file is corrupted
            throw new BadLessonException("Probably scenario file is corrupted: " + pathToScenario);
        } catch (IOException e) {
            //Scenario file cannot be read
            throw new BadLessonException("Probably scenario file cannot be read: " + pathToScenario);
        }
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


}
