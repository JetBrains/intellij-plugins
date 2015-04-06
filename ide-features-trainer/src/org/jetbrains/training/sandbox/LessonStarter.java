package org.jetbrains.training.sandbox;

import com.intellij.CommonBundle;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.LessonIsOpenedException;
import org.jetbrains.training.MyClassLoader;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.lesson.Course;
import org.jetbrains.training.lesson.CourseManager;
import org.jetbrains.training.lesson.Lesson;

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

//            openLesson(anActionEvent, lesson);


        } catch (BadLessonException ble) {
            Notifications.Bus.notify(new Notification("Unable to open new lesson: all lessons are opened or passed", CommonBundle.getErrorTitle(), ble.getMessage(), NotificationType.ERROR));
        } catch (BadCourseException bce) {
            bce.printStackTrace();
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        } catch (InterruptedException e1) {
//            e1.printStackTrace();
//        } catch (ExecutionException e1) {
//            e1.printStackTrace();
//        } catch (FontFormatException e1) {
//            e1.printStackTrace();
//        } catch (LessonIsOpenedException e) {
//            Notifications.Bus.notify(new Notification("Unable to open new lesson: all lessons are opened or passed", CommonBundle.getErrorTitle(), e.getMessage(), NotificationType.ERROR));
        }
    }



}
