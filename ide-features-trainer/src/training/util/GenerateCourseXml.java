package training.util;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import training.lesson.Course;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by karashevich on 11/08/15.
 */
public class GenerateCourseXml {

    public final static String COURSE_ALLCOURSE_ATTR = "courses";
    public final static String COURSE_ALLCOURSE_FILENAME = "courses.xml";
    public final static String COURSE_COURSES_PATH = "courses";
    public final static String COURSE_TYPE_ATTR = "course";

    public final static String COURSE_NAME_ATTR = "name";
    public final static String COURSE_XML_VER_ATTR = "version";
    public final static String COURSE_ID_ATTR = "id";
    public final static String COURSE_XML_VERSION = "0.3";
    public final static String COURSE_LESSON_ELEMENT = "lesson";
    public final static String COURSE_ANSWER_PATH_ATTR = "answerPath";
    public final static String COURSE_SDK_TYPE = "sdkType";
    public final static String COURSE_FILE_TYPE = "fileType";

    public final static String COURSE_LESSONS_PATH_ATTR = "lessonsPath";
    public final static String COURSE_LESSON_FILENAME_ATTR = "filename";
    public final static String COURSE_LESSON_SOLUTION = "solution";

    public static void gen(String courseName, String id, String path) throws URISyntaxException {
        try {

            Element course = new Element(COURSE_TYPE_ATTR);
            course.setAttribute(COURSE_NAME_ATTR, courseName);
            course.setAttribute(COURSE_LESSONS_PATH_ATTR, path);
            course.setAttribute(COURSE_XML_VER_ATTR, COURSE_XML_VERSION);
            course.setAttribute(COURSE_ID_ATTR, id);
            Document doc = new Document(course);
            doc.setRootElement(course);

            File dir = new File(GenerateCourseXml.class.getResource("/data/" + path).toURI());

            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    Element lesson = new Element(COURSE_LESSON_ELEMENT);
                    lesson.setAttribute(COURSE_LESSON_FILENAME_ATTR, file.getName());
                    doc.getRootElement().addContent(lesson);
                }
            }
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            String dataPath = GenerateCourseXml.class.getResource("/data/").getPath();
            File outputFile = new File(dataPath + courseName + ".xml");
            outputFile.createNewFile();
            xmlOutput.output(doc, new FileWriter(outputFile));


        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void genCourses() throws URISyntaxException, IOException {

        Element courses = new Element(COURSE_ALLCOURSE_ATTR);
        courses.setAttribute(COURSE_XML_VER_ATTR, COURSE_XML_VERSION);
        Document doc = new Document(courses);
        doc.setRootElement(courses);



        File dir = new File(GenerateCourseXml.class.getResource("/data/").toURI());

        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                if (!file.getName().equals(COURSE_ALLCOURSE_FILENAME)) {
                    String name = file.getName();
                    doc.getRootElement().addContent((new Element(COURSE_TYPE_ATTR)).setAttribute(COURSE_NAME_ATTR, name));
                }
            }
        }

        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        String dataPath = GenerateCourseXml.class.getResource("/data/").getPath();
        File outputFile = new File(dataPath + COURSE_ALLCOURSE_FILENAME);
        outputFile.createNewFile();
        xmlOutput.output(doc, new FileWriter(outputFile));
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
//        gen("Completions", "completions", "Completions/");
//        gen("Refactorings", "refactorings", "Refactorings/");
        gen("Navigation", "navigation", "Navigation/");
//        genCourses();
    }

    public static Course.CourseSdkType getSdkTypeFromString(String string) {
        for (Course.CourseSdkType courseSdkType : Course.CourseSdkType.values()) {
            if(courseSdkType.toString().equals(string)) return courseSdkType;
        }
        return null;
    }
}
