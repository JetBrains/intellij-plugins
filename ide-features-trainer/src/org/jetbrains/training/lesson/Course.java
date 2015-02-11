package org.jetbrains.training.lesson;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

/**
 * Created by karashevich on 29/01/15.
 */
public class Course {

    private HashSet<Lesson> lessons;
    private String path;
    private String pathDir;
    final private String defaultPath = "data/DefaultCourse.xml";
    final private String defaultPathDir = "data/";
    private String answersPath;
    private Element root;

    public Course() throws BadCourseException, BadLessonException {
        path = defaultPath;
        pathDir = defaultPathDir;
        lessons = new HashSet<Lesson>();

        initLessons();
        answersPath = root.getAttribute("answerspath").getValue();
    }

    public Course(String coursePath) throws BadCourseException, BadLessonException {
        //load xml with lessons
        path = coursePath;
        pathDir = defaultPathDir;
        lessons = new HashSet<Lesson>();

        initLessons();
        answersPath = root.getAttribute("answerspath").getValue();
    }

    public String getAnswersPath() {
        return answersPath;
    }

    private void initLessons() throws BadCourseException, BadLessonException {

        InputStream is = this.getClass().getResourceAsStream(path);

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
                Lesson tmpl = new Lesson(pathDir + lessonEl.getAttribute("path").getValue(), lessonIsPassed);
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

}
