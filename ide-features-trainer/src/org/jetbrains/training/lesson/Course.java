package org.jetbrains.training.lesson;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.util.GenerateCourseXml;
import org.jetbrains.training.util.MyClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by karashevich on 29/01/15.
 */
public class Course extends ActionGroup{

    private ArrayList<Lesson> lessons;
    @Nullable
    private String answersPath;
    private Element root;
    private String id;
    private String name;

    public Course(String name, Element root) throws JDOMException, BadLessonException, BadCourseException, IOException, URISyntaxException {
        super(name, true);
        lessons = new ArrayList<Lesson>();
        this.name = name;
        this.root = root;
        initLessons();
        if (root.getAttribute(GenerateCourseXml.COURSE_ANSWER_PATH_ATTR) != null) {
            answersPath = root.getAttribute(GenerateCourseXml.COURSE_ANSWER_PATH_ATTR).getValue();
        } else {
            answersPath = null;
        }
        id = root.getAttribute(GenerateCourseXml.COURSE_ID_ATTR).getValue();
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        AnAction[] actions = new AnAction[lessons.size()];
        actions = lessons.toArray(actions);
        return actions;
    }

    @Nullable
    public static Course initCourse(String coursePath) throws BadCourseException, BadLessonException, JDOMException, IOException, URISyntaxException {
        //load xml with lessons

        //Check DOM with Course
        Element init_root = getRootFromPath(coursePath);
        if(init_root.getAttribute(GenerateCourseXml.COURSE_NAME_ATTR) == null) return null;
        String init_name = init_root.getAttribute(GenerateCourseXml.COURSE_NAME_ATTR).getValue();

        return new Course(init_name, init_root);

    }

    private static Element getRootFromPath(String pathToFile) throws JDOMException, IOException {
        InputStream is = MyClassLoader.getInstance().getResourceAsStream(pathToFile);
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(is);
        return document.getRootElement();
    }

    @Nullable
    public String getAnswersPath() {
        return answersPath;
    }

    public ArrayList<Lesson> getLessons() {
        return lessons;
    }

    private void initLessons() throws BadCourseException, BadLessonException, JDOMException, IOException, URISyntaxException {

        name = root.getAttribute(GenerateCourseXml.COURSE_NAME_ATTR).getValue();

        if (root.getAttribute(GenerateCourseXml.COURSE_LESSONS_PATH_ATTR)!=null){

            //retieve list of xml files inside lessonspath directory
            String lessonsPath = root.getAttribute(GenerateCourseXml.COURSE_LESSONS_PATH_ATTR).getValue();
//            String lessonsFullpath = MyClassLoader.getInstance().getDataPath() + lessonsPath;
//            URL url = Course.class.getResource(lessonsFullpath);
//            File dir = new File(Course.class.getResource("/data/" + lessonsPath).toURI());

            for (Element lessonElement : root.getChildren()) {
                if (!lessonElement.getName().equals(GenerateCourseXml.COURSE_LESSON_ELEMENT)) throw new BadCourseException("Course file is corrupted or cannot be read properly");

                String lessonFilename = lessonElement.getAttributeValue(GenerateCourseXml.COURSE_LESSON_FILENAME_ATTR).toString();
                String lessonPath = lessonsPath + lessonFilename;
                try {
                    Scenario scn = new Scenario(lessonPath);
                    Lesson lesson = new Lesson(scn, false, this);
                    lessons.add(lesson);
                } catch (JDOMException e) {
                    //Lesson file is corrupted
                    throw new BadLessonException("Probably lesson file is corrupted: " + lessonPath);
                } catch (IOException e) {
                    //Lesson file cannot be read
                    throw new BadLessonException("Probably lesson file cannot be read: " + lessonPath);
                }
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
