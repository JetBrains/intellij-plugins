package com.jetbrains.lang.dart.psi;

import com.intellij.codeInsight.highlighting.HighlightUsagesDescriptionLocation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartComponentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartElementDescriptionProvider implements ElementDescriptionProvider {
  @Nullable
  @Override
  public String getElementDescription(@NotNull final PsiElement element, @NotNull final ElementDescriptionLocation location) {
    if (!(location instanceof HighlightUsagesDescriptionLocation)) return null;

    if (element instanceof DartLibraryNameElement) return "library " + ((DartLibraryNameElement)element).getName();

    if (element instanceof DartNamedElement) {
      final String name = ((DartNamedElement)element).getName();
      final DartComponentType type = DartComponentType.typeOf(element);
      if (type != null) {
        final String typeText = StringUtil.toLowerCase(type.toString());
        return name != null ? typeText + " " + name : typeText;
      }
    }

    return null;
  }
}
