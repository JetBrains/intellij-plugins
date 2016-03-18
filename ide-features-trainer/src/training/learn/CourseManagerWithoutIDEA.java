package training.learn;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;
import training.learn.exceptons.*;
import training.solutions.BaseSolutionClass;
import training.testFramework.LessonSolution;
import training.util.GenerateCourseXml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by karashevich on 11/03/15.
 */

public class CourseManagerWithoutIDEA {

    private static final String DOT = ".";
    public static CourseManagerWithoutIDEA INSTANCE = new CourseManagerWithoutIDEA();

    public static CourseManagerWithoutIDEA getInstance(){
        return INSTANCE;
    }

    private ArrayList<Course> courses;

    public CourseManagerWithoutIDEA() {
        courses = new ArrayList<Course>();
        if (courses == null || courses.size() == 0) try {
            initCourses();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (BadCourseException e) {
            e.printStackTrace();
        } catch (BadLessonException e) {
            e.printStackTrace();
        }
    }


    public void initCourses() throws JDOMException, IOException, URISyntaxException, BadCourseException, BadLessonException {
        Element coursesRoot = Course.getRootFromPath(GenerateCourseXml.COURSE_ALLCOURSE_FILENAME);
        for (Element element : coursesRoot.getChildren()) {
            if (element.getName().equals(GenerateCourseXml.COURSE_TYPE_ATTR)) {
                String courseFilename = element.getAttribute(GenerateCourseXml.COURSE_NAME_ATTR).getValue();
                final Course course = Course.initCourse(courseFilename);
                addCourse(course);
            }
        }
    }


    @Nullable
    public Course getCourseById(String id) {
        final Course[] courses = getCourses();
        if (courses == null || courses.length == 0) return null;

        for (Course course : courses) {
            if (course.getId().toUpperCase().equals(id.toUpperCase())) return course;
        }
        return null;
    }


    @Nullable
    public Lesson findLesson(String lessonName) {
        if (getCourses() == null) return null;
        for (Course course : getCourses()) {
            for (Lesson lesson : course.getLessons()) {
                if (lesson.getName() != null)
                    if (lesson.getName().toUpperCase().equals(lessonName.toUpperCase()))
                        return lesson;
            }
        }
        return null;
    }



    public void addCourse(Course course) {
        courses.add(course);
    }

    @Nullable
    public Course[] getCourses() {
        if (courses == null) return null;
        return courses.toArray(new Course[courses.size()]);
    }


    @Nullable
    public LessonSolution findSolution(String lessonId) throws Exception {
        final Lesson lesson = findLesson(lessonId);
        assert lesson != null;
        if (lesson.getCourse() == null) return null;
        final Element courseRoot = lesson.getCourse().getCourseRoot();
        if (courseRoot == null) return null;

        String lessonsPath = (courseRoot.getAttribute(GenerateCourseXml.COURSE_LESSONS_PATH_ATTR) != null) ? lesson.getCourse().getCoursePath() + courseRoot.getAttribute(GenerateCourseXml.COURSE_LESSONS_PATH_ATTR).getValue() : "";
        String lessonSolutionName = null;

        for (Element lessonElement : courseRoot.getChildren()) {
            if (!lessonElement.getName().equals(GenerateCourseXml.COURSE_LESSON_ELEMENT))
                throw new BadCourseException("Course file is corrupted or cannot be read properly");

            String lessonFilename = lessonElement.getAttributeValue(GenerateCourseXml.COURSE_LESSON_FILENAME_ATTR);
            String lessonPath = lessonsPath + lessonFilename;
            if (lessonPath.equals(lesson.getScn().getPath())){
                lessonSolutionName = lessonElement.getAttributeValue(GenerateCourseXml.COURSE_LESSON_SOLUTION);
                break;
            }

        }

        final String solutionPrefix = BaseSolutionClass.class.getPackage().getName();
        final String solutionName = (lessonSolutionName != null) ? lessonSolutionName : "";
        Class mySolutionClass = null;
        try {
            mySolutionClass = Class.forName(solutionName);
        } catch (ClassNotFoundException e) {
            throw new Exception("Unable to find solution class for " + lessonId + " lesson");
        }
        return (LessonSolution) mySolutionClass.newInstance();
    }
}
