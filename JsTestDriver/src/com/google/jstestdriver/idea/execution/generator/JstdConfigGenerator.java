package com.google.jstestdriver.idea.execution.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.lang.javascript.index.JSIndexedRootProvider;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class JstdConfigGenerator {

  public static final JstdConfigGenerator INSTANCE = new JstdConfigGenerator();

  @NotNull
  public JstdGeneratedConfigStructure generateJstdConfigStructure(@NotNull JSFile jsFile) {
    VirtualFile jsVirtualFile = jsFile.getVirtualFile();
    if (jsVirtualFile == null) {
      throw new RuntimeException("JavaScript file should have virtual file.");
    }
    JstdGeneratedConfigStructure configStructure = new JstdGeneratedConfigStructure();
    DependencyContainer dependencyContainer = new DependencyContainer(jsFile.getProject());
    fillDependencyMap(jsFile, dependencyContainer);
    Set<VirtualFile> added = Sets.newHashSet();
    addAllDependenciesInOrder(added, configStructure, jsVirtualFile, dependencyContainer);
    return configStructure;
  }

  @NotNull
  private JstdGeneratedConfigStructure generateJstdConfigStructure(@NotNull Project project, @NotNull File jsIoFile) {
    VirtualFile jsVirtualFile = LocalFileSystem.getInstance().findFileByIoFile(jsIoFile);
    if (jsVirtualFile == null) {
      throw new RuntimeException("Could not find virtual file by io file " + jsIoFile.getAbsolutePath());
    }
    PsiFile psiFile = PsiManager.getInstance(project).findFile(jsVirtualFile);
    JSFile jsPsiFile = CastUtils.tryCast(psiFile, JSFile.class);
    if (jsPsiFile == null) {
      throw new RuntimeException("Could not process " + jsVirtualFile.getPath() + " as JavaScript file.");
    }
    return generateJstdConfigStructure(jsPsiFile);
  }

  private void addAllDependenciesInOrder(Set<VirtualFile> added, JstdGeneratedConfigStructure configStructure, VirtualFile jsVirtualFile, DependencyContainer dependencyContainer) {
    if (added.contains(jsVirtualFile)) {
      return;
    }
    added.add(jsVirtualFile);
    List<VirtualFile> dependentJsVirtualFiles = dependencyContainer.getDependentJsVirtualFiles(jsVirtualFile);
    if (dependentJsVirtualFiles != null) {
      for (VirtualFile dependentJsVirtualFile : dependentJsVirtualFiles) {
        addAllDependenciesInOrder(added, configStructure, dependentJsVirtualFile, dependencyContainer);
      }
    }
    configStructure.addLoadFile(new File(jsVirtualFile.getPath()));
  }

  private void fillDependencyMap(JSFile jsFile, DependencyContainer dependencyContainer) {
    final VirtualFile jsVirtualFile = jsFile.getVirtualFile();
    if (jsVirtualFile == null) {
      return;
    }
    if (dependencyContainer.hasDependenciesFor(jsVirtualFile)) {
      return;
    }
    if (dependencyContainer.handleSpecialCase(jsVirtualFile)) {
      return;
    }
    if (dependencyContainer.getLibraryFileNames().contains(jsVirtualFile.getName())) {
      return;
    }
    List<JSFile> dependentJsFiles = collectDependentJsFiles(jsFile, dependencyContainer);
    List<VirtualFile> dependentVirtualFiles = Lists.newArrayList();
    for (JSFile dependentJsFile : dependentJsFiles) {
      dependentVirtualFiles.add(dependentJsFile.getVirtualFile());
    }
    dependencyContainer.registerDependency(jsVirtualFile, dependentVirtualFiles);
    for (JSFile dependentJsFile : dependentJsFiles) {
      fillDependencyMap(dependentJsFile, dependencyContainer);
    }
  }

  @NotNull
  private List<JSFile> collectDependentJsFiles(@NotNull JSFile jsFile, DependencyContainer dependencyContainer) {
    Set<JSFile> dependentJsFiles = Sets.newLinkedHashSet();
    collectDependentJsFilesByJsElement(jsFile, dependentJsFiles, dependencyContainer);
    dependentJsFiles.remove(jsFile);
    return Lists.newArrayList(dependentJsFiles);
  }

  private void collectDependentJsFilesByJsElement(PsiElement psiElement, Set<JSFile> dependentFiles, DependencyContainer dependencyContainer) {
    if (psiElement instanceof JSElement) {
      boolean resolve = !(psiElement instanceof JSLiteralExpression);
      PsiReference[] psiReferences = resolve ? psiElement.getReferences() : PsiReference.EMPTY_ARRAY;
      for (PsiReference psiReference : psiReferences) {
        if (psiReference instanceof PsiPolyVariantReference) {
          PsiPolyVariantReference psiPolyVariantReference = (PsiPolyVariantReference) psiReference;
          ResolveResult[] resolveResults = psiPolyVariantReference.multiResolve(false);
          for (ResolveResult resolveResult : resolveResults) {
            PsiElement resolvedElement = resolveResult.getElement();
            if (resolvedElement != null && resolveResult.isValidResult()) {
              PsiFile resolvedPsiFile = resolvedElement.getContainingFile();
              if (dependencyContainer.validatePsiFile(resolvedPsiFile)) {
                dependentFiles.add((JSFile) resolvedPsiFile);
                break;
              }
            }
          }
        }
      }
    }
    for (PsiElement child : psiElement.getChildren()) {
      collectDependentJsFilesByJsElement(child, dependentFiles, dependencyContainer);
    }
  }

  private static Trinity<Integer, Integer, String> resolveInfo(PsiElement psiElement) {
    Document document = PsiDocumentManager.getInstance(psiElement.getProject()).getDocument(psiElement.getContainingFile());
    int textOffset = psiElement.getTextOffset();
    if (textOffset < 0) {
      System.err.println("text offset is negative: " + textOffset);
      textOffset = 0;
    }
    assert document != null;
    int lineNo = document.getLineNumber(textOffset);
    int columnNo = psiElement.getTextOffset() - document.getLineStartOffset(lineNo);
    String text = psiElement.getText();
    return Trinity.create(lineNo, columnNo, text);
  }

  public File generateTempConfig(Project project, File jsIoFile) throws IOException {
    JstdGeneratedConfigStructure configStructure = generateJstdConfigStructure(project, jsIoFile);
    String lastName = jsIoFile.getName().replace('.', '-');
    File tempConfigFile = FileUtil.createTempFile("generated-" + lastName, ".jstd");
    PrintWriter writer = new PrintWriter(tempConfigFile);
    try {
      writer.print(configStructure.asFileContent());
    } finally {
      writer.close();
    }
    return tempConfigFile;
  }

  private static class DependencyContainer {
    private Project myProject;
    private Map<VirtualFile, List<VirtualFile>> myDependencyMap;
    private Set<VirtualFile> myLibraryVirtualFiles;
    private boolean myQUnitAdapterHandled = false;
    private Set<String> myLibraryFileNames;

    private DependencyContainer(Project project) {
      myProject = project;
      myDependencyMap = Maps.newHashMap();
      myLibraryVirtualFiles = new JSIndexedRootProvider().getLibraryFiles(project);
      myLibraryFileNames = Sets.newHashSet();
      for (VirtualFile libraryVirtualFile : myLibraryVirtualFiles) {
        myLibraryFileNames.add(libraryVirtualFile.getName());
      }
    }

    public boolean hasDependenciesFor(VirtualFile jsVirtualFile) {
      return myDependencyMap.containsKey(jsVirtualFile);
    }

    public boolean handleSpecialCase(VirtualFile jsVirtualFile) {
      if ("QUnitAdapter.js".equals(jsVirtualFile.getName())) {
        if (myQUnitAdapterHandled) {
          return true;
        }
        myQUnitAdapterHandled = true;
        final List<VirtualFile> virtualFiles = Lists.newArrayList();
        FileBasedIndex.getInstance().processValues(FilenameIndex.NAME, "equiv.js", null, new FileBasedIndex.ValueProcessor<Void>() {
              @Override
              public boolean process(VirtualFile file, Void value) {
                PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
                if (validatePsiFile(psiFile)) {
                  virtualFiles.add(file);
                }
                return true;
              }
            }, new ProjectAndLibrariesScope(myProject)
        );
        final String jsVirtualFileDirPath = jsVirtualFile.getParent().getPath();
        Collections.sort(virtualFiles, new Comparator<VirtualFile>() {
          @Override
          public int compare(VirtualFile vf1, VirtualFile vf2) {
            boolean sameDir1 = vf1.getParent().getPath().equals(jsVirtualFileDirPath);
            boolean sameDir2 = vf2.getParent().getPath().equals(jsVirtualFileDirPath);
            if (sameDir1) {
              return -1;
            }
            if (sameDir2) {
              return 1;
            }
            return 0;
          }
        });
        if (!virtualFiles.isEmpty()) {
          myDependencyMap.put(jsVirtualFile, Collections.singletonList(virtualFiles.get(0)));
        }
        return true;
      }
      return false;
    }

    public Set<String> getLibraryFileNames() {
      return myLibraryFileNames;
    }

    public void registerDependency(VirtualFile jsVirtualFile, List<VirtualFile> dependentVirtualFiles) {
      myDependencyMap.put(jsVirtualFile, dependentVirtualFiles);
    }

    public boolean validatePsiFile(@Nullable PsiFile psiFile) {
      if (psiFile == null) {
        return false;
      }
      VirtualFile vf = psiFile.getVirtualFile();
      if (vf != null && !myLibraryVirtualFiles.contains(vf) && (psiFile instanceof JSFile)) {
        JSFile jsFile = (JSFile) psiFile;
        return !jsFile.isPredefined();
      }
      return false;
    }

    public List<VirtualFile> getDependentJsVirtualFiles(VirtualFile jsVirtualFile) {
      List<VirtualFile> dependentVirtualFiles = myDependencyMap.get(jsVirtualFile);
      return dependentVirtualFiles != null ? dependentVirtualFiles : Collections.<VirtualFile>emptyList();
    }
  }

}
