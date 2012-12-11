package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateBuilder;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.roots.impl.DirectoryInfo;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.util.CreateClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import cucumber.runtime.java.JavaSnippet;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Step;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.java.config.CucumberConfigUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.ArrayList;

/**
 * User: Andrey.Vokin
 * Date: 8/1/12
 */
public class JavaStepDefinitionCreator implements StepDefinitionCreator {
  public static final String CUCUMBER_1_1_ANNOTATION_PACKAGE = "@cucumber.api.java.en.";
  public static final String CUCUMBER_1_0_ANNOTATION_PACKAGE = "@cucumber.annotation.en.";

  @NotNull
  @Override
  public PsiFile createStepDefinitionContainer(@NotNull PsiDirectory dir, @NotNull String name) {
    PsiClass newClass = CreateClassUtil.createClassNamed(name, CreateClassUtil.DEFAULT_CLASS_TEMPLATE, dir);
    assert newClass != null;
    return newClass.getContainingFile();
  }

  @Override
  public boolean createStepDefinition(@NotNull GherkinStep step, @NotNull PsiFile file) {
    if (!(file instanceof PsiJavaFile)) return false;

    final Project project = file.getProject();
    final VirtualFile vFile = ObjectUtils.assertNotNull(file.getVirtualFile());
    final OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vFile);
    FileEditorManager.getInstance(project).getAllEditors(vFile);
    FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
    final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

    assert editor != null;
    // ToDo: duplication of code with Ruby analog
    final TemplateManager templateManager = TemplateManager.getInstance(file.getProject());
    final TemplateState templateState = TemplateManagerImpl.getTemplateState(editor);
    final Template template = templateManager.getActiveTemplate(editor);
    if (templateState != null && template != null) {
      templateState.gotoEnd(false);
    }

    // snippet text
    final PsiMethod element = buildStepDefinitionByStep(step);

    final PsiClass clazz = PsiTreeUtil.getChildOfType(file, PsiClass.class);
    if (clazz != null) {
      PsiDocumentManager.getInstance(project).commitAllDocuments();
      PsiMethod addedElement = (PsiMethod)clazz.add(element);
      addedElement = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(addedElement);
      JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedElement);
      final TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(addedElement);

      final PsiAnnotation annotation = addedElement.getModifierList().getAnnotations()[0];
      final PsiNameValuePair regexpElement = annotation.getParameterList().getAttributes()[0];
      final TextRange range = new TextRange(1, regexpElement.getTextLength() - 1);
      builder.replaceElement(regexpElement, range, regexpElement.getText().substring(range.getStartOffset(), range.getEndOffset()));

      final PsiParameterList blockVars = addedElement.getParameterList();
      for (PsiParameter var : blockVars.getParameters()) {
        final PsiElement nameIdentifier = var.getNameIdentifier();
        if (nameIdentifier != null) {
          builder.replaceElement(nameIdentifier, nameIdentifier.getText());
        }
      }

      final PsiCodeBlock body = addedElement.getBody();
      if (body != null && body.getStatements().length > 0) {
        final PsiElement firstStatement = body.getStatements()[0];
        final TextRange pendingRange = new TextRange(0, firstStatement.getTextLength() - 1);
        builder.replaceElement(firstStatement, pendingRange,
                               firstStatement.getText().substring(pendingRange.getStartOffset(), pendingRange.getEndOffset()));
      }

