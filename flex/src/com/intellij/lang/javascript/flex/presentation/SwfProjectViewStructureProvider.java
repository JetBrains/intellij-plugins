package com.intellij.lang.javascript.flex.presentation;

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
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SwfProjectViewStructureProvider implements SelectableTreeStructureProvider {

  private static final Comparator<JSQualifiedNamedElement> QNAME_COMPARATOR = new Comparator<JSQualifiedNamedElement>() {
    @Override
    public int compare(JSQualifiedNamedElement o1, JSQualifiedNamedElement o2) {
      String[] tokens1 = o1.getQualifiedName().split("\\.");
      String[] tokens2 = o2.getQualifiedName().split("\\.");

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
    }
  };

  @Override
  public PsiElement getTopLevelElement(PsiElement element) {
    JSQualifiedNamedElement parent = PsiTreeUtil.getNonStrictParentOfType(element, JSClass.class);
    if (parent == null) {
      parent = PsiTreeUtil.getParentOfType(element, JSFunction.class, JSVariable.class, JSNamespaceDeclaration.class);
    }
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

  @Nullable
  private static PsiElement findDecompiledElement(JSQualifiedNamedElement element) {
    final String qName = element.getQualifiedName();
    if (qName == null) {
      return null;
    }
    VirtualFile elementVFile = element.getContainingFile().getVirtualFile();
    if (elementVFile == null) {
      return null;
    }

    ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(element.getProject()).getFileIndex();

    GlobalSearchScope searchScope = JSResolveUtil.getSearchScope(element);
    Collection<JSQualifiedNamedElement> candidates =
      StubIndex.getInstance().get(JSQualifiedElementIndex.KEY, qName.hashCode(), element.getProject(), searchScope);
    List<OrderEntry> sourceFileEntries = projectFileIndex.getOrderEntriesForFile(elementVFile);

    for (JSQualifiedNamedElement candidate : candidates) {
      if (candidate == element || !qName.equals(candidate.getQualifiedName())) {
        continue;
      }

      VirtualFile vFile = candidate.getContainingFile().getVirtualFile();
      if (vFile != null && projectFileIndex.getClassRootForFile(vFile) != null) {
        List<OrderEntry> candidateEntries = projectFileIndex.getOrderEntriesForFile(vFile);
        if (ContainerUtil.intersects(sourceFileEntries, candidateEntries)) {
          return candidate;
        }
      }
    }
    return null;
  }

  @Override
  public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings) {
    if (!(parent instanceof PsiFileNode)) {
      return children;
    }
    PsiFile psiFile = ((PsiFileNode)parent).getValue();
    if (FileTypeManager.getInstance().getFileTypeByFileName(psiFile.getName()) != FlexApplicationComponent.SWF_FILE_TYPE) {
      return children;
    }

    VirtualFile vFile = psiFile.getVirtualFile();
    if (vFile == null) {
      return children;
    }

    PsiFile file = PsiManager.getInstance(parent.getProject()).findFile(vFile);
    if (file == null) {
      return children;
    }

    List<JSQualifiedNamedElement> elements = new ArrayList<JSQualifiedNamedElement>();
    for (JSSourceElement e : ((JSFile)file).getStatements()) {
      if (e instanceof JSQualifiedNamedElement) {
        elements.add((JSQualifiedNamedElement)e);
      }
      else if (e instanceof JSVarStatement) {
        Collections.addAll(elements, ((JSVarStatement)e).getVariables());
      }
    }

    Collections.sort(elements, QNAME_COMPARATOR);
    return getChildrenForPackage("", elements, 0, elements.size(), psiFile.getProject(), ((PsiFileNode)parent).getSettings());
  }

  static Collection<AbstractTreeNode> getChildrenForPackage(String aPackage,
                                                            List<JSQualifiedNamedElement> elements,
                                                            int from,
                                                            int to,
                                                            Project project,
                                                            ViewSettings settings) {
    List<AbstractTreeNode> packages = new ArrayList<AbstractTreeNode>();
    List<AbstractTreeNode> classes = new ArrayList<AbstractTreeNode>();

    int subpackageStart = -1;
    String currentSubpackage = null;
    for (int i = from; i < to; i++) {
      JSQualifiedNamedElement element = elements.get(i);
      String qName = element.getQualifiedName();
      assert qName.startsWith(aPackage) : qName + " does not start with " + aPackage;
      if (StringUtil.getPackageName(qName).equals(aPackage)) {
        classes.add(new SwfQualifiedNamedElementNode(project, element));
      }
      else {
        String subpackage =
          settings.isFlattenPackages() ? StringUtil.getPackageName(qName) : qName.substring(0, qName.indexOf('.', aPackage.length() + 1));
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
                                                            int from, int to, String qName) {
    // SWF-s don't contain empty packages, so it makes no sense to handle "flatten packages and hide empty middle packages" mode
    if (settings.isFlattenPackages()) {
      return new SwfPackageElementNode(project, qName, qName, settings, elements, from, to);
    }
    else {
      if (settings.isHideEmptyMiddlePackages()) {
        String subQname = getEmptyMiddlePackageQname(elements, from, to, qName);
        if (subQname != null) {
          return new SwfPackageElementNode(project, subQname, subQname.substring(StringUtil.getPackageName(qName).length() + 1), settings,
                                           elements, from, to);
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

  @Override
  public Object getData(Collection<AbstractTreeNode> selected, String dataName) {
    return null;
  }
}
