package org.jetbrains.training.lesson;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.MyClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by karashevich on 29/01/15.
 */
public class Course extends ActionGroup{

    private ArrayList<Lesson> lessons;
    private String path;
    private final static String DEFAULT_PATH = "DefaultCourse.xml";
    private final static String DEFAULT_NAME = "Default Course";
    private String answersPath;
    private Element root;
    private String id;
    private String name;

    public Course() throws BadCourseException, BadLessonException, JDOMException, IOException {
        super(DEFAULT_NAME, true);
        path = DEFAULT_PATH;
        lessons = new ArrayList<Lesson>();
        id = "default";
        name = "default";

        initLessons();
        answersPath = root.getAttribute("answerspath").getValue();
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        AnAction[] actions = new AnAction[lessons.size()];
        actions = lessons.toArray(actions);
        return actions;
    }

    public Course(String coursePath) throws BadCourseException, BadLessonException, JDOMException, IOException {
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

    public ArrayList<Lesson> getLessons() {
        return lessons;
    }

    private void initLessons() throws BadCourseException, BadLessonException, JDOMException, IOException {

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

        name = root.getAttribute("name").getValue();

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

                String pathToScenario = lessonEl.getAttribute("path").getValue();
                try{
                    Scenario scn = new Scenario(pathToScenario);
                    Lesson tmpl = new Lesson(scn, lessonIsPassed, this);
                    lessons.add(tmpl);
                } catch (JDOMException e) {
                    //Scenario file is corrupted
                    throw new BadLessonException("Probably scenario file is corrupted: " + pathToScenario);
                } catch (IOException e) {
                    //Scenario file cannot be read
                    throw new BadLessonException("Probably scenario file cannot be read: " + pathToScenario);
                }
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


    public String getName() {
        return name;
    }
}