      final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
      documentManager.doPostponedOperationsAndUnblockDocument(editor.getDocument());
      builder.run(editor, false);
    }

    return true;
  }

  @Override
  public boolean validateNewStepDefinitionFileName(@NotNull Project project, @NotNull String name) {
    if(name.length() == 0) return false;
    if (! Character.isJavaIdentifierStart(name.charAt(0))) return false;
    for (int i = 1; i < name.length(); i++) {
      if (! Character.isJavaIdentifierPart(name.charAt(i))) return false;
    }
    return true;
  }

  @NotNull
  @Override
  public PsiDirectory getDefaultStepDefinitionFolder(@NotNull final GherkinStep step) {
    PsiFile featureFile = step.getContainingFile();
    if (featureFile != null) {
      PsiDirectory directory = featureFile.getContainingDirectory();
      if (directory != null && directory.getManager() != null) {
        PsiManager manager = directory.getManager();
        DirectoryIndex directoryIndex = DirectoryIndex.getInstance(manager.getProject());
        DirectoryInfo info = directoryIndex.getInfoForDirectory(directory.getVirtualFile());
        if (info != null) {
          VirtualFile sourceRoot = info.getSourceRoot();
          //noinspection ConstantConditions
          final Module module = ProjectRootManager.getInstance(step.getProject()).getFileIndex().getModuleForFile(featureFile.getVirtualFile());
          if (module != null) {
            final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
            final VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots();
            if (sourceRoot != null && sourceRoot.getName().equals("resources")) {
              final VirtualFile resourceParent = sourceRoot.getParent();
              for (VirtualFile vFile : sourceRoots) {
                if (vFile.getPath().startsWith(resourceParent.getPath()) && vFile.getName().equals("java")) {
                  sourceRoot = vFile;
                  break;
                }
              }
            }
            else {
              if (sourceRoots.length > 0) {
                sourceRoot = sourceRoots[sourceRoots.length - 1];
              }
            }
          }
          String packageName = "";
          if (sourceRoot != null) {
            packageName = CucumberJavaUtil.getPackageOfStepDef(step);
          }

          final String packagePath = packageName.replace('.', '/');
          final String path = sourceRoot != null ? sourceRoot.getPath() : directory.getVirtualFile().getPath();
          // ToDo: I shouldn't create directories, only create VirtualFile object.
          final Ref<PsiDirectory> resultRef = new Ref<PsiDirectory>();
          new WriteAction() {
            protected void run(Result result) throws Throwable {
              final VirtualFile packageFile = VfsUtil.createDirectoryIfMissing(path + '/' + packagePath);
              if (packageFile != null) {
                resultRef.set(PsiDirectoryFactory.getInstance(step.getProject()).createDirectory(packageFile));
              }
            }
          }.execute();
          return resultRef.get();
        }
      }
    }

    assert featureFile != null;
    return ObjectUtils.assertNotNull(featureFile.getParent());
  }

  @NotNull
  @Override
  public String getStepDefinitionFilePath(@NotNull final PsiFile file) {
    final VirtualFile vFile = file.getVirtualFile();
    if (file instanceof PsiJavaFile && vFile != null) {
      String packageName = ((PsiJavaFile)file).getPackageName();
      if (StringUtil.isEmptyOrSpaces(packageName)) {
        return vFile.getNameWithoutExtension();
      }
      else {
        return vFile.getNameWithoutExtension() + " (" + packageName + ")";
      }
    }
    return file.getName();
  }

  private static PsiMethod buildStepDefinitionByStep(@NotNull final GherkinStep step) {
    String annotationPackage = CUCUMBER_1_1_ANNOTATION_PACKAGE;
    final String version = CucumberConfigUtil.getCucumberCoreVersion(step);
    if (version != null && version.compareTo(CucumberConfigUtil.CUCUMBER_VERSION_1_1) < 0) {
      annotationPackage = CUCUMBER_1_0_ANNOTATION_PACKAGE;
    }

    final PsiElementFactory factory = JavaPsiFacade.getInstance(step.getProject()).getElementFactory();
    final Step cucumberStep = new Step(new ArrayList<Comment>(), step.getKeyword().getText(), step.getStepName(), 0, null, null);
    final String snippet = new SnippetGenerator(new JavaSnippet()).getSnippet(cucumberStep)
      .replace("PendingException", "cucumber.runtime.PendingException")
      .replaceFirst("@", annotationPackage)
      .replaceAll("\\\\\\\\", "\\\\")
      .replaceAll("\\\\d", "\\\\\\\\d");

    return factory.createMethodFromText(snippet, step);
  }
}
