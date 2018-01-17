package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public final class DartTypeHierarchyNodeDescriptor extends HierarchyNodeDescriptor {
  public DartTypeHierarchyNodeDescriptor(@NotNull final Project project,
                                         @Nullable final HierarchyNodeDescriptor parentDescriptor,
                                         @NotNull final DartClass dartClass,
                                         final boolean isBase) {
    super(project, parentDescriptor, dartClass, isBase);
  }

  @Nullable
  public final DartClass getDartClass() {
    PsiElement element = getPsiElement();
    return element instanceof DartClass ? (DartClass)element : null;
  }

  public final boolean update() {
    boolean changes = super.update();

    final DartClass dartClass = getDartClass();

    if (dartClass == null) {
      return invalidElement();
    }

    installIcon(changes);

    final CompositeAppearance oldText = myHighlightedText;

    myHighlightedText = new CompositeAppearance();

    TextAttributes classNameAttributes = null;
    if (myColor != null) {
      classNameAttributes = new TextAttributes(myColor, null, null, null, Font.PLAIN);
    }

    final String libraryName = DartResolveUtil.getLibraryName(dartClass.getContainingFile());
    myHighlightedText.getEnding().addText(dartClass.getName(), classNameAttributes);
    myHighlightedText.getEnding().addText(" (" + libraryName + ")", HierarchyNodeDescriptor.getPackageNameAttributes());
    myName = myHighlightedText.getText();

    if (!Comparing.equal(myHighlightedText, oldText)) {
      changes = true;
    }
    return changes;
  }
}
