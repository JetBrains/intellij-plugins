package org.jetbrains.training.lesson;

import com.intellij.ide.scratch.ScratchpadManager;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.graphics.DetailPanel;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 10/03/15.
 */
public class LessonStarter {

    AnActionEvent anActionEvent;
    Dimension dimension;

    public LessonStarter(AnActionEvent ideActionEvent) {
        anActionEvent = ideActionEvent;

        try {

            //get default course via CourseManager
            final Course course = CourseManager.getInstance().getCourseById("default");
            if (course == null) throw new BadCourseException("Cannot find \"default\" course.");

            final Lesson lesson = course.giveNotPassedLesson();
            if (lesson == null) throw new BadLessonException("Cannot load not passed lessons for \"" + course.getId() + "\" course");

            openLesson(anActionEvent, lesson);


        } catch (BadLessonException ble) {
            ble.printStackTrace();
        } catch (BadCourseException bce) {
            bce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        } catch (FontFormatException e1) {
            e1.printStackTrace();
        }
    }

    private void openLesson(AnActionEvent e, final Lesson lesson) throws BadCourseException, BadLessonException, IOException, FontFormatException, InterruptedException, ExecutionException {
        final VirtualFile vf;
        vf = ScratchpadManager.getInstance(e.getProject()).createScratchFile(Language.findLanguageByID("JAVA"));

        OpenFileDescriptor descriptor = new OpenFileDescriptor(e.getProject(), vf);
        final Editor editor = FileEditorManager.getInstance(e.getProject()).openTextEditor(descriptor, true);
        final Document document = editor.getDocument();


        if(lesson.getParentCourse() == null) return;


        //open next lesson if current is passed
        lesson.addLessonListener(new LessonListenerAdapter(){
            @Override
            public void lessonNext(Lesson lesson) throws BadLessonException, ExecutionException, IOException, FontFormatException, InterruptedException, BadCourseException {
                if (lesson.getParentCourse() == null) return;

                if(lesson.getParentCourse().hasNotPassedLesson()) {
                    Lesson nextLesson = lesson.getParentCourse().giveNotPassedLesson();
                    openLesson(anActionEvent, nextLesson);
                }
            }
        });

        vf.rename(this, lesson.getId()); //Rename scratch file as a lesson name
        InputStream is = this.getClass().getResourceAsStream(lesson.getParentCourse().getAnswersPath() + lesson.getTargetPath());
        final String target = new Scanner(is).useDelimiter("\\Z").next();

        dimension = new Dimension(500, 60);
        lesson.open(dimension);

        showInfoPanel(lesson.getInfoPanel(), editor);

        //Dispose balloon while scratch file is closing. InfoPanel still exists.
        e.getProject().getMessageBus().connect(e.getProject()).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(FileEditorManager source, VirtualFile file) {

            }

            @Override
            public void fileClosed(FileEditorManager source, VirtualFile file) {
                if (source.getSelectedEditor(file) == editor) {
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
                        showInfoPanel(lesson.getInfoPanel(), editor);
                } else {
                    if (lesson.getInfoPanel() != null)
                        lesson.getInfoPanel().hideBalloon();
                }
            }
        });

        //Process lesson
        LessonProcessor.process(lesson, editor, e, document, target);
    }

    private void showInfoPanel(DetailPanel infoPanel, Editor editor) {

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
