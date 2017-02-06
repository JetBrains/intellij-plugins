package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartGetterDeclaration;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.getChildrenOfType;

public abstract class BaseDartGenerateAction extends AnAction {

  @Override
  public boolean startInTransaction() {
    return true;
  }

  public void actionPerformed(final AnActionEvent e) {
    final Project project = e.getData(CommonDataKeys.PROJECT);
    assert project != null;
    final Pair<Editor, PsiFile> editorAndPsiFile = getEditorAndPsiFile(e);
    getGenerateHandler().invoke(project, editorAndPsiFile.first, editorAndPsiFile.second);
  }

  private static Pair<Editor, PsiFile> getEditorAndPsiFile(final AnActionEvent e) {
    final Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) return Pair.create(null, null);
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    return Pair.create(editor, psiFile);
  }

  @NotNull
  protected abstract BaseDartGenerateHandler getGenerateHandler();

  @Override
  public void update(final AnActionEvent e) {
    final Pair<Editor, PsiFile> editorAndPsiFile = getEditorAndPsiFile(e);
    final Editor editor = editorAndPsiFile.first;
    final PsiFile psiFile = editorAndPsiFile.second;

    final int caretOffset = editor == null ? -1 : editor.getCaretModel().getOffset();
    final boolean enable = psiFile != null && doEnable(PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartClass.class));

    e.getPresentation().setEnabled(enable);
    e.getPresentation().setVisible(enable);
  }

  protected boolean doEnable(@Nullable final DartClass dartClass) {
    return dartClass != null;
  }

  protected static boolean doesClassContainMethod(@NotNull final DartClass dartClass, @NotNull final String methodName) {
    if (methodName.isEmpty()) {
      return false;
    }
    final DartMethodDeclaration[] methodDeclarations = getChildrenOfType(DartResolveUtil.getBody(dartClass), DartMethodDeclaration.class);
    if (methodDeclarations == null) {
      return false;
    }
    for (DartMethodDeclaration methodDeclaration : methodDeclarations) {
      if (methodName.equals(methodDeclaration.getName())) {
        return true;
      }
    }
    return false;
  }

  protected static boolean doesClassContainGetter(@NotNull final DartClass dartClass, @NotNull final String getterName) {
    if (getterName.isEmpty()) {
      return false;
    }
    final DartGetterDeclaration[] getterDeclarations = getChildrenOfType(DartResolveUtil.getBody(dartClass), DartGetterDeclaration.class);
    if (getterDeclarations == null) {
      return false;
    }
    for (DartGetterDeclaration getterDeclaration : getterDeclarations) {
      if (getterName.equals(getterDeclaration.getName())) {
        return true;
      }
    }
    return false;
  }

  protected static boolean hasNonStaticField(@NotNull final DartClass dartClass) {
    for (DartComponent component : DartResolveUtil.getNamedSubComponents(dartClass)) {
      if (DartComponentType.typeOf(component) == DartComponentType.FIELD && !component.isStatic()) {
        return true;
      }
    }
    return false;
  }
}
