package org.jetbrains.training.lesson;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by karashevich on 11/03/15.
 */
public class CourseManager extends ActionGroup{

    public static final CourseManager INSTANCE = new CourseManager();

    public ArrayList<Course> courses;


    public static CourseManager getInstance(){
        return INSTANCE;
    }

    public CourseManager() {
        //init courses; init default course by default
        super("Courses", true);
        courses = new ArrayList<Course>();

        try {
            final Course defaultCourse = new Course();
            courses.add(defaultCourse);
        } catch (BadCourseException e) {
            e.printStackTrace();
        } catch (BadLessonException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        AnAction[] actions = new AnAction[courses.size()];
        actions = courses.toArray(actions);
        return actions;
    }

    @Nullable
    public Course getAnyCourse(){
        if(courses == null || courses.size() == 0) return null;
        return courses.get(0);
    }

    @Nullable
    public Course getCourseById(String id){
        if(courses == null || courses.size() == 0) return null;

        for(Course course: courses){
            if(course.getId().toUpperCase().equals(id.toUpperCase())) return course;
        }
        return null;
    }

}
