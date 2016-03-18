package training.learn;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.learn.exceptons.BadCourseException;
import training.learn.exceptons.BadLessonException;
import training.util.GenerateCourseXml;
import training.util.MyClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by karashevich on 29/01/15.
 */
@Tag("course")
public class Course extends ActionGroup{

    private String coursePath;

    public String getCoursePath() {
        return (coursePath != null ? coursePath + "/" : "");
    }

    public enum CourseType {SCRATCH, PROJECT};

    private ArrayList<Lesson> lessons;
    @Nullable
    private String answersPath;
    @Nullable
    private Element root = null;
    private String id;
    @NotNull
    public String name;
    public CourseType courseType;
    @Nullable
    private CourseSdkType mySdkType = null;

    public enum CourseSdkType {JAVA}

    public void setAnswersPath(@Nullable String answersPath) {
        this.answersPath = answersPath;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLessons(ArrayList<Lesson> lessons) {
        this.lessons = lessons;
    }

    @Nullable
    public CourseSdkType getMySdkType() {
        return mySdkType;
    }

    public void setMySdkType(@Nullable CourseSdkType mySdkType) {
        this.mySdkType = mySdkType;
    }

    public void setName(String name) {
        if(name != null) getTemplatePresentation().setText(name);
        this.name = name;
    }


    public void setCoursePath(String coursePath) {
        this.coursePath = coursePath;
    }

    public Course(){
        name = "Test";
        lessons = new ArrayList<Lesson>();
    }


    public Course(String name, Element root) throws JDOMException, BadLessonException, BadCourseException, IOException, URISyntaxException {
        super(name, true);
        lessons = new ArrayList<Lesson>();
        this.name = name;
        this.root = root;
        coursePath = GenerateCourseXml.COURSE_COURSES_PATH;
        initLessons();
        if (root.getAttribute(GenerateCourseXml.COURSE_ANSWER_PATH_ATTR) != null) {
            answersPath = root.getAttribute(GenerateCourseXml.COURSE_ANSWER_PATH_ATTR).getValue();
        } else {
            answersPath = null;
        }
        id = root.getAttribute(GenerateCourseXml.COURSE_ID_ATTR).getValue();
        if (root.getAttribute(GenerateCourseXml.COURSE_SDK_TYPE) != null){
            mySdkType = GenerateCourseXml.getSdkTypeFromString(root.getAttribute(GenerateCourseXml.COURSE_SDK_TYPE).getValue());
        }
        final Attribute attributeFileType = root.getAttribute(GenerateCourseXml.COURSE_FILE_TYPE);
        if (attributeFileType != null) {
            if(attributeFileType.getValue().toUpperCase().equals(CourseType.SCRATCH.toString().toUpperCase())) courseType = CourseType.SCRATCH;
            else if(attributeFileType.getValue().toUpperCase().equals(CourseType.PROJECT.toString().toUpperCase())) courseType = CourseType.PROJECT;
            else throw new BadCourseException("Unable to recognise CourseType (should be SCRATCH or PROJECT)");
        }


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

    public static Element getRootFromPath(String pathToFile) throws JDOMException, IOException {
        InputStream is = MyClassLoader.getInstance().getResourceAsStream(pathToFile);
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(is);
        return document.getRootElement();
    }

    @Nullable
    public String getAnswersPath() {
        return answersPath;
    }


    @AbstractCollection(surroundWithTag = true)
    public ArrayList<Lesson> getLessons() {
        return lessons;
    }

    private void initLessons() throws BadCourseException, BadLessonException, JDOMException, IOException, URISyntaxException {

        name = root.getAttribute(GenerateCourseXml.COURSE_NAME_ATTR).getValue();

        if (root.getAttribute(GenerateCourseXml.COURSE_LESSONS_PATH_ATTR) != null) {

            //retrieve list of xml files inside lessonspath directory
            String lessonsPath = getCoursePath() + root.getAttribute(GenerateCourseXml.COURSE_LESSONS_PATH_ATTR).getValue();
//            String lessonsFullpath = MyClassLoader.getInstance().getDataPath() + lessonsPath;
//            URL url = Course.class.getResource(lessonsFullpath);
//            File dir = new File(Course.class.getResource("/data/" + lessonsPath).toURI());

            for (Element lessonElement : root.getChildren()) {
                if (!lessonElement.getName().equals(GenerateCourseXml.COURSE_LESSON_ELEMENT))
                    throw new BadCourseException("Course file is corrupted or cannot be read properly");

                String lessonFilename = lessonElement.getAttributeValue(GenerateCourseXml.COURSE_LESSON_FILENAME_ATTR);
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
            if (!lesson.getPassed()) return lesson;
        }
        return null;
    }

    @Nullable
    public Lesson giveNotPassedAndNotOpenedLesson() {
        for (Lesson lesson : lessons) {
            if (!lesson.getPassed() && !lesson.isOpen()) return lesson;
        }
        return null;
    }

    public boolean hasNotPassedLesson() {
        for (Lesson lesson : lessons) {
            if (!lesson.getPassed()) return true;
        }
        return false;
    }

    public String getId(){
        return id;
    }


    @NotNull
    public String getName() {
        return name;
    }

    public CourseSdkType getSdkType() {
         return mySdkType;
    }

    public boolean equals(Object o) {
        if(o == null) return false;
        if(!(o instanceof Course)) return false;
        if(((Course) o).getName().equals(this.getName())) return true;
        return false;

    }

    @Nullable
    Element getCourseRoot(){
        return root;
    }
}
