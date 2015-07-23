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
import org.jetbrains.training.util.MyClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by karashevich on 29/01/15.
 */
public class Course extends ActionGroup{

    private ArrayList<Lesson> lessons;
    private String answersPath;
    private Element root;
    private String id;
    private String name;

    public Course(String name, Element root) throws JDOMException, BadLessonException, BadCourseException, IOException {
        super(name, true);
        lessons = new ArrayList<Lesson>();
        this.name = name;
        this.root = root;
        initLessons();
        answersPath = root.getAttribute("answerspath").getValue();
        id = root.getAttribute("id").getValue();
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        AnAction[] actions = new AnAction[lessons.size()];
        actions = lessons.toArray(actions);
        return actions;
    }

    @Nullable
    public static Course initCourse(String coursePath) throws BadCourseException, BadLessonException, JDOMException, IOException {
        //load xml with lessons

        //Check DOM with Course
        Element init_root = getRootFromPath(coursePath);
        if(init_root.getAttribute("name") == null) return null;
        String init_name = init_root.getAttribute("name").getValue();

        return new Course(init_name, init_root);

    }

    private static Element getRootFromPath(String pathToFile) throws JDOMException, IOException {
        InputStream is = MyClassLoader.getInstance().getResourceAsStream(pathToFile);
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(is);
        return document.getRootElement();
    }

    public String getAnswersPath() {
        return answersPath;
    }

    public ArrayList<Lesson> getLessons() {
        return lessons;
    }

    private void initLessons() throws BadCourseException, BadLessonException, JDOMException, IOException {

        name = root.getAttribute("name").getValue();

        if (root.getAttribute("lessonspath")!=null){

            //retieve list of xml files inside lessonspath directory
            String lessonsPath = root.getAttribute("lessonspath").getValue();
            String lessonsFullpath = MyClassLoader.getInstance().getDataPath() + lessonsPath;
            File dir = new File(lessonsFullpath);

            for (String lessonFilename : dir.list()) {
                String lessonPath = lessonsPath + lessonFilename;
                try {
                    Scenario scn = new Scenario(lessonPath);
                    Lesson lesson = new Lesson(scn, false, this);
                    lessons.add(lesson);
                } catch (JDOMException e) {
                    //Scenario file is corrupted
                    throw new BadLessonException("Probably scenario file is corrupted: " + lessonPath);
                } catch (IOException e) {
                    //Scenario file cannot be read
                    throw new BadLessonException("Probably scenario file cannot be read: " + lessonPath);
                }
            }

        } else {

            //Goto Lessons
            Element lessonsRoot = root.getChild("lessons");
            for (Element lessonEl : lessonsRoot.getChildren()) {
                if (lessonEl.getAttribute("path") != null) {
                    boolean lessonIsPassed = false;

                    if (lessonEl.getAttribute("isPassed") != null) {
                        lessonIsPassed = (lessonEl.getAttribute("isPassed").getValue().equals("true"));
                    } else {
                        throw new BadLessonException();
                    }

                    String lessonPath = lessonEl.getAttribute("path").getValue();
                    try {
                        Scenario scn = new Scenario(lessonPath);
                        Lesson lesson = new Lesson(scn, lessonIsPassed, this);
                        lessons.add(lesson);
                    } catch (JDOMException e) {
                        //Scenario file is corrupted
                        throw new BadLessonException("Probably scenario file is corrupted: " + lessonPath);
                    } catch (IOException e) {
                        //Scenario file cannot be read
                        throw new BadLessonException("Probably scenario file cannot be read: " + lessonPath);
                    }
                } else throw new BadCourseException();
            }
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
