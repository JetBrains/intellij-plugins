package training.lesson;

import com.intellij.ide.RecentProjectsManager;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.util.containers.ContainerUtilRt;
import com.intellij.vcs.log.Hash;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.commands.BadCommandException;
import training.editor.EduEditor;
import training.editor.EduEditorProvider;
import training.lesson.dialogs.SdkProblemDialog;
import training.lesson.exceptons.*;
import training.lesson.log.GlobalLessonLog;
import training.util.GenerateCourseXml;
import training.util.MyClassLoader;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 11/03/15.
 */
@State(
        name = "TrainingPluginCourses",
        storages = {
                @Storage(
                        file = StoragePathMacros.APP_CONFIG + "/trainingPlugin.xml"
                )
        }
)
public class CourseManager implements PersistentStateComponent<CourseManager.State> {

    private Project eduProject;

    final public static String EDU_PROJECT_NAME = "EduProject";

    CourseManager() {
        if (myState.courses == null || myState.courses.size() == 0) try {
            initCourses();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (BadCourseException e) {
            e.printStackTrace();
        } catch (BadLessonException e) {
            e.printStackTrace();
        }
    }

    private HashMap<Course, VirtualFile> mapCourseVirtualFile = new HashMap<Course, VirtualFile>();
    private State myState = new State();

    public static CourseManager getInstance() {
        return ServiceManager.getService(CourseManager.class);
    }

    public void initCourses() throws JDOMException, IOException, URISyntaxException, BadCourseException, BadLessonException {
        Element coursesRoot = Course.getRootFromPath(GenerateCourseXml.COURSE_ALLCOURSE_FILENAME);
        for (Element element : coursesRoot.getChildren()) {
            if (element.getName().equals(GenerateCourseXml.COURSE_TYPE_ATTR)) {
                String courseFilename = element.getAttribute(GenerateCourseXml.COURSE_NAME_ATTR).getValue();
                final Course course = Course.initCourse(courseFilename);
                addCourse(course);
            }
        }
    }


    @Nullable
    public Course getCourseById(String id) {
        final Course[] courses = getCourses();
        if (courses == null || courses.length == 0) return null;

        for (Course course : courses) {
            if (course.getId().toUpperCase().equals(id.toUpperCase())) return course;
        }
        return null;
    }

    public void registerVirtualFile(Course course, VirtualFile virtualFile) {
        mapCourseVirtualFile.put(course, virtualFile);
    }

    public boolean isVirtualFileRegistered(VirtualFile virtualFile) {
        return mapCourseVirtualFile.containsValue(virtualFile);
    }

    public void unregisterVirtaulFile(VirtualFile virtualFile) {
        if (!mapCourseVirtualFile.containsValue(virtualFile)) return;
        for (Course course : mapCourseVirtualFile.keySet()) {
            if (mapCourseVirtualFile.get(course).equals(virtualFile)) {
                mapCourseVirtualFile.remove(course);
                return;
            }
        }
    }

    public void unregisterCourse(Course course) {
        mapCourseVirtualFile.remove(course);
    }


    public synchronized void openLesson(Project project, final @Nullable Lesson lesson) throws BadCourseException, BadLessonException, IOException, FontFormatException, InterruptedException, ExecutionException, LessonIsOpenedException {

        try {

            assert lesson != null;


            checkEnvironment(project, lesson.getCourse());

            if (lesson.isOpen()) throw new LessonIsOpenedException(lesson.getName() + " is opened");

            //If lesson doesn't have parent course
            if (lesson.getCourse() == null)
                throw new BadLessonException("Unable to open lesson without specified course");
            final Project myProject = project;
            final VirtualFile vf = ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
                @Override
                public VirtualFile compute() {
                    try {
                        if (lesson.getCourse().courseType == Course.CourseType.SCRATCH) {
                            return getScratchFile(myProject, lesson);
                        } else {
                            if (!initEduProject(myProject)) return null;
                            return getFileInEduProject(lesson);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
            if (vf == null) return; //if user aborts opening lesson in EduProject or Virtual File couldn't be computed
            if (lesson.getCourse().courseType != Course.CourseType.SCRATCH) project = eduProject;

            //open next lesson if current is passed
            final Project currentProject = project;

            lesson.onStart();

            lesson.addLessonListener(new LessonListenerAdapter() {
                @Override
                public void lessonNext(Lesson lesson) throws BadLessonException, ExecutionException, IOException, FontFormatException, InterruptedException, BadCourseException, LessonIsOpenedException {
                    if (lesson.getCourse() == null) return;

                    if (lesson.getCourse().hasNotPassedLesson()) {
                        Lesson nextLesson = lesson.getCourse().giveNotPassedAndNotOpenedLesson();
                        if (nextLesson == null)
                            throw new BadLessonException("Unable to obtain not passed and not opened lessons");
                        openLesson(currentProject, nextLesson);
                    }
                }
            });

            final String target;
            if (lesson.getTargetPath() != null) {
                InputStream is = MyClassLoader.getInstance().getResourceAsStream(lesson.getCourse().getAnswersPath() + lesson.getTargetPath());
                if (is == null) throw new IOException("Unable to get answer for \"" + lesson.getName() + "\" lesson");
                target = new Scanner(is).useDelimiter("\\Z").next();
            } else {
                target = null;
            }


            //Dispose balloon while scratch file is closing. InfoPanel still exists.
            project.getMessageBus().connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                @Override
                public void fileOpened(FileEditorManager source, VirtualFile file) {
                }

                @Override
                public void fileClosed(FileEditorManager source, VirtualFile file) {
                }

                @Override
                public void selectionChanged(FileEditorManagerEvent event) {
                }
            });


            //Get EduEditor for the lesson. Select corresponding when EduEditor for this course is opened. Create a new EduEditor if no lessons for this course are opened.
            EduEditor eduEditor = getEduEditor(project, vf);
            assert eduEditor != null;
            eduEditor.selectIt(); // Select EduEditor with this lesson.
            //Return focus to Editor Content Component
            IdeFocusManager.getInstance(project).requestFocus(eduEditor.getEditor().getContentComponent(), true);


            //Process lesson
            LessonProcessor.process(lesson, eduEditor, project, eduEditor.getEditor().getDocument(), target);

        } catch (OldJdkException oldJdkException) {
            oldJdkException.printStackTrace();
        } catch (NoSdkException noSdkException){
            showSdkProblemDialog(project, noSdkException.getMessage());
        } catch (InvalidSdkException e) {
            showSdkProblemDialog(project, e.getMessage());
        } catch (BadCommandException e) {
            e.printStackTrace();
        }
    }

    private VirtualFile getFileInEduProject(Lesson lesson) throws IOException {

        final VirtualFile sourceRootFile = ProjectRootManager.getInstance(eduProject).getContentSourceRoots()[0];
        String courseFileName = "Test.java";
        if (lesson.getCourse() != null) courseFileName = lesson.getCourse().getName() + ".java";


        VirtualFile courseVirtualFile = sourceRootFile.findChild(courseFileName);
        if (courseVirtualFile == null) {
            courseVirtualFile = sourceRootFile.createChildData(this, courseFileName);
        }

        registerVirtualFile(lesson.getCourse(), courseVirtualFile);
        return courseVirtualFile;
    }

    private boolean initEduProject(Project projectToClose) {
        Project myEduProject = null;

        //if projectToClose is open
        final Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project openProject : openProjects) {
            final String name = openProject.getName();
            if (name.equals(EDU_PROJECT_NAME)) {
                myEduProject = openProject;
            }
        }
        if (myEduProject == null || myEduProject.getProjectFile() == null) {

            if(!ApplicationManager.getApplication().isUnitTestMode()) if (!NewEduProjectUtil.showDialogOpenEduProject(projectToClose)) return false; //if user abort to open lesson in a new Project
            if(myState.eduProjectPath != null) {
                try {
                    myEduProject = ProjectManager.getInstance().loadAndOpenProject(myState.eduProjectPath);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JDOMException e) {
                    e.printStackTrace();
                } catch (InvalidDataException e) {
                    e.printStackTrace();
                }
            } else {

                try {

                    JavaSdk javaSdk = JavaSdk.getInstance();
                    final String suggestedHomePath = javaSdk.suggestHomePath();
                    final String versionString = javaSdk.getVersionString(suggestedHomePath);
                    final Sdk jdk = javaSdk.createJdk(javaSdk.getVersion(versionString).name(), suggestedHomePath);

                    if (ProjectJdkTable.getInstance().findJdk(jdk.getName()) == null) SdkConfigurationUtil.addSdk(jdk);
//                    final Sdk sdk = SdkConfigurationUtil.findOrCreateSdk(null, javaSdk);
                    myEduProject = NewEduProjectUtil.createEduProject(EDU_PROJECT_NAME, projectToClose, jdk);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        eduProject = myEduProject;

        assert eduProject != null;
        assert eduProject.getProjectFile() != null;
        assert eduProject.getProjectFile().getParent() != null;
        assert eduProject.getProjectFile().getParent().getParent() != null;

        myState.eduProjectPath = eduProject.getBasePath();
        //Hide EduProject from Recent projects
        RecentProjectsManager.getInstance().removePath(eduProject.getPresentableUrl());
        return true;
    }

    @Nullable
    public Project getEduProject(){
        if (eduProject == null) {
            if (initEduProject(getCurrentProject()))
                return eduProject;
            else
                return null;
        } else {
            return eduProject;
        }
    }

    @Nullable
    public Project getCurrentProject(){
        final IdeFrame lastFocusedFrame = IdeFocusManager.getGlobalInstance().getLastFocusedFrame();
        if (lastFocusedFrame == null) return null;
        return lastFocusedFrame.getProject();
    }

    @NotNull
    private VirtualFile getScratchFile(final Project project, @Nullable Lesson lesson) throws IOException {
        VirtualFile vf = null;
        assert lesson != null;
        assert lesson.getCourse() != null;
        if (mapCourseVirtualFile.containsKey(lesson.getCourse()))
            vf = mapCourseVirtualFile.get(lesson.getCourse());
        if (vf == null || !vf.isValid()) {
            //while course info is not stored
            final String courseName = lesson.getCourse().getName();

            //find file if it is existed
            vf = ScratchFileService.getInstance().findFile(ScratchRootType.getInstance(), courseName, ScratchFileService.Option.existing_only);
            if (vf != null) {
                FileEditorManager.getInstance(project).closeFile(vf);
                ScratchFileService.getInstance().getScratchesMapping().setMapping(vf, Language.findLanguageByID("JAVA"));
            }


            if (vf == null || !vf.isValid()) {
                vf = ScratchRootType.getInstance().createScratchFile(project, courseName, Language.findLanguageByID("JAVA"), "");
                final VirtualFile finalVf = vf;
                if (!vf.getName().equals(courseName)) {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                finalVf.rename(project, courseName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
            registerVirtualFile(lesson.getCourse(), vf);
        }
        return vf;
    }

    @Nullable
    public EduEditor getEduEditor(Project project, VirtualFile vf) {
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
        final FileEditor[] allEditors = FileEditorManager.getInstance(project).getAllEditors(vf);
        if (allEditors.length == 0) {
            FileEditorManager.getInstance(project).openEditor(descriptor, true);
        } else {
            boolean editorIsFind = false;
            for (FileEditor curEditor : allEditors) {
                if (curEditor instanceof EduEditor) editorIsFind = true;
            }
            if (!editorIsFind) {
//              close other editors with this file
                FileEditorManager.getInstance(project).closeFile(vf);
                ScratchFileService.getInstance().getScratchesMapping().setMapping(vf, Language.findLanguageByID("JAVA"));
                FileEditorManager.getInstance(project).openEditor(descriptor, true);
            }
        }
        final FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor(vf);

        EduEditor eduEditor = null;
        if (selectedEditor instanceof EduEditor) eduEditor = (EduEditor) selectedEditor;
        else eduEditor = (EduEditor) (new EduEditorProvider()).createEditor(project, vf);
        return eduEditor;
    }

    /**
     * checking environment to start education plugin. Checking SDK.
     *
     * @param project where lesson should be started
     * @param course  education course
     * @throws OldJdkException     - if project JDK version is not enough for this course
     * @throws InvalidSdkException - if project SDK is not suitable for course
     */
    public void checkEnvironment(Project project, @Nullable Course course) throws OldJdkException, InvalidSdkException, NoSdkException {

        if (course == null) return;

        final Sdk projectJdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectJdk == null) throw new NoSdkException();

        final SdkTypeId sdkType = projectJdk.getSdkType();
        if (course.getSdkType() == Course.CourseSdkType.JAVA) {
            if (sdkType instanceof JavaSdk) {
                final JavaSdkVersion version = ((JavaSdk) sdkType).getVersion(projectJdk);
                if (version != null) {
                    if (!version.isAtLeast(JavaSdkVersion.JDK_1_6)) throw new OldJdkException(JavaSdkVersion.JDK_1_6);
                }
            } else if (sdkType.getName().equals("IDEA JDK")) {
                //do nothing
            } else {
                throw new InvalidSdkException("Please use at least JDK 1.6 or IDEA SDK with corresponding JDK");
            }
        }
    }

    public void showSdkProblemDialog(Project project, String sdkMessage) {
//        final SdkProblemDialog dialog = new SdkProblemDialog(project, "at least JDK 1.6 or IDEA SDK with corresponding JDK");
        final SdkProblemDialog dialog = new SdkProblemDialog(project, sdkMessage);
        dialog.show();
    }

    @Nullable
    public Lesson findLesson(String lessonName) {
        if (getCourses() == null) return null;
        for (Course course : getCourses()) {
            for (Lesson lesson : course.getLessons()) {
                if (lesson.getName() != null)
                    if (lesson.getName().toUpperCase().equals(lessonName.toUpperCase()))
                        return lesson;
            }
        }
        return null;
    }


    static class State {
        public final ArrayList<Course> courses = new ArrayList<Course>();
        public String eduProjectPath;
        public GlobalLessonLog globalLessonLog = new GlobalLessonLog();

        public State() {
        }


    }


    public void addCourse(Course course) {
        myState.courses.add(course);
    }

    @Nullable
    public Course[] getCourses() {
        if (myState == null) return null;
        if (myState.courses == null) return null;

        return myState.courses.toArray(new Course[myState.courses.size()]);
    }

    public GlobalLessonLog getGlobalLessonLog(){
        return myState.globalLessonLog;
    }

    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(State state) {
        myState.eduProjectPath = null;
        myState.globalLessonLog = state.globalLessonLog;

        if (state.courses == null || state.courses.size() == 0) {
            try {
                initCourses();
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (BadCourseException e) {
                e.printStackTrace();
            } catch (BadLessonException e) {
                e.printStackTrace();
            }
        } else {

            for (Course course : myState.courses) {
                if (state.courses.contains(course)) {
                    final Course courseFromPersistentState = state.courses.get(state.courses.indexOf(course));
                    for (Lesson lesson : course.getLessons()) {
                        if (courseFromPersistentState.getLessons().contains(lesson)) {
                            final Lesson lessonFromPersistentState = courseFromPersistentState.getLessons().get(courseFromPersistentState.getLessons().indexOf(lesson));
                            lesson.setPassed(lessonFromPersistentState.getPassed());
                        }
                    }
                }
            }
        }
    }


}
