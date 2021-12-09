// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.SelectableTreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSNamespaceDeclaration;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSQualifiedElementIndex;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiCompiledFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SwfProjectViewStructureProvider implements SelectableTreeStructureProvider, DumbAware {

  private static final Logger LOG = Logger.getInstance(SwfProjectViewStructureProvider.class.getName());
  private static final int MAX_TOTAL_SWFS_SIZE_IN_FOLDER_TO_SHOW_STRUCTURE = 5 * 1024 * 1024; // 5Mb

  private static final Comparator<JSQualifiedNamedElement> QNAME_COMPARATOR = (o1, o2) -> {
    final String qName = o1.getQualifiedName();
    final String qName2 = o2.getQualifiedName();
    if (qName == null || qName2 == null) return qName == null && qName2 == null ? 0 : qName != null ? 1 : -1;
    String[] tokens1 = qName.split("\\.");
    String[] tokens2 = qName2.split("\\.");

    for (int i = 0; i < tokens1.length && i < tokens2.length; i++) {
      int result = tokens1[i].compareTo(tokens2[i]);
      if (result != 0) {
        // class from package goes before subpackages
        if (i == tokens1.length - 1 && i != tokens2.length - 1) return -1;
        if (i != tokens1.length - 1 && i == tokens2.length - 1) return 1;
        return result;
      }
    }
    return 0;
  };

  @Override
  public PsiElement getTopLevelElement(PsiElement element) {
    JSQualifiedNamedElement parent = PsiTreeUtil.getNonStrictParentOfType(element, JSClass.class, JSFunction.class, JSVariable.class,
                                                                          JSNamespaceDeclaration.class);
    if (parent != null) {
      PsiFile file = parent.getContainingFile();
      if (file != null && (ActionScriptFileType.INSTANCE == file.getFileType() || FlexApplicationComponent.MXML == file.getFileType())) {
        VirtualFile vFile = file.getVirtualFile();
        if (vFile != null && ProjectRootManager.getInstance(element.getProject()).getFileIndex().isInLibrarySource(vFile)) {
          PsiElement fromLibrary = findDecompiledElement(parent);
          if (fromLibrary != null) {
            return fromLibrary;
          }
        }
      }
      return parent;
    }
    return null;
  }

  /**
   * this is is needed to allow selecting classes and members in project view
   * @deprecated remove this method with proper check when Tree API is improved (e.g. ProjectViewNode#contains(object))
   */
  @Deprecated
  static boolean nodeContainsFile(ProjectViewNode node, VirtualFile file) {
    AbstractTreeNode parent = node.getParent();
    while (parent instanceof SwfPackageElementNode) {
      parent = parent.getParent();
    }
    return ((PsiFileNode)parent).contains(file);
  }

  @Nullable
  private static PsiElement findDecompiledElement(JSQualifiedNamedElement element) {
    if (DumbService.isDumb(element.getProject())) {
      return null;
    }

    JSQualifiedNamedElement mainElement = JSUtils.getMemberContainingClass(element);
    if (mainElement == null) {
      mainElement = element;
    }
    final String qName = mainElement.getQualifiedName();
    if (qName == null) {
      return null;
    }
    VirtualFile elementVFile = mainElement.getContainingFile().getVirtualFile();
    if (elementVFile == null) {
      return null;
    }

    ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(mainElement.getProject()).getFileIndex();

    GlobalSearchScope searchScope = JSResolveUtil.getResolveScope(mainElement);
    Collection<JSQualifiedNamedElement> candidates =
      StubIndex.getElements(JSQualifiedElementIndex.KEY, qName.hashCode(), mainElement.getProject(), searchScope,
                            JSQualifiedNamedElement.class);
    List<OrderEntry> sourceFileEntries = projectFileIndex.getOrderEntriesForFile(elementVFile);

    for (JSQualifiedNamedElement candidate : candidates) {
      if (candidate == mainElement || !qName.equals(candidate.getQualifiedName())) {
        continue;
      }

      VirtualFile vFile = candidate.getContainingFile().getVirtualFile();
      if (vFile != null && projectFileIndex.getClassRootForFile(vFile) != null) {
        List<OrderEntry> candidateEntries = projectFileIndex.getOrderEntriesForFile(vFile);
        if (ContainerUtil.intersects(sourceFileEntries, candidateEntries)) {
          if (element == mainElement) {
            return candidate;
          }
          else {
            LOG.assertTrue(candidate instanceof JSClass, candidate);
            if (element instanceof JSVariable) {
              return ((JSClass)candidate).findFieldByName(element.getName());
            }
            else {
              LOG.assertTrue(element instanceof JSFunction, element);
              return ((JSClass)candidate).findFunctionByNameAndKind(element.getName(), ((JSFunction)element).getKind());
            }
          }
        }
      }
    }
    return null;
  }

  @NotNull
  @Override
  public Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
    if (!(parent instanceof PsiFileNode)) {
      return children;
    }

    final PsiFile psiFile = ((PsiFileNode)parent).getValue();
    if (!(psiFile instanceof PsiCompiledFile) || !(psiFile instanceof JSFile)) {
      return children;
    }

    final VirtualFile vFile = psiFile.getVirtualFile();
    if (vFile == null || !FileTypeRegistry.getInstance().isFileOfType(vFile, FlexApplicationComponent.SWF_FILE_TYPE)) {
      return children;
    }

    if (isTooManySWFs(vFile.getParent())) {
      return children;
    }

    List<JSQualifiedNamedElement> elements = new ArrayList<>();
    for (JSSourceElement e : ((JSFile)psiFile).getStatements()) {
      if (e instanceof JSQualifiedNamedElement) {
        String qName = ((JSQualifiedNamedElement)e).getQualifiedName();
        if (qName == null) {
          final Attachment attachment = e.getParent() != null
                                        ? new Attachment("Parent element.txt", e.getParent().getText())
                                        : new Attachment("Element text.txt", e.getText());
          LOG.error("Null qname: '" + e.getClass().getName() + "'", new Throwable(), attachment);
          continue;
        }
        elements.add((JSQualifiedNamedElement)e);
      }
      else if (e instanceof JSVarStatement) {
        Collections.addAll(elements, ((JSVarStatement)e).getVariables());
      }
    }

    elements.sort(QNAME_COMPARATOR);
    return getChildrenForPackage("", elements, 0, elements.size(), psiFile.getProject(), ((PsiFileNode)parent).getSettings());
  }

  private static boolean isTooManySWFs(final VirtualFile folder) {
    int size = 0;
    for (VirtualFile file : folder.getChildren()) {
      if (FileTypeRegistry.getInstance().isFileOfType(file, FlexApplicationComponent.SWF_FILE_TYPE)) {
        size += file.getLength();
        if (size > MAX_TOTAL_SWFS_SIZE_IN_FOLDER_TO_SHOW_STRUCTURE) {
          return true;
        }
      }
    }
    return false;
  }

  static Collection<AbstractTreeNode<?>> getChildrenForPackage(String aPackage,
                                                            List<JSQualifiedNamedElement> elements,
                                                            int from,
                                                            int to,
                                                            Project project,
                                                            ViewSettings settings) {
    List<AbstractTreeNode<?>> packages = new ArrayList<>();
    List<AbstractTreeNode<?>> classes = new ArrayList<>();

    int subpackageStart = -1;
    String currentSubpackage = null;
    for (int i = from; i < to; i++) {
      JSQualifiedNamedElement element = elements.get(i);
      String qName = element.getQualifiedName();
      assert aPackage.isEmpty() || qName.startsWith(aPackage + ".") : qName + " does not start with " + aPackage;
      if (StringUtil.getPackageName(qName).equals(aPackage)) {
        classes.add(new SwfQualifiedNamedElementNode(project, element, settings));
      }
      else {
        final int endIndex = qName.indexOf('.', aPackage.length() + 1);
        if (endIndex <= 0) {
          final Attachment attachment = element.getParent() != null
                                        ? new Attachment("Parent element.txt", element.getParent().getText())
                                        : new Attachment("Element text.txt", element.getText());
          LOG.error("package=[" + aPackage + "], qName=[" + qName + "]", new Throwable(), attachment);
          continue;
        }

        String subpackage = settings.isFlattenPackages() ? StringUtil.getPackageName(qName) : qName.substring(0, endIndex);
        if (currentSubpackage == null) {
          subpackageStart = i;
        }
        else if (!currentSubpackage.equals(subpackage)) {
          packages.add(createSubpackageNode(elements, project, settings, subpackageStart, i, currentSubpackage));
          subpackageStart = i;
        }
        currentSubpackage = subpackage;
      }
    }
    if (currentSubpackage != null) {
      packages.add(createSubpackageNode(elements, project, settings, subpackageStart, to, currentSubpackage));
    }

    return ContainerUtil.concat(packages, classes);
  }

  private static SwfPackageElementNode createSubpackageNode(List<JSQualifiedNamedElement> elements,
                                                            Project project,
                                                            ViewSettings settings,
                                                            int from, int to, @NotNull String qName) {
    // SWF-s don't contain empty packages, so it makes no sense to handle "flatten packages and hide empty middle packages" mode
    if (settings.isFlattenPackages()) {
      return new SwfPackageElementNode(project, qName, qName, settings, elements, from, to);
    }
    else {
      if (settings.isHideEmptyMiddlePackages()) {
        String subQname = getEmptyMiddlePackageQname(elements, from, to, qName);
        if (subQname != null) {
          String displayText = qName.contains(".") ? subQname.substring(StringUtil.getPackageName(qName).length() + 1) : subQname;
          return new SwfPackageElementNode(project, subQname, displayText, settings, elements, from, to);
        }
      }
      return new SwfPackageElementNode(project, qName, StringUtil.getShortName(qName), settings, elements, from, to);
    }
  }

  @Nullable
  private static String getEmptyMiddlePackageQname(List<JSQualifiedNamedElement> elements, int from, int to, String packageName) {
    if (from == to) {
      return null;
    }

    String currentSubpackage = null;
    for (int i = from; i < to; i++) {
      String qName = elements.get(i).getQualifiedName();
      int index = qName.indexOf('.', packageName.length() + 1);
      if (index == -1) {
        // class is in the package
        return null;
      }
      String subpackage = qName.substring(0, index);
      if (currentSubpackage == null) {
        currentSubpackage = subpackage;
      }
      else if (!currentSubpackage.equals(subpackage)) {
        return null;
      }
    }

    String deeperSubpackage = getEmptyMiddlePackageQname(elements, from, to, currentSubpackage);
    return deeperSubpackage != null ? deeperSubpackage : currentSubpackage;
  }
}
