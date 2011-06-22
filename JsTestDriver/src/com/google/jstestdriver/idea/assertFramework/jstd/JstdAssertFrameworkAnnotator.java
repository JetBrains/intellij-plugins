package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Sets;
import com.google.jstestdriver.idea.javascript.predefined.Marker;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.scripting.ScriptingLibraryModel;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.util.FileContentUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class JstdAssertFrameworkAnnotator implements Annotator {

  private static final String LIBRARY_NAME = "JsTestDriver Default Assertion Framework";

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    JSCallExpression callExpression = CastUtils.tryCast(element, JSCallExpression.class);
    if (callExpression != null) {
      JSReferenceExpression referenceExpression = CastUtils.tryCast(callExpression.getMethodExpression(), JSReferenceExpression.class);
      if (referenceExpression != null) {
        JSArgumentList jsArgumentList = callExpression.getArgumentList();
        if (jsArgumentList != null) {
          JSExpression[] arguments = jsArgumentList.getArguments();
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
  }

  private boolean canBeResolved(PsiPolyVariantReference psiPolyVariantReference) {
    ResolveResult[] resolveResults = psiPolyVariantReference.multiResolve(false);
    for (ResolveResult resolveResult : resolveResults) {
      PsiElement resolvedElement = resolveResult.getElement();
      if (resolvedElement != null && resolveResult.isValidResult()) {
        return true;
      }
    }
    return false;
  }

  private void installAnnotation(AnnotationHolder holder, final JSReferenceExpression referenceExpression) {
    Project project = referenceExpression.getProject();
    PsiFile containingFile = referenceExpression.getContainingFile();
    if (containingFile == null) return;

    VirtualFile classVFile = containingFile.getVirtualFile();
    if (classVFile == null) return;

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    final Module module = fileIndex.getModuleForFile(classVFile);
    if (module == null) return;

    String referencedName = referenceExpression.getReferencedName();
    Annotation annotation = holder.createErrorAnnotation(referenceExpression, referencedName + " is unresolved.");
    System.out.println("installing annotation '" + annotation.getMessage() + "'");
    annotation.registerFix(new IntentionAction() {
      @NotNull
      @Override
      public String getText() {
        return "Add '" + LIBRARY_NAME + "' JavaScript library";
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
        final Library library = createJstdLibrary(project);

        addLibraryToModule(module, library);

        PsiManagerEx.getInstance(project).dropFileCaches(file);
        PsiManagerEx.getInstance(project).reloadFromDisk(file);
        PsiManagerEx.getInstance(project).dropResolveCaches();

        final VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile != null) {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              for (OrderRootType orderRootType : OrderRootType.getAllTypes()) {
                for (VirtualFile virtualFile : library.getFiles(orderRootType)) {
                  FileBasedIndex.getInstance().requestReindex(virtualFile);
                  System.out.println("reindexing " + virtualFile.getPath());
                }
              }
              FileBasedIndex.getInstance().requestReindex(virtualFile);
              System.out.println("reindexing " + virtualFile.getPath());
            }
          });
          virtualFile.refresh(true, false, new Runnable() {
            @Override
            public void run() {
              FileContentUtil.reparseFiles(referenceExpression.getProject(), Collections.singletonList(virtualFile), true);
            }
          });
          System.out.println("FileContentUtil.reparseFiles done for " + virtualFile.getPath());
        }
      }

      @Override
      public boolean startInWriteAction() {
        return false;
      }
    });
  }

  private void addLibraryToModule(final Module module, final Library library) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        ModifiableRootModel rootModel = manager.getModifiableModel();
        rootModel.addLibraryEntry(library);
        ProjectRootManagerEx.getInstanceEx(module.getProject()).multiCommit(new ModifiableRootModel[] {rootModel});
//        rootModel.commit();
      }
    });
  }

  private Library createJstdLibrary(@NotNull Project project) {
    final JSLibraryManager libraryManager = ServiceManager.getService(project, JSLibraryManager.class);
    String[] resourceNames = new String[] {"Asserts.js", "TestCase.js"};
    VirtualFile[] sourceFiles = new VirtualFile[resourceNames.length];
    for (int i = 0; i < resourceNames.length; i++) {
      sourceFiles[i] = getVirtualFiles(Marker.class, resourceNames[i]);
    }
    ScriptingLibraryModel scriptingLibraryModel = libraryManager.getLibraryByName(LIBRARY_NAME);
    if (scriptingLibraryModel != null) {
      Set<VirtualFile> sourceFileSet1 = Sets.newHashSet(Arrays.asList(sourceFiles));
      Set<VirtualFile> sourceFileSet2 = scriptingLibraryModel.getSourceFiles();
      boolean sourceFileSetsEquals = sourceFileSet1.equals(sourceFileSet2);
      if (!sourceFileSetsEquals) {
        libraryManager.removeLibrary(scriptingLibraryModel);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            libraryManager.commitChanges();
          }
        });
        scriptingLibraryModel = createLibrary(libraryManager, LIBRARY_NAME, sourceFiles);
      }
    } else {
      scriptingLibraryModel = createLibrary(libraryManager, LIBRARY_NAME, sourceFiles);
    }

    if (scriptingLibraryModel == null) {
      throw new RuntimeException("Unable to create js library '" + LIBRARY_NAME + "'!");
    }
    JSLibraryMappings mappings = ServiceManager.getService(project, JSLibraryMappings.class);
    ScriptingLibraryModel mappedLibrary = mappings.getMapping(project.getBaseDir());
    if (mappedLibrary == null || !LIBRARY_NAME.equals(mappedLibrary.getName())) {
      mappings.associate(project.getBaseDir(), LIBRARY_NAME);
      System.out.println("associated to " + project.getBaseDir());
    }
    return libraryManager.getOriginalLibrary(scriptingLibraryModel);
  }

  @NotNull
  private VirtualFile getVirtualFiles(Class<?> clazz, String resourceName) {
    VirtualFile file = VfsUtil.findFileByURL(clazz.getResource(resourceName));
    if (file == null) {
      throw new RuntimeException("Can't find virtual file for '" + resourceName + "', class " + clazz);
    }
    return file;
  }

  @Nullable
  ScriptingLibraryModel createLibrary(final JSLibraryManager libraryManager, final String libraryName, final VirtualFile[] sourceFiles) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<ScriptingLibraryModel>() {
      @Override
      public ScriptingLibraryModel compute() {
        ScriptingLibraryModel scriptingLibraryModel = libraryManager.createLibrary(libraryName, sourceFiles, VirtualFile.EMPTY_ARRAY, new String[]{});
        libraryManager.commitChanges();
        return scriptingLibraryModel;
      }
    });
  }

}
