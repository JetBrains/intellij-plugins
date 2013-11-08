package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighterColors;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.psi.DartType;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;

public class DartColorAnnotator implements Annotator {
  private static final Set<String> builtinTypes = new THashSet<String>(Arrays.asList(
    "int", "num", "bool", "double", "String"
  ));

  @Override
  public void annotate(@NotNull PsiElement node, @NotNull AnnotationHolder holder) {
    if (holder.isBatchMode()) return;

    PsiElement element = node;
    if (element instanceof DartReference && element.getParent() instanceof DartType) {
      final TextAttributesKey attribute = getAttributeByBuiltinType(element.getText());
      if (attribute != null) {
        holder.createInfoAnnotation(node, null).setTextAttributes(attribute);
        return;
      }
    }

    if (element instanceof DartReference) {
      final DartReference[] references = PsiTreeUtil.getChildrenOfType(element, DartReference.class);
      boolean chain = references != null && references.length > 1;
      if (!chain) {
        element = ((DartReference)element).resolve(); // todo this takes too much time
      }
    }
    if (element instanceof DartComponentName) {
      TextAttributesKey attribute = getAttributeByBuiltinType(((DartComponentName)element).getName());
      if (attribute != null) {
        holder.createInfoAnnotation(node, null).setTextAttributes(attribute);
        return;
      }
      final boolean isStatic = checkStatic(element.getParent());
      attribute = getAttributeByType(DartComponentType.typeOf(element.getParent()), isStatic);
      if (attribute != null) {
        holder.createInfoAnnotation(node, null).setTextAttributes(attribute);
      }
    }
    else if (node instanceof DartType) {
      final TextAttributesKey attribute = getAttributeByTypeName(((DartType)node).getReferenceExpression().getText());
      if (attribute != null) {
        holder.createInfoAnnotation(node, null).setTextAttributes(attribute);
      }
    }
  }

  @Nullable
  private static TextAttributesKey getAttributeByTypeName(String type) {
    return "void".equals(type) ? TextAttributesKey.find(DartSyntaxHighlighterColors.DART_KEYWORD) : null;
  }

  private static boolean checkStatic(PsiElement parent) {
    if (parent instanceof DartComponent) {
      return ((DartComponent)parent).isStatic();
    }
    return false;
  }

  @Nullable
  private static TextAttributesKey getAttributeByBuiltinType(String name) {
    return builtinTypes.contains(name) ? TextAttributesKey.find(DartSyntaxHighlighterColors.DART_BUILTIN) : null;
  }

  @Nullable
  private static TextAttributesKey getAttributeByType(@Nullable DartComponentType type, boolean isStatic) {
    if (type == null) {
      return null;
    }
    switch (type) {
      case CLASS:
      case TYPEDEF:
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_CLASS);
      case INTERFACE:
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_INTERFACE);
      case PARAMETER:
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_PARAMETER);
      case FUNCTION:
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_FUNCTION);
      case VARIABLE:
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE);
      case LABEL:
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_LABEL);
      case FIELD:
        if (isStatic) return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_STATIC_MEMBER_VARIABLE);
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_VARIABLE);
      case METHOD:
        if (isStatic) return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_STATIC_MEMBER_FUNCTION);
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_FUNCTION);
      default:
        return null;
    }
  }
}
