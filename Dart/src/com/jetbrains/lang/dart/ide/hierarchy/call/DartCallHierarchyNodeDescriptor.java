// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.Navigatable;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartMethodDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartCallHierarchyNodeDescriptor extends HierarchyNodeDescriptor implements Navigatable {

  private final List<PsiReference> myReferences = new ArrayList<>();

  public DartCallHierarchyNodeDescriptor(final NodeDescriptor parentDescriptor, @NotNull final PsiElement element, final boolean isBase) {
    super(element.getProject(), parentDescriptor, element, isBase);
  }

  public void addReference(PsiReference reference) {
    myReferences.add(reference);
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
      PsiFile file = element.getContainingFile();
      if (file != null) {
        myHighlightedText.getEnding().addText(" (" + file.getName() + ")", HierarchyNodeDescriptor.getPackageNameAttributes());
      }
      var myUsageCount = myReferences.size();
      if (myUsageCount > 1) {
        myHighlightedText.getEnding().addText(IdeBundle.message("node.call.hierarchy.N.usages", myUsageCount), HierarchyNodeDescriptor.getUsageCountPrefixAttributes());
      }

    }
    myName = myHighlightedText.getText();
    if (!Comparing.equal(myHighlightedText, oldText)) {
      changes = true;
    }
    return changes;
  }

  private @Nullable Navigatable getNavigatable() {
    if (!myReferences.isEmpty()) {
      final var reference = myReferences.get(0);
      if (reference instanceof Navigatable) {
        return (Navigatable)reference;
      }
    }
    final var ret = getPsiElement();
    if (ret instanceof Navigatable) {
      return (Navigatable)ret;
    }
    return null;
  }

  @Override
  public void navigate(boolean requestFocus) {
    final var nav = getNavigatable();
    if (nav == null) {
      return;
    }
    nav.navigate(requestFocus);

    if (!(nav instanceof PsiElement)) {
      return;
    }

    Editor editor = PsiEditorUtil.findEditor((PsiElement) nav);

    if (editor != null) {
      HighlightManager highlightManager = HighlightManager.getInstance(myProject);
      List<RangeHighlighter> highlighters = new ArrayList<>();
      for (PsiReference psiReference : myReferences) {
        PsiElement eachElement = psiReference.getElement();
        PsiElement eachMethodCall = eachElement.getParent();
        if (eachMethodCall != null) {
          TextRange textRange = eachMethodCall.getTextRange();
          highlightManager.addRangeHighlight(editor, textRange.getStartOffset(), textRange.getEndOffset(),
                                             EditorColors.SEARCH_RESULT_ATTRIBUTES, false, highlighters);
        }
      }
    }
  }

  @Override
  public boolean canNavigate() {
    final var nav = getNavigatable();
    if (nav != null) {
      return nav.canNavigate();
    }
    return false;
  }

  @Override
  public boolean canNavigateToSource() {
    final var nav = getNavigatable();
    if (nav != null) {
      return nav.canNavigateToSource();
    }
    return false;
  }
}
