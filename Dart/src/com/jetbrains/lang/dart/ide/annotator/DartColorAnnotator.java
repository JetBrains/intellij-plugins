package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighterColors;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.psi.DartType;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DartColorAnnotator implements Annotator {
  private static final Set<String> builtinTypes = new THashSet<String>(Arrays.asList(
    "int", "num", "bool", "double", "dynamic"
  ));

  @Override
  public void annotate(final @NotNull PsiElement node, final @NotNull AnnotationHolder holder) {
    if (holder.isBatchMode()) return;

    if (node.getNode().getElementType() == DartTokenTypes.REGULAR_STRING_PART) {
      highlightEscapeSequences(node, holder);
      return;
    }

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

  private static void highlightEscapeSequences(final PsiElement node, final AnnotationHolder holder) {
    final List<Pair<TextRange, Boolean>> escapeSequenceRangesAndValidity = getEscapeSequenceRangesAndValidity(node.getText());
    for (Pair<TextRange, Boolean> rangeAndValidity : escapeSequenceRangesAndValidity) {
      final TextRange range = rangeAndValidity.first.shiftRight(node.getTextRange().getStartOffset());
      final TextAttributesKey attribute = rangeAndValidity.second ? DartSyntaxHighlighterColors.VALID_STRING_ESCAPE
                                                                  : DartSyntaxHighlighterColors.INVALID_STRING_ESCAPE;
      if (rangeAndValidity.second) {
        holder.createInfoAnnotation(range, null).setTextAttributes(attribute);
      }
      else {
        holder
          .createErrorAnnotation(range, DartBundle.message("dart.color.settings.description.invalid.string.escape"))
          .setTextAttributes(attribute);
      }
    }
  }

  @Nullable
  private static TextAttributesKey getAttributeByTypeName(String type) {
    return "void".equals(type) ? TextAttributesKey.find(DartSyntaxHighlighterColors.DART_KEYWORD) : null;    // todo dynamic
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

  @NotNull
  private static List<Pair<TextRange, Boolean>> getEscapeSequenceRangesAndValidity(final @Nullable String text) {
    // \\xFF                 2 hex digits
    // \\uFFFF               4 hex digits
    // \\u{F} - \\u{FFFFFF}  from 1 up to 6 hex digits
    // \\.                   any char except 'x' and 'u'

    if (StringUtil.isEmpty(text)) return Collections.emptyList();

    final List<Pair<TextRange, Boolean>> result = new ArrayList<Pair<TextRange, Boolean>>();

    int currentIndex = -1;
    while ((currentIndex = text.indexOf('\\', currentIndex)) != -1) {
      final int startIndex = currentIndex;

      if (text.length() <= currentIndex + 1) {
        result.add(Pair.create(new TextRange(startIndex, text.length()), false));
        break;
      }

      final char nextChar = text.charAt(++currentIndex);

      if (nextChar == 'x') {
        if (text.length() <= currentIndex + 2) {
          result.add(Pair.create(new TextRange(startIndex, text.length()), false));
          break;
        }

        final char hexChar1 = text.charAt(++currentIndex);
        final char hexChar2 = text.charAt(++currentIndex);
        final boolean valid = StringUtil.isHexDigit(hexChar1) && StringUtil.isHexDigit(hexChar2);
        currentIndex++;
        result.add(Pair.create(new TextRange(startIndex, currentIndex), valid));
      }
      else if (nextChar == 'u') {
        if (text.length() <= currentIndex + 1) {
          result.add(Pair.create(new TextRange(startIndex, text.length()), false));
          break;
        }

        final char hexOrBraceChar1 = text.charAt(++currentIndex);

        if (hexOrBraceChar1 == '{') {
          currentIndex++;

          final int closingBraceIndex = text.indexOf('}', currentIndex);
          if (closingBraceIndex == -1) {
            result.add(Pair.create(new TextRange(startIndex, currentIndex), false));
          }
          else {
            final String textInBrackets = text.substring(currentIndex, closingBraceIndex);
            currentIndex = closingBraceIndex + 1;

            final boolean valid = textInBrackets.length() > 0 && textInBrackets.length() <= 6 && isHexString(textInBrackets);
            result.add(Pair.create(new TextRange(startIndex, currentIndex), valid));
          }
        }
        else {
          //noinspection UnnecessaryLocalVariable
          final char hexChar1 = hexOrBraceChar1;

          if (text.length() <= currentIndex + 3) {
            result.add(Pair.create(new TextRange(startIndex, text.length()), false));
            break;
          }

          final char hexChar2 = text.charAt(++currentIndex);
          final char hexChar3 = text.charAt(++currentIndex);
          final char hexChar4 = text.charAt(++currentIndex);
          final boolean valid = StringUtil.isHexDigit(hexChar1) &&
                                StringUtil.isHexDigit(hexChar2) &&
                                StringUtil.isHexDigit(hexChar3) &&
                                StringUtil.isHexDigit(hexChar4);
          currentIndex++;
          result.add(Pair.create(new TextRange(startIndex, currentIndex), valid));
        }
      }
      else {
        // not 'x' and not 'u', just any other single character escape
        currentIndex++;
        result.add(Pair.create(new TextRange(startIndex, currentIndex), true));
      }
    }

    return result;
  }

  private static boolean isHexString(final String text) {
    if (StringUtil.isEmpty(text)) return false;

    for (int i = 0; i < text.length(); i++) {
      if (!StringUtil.isHexDigit(text.charAt(i))) return false;
    }

    return true;
  }
}
