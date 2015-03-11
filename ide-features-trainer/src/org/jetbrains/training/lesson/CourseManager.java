package org.jetbrains.training.lesson;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;

import java.util.ArrayList;

/**
 * Created by karashevich on 11/03/15.
 */
public class CourseManager {

    public static final CourseManager INSTANCE = new CourseManager();

    public ArrayList<Course> courses;


    public static CourseManager getInstance(){
        return INSTANCE;
    }

    public CourseManager() {
        //init courses; init default course by default
        courses = new ArrayList<Course>();

        try {
            final Course defaultCourse = new Course();
            courses.add(defaultCourse);
        } catch (BadCourseException e) {
            e.printStackTrace();
        } catch (BadLessonException e) {
            e.printStackTrace();
        }
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
