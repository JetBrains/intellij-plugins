package org.jetbrains.training.lesson;

import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
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
import org.jetbrains.training.graphics.DetailPanel;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 11/03/15.
 */
public class CourseManager extends ActionGroup{

    public static final CourseManager INSTANCE = new CourseManager();
    public static final Dimension DIMENSION = new Dimension(500, 60);

    public ArrayList<Course> courses;


    public static CourseManager getInstance(){
        return INSTANCE;
    }

    public CourseManager() {
        //init courses; init default course by default
        super("Courses", true);
        courses = new ArrayList<Course>();

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


    public synchronized void openLesson(final AnActionEvent e, final @Nullable Lesson lesson) throws BadCourseException, BadLessonException, IOException, FontFormatException, InterruptedException, ExecutionException, LessonIsOpenedException {

        if (lesson == null) throw new BadLessonException("Cannot open \"null\" lesson");
        if (lesson.isOpen()) throw new LessonIsOpenedException(lesson.getId() + " is opened");

        final VirtualFile vf;
//        vf = ScratchpadManager.getInstance(e.getProject()).createScratchFile(Language.findLanguageByID("JAVA"));
        //TODO: remove const "scratch" here
        vf = ScratchRootType.getInstance().createScratchFile(e.getProject(), "scratch", Language.findLanguageByID("JAVA"), "");

        OpenFileDescriptor descriptor = new OpenFileDescriptor(e.getProject(), vf);
        final Editor editor = FileEditorManager.getInstance(e.getProject()).openTextEditor(descriptor, true);
        final Document document = editor.getDocument();


        if(lesson.getParentCourse() == null) return;

        //open next lesson if current is passed
        lesson.addLessonListener(new LessonListenerAdapter(){
            @Override
            public void lessonNext(Lesson lesson) throws BadLessonException, ExecutionException, IOException, FontFormatException, InterruptedException, BadCourseException, LessonIsOpenedException {
                if (lesson.getParentCourse() == null) return;

                if(lesson.getParentCourse().hasNotPassedLesson()) {
                    Lesson nextLesson = lesson.getParentCourse().giveNotPassedAndNotOpenedLesson();
                    if (nextLesson == null) throw new BadLessonException("Unable to obtain not passed and not opened lessons");
                    openLesson(e, nextLesson);
                }
            }
        });


        //Rename Scratch file if previous existed
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                boolean vacantName = false;
                String lessonName = lesson.getId();
                int version = 1;

                while(!vacantName) {
                    try {
                        if (version == 1) {
                            vf.rename(this, lessonName);
                            vacantName = true;
                        } else {
                            vf.rename(this, lessonName + "_" + version);
                            vacantName = true;
                        }
                    } catch (IOException e1) {
                        version++;
                    }
                }
            }
        });

        InputStream is = MyClassLoader.getInstance().getResourceAsStream(lesson.getParentCourse().getAnswersPath() + lesson.getTargetPath());
        if(is == null) throw new IOException("Unable to get answer for \"" + lesson.getId() + "\" lesson");
        final String target = new Scanner(is).useDelimiter("\\Z").next();

        lesson.open(DIMENSION);
        showInfoPanel(lesson.getInfoPanel(), editor, DIMENSION);

        //Dispose balloon while scratch file is closing. InfoPanel still exists.
        e.getProject().getMessageBus().connect(e.getProject()).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(FileEditorManager source, VirtualFile file) {

            }

            @Override
            public void fileClosed(FileEditorManager source, VirtualFile file) {
                if (file == vf) {
                    if (lesson.getInfoPanel() != null) {
                        lesson.getInfoPanel().hideBalloon();
                        lesson.close();
                    }
                }
            }

            @Override
            public void selectionChanged(FileEditorManagerEvent event) {
                if (((FileEditorManager)event.getSource()).getSelectedTextEditor() == editor) {
                    if(lesson.getInfoPanel() != null)
                        showInfoPanel(lesson.getInfoPanel(), editor, DIMENSION);
                } else {
                    if (lesson.getInfoPanel() != null)
                        lesson.getInfoPanel().hideBalloon();
                }
            }
        });

        //Process lesson
        LessonProcessor.process(lesson, editor, e, document, target);
    }

    private void showInfoPanel(DetailPanel infoPanel, Editor editor, Dimension dimension) {

        final IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(editor.getProject());
        RelativePoint location = computeLocation(ideFrame, dimension);
        infoPanel.showBalloon(dimension, location);

    }

    private RelativePoint computeLocation(IdeFrame ideFrame, Dimension dimension){
        int statusBarHeight = ideFrame.getStatusBar().getComponent().getHeight();
        Rectangle visibleRect = ideFrame.getComponent().getVisibleRect();
        Point point = new Point(visibleRect.x + (visibleRect.width - dimension.width) / 2, visibleRect.y + visibleRect.height - dimension.height - statusBarHeight - 20);
        return new RelativePoint(ideFrame.getComponent(), point);
    }
}
