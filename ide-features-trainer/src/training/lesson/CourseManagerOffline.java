package training.lesson;

import com.intellij.ide.RecentProjectsManager;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.commands.BadCommandException;
import training.editor.EduEditor;
import training.editor.EduEditorProvider;
import training.lesson.dialogs.SdkProblemDialog;
import training.lesson.exceptons.*;
import training.lesson.log.GlobalLessonLog;
import training.util.GenerateCourseXml;
import training.util.MyClassLoader;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 11/03/15.
 */

public class CourseManagerOffline {

    public static CourseManagerOffline INSTANCE = new CourseManagerOffline();

    public static CourseManagerOffline getInstance(){
        return INSTANCE;
    }

    private ArrayList<Course> courses;

    public CourseManagerOffline() {
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



}
