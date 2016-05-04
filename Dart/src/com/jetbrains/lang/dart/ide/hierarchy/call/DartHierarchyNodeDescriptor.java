package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import org.jetbrains.annotations.NotNull;

public class DartHierarchyNodeDescriptor extends HierarchyNodeDescriptor {

  public DartHierarchyNodeDescriptor(final NodeDescriptor parentDescriptor, @NotNull final PsiElement element, final boolean isBase) {
    super(element.getProject(), parentDescriptor, element, isBase);
  }

  @Override
  public boolean update() {
    boolean changes = super.update();
    final CompositeAppearance oldText = myHighlightedText;
    myHighlightedText = new CompositeAppearance();
    NavigatablePsiElement element = (NavigatablePsiElement)getPsiElement();
    if (element == null) {
      return invalidElement();
    }

    final ItemPresentation presentation = element.getPresentation();
    if (presentation != null) {
      if (element instanceof DartMethodDeclaration) {
        if (DartComponentType.typeOf(element) != DartComponentType.CONSTRUCTOR) {
          // Do not print constructors as Class.Class.Class(args) Class
          final DartClass cls = PsiTreeUtil.getParentOfType(element, DartClass.class);
          if (cls != null) {
            myHighlightedText.getEnding().addText(cls.getName() + ".");
          }
        }
      }
      myHighlightedText.getEnding().addText(presentation.getPresentableText());
      myHighlightedText.getEnding().addText(" " + presentation.getLocationString(), HierarchyNodeDescriptor.getPackageNameAttributes());
    }
    myName = myHighlightedText.getText();
    if (!Comparing.equal(myHighlightedText, oldText)) {
      changes = true;
    }
    return changes;
  }

  private boolean invalidElement() {
    final String invalidPrefix = IdeBundle.message("node.hierarchy.invalid");
    if (!myHighlightedText.getText().startsWith(invalidPrefix)) {
      myHighlightedText.getBeginning().addText(invalidPrefix, HierarchyNodeDescriptor.getInvalidPrefixAttributes());
    }
    return true;
  }
}
