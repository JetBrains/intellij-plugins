package com.jetbrains.lang.dart.ide.hierarchy.method;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartMethodHierarchyNodeDescriptor extends HierarchyNodeDescriptor {
  private static final String INVALID_PREFIX = IdeBundle.message("node.hierarchy.invalid");
  private DartMethodHierarchyTreeStructure myTreeStructure;

  protected DartMethodHierarchyNodeDescriptor(@NotNull Project project,
                                              NodeDescriptor parentDescriptor,
                                              PsiElement type,
                                              boolean isBase,
                                              @NotNull DartMethodHierarchyTreeStructure treeStructure) {
    super(project, parentDescriptor, type, isBase);
    assert type instanceof DartClass;
    myTreeStructure = treeStructure;
  }

  public final void setTreeStructure(final DartMethodHierarchyTreeStructure treeStructure) {
    myTreeStructure = treeStructure;
  }

  DartMethodDeclaration getMethod(final DartClass aClass, final boolean checkBases) {
    return DartHierarchyUtil.findBaseMethodInClass(myTreeStructure.getBaseMethod(), aClass, checkBases);
  }

  public final DartClass getType() {
    return (DartClass)getPsiElement();
  }

  //TODO DELETE
  public PsiElement getPsiClass() {
    return getType();
  }

  public final boolean update() {
    boolean changes = super.update();
    final CompositeAppearance oldText = myHighlightedText;
    myHighlightedText = new CompositeAppearance();
    DartClass dartClass = getType();
    if (dartClass == null){
      if (!myHighlightedText.getText().startsWith(INVALID_PREFIX)) {
        myHighlightedText.getBeginning().addText(INVALID_PREFIX, HierarchyNodeDescriptor.getInvalidPrefixAttributes());
      }
      return true;
    }

    // TODO Add icons for + - !
    final ItemPresentation presentation = dartClass.getPresentation();
    Icon baseIcon = null;
    if (presentation != null) {
      myHighlightedText.getEnding().addText(presentation.getPresentableText());
      PsiFile file = dartClass.getContainingFile();
      if (file != null) {
        myHighlightedText.getEnding().addText(" (" + file.getName() + ")", HierarchyNodeDescriptor.getPackageNameAttributes());
      }
      baseIcon = presentation.getIcon(false);
    }
    myName = myHighlightedText.getText();
    if (!Comparing.equal(myHighlightedText, oldText)) {
      changes = true;
    }
    return changes;
  }

  private Icon calculateStateIcon(final DartClass dartClass) {
    DartMethodDeclaration method = getMethod(dartClass, false);
    if (method != null) {
      if (method.isAbstract()) {
        return null;
      }
      return AllIcons.Hierarchy.MethodDefined;
    }

    if (myTreeStructure.isSuperClassForBaseClass(dartClass)) {
      return AllIcons.Hierarchy.MethodNotDefined;
    }

    final boolean isAbstractClass = dartClass.isAbstract();

    // was it implemented is in superclasses?
    final DartMethodDeclaration baseClassMethod = getMethod(dartClass, true);

    final boolean hasBaseImplementation = baseClassMethod != null && !baseClassMethod.isAbstract();

    if (hasBaseImplementation || isAbstractClass) {
      return AllIcons.Hierarchy.MethodNotDefined;
    }
    else {
      return AllIcons.Hierarchy.ShouldDefineMethod;
    }
  }
}