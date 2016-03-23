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
import training.learn.exceptons.BadModuleException;
import training.learn.exceptons.BadLessonException;
import training.util.generateModuleXml;
import training.util.MyClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by karashevich on 29/01/15.
 */
@Tag("course")
public class Module extends ActionGroup{

    private String modulePath;

    public String getModulePath() {
        return (modulePath != null ? modulePath + "/" : "");
    }

    public enum ModuleType {SCRATCH, PROJECT};

    private ArrayList<Lesson> lessons;
    @Nullable
    private String answersPath;
    @Nullable
    private Element root = null;
    private String id;
    @NotNull
    public String name;
    public ModuleType moduleType;
    @Nullable
    private ModuleSdkType mySdkType = null;

    public enum ModuleSdkType {JAVA}

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
    public ModuleSdkType getMySdkType() {
        return mySdkType;
    }

    public void setMySdkType(@Nullable ModuleSdkType mySdkType) {
        this.mySdkType = mySdkType;
    }

    public void setName(String name) {
        if(name != null) getTemplatePresentation().setText(name);
        this.name = name;
    }


    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public Module(){
        name = "Test";
        lessons = new ArrayList<Lesson>();
    }


    public Module(String name, Element root) throws JDOMException, BadLessonException, BadModuleException, IOException, URISyntaxException {
        super(name, true);
        lessons = new ArrayList<Lesson>();
        this.name = name;
        this.root = root;
        modulePath = generateModuleXml.MODULE_MODULES_PATH;
        initLessons();
        if (root.getAttribute(generateModuleXml.MODULE_ANSWER_PATH_ATTR) != null) {
            answersPath = root.getAttribute(generateModuleXml.MODULE_ANSWER_PATH_ATTR).getValue();
        } else {
            answersPath = null;
        }
        id = root.getAttribute(generateModuleXml.MODULE_ID_ATTR).getValue();
        if (root.getAttribute(generateModuleXml.MODULE_SDK_TYPE) != null){
            mySdkType = generateModuleXml.getSdkTypeFromString(root.getAttribute(generateModuleXml.MODULE_SDK_TYPE).getValue());
        }
        final Attribute attributeFileType = root.getAttribute(generateModuleXml.MODULE_FILE_TYPE);
        if (attributeFileType != null) {
            if(attributeFileType.getValue().toUpperCase().equals(ModuleType.SCRATCH.toString().toUpperCase())) moduleType = ModuleType.SCRATCH;
            else if(attributeFileType.getValue().toUpperCase().equals(ModuleType.PROJECT.toString().toUpperCase())) moduleType = ModuleType.PROJECT;
            else throw new BadModuleException("Unable to recognise ModuleType (should be SCRATCH or PROJECT)");
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
    public static Module initModule(String modulePath) throws BadModuleException, BadLessonException, JDOMException, IOException, URISyntaxException {
        //load xml with lessons

        //Check DOM with Module
        Element init_root = getRootFromPath(modulePath);
        if(init_root.getAttribute(generateModuleXml.MODULE_NAME_ATTR) == null) return null;
        String init_name = init_root.getAttribute(generateModuleXml.MODULE_NAME_ATTR).getValue();

        return new Module(init_name, init_root);

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

    private void initLessons() throws BadModuleException, BadLessonException, JDOMException, IOException, URISyntaxException {

        name = root.getAttribute(generateModuleXml.MODULE_NAME_ATTR).getValue();

        if (root.getAttribute(generateModuleXml.MODULE_LESSONS_PATH_ATTR) != null) {

            //retrieve list of xml files inside lessonspath directory
            String lessonsPath = getModulePath() + root.getAttribute(generateModuleXml.MODULE_LESSONS_PATH_ATTR).getValue();
//            String lessonsFullpath = MyClassLoader.getInstance().getDataPath() + lessonsPath;
//            URL url = Module.class.getResource(lessonsFullpath);
//            File dir = new File(Module.class.getResource("/data/" + lessonsPath).toURI());

            for (Element lessonElement : root.getChildren()) {
                if (!lessonElement.getName().equals(generateModuleXml.MODULE_LESSON_ELEMENT))
                    throw new BadModuleException("Module file is corrupted or cannot be read properly");

                String lessonFilename = lessonElement.getAttributeValue(generateModuleXml.MODULE_LESSON_FILENAME_ATTR);
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

    public ModuleSdkType getSdkType() {
         return mySdkType;
    }

    public boolean equals(Object o) {
        if(o == null) return false;
        if(!(o instanceof Module)) return false;
        if(((Module) o).getName().equals(this.getName())) return true;
        return false;

    }

    @Nullable
    Element getModuleRoot(){
        return root;
    }
}
