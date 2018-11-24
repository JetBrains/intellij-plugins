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
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.RowIcon;
import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartMethodHierarchyNodeDescriptor extends HierarchyNodeDescriptor {
  private static final String INVALID_PREFIX = IdeBundle.message("node.hierarchy.invalid");
  private DartMethodHierarchyTreeStructure myTreeStructure;
  private Icon myRawIcon;
  private Icon myStateIcon;
  // If myIsImplementor => show '+' icon unless method is abstract, never filtered from view
  protected boolean myIsImplementor = false;
  // If myShouldImplement && !myIsImplementor => show '!' icon
  protected boolean myShouldImplement = false;
  // If myIsSuperclassOfImplementor && !myIsImplementor && !myShouldImplement => show '-' icon
  // If !myIsSuperclassOfImplementor && !myIsImplementor => may be filtered from view
  protected boolean myIsSuperclassOfImplementor = false;

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

  public final DartClass getType() {
    return (DartClass)getPsiElement();
  }

  public final boolean update() {
    boolean changes = super.update();
    final CompositeAppearance oldText = myHighlightedText;
    myHighlightedText = new CompositeAppearance();
    DartClass dartClass = getType();
    if (dartClass == null) {
      if (!myHighlightedText.getText().startsWith(INVALID_PREFIX)) {
        myHighlightedText.getBeginning().addText(INVALID_PREFIX, HierarchyNodeDescriptor.getInvalidPrefixAttributes());
      }
      return true;
    }

    final ItemPresentation presentation = dartClass.getPresentation();
    Icon baseIcon = null;
    Icon stateIcon = null;
    if (presentation != null) {
      myHighlightedText.getEnding().addText(presentation.getPresentableText());
      PsiFile file = dartClass.getContainingFile();
      if (file != null) {
        myHighlightedText.getEnding().addText(" (" + file.getName() + ")", HierarchyNodeDescriptor.getPackageNameAttributes());
      }
      baseIcon = presentation.getIcon(false);
      stateIcon = calculateStateIcon(dartClass);
    }

    if (changes || baseIcon != myRawIcon || stateIcon != myStateIcon) {
      changes = true;

      Icon newIcon = myRawIcon = baseIcon;
      myStateIcon = stateIcon;
      if (myIsBase) {
        final LayeredIcon icon = new LayeredIcon(2);
        icon.setIcon(newIcon, 0);
        newIcon = icon;
        icon.setIcon(AllIcons.Hierarchy.Base, 1, -AllIcons.Hierarchy.Base.getIconWidth() / 2, 0);
      }
      if (myStateIcon != null) {
        newIcon = new RowIcon(myStateIcon, newIcon);
      }

      setIcon(newIcon);
    }
    myName = myHighlightedText.getText();
    if (!Comparing.equal(myHighlightedText, oldText)) {
      changes = true;
    }
    return changes;
  }

  private Icon calculateStateIcon(final DartClass dartClass) {
    if (myIsImplementor) {
      return AllIcons.Hierarchy.MethodDefined;
    }
    if (myShouldImplement) {
      return AllIcons.Hierarchy.ShouldDefineMethod;
    }
    if (!myIsBase) {
      return AllIcons.Hierarchy.MethodNotDefined;
    }
    return null;
  }
}