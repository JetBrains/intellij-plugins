package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.LayeredIcon;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public final class DartTypeHierarchyNodeDescriptor extends HierarchyNodeDescriptor {
  public DartTypeHierarchyNodeDescriptor(final Project project,
                                         final HierarchyNodeDescriptor parentDescriptor,
                                         final DartClass dartClass,
                                         final boolean isBase) {
    super(project, parentDescriptor, dartClass, isBase);
  }

  @Nullable
  public final DartClass getDartClass() {
    return (myElement instanceof DartClass) ? (DartClass)myElement : null;
  }

  public final boolean isValid() {
    final DartClass dartClass = getDartClass();
    return dartClass != null && dartClass.isValid();
  }

  public final boolean update() {
    boolean changes = super.update();

    final DartClass dartClass = getDartClass();

    if (dartClass == null) {
      final String invalidPrefix = IdeBundle.message("node.hierarchy.invalid");
      if (!myHighlightedText.getText().startsWith(invalidPrefix)) {
        myHighlightedText.getBeginning().addText(invalidPrefix, HierarchyNodeDescriptor.getInvalidPrefixAttributes());
      }
      return true;
    }

    if (changes && myIsBase) {
      final LayeredIcon icon = new LayeredIcon(2);
      icon.setIcon(getIcon(), 0);
      icon.setIcon(AllIcons.Hierarchy.Base, 1, -AllIcons.Hierarchy.Base.getIconWidth() / 2, 0);
      setIcon(icon);
    }

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
