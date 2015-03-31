package org.jetbrains.training.lesson;

import com.intellij.CommonBundle;
//import com.intellij.ide.scratch.ScratchpadManager;
import com.intellij.lang.Language;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.LessonIsOpenedException;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.keymap.KeymapUtil;
import org.jetbrains.training.keymap.SubKeymapUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 10/03/15.
 */
public class LessonStarterEx {

    AnActionEvent anActionEvent;
    Dimension dimension;

    public LessonStarterEx(AnActionEvent ideActionEvent) {
        anActionEvent = ideActionEvent;

        //TODO: delete that
//        ActionManager.getInstance().addAnActionListener(new AnActionListener() {
//            @Override
//            public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
//                final String actionId = ActionManager.getInstance().getId(action);
//                final KeyStroke shortcutByActionId = KeymapUtil.getShortcutByActionId(actionId);
//                System.err.println("ACTION-INFO-SYSTEM >>> [shortcut]: " + SubKeymapUtil.getKeyStrokeTextSub(shortcutByActionId));
//            }
//
//            @Override
//            public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
//
//            }
//
//            @Override
//            public void beforeEditorTyping(char c, DataContext dataContext) {
//
//            }
//        });

        try {

            //get default course via CourseManager
            final Course course = CourseManager.getInstance().getCourseById("default");
            if (course == null) throw new BadCourseException("Cannot find \"default\" course.");

            final Lesson lesson = course.giveNotPassedAndNotOpenedLesson();
            if (lesson == null) throw new BadLessonException("Cannot load not passed lessons for \"" + course.getId() + "\" course");

            openLesson(anActionEvent, lesson);


        } catch (BadLessonException ble) {
            Notifications.Bus.notify(new Notification("Unable to open new lesson: all lessons are opened or passed", CommonBundle.getErrorTitle(), ble.getMessage(), NotificationType.ERROR));
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
        } catch (LessonIsOpenedException e) {
            Notifications.Bus.notify(new Notification("Unable to open new lesson: all lessons are opened or passed", CommonBundle.getErrorTitle(), e.getMessage(), NotificationType.ERROR));
        }
    }

    private synchronized void openLesson(AnActionEvent e, final @Nullable Lesson lesson) throws BadCourseException, BadLessonException, IOException, FontFormatException, InterruptedException, ExecutionException, LessonIsOpenedException {

        if (lesson == null) throw new BadLessonException("Cannot open \"null\" lesson");
        if (lesson.isOpen()) throw new LessonIsOpenedException(lesson.getId() + " is opened");

        final VirtualFile vf = null;
        //vf = ScratchpadManager.getInstance(e.getProject()).createScratchFile(Language.findLanguageByID("JAVA"));


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
                    openLesson(anActionEvent, nextLesson);
                }
            }
        });

        vf.rename(this, lesson.getId()); //Rename scratch file as a lesson name
        InputStream is = this.getClass().getResourceAsStream(lesson.getParentCourse().getAnswersPath() + lesson.getTargetPath());
        if(is == null) throw new IOException("Unable to get answer for \"" + lesson.getId() + "\" lesson");
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
