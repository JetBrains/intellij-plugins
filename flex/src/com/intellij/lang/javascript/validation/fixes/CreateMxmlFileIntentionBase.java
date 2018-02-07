package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Consumer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CreateMxmlFileIntentionBase implements CreateClassIntentionWithCallback {
  protected PsiElement myElement;
  protected String myPackageName;
  protected String myClassName;
  private final boolean myIdentifierIsValid;
  private Consumer<String> myCreatedClassFqnConsumer;

  public CreateMxmlFileIntentionBase(final String classFqn, final @NotNull PsiElement element) {
    myElement = element;
    myClassName = StringUtil.getShortName(classFqn);
    myIdentifierIsValid =
      LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.JAVASCRIPT.getLanguage()).isIdentifier(myClassName, null);
    myPackageName = StringUtil.getPackageName(classFqn);
  }

  @NotNull
  public String getFamilyName() {
    return CodeInsightBundle.message("create.file.family");
  }

  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return myIdentifierIsValid && myElement.isValid();
  }

  public void setCreatedClassFqnConsumer(final Consumer<String> consumer) {
    myCreatedClassFqnConsumer = consumer;
  }

  @NotNull
  public String getName() {
    return getText();
  }

  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    invoke(project, null, myElement.getContainingFile());
  }

  public void invoke(@NotNull final Project project, final @Nullable Editor editor, final PsiFile file) throws IncorrectOperationException {
    final Module module = ModuleUtil.findModuleForPsiElement(file);
    if (module == null) {
      return;
    }

    final Pair<String, PsiDirectory> fileTextAndDir = getFileTextAndDir(module);
    if (fileTextAndDir.first == null || fileTextAndDir.second == null) {
      return;
    }

    ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        final String fileName = myClassName + JavaScriptSupportLoader.MXML_FILE_EXTENSION_DOT;
        final PsiFile newFile = fileTextAndDir.second.createFile(fileName);

        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        final Document document = psiDocumentManager.getDocument(newFile);
        document.setText(fileTextAndDir.first);
        psiDocumentManager.commitDocument(document);
        CodeStyleManager.getInstance(project).reformat(newFile);
        FileEditorManager.getInstance(project).openFile(newFile.getVirtualFile(), true);

        if (myCreatedClassFqnConsumer != null) {
          final String packageName = ProjectRootManager.getInstance(project).getFileIndex().getPackageNameByDirectory(
            fileTextAndDir.second.getVirtualFile());
          myCreatedClassFqnConsumer.consume(packageName + (packageName.isEmpty() ? "" : ".") + myClassName);
        }
      }
      catch (IncorrectOperationException e) {
        Messages.showErrorDialog(project, e.getMessage(), getText());
      }
    });
  }

  protected Pair<String, PsiDirectory> getFileTextAndDir(final @NotNull Module module) {
    final PsiDirectory baseDir = myElement.getContainingFile().getParent();
    final GlobalSearchScope scope =
      PlatformPackageUtil.adjustScope(baseDir, GlobalSearchScope.moduleWithDependenciesScope(module), false, true);
    final PsiDirectory psiDirectory = JSRefactoringUtil
      .chooseOrCreateDirectoryForClass(module.getProject(), module, scope, myPackageName, null, baseDir, ThreeState.UNSURE);

    return Pair.create(getFileText(), psiDirectory);
  }

  protected String getFileText() {
    return "";
  }
}
