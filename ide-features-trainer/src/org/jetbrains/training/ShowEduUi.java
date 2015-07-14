package org.jetbrains.training;

import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.training.eduUI.EduEditor;
import org.jetbrains.training.eduUI.EduEditorProvider;
import org.jetbrains.training.lesson.CourseManager;
import org.jetbrains.training.lesson.Lesson;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 23/06/15.
 */
public class ShowEduUi extends AnAction{
    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        boolean focusEditor = true;

        final Project project = anActionEvent.getProject();

        final VirtualFile vf;
//        vf = ScratchpadManager.getInstance(e.getProject()).createScratchFile(Language.findLanguageByID("JAVA"));
        //TODO: remove const "scratch" here

//        vf = ScratchRootType.getInstance().createScratchFile(anActionEvent.getProject(), "SCRATCH_FILE", Language.findLanguageByID("JAVA"), "");
//        CourseManager.getInstance().registerVirtaulFile(CourseManager.getInstance().getAnyCourse(), vf);

        //Open file with EduEditorProvider
//        FileEditorManagerEx.getInstanceEx(project).openFileWithProviders(vf, true, true);


        final Lesson lesson = CourseManager.getInstance().getCourseById("default").giveNotPassedLesson();
        try {
            CourseManager.getInstance().openLesson(anActionEvent.getProject(), lesson);
        } catch (BadCourseException e) {
            e.printStackTrace();
        } catch (BadLessonException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (LessonIsOpenedException e) {
            e.printStackTrace();
        }

    }


}
