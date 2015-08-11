package org.jetbrains.training.lesson;

import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.util.MyClassLoader;
import org.jetbrains.training.eduUI.EduEditor;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 11/03/15.
 */
public class CourseManager{

    public static final CourseManager INSTANCE = new CourseManager();
    public static final Dimension DIMENSION = new Dimension(500, 60);

    public ArrayList<Course> courses;
    private HashMap<Course, VirtualFile> mapCourseVirtualFile;


    public static CourseManager getInstance(){
        return INSTANCE;
    }

    public CourseManager() {
        //init courses; init default course by default
        courses = new ArrayList<Course>();
        mapCourseVirtualFile = new HashMap<Course, VirtualFile>();


        try {
            final Course defaultCourse = Course.initCourse("DefaultCourse.xml");
            courses.add(defaultCourse);
        } catch (BadCourseException e) {
            e.printStackTrace();
        } catch (BadLessonException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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

        //If lesson from some course
        if(lesson.getCourse() == null) return;
        VirtualFile vf = null;
        //If virtual file for this course exists;
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
            registerVirtaulFile(lesson.getCourse(), vf);


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
