package org.jetbrains.training.lesson;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.MyClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by karashevich on 29/01/15.
 */
public class Course {

    private ArrayList<Lesson> lessons;
    private String path;
    final private String defaultPath = "DefaultCourse.xml";
    private String answersPath;
    private Element root;
    private String id;

    public Course() throws BadCourseException, BadLessonException {
        path = defaultPath;
        lessons = new ArrayList<Lesson>();
        id = "default";

        initLessons();
        answersPath = root.getAttribute("answerspath").getValue();
    }

    public Course(String coursePath) throws BadCourseException, BadLessonException {
        //load xml with lessons
        path = coursePath;

        lessons = new ArrayList<Lesson>();
        initLessons();
        answersPath = root.getAttribute("answerspath").getValue();
        id = root.getAttribute("id").getValue();
    }

    public String getAnswersPath() {
        return answersPath;
    }

    private void initLessons() throws BadCourseException, BadLessonException {

        InputStream is = MyClassLoader.getInstance().getResourceAsStream(path);

        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(is);
        } catch (JDOMException e) {
            throw new BadCourseException("Probably course file has been corrupted.");
        } catch (IOException e) {
            throw new BadCourseException("Probably cannot open file.");
        }
        root = doc.getRootElement();

        //Goto Lessons
        Element lessonsRoot = root.getChild("lessons");
        for (Element lessonEl: lessonsRoot.getChildren()){
            if (lessonEl.getAttribute("path") != null) {
                boolean lessonIsPassed = false;

                if (lessonEl.getAttribute("isPassed") != null) {
                    lessonIsPassed = (lessonEl.getAttribute("isPassed").getValue().equals("true"));
                } else {
                    throw new BadLessonException("Cannot obtain lessons from " + path + " course file");
                }
                Lesson tmpl = new Lesson(lessonEl.getAttribute("path").getValue(), lessonIsPassed, this);
                lessons.add(tmpl);
            } else throw new BadCourseException("Cannot obtain lessons from " + path + " course file");
        }
    }

    @Nullable
    public Lesson giveNotPassedLesson() {
        for (Lesson lesson : lessons) {
            if (!lesson.isPassed()) return lesson;
        }
        return null;
    }

    @Nullable
    public Lesson giveNotPassedAndNotOpenedLesson() {
        for (Lesson lesson : lessons) {
            if (!lesson.isPassed() && !lesson.isOpen()) return lesson;
        }
        return null;
    }

    public boolean hasNotPassedLesson() {
        for (Lesson lesson : lessons) {
            if (!lesson.isPassed()) return true;
        }
        return false;
    }

    public String getId(){
        return id;
    }


}
