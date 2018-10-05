package com.google.jstestdriver.idea.assertFramework.support;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.NotNullProducer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class AddAdapterSupportIntentionAction implements IntentionAction {

  private final String myAssertionFrameworkName;
  private final NotNullProducer<? extends List<VirtualFile>> myAdapterSourceFilesProvider;
  private final String myAdapterHomePageUrl;

  public AddAdapterSupportIntentionAction(@NotNull String assertionFrameworkName,
                                          @NotNull NotNullProducer<? extends List<VirtualFile>> adapterSourceFilesProvider,
                                          @Nullable String adapterHomePageUrl) {
    myAssertionFrameworkName = assertionFrameworkName;
    myAdapterSourceFilesProvider = adapterSourceFilesProvider;
    myAdapterHomePageUrl = adapterHomePageUrl;
  }

  @NotNull
  @Override
  public String getText() {
    return "Add " + myAssertionFrameworkName + " JsTestDriver adapter";
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getText();
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, Editor editor, final PsiFile psiFile) {
    return psiFile != null && psiFile.isValid();
  }

  @Override
  public void invoke(@NotNull final Project project, Editor editor, final PsiFile file) throws IncorrectOperationException {
    AddAdapterSupportDialog dialog = new AddAdapterSupportDialog(
      project,
      file,
      myAssertionFrameworkName,
      myAdapterSourceFilesProvider.produce(),
      myAdapterHomePageUrl
    );
    final boolean result = dialog.showAndGet();
    final VirtualFile virtualFile = file.getVirtualFile();
    if (virtualFile != null && result) {
      FileContentUtil.reparseFiles(project, Collections.singletonList(virtualFile), true);
    }
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

}
