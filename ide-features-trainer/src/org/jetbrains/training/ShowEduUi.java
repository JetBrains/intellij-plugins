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

/**
 * Created by karashevich on 23/06/15.
 */
public class ShowEduUi extends AnAction{
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        boolean focusEditor = true;

        final Project project = anActionEvent.getProject();

        final VirtualFile vf;
//        vf = ScratchpadManager.getInstance(e.getProject()).createScratchFile(Language.findLanguageByID("JAVA"));
        //TODO: remove const "scratch" here
        vf = ScratchRootType.getInstance().createScratchFile(anActionEvent.getProject(), "scratch2", Language.findLanguageByID("JAVA"), "some text here");
        CourseManager.getInstance().registerVirtaulFile(CourseManager.getInstance().getAnyCourse(), vf);
        OpenFileDescriptor descriptor = new OpenFileDescriptor(anActionEvent.getProject(), vf);



//        FileEditorManager.getInstance(project).openTextEditor()
//        final Document document = editor.getDocument();

//        FileEditorManager.getInstance(project).setSelectedEditor(descriptor.getFile(), new EduEditorProvider(project, vf).getEditorTypeId());
//        Editor editor = new EduEditor(project, vf).getEditor();
//        final Editor editor1 = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);

//        FileEditorManager.getInstance(project).getOpenedEditor(editor, focusEditor);


        final Pair<FileEditor[], FileEditorProvider[]> pair = FileEditorManagerEx.getInstanceEx(project).openFileWithProviders(vf, true, true);


//        if (openedFile != null && openedFile.getCanonicalPath() != null) {
//            String filePath = openedFile.getCanonicalPath();
//            executeFile(project, openedFile, filePath);
//        }
    }


}
