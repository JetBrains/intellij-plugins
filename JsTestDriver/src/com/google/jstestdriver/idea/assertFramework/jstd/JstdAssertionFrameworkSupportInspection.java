package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.jstd.jsSrc.JstdDefaultAssertionFrameworkSrcMarker;
import com.google.jstestdriver.idea.assertFramework.library.JstdLibraryUtil;
import com.google.jstestdriver.idea.assertFramework.support.AbstractMethodBasedInspection;
import com.google.jstestdriver.idea.assertFramework.support.ChooseScopeAndCreateLibraryDialog;
import com.google.jstestdriver.idea.util.JstdResolveUtil;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.FileContentUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.ScriptingFrameworkDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class JstdAssertionFrameworkSupportInspection extends AbstractMethodBasedInspection {

  private static final AddJstdLibraryIntentionAction ADD_JSTD_LIBRARY_INTENTION_ACTION = new AddJstdLibraryIntentionAction();

  @Override
  protected boolean isSuitableElement(@NotNull JSFile jsFile, @NotNull JSCallExpression callExpression) {
    JstdTestFileStructure structure = JstdTestFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    String name = structure.getNameByPsiElement(callExpression.getMethodExpression());
    return name != null;
  }

  @Override
  protected IntentionAction getFix() {
    return ADD_JSTD_LIBRARY_INTENTION_ACTION;
  }

  @Override
  protected String getProblemDescription() {
    return "No coding assistance for JsTestDriver assertion framework";
  }

  @Override
  protected boolean isResolved(@NotNull JSReferenceExpression methodExpression) {
    if (JstdResolveUtil.isResolvedToFunction(methodExpression)) {
      return true;
    }
    VirtualFile virtualFile = PsiUtilCore.getVirtualFile(methodExpression);
    if (virtualFile != null) {
      return JstdLibraryUtil.isFileInJstdLibScope(methodExpression.getProject(), virtualFile);
    }
    return true;
  }

  private static class AddJstdLibraryIntentionAction implements IntentionAction {

    @NotNull
    @Override
    public String getText() {
      return "Add JsTestDriver assertion framework support";
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return getText();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
      return file != null && file.isValid();
    }

    @Override
    public void invoke(@NotNull final Project project, Editor editor, final PsiFile file) throws IncorrectOperationException {
      UIUtil.invokeLaterIfNeeded(() -> {
        List<VirtualFile> sources = getLibrarySourceFiles();
        final VirtualFile fileRequestor = PsiUtilCore.getVirtualFile(file);
        DialogWrapper dialog = new ChooseScopeAndCreateLibraryDialog(
          project,
          JstdLibraryUtil.LIBRARY_NAME,
          sources,
          new ScriptingFrameworkDescriptor(JstdLibraryUtil.LIBRARY_NAME, "1.3.5"),
          fileRequestor,
          false
        );
        boolean done = dialog.showAndGet();
        if (done) {
          FileContentUtil.reparseFiles(project, Collections.singletonList(fileRequestor), true);
        }
      });
    }

    @Override
    public boolean startInWriteAction() {
      return false;
    }

    @NotNull
    private static List<VirtualFile> getLibrarySourceFiles() {
      return VfsUtils.findVirtualFilesByResourceNames(
        JstdDefaultAssertionFrameworkSrcMarker.class,
        new String[]{"Asserts.js", "TestCase.js"}
      );
    }

  }

}
