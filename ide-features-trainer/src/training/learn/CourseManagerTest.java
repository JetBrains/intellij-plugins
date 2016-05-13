package training.learn;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import training.learn.exceptons.*;
import training.solutions.BaseSolutionClass;
import training.testFramework.LessonSolution;
import training.util.generateModuleXml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by karashevich on 11/03/15.
 */

@TestOnly
public class CourseManagerTest {

    private static final String DOT = ".";

    public static CourseManagerTest INSTANCE = new CourseManagerTest();

    @TestOnly
    public static CourseManagerTest getInstance(){
        return INSTANCE;
    }

    private ArrayList<Module> modules;

    public CourseManagerTest() {
        modules = new ArrayList<Module>();
        if (modules.size() == 0) try {
            initCourses();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void initCourses() throws JDOMException, IOException, URISyntaxException, BadModuleException, BadLessonException {
        Element coursesRoot = Module.getRootFromPath(generateModuleXml.MODULE_ALLMODULE_FILENAME);
        for (Element element : coursesRoot.getChildren()) {
            if (element.getName().equals(generateModuleXml.MODULE_TYPE_ATTR)) {
                String courseFilename = element.getAttribute(generateModuleXml.MODULE_NAME_ATTR).getValue();
                final Module module = Module.initModule(courseFilename);
                addCourse(module);
            }
        }
    }


    @Nullable
    public Module getModuleById(String id) {
        final Module[] modules = getModules();
        if (modules == null || modules.length == 0) return null;

        for (Module module : modules) {
            if (module.getId().toUpperCase().equals(id.toUpperCase())) return module;
        }
        return null;
    }


    @Nullable
    public Lesson findLesson(String lessonName) {
        if (getModules() == null) return null;
        for (Module module : getModules()) {
            for (Lesson lesson : module.getLessons()) {
                if (lesson.getName() != null)
                    if (lesson.getName().toUpperCase().equals(lessonName.toUpperCase()))
                        return lesson;
            }
        }
        return null;
    }



    public void addCourse(Module module) {
        modules.add(module);
    }

    @Nullable
    public Module[] getModules() {
        if (modules == null) return null;
        return modules.toArray(new Module[modules.size()]);
    }


    @Nullable
    public LessonSolution findSolution(String lessonId) throws Exception {
        final Lesson lesson = findLesson(lessonId);
        assert lesson != null;
        if (lesson.getModule() == null) return null;
        final Element courseRoot = lesson.getModule().getModuleRoot();
        if (courseRoot == null) return null;

        String lessonsPath = (courseRoot.getAttribute(generateModuleXml.MODULE_LESSONS_PATH_ATTR) != null) ? lesson.getModule().getModulePath() + courseRoot.getAttribute(generateModuleXml.MODULE_LESSONS_PATH_ATTR).getValue() : "";
        String lessonSolutionName = null;

        for (Element lessonElement : courseRoot.getChildren()) {
            if (!lessonElement.getName().equals(generateModuleXml.MODULE_LESSON_ELEMENT))
                throw new BadModuleException("Module file is corrupted or cannot be read properly");

            String lessonFilename = lessonElement.getAttributeValue(generateModuleXml.MODULE_LESSON_FILENAME_ATTR);
            String lessonPath = lessonsPath + lessonFilename;
            if (lessonPath.equals(lesson.getScn().getPath())){
                lessonSolutionName = lessonElement.getAttributeValue(generateModuleXml.MODULE_LESSON_SOLUTION);
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
