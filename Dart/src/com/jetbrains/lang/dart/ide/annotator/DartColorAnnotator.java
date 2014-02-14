package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.highlight.DartSyntaxHighlighterColors;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.sdk.DartSdk;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DartColorAnnotator implements Annotator {
  private static final Set<String> BUILT_IN_TYPES_HIGHLIGHTED_AS_KEYWORDS = new THashSet<String>(Arrays.asList(
    "int", "num", "bool", "double"
  ));

  @Override
  public void annotate(final @NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    if (holder.isBatchMode()) return;

    final DartSdk sdk = DartSdk.getGlobalDartSdk();

    if (DartTokenTypesSets.BUILT_IN_IDENTIFIERS.contains(element.getNode().getElementType())) {
      if (element.getNode().getTreeParent().getElementType() != DartTokenTypes.ID) {
        final String message = ApplicationManager.getApplication().isUnitTestMode() ? "highlighted as keyword" : null;
        holder.createInfoAnnotation(element, message).setTextAttributes(TextAttributesKey.find(DartSyntaxHighlighterColors.DART_KEYWORD));
        return;
      }
    }

    if (element.getNode().getElementType() == DartTokenTypes.REGULAR_STRING_PART) {
      highlightEscapeSequences(element, holder);
      return;
    }

    if (element instanceof DartMetadata) {
      final DartArguments arguments = ((DartMetadata)element).getArguments();
      final int endOffset = arguments == null ? element.getTextRange().getEndOffset() : arguments.getTextRange().getStartOffset();
      final TextRange range = TextRange.create(element.getTextRange().getStartOffset(), endOffset);

      final String message = ApplicationManager.getApplication().isUnitTestMode() ? "metadata" : null;
      holder.createInfoAnnotation(range, message).setTextAttributes(TextAttributesKey.find(DartSyntaxHighlighterColors.DART_METADATA));
      return;
    }

    if (element instanceof DartReference && element.getParent() instanceof DartType && "dynamic".equals(element.getText())) {
      holder.createInfoAnnotation(element, null).setTextAttributes(TextAttributesKey.find(DartSyntaxHighlighterColors.DART_BUILTIN));
      return;
    }

    highlightIfDeclarationOrReference(element, holder, sdk);
  }

  private static void highlightIfDeclarationOrReference(final PsiElement element,
                                                        final AnnotationHolder holder,
                                                        final @Nullable DartSdk sdk) {
    DartComponentName componentName = null;

    if (element instanceof DartComponentName) {
      componentName = (DartComponentName)element;
    }
    else if (element instanceof DartReference) {
      final DartReference[] references = PsiTreeUtil.getChildrenOfType(element, DartReference.class);
      boolean chain = references != null && references.length > 1;
      if (!chain) {
        final PsiElement resolved = ((DartReference)element).resolve(); // todo this takes too much time
        if (resolved instanceof DartComponentName) componentName = (DartComponentName)resolved;
      }
    }

    if (componentName != null) {
      if (BUILT_IN_TYPES_HIGHLIGHTED_AS_KEYWORDS.contains(componentName.getName()) &&
          sdk != null && isInSdkCore(sdk, componentName.getContainingFile())) {
        holder.createInfoAnnotation(element, null).setTextAttributes(TextAttributesKey.find(DartSyntaxHighlighterColors.DART_BUILTIN));
        return;
      }

      final boolean isStatic = isStatic(componentName.getParent());
      final TextAttributesKey attribute = getAttributeByType(DartComponentType.typeOf(componentName.getParent()), isStatic);
      if (attribute != null) {
        holder.createInfoAnnotation(element, null).setTextAttributes(attribute);
      }
    }
  }

  private static boolean isInSdkCore(final @NotNull DartSdk sdk, final @NotNull PsiFile psiFile) {
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    return virtualFile != null && virtualFile.getParent().getPath().equals(sdk.getHomePath() + "/lib/core");
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

  private static boolean isStatic(final PsiElement element) {
    if (element instanceof DartComponent) {
      return ((DartComponent)element).isStatic();
    }
    return false;
  }

  @Nullable
  private static TextAttributesKey getAttributeByType(final @Nullable DartComponentType type, boolean isStatic) {
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
