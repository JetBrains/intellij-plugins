package org.jetbrains.training.util;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.training.lesson.Course;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by karashevich on 11/08/15.
 */
public class GenerateCourseXml {

    public final static String COURSE_NAME_ATTR = "name";
    public final static String COURSE_XML_VER_ATTR = "version";
    public final static String COURSE_ID_ATTR = "id";
    public final static String COURSE_XML_VERSION = "0.3";
    public final static String COURSE_LESSON_ELEMENT = "lesson";
    public final static String COURSE_ANSWER_PATH_ATTR = "answerPath";

    public final static String COURSE_LESSONS_PATH_ATTR = "lessonsPath";
    public final static String COURSE_LESSON_FILENAME_ATTR = "filename";

    public static void gen(String courseName, String id, String path) throws URISyntaxException {
        try {

            Element course = new Element("course");
            course.setAttribute(COURSE_NAME_ATTR, courseName);
            course.setAttribute(COURSE_LESSONS_PATH_ATTR, path);
            course.setAttribute(COURSE_XML_VER_ATTR, COURSE_XML_VERSION);
            course.setAttribute(COURSE_ID_ATTR, id);
            Document doc = new Document(course);
            doc.setRootElement(course);

            File dir = new File(GenerateCourseXml.class.getResource("/data/" + path).toURI());

            for (String lessonFilename : dir.list()) {
                Element lesson = new Element(COURSE_LESSON_ELEMENT);
                lesson.setAttribute(COURSE_LESSON_FILENAME_ATTR, lessonFilename);
                doc.getRootElement().addContent(lesson);
            }

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            File outputFile = new File(GenerateCourseXml.class.getResource("/data/" + courseName + ".xml").toURI());
            if (!outputFile.exists()) {
                if (outputFile.createNewFile()) xmlOutput.output(doc, new FileWriter(outputFile));
                else throw new IOException("Unable to create new file");
            }
            xmlOutput.output(doc, new FileWriter(outputFile));

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void main(String[] args) throws URISyntaxException {
        gen("DefaultCourse", "default", "EditorBasics/");
    }
}
