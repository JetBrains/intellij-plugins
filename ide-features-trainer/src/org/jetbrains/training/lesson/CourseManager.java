package org.jetbrains.training.lesson;

import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.LessonIsOpenedException;
import org.jetbrains.training.MyClassLoader;
import org.jetbrains.training.eduUI.EduEditor;
import org.jetbrains.training.graphics.DetailPanel;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 11/03/15.
 */
public class CourseManager extends ActionGroup{

    public static final CourseManager INSTANCE = new CourseManager();
    public static final Dimension DIMENSION = new Dimension(500, 60);

    public ArrayList<Course> courses;
    private HashMap<Course, VirtualFile> mapCourseVirtualFile;


    public static CourseManager getInstance(){
        return INSTANCE;
    }

    public CourseManager() {
        //init courses; init default course by default
        super("Courses", true);
        courses = new ArrayList<Course>();
        mapCourseVirtualFile = new HashMap<Course, VirtualFile>();


        try {
            final Course defaultCourse = new Course();
            courses.add(defaultCourse);
        } catch (BadCourseException e) {
            e.printStackTrace();
        } catch (BadLessonException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        AnAction[] actions = new AnAction[courses.size()];
        actions = courses.toArray(actions);
        return actions;
    }

    @Nullable
    public Course getAnyCourse(){
        if(courses == null || courses.size() == 0) return null;
        return courses.get(0);
    }

    @Nullable
    public Course getCourseById(String id){
        if(courses == null || courses.size() == 0) return null;

        for(Course course: courses){
            if(course.getId().toUpperCase().equals(id.toUpperCase())) return course;
        }
        return null;
    }

    public void registerVirtaulFile(Course course, VirtualFile virtualFile){
        mapCourseVirtualFile.put(course, virtualFile);
    }

    public boolean isVirtualFileRegistered(VirtualFile virtualFile){
        return mapCourseVirtualFile.containsValue(virtualFile);
    }

    public synchronized void openLesson(final Project project, final @Nullable Lesson lesson) throws BadCourseException, BadLessonException, IOException, FontFormatException, InterruptedException, ExecutionException, LessonIsOpenedException {

        if (lesson == null) throw new BadLessonException("Cannot open \"null\" lesson");
        if (lesson.isOpen()) throw new LessonIsOpenedException(lesson.getId() + " is opened");


//        final VirtualFile vf;
//        vf = ScratchpadManager.getInstance(e.getProject()).createScratchFile(Language.findLanguageByID("JAVA"));
        //TODO: remove const "scratch" here
//        vf = ScratchRootType.getInstance().createScratchFile(e.getProject(), "scratch", Language.findLanguageByID("JAVA"), "");

//        OpenFileDescriptor descriptor = new OpenFileDescriptor(e.getProject(), vf);
//        final Editor editor = FileEditorManager.getInstance(e.getProject()).openTextEditor(descriptor, true);
//        final Document document = editor.getDocument();

        //Lesson without course
        if(lesson.getCourse() == null) return;
        final VirtualFile vf;
        if (mapCourseVirtualFile.containsKey(lesson.getCourse())) {
            vf = mapCourseVirtualFile.get(lesson.getCourse());
        } else {
            vf = ScratchRootType.getInstance().createScratchFile(project, "SCRATCH_FILE", Language.findLanguageByID("JAVA"), "");
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        vf.rename(CourseManager.getInstance(), lesson.getCourse().getId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        //open next lesson if current is passed
        lesson.addLessonListener(new LessonListenerAdapter(){
            @Override
            public void lessonNext(Lesson lesson) throws BadLessonException, ExecutionException, IOException, FontFormatException, InterruptedException, BadCourseException, LessonIsOpenedException {
                if (lesson.getCourse() == null) return;

                if(lesson.getCourse().hasNotPassedLesson()) {
                    Lesson nextLesson = lesson.getCourse().giveNotPassedAndNotOpenedLesson();
                    if (nextLesson == null) throw new BadLessonException("Unable to obtain not passed and not opened lessons");
                    openLesson(project, nextLesson);
                }
            }
        });

        final String target;
        if(lesson.getTargetPath() != null) {
            InputStream is = MyClassLoader.getInstance().getResourceAsStream(lesson.getCourse().getAnswersPath() + lesson.getTargetPath());
            if (is == null) throw new IOException("Unable to get answer for \"" + lesson.getId() + "\" lesson");
            target = new Scanner(is).useDelimiter("\\Z").next();
        } else {
            target = null;
        }

//        lesson.open(DIMENSION);

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


        EduEditor eduEditor = getEduEditor(project, vf);

        //Process lesson
        LessonProcessor.process(lesson, eduEditor, project, eduEditor.getEditor().getDocument(), target);
    }

    @Nullable
    private EduEditor getEduEditor(Project project, VirtualFile vf) {
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
        final FileEditor[] allEditors = FileEditorManager.getInstance(project).getAllEditors(vf);
        if(allEditors == null) {
            FileEditorManager.getInstance(project).openEditor(descriptor, true);
        } else {
            boolean editorIsFind = false;
            for (FileEditor curEditor : allEditors) {
                if(curEditor instanceof EduEditor) editorIsFind = true;
            }
            if (!editorIsFind) FileEditorManager.getInstance(project).openEditor(descriptor, true);
        }
        final FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor(vf);

        EduEditor eduEditor = null;
        if (selectedEditor instanceof EduEditor) eduEditor = (EduEditor) selectedEditor;
        return eduEditor;
    }

}
