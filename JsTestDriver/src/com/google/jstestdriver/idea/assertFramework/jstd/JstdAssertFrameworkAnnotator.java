package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.JsAssertFrameworkLibraryManager;
import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.scripting.ScriptingLibraryModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.FileContentUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class JstdAssertFrameworkAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    JSCallExpression callExpression = CastUtils.tryCast(element, JSCallExpression.class);
    if (callExpression != null) {
      JSReferenceExpression referenceExpression = CastUtils.tryCast(callExpression.getMethodExpression(), JSReferenceExpression.class);
      JSArgumentList jsArgumentList = callExpression.getArgumentList();
      if (referenceExpression != null && jsArgumentList != null) {
        JSExpression[] arguments = ObjectUtils.notNull(jsArgumentList.getArguments(), JSExpression.EMPTY_ARRAY);
        boolean subject = "TestCase".equals(referenceExpression.getReferencedName()) && arguments.length <= 3;
        subject = subject || ("AsyncTestCase".equals(referenceExpression.getReferencedName()) && arguments.length <= 2);
        if (subject) {
          boolean resolved = canBeResolved(referenceExpression);
          if (!resolved) {
            installAnnotation(holder, referenceExpression);
          }
        }
      }
    }
  }

  private static boolean canBeResolved(PsiPolyVariantReference psiPolyVariantReference) {
    ResolveResult[] resolveResults = psiPolyVariantReference.multiResolve(false);
    for (ResolveResult resolveResult : resolveResults) {
      PsiElement resolvedElement = resolveResult.getElement();
      if (resolvedElement != null && resolveResult.isValidResult()) {
        return true;
      }
    }
    return false;
  }

  private static void installAnnotation(AnnotationHolder holder, final JSReferenceExpression referenceExpression) {
    String referencedName = referenceExpression.getReferencedName();
    Annotation annotation = holder.createErrorAnnotation(referenceExpression, referencedName + " is unresolved.");
    System.out.println("installing annotation '" + annotation.getMessage() + "'");
    annotation.registerFix(new IntentionAction() {
      @NotNull
      @Override
      public String getText() {
        return "Add '" + JsAssertFrameworkLibraryManager.LIBRARY_NAME + "' JavaScript library";
      }

      @NotNull
      @Override
      public String getFamilyName() {
        return getText();
      }

      @Override
      public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (referenceExpression.getContainingFile() == file) {
          Document document = editor.getDocument();
          int editorCaretLineNo = document.getLineNumber(editor.getCaretModel().getOffset());
          int methodReferenceLineNo = document.getLineNumber(referenceExpression.getTextOffset());
          return editorCaretLineNo == methodReferenceLineNo;
        }
        return false;
      }

      @Override
      public void invoke(@NotNull final Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        final ScriptingLibraryModel scriptingLibraryModel = JsAssertFrameworkLibraryManager.createScriptingLibraryModelAndAssociateIt(
          project, project.getBaseDir()
        );
        FileContentUtil.reparseFiles(project, Arrays.asList(file.getVirtualFile()), true);
        if (scriptingLibraryModel == null) {
          System.out.println("Unable to create library '" + JsAssertFrameworkLibraryManager.LIBRARY_NAME + "'");
        }
      }

      @Override
      public boolean startInWriteAction() {
        return false;
      }
    });
  }

  /*
  @Nullable
  private Module getModuleByPsiFile(@Nullable PsiFile psiFile) {
    if (psiFile == null) return null;
    VirtualFile virtualFile = psiFile.getVirtualFile();
    if (virtualFile == null) return null;
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(psiFile.getProject()).getFileIndex();
    return fileIndex.getModuleForFile(virtualFile);
  }
  */

  /*
  private void addLibraryToModule(final Module module, final Library library) {
    System.out.println("Adding library '" + library.getName() + "' to module '" + module.getName() + "'");
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        ModifiableRootModel rootModel = manager.getModifiableModel();
        LibraryOrderEntry libraryOrderEntry = rootModel.addLibraryEntry(library);
        libraryOrderEntry.setScope(DependencyScope.COMPILE);
        //for (OrderRootType orderRootType : OrderRootType.getAllTypes()) {
        //  for (VirtualFile virtualFile : library.getFiles(orderRootType)) {
        //    Library.ModifiableModel modifiableModel = library.getModifiableModel();
        //    modifiableModel.addRoot(virtualFile, OrderRootType.SOURCES);
        //    modifiableModel.addRoot(virtualFile, OrderRootType.CLASSES);
        //    modifiableModel.addRoot(virtualFile, OrderRootType.DOCUMENTATION);
        //    modifiableModel.commit();
        //  }
        //}
        rootModel.commit();
      }
    });
  }
 */
  /*
  @Nullable
  private Library createJstdLibrary(@NotNull Project project, final @NotNull Module module) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<Library>() {

      @Override
      public Library compute() {
        LibraryTable applicationLibraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable();
        LibraryTable.ModifiableModel applicationLibraryTableModel = applicationLibraryTable.getModifiableModel();
        Library library =
          ((LibraryTableBase.ModifiableModelEx)applicationLibraryTableModel).createLibrary(JsAssertFrameworkLibraryManager.LIBRARY_NAME, JSLibraryType.getInstance());
        Library.ModifiableModel libraryModel = library.getModifiableModel();
        VirtualFile[] sourceFiles = JsAssertFrameworkLibraryManager.getAdditionalSourceFiles();
        for (VirtualFile sourceFile : sourceFiles) {
          libraryModel.addRoot(sourceFile, OrderRootType.SOURCES);
        }
        libraryModel.commit();
        applicationLibraryTableModel.commit();

        //ModuleRootManager manager = ModuleRootManager.getInstance(module);
        //ModifiableRootModel rootModel = manager.getModifiableModel();
        //LibraryOrderEntry libraryOrderEntry = rootModel.addLibraryEntry(library);
        //libraryOrderEntry.setScope(DependencyScope.COMPILE);
        //rootModel.commit();

        return library;
      }
    });
  }
*/

}
