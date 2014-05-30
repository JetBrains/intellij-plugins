package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Condition;
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
        createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_KEYWORD);
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
      createInfoAnnotation(holder, range, DartSyntaxHighlighterColors.DART_METADATA);
      return;
    }

    if (element instanceof DartReference && element.getParent() instanceof DartType && "dynamic".equals(element.getText())) {
      createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_BUILTIN);
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
      componentName = highlightReference(element, holder);
    }

    if (componentName != null) {
      if (BUILT_IN_TYPES_HIGHLIGHTED_AS_KEYWORDS.contains(componentName.getName()) &&
          sdk != null && isInSdkCore(sdk, componentName.getContainingFile())) {
        createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_BUILTIN);
      }
      else {
        final boolean isStatic = isStatic(componentName.getParent());
        final boolean isTopLevel = !isStatic && isTopLevel(componentName.getParent());
        final TextAttributesKey attribute = getDeclarationAttributeByType(DartComponentType.typeOf(componentName.getParent()), isStatic,
                                                                          isTopLevel);
        createInfoAnnotation(holder, element, attribute);
      }
    }
    else {
      highlightDeclarationsAndInvocations(element, holder);
    }
  }

  private static DartComponentName highlightReference(final PsiElement element,
                                                      final AnnotationHolder holder) {
    DartComponentName componentName = null;
    final DartReference[] references = PsiTreeUtil.getChildrenOfType(element, DartReference.class);
    boolean chain = references != null && references.length > 1;
    if (!chain) {
      final PsiElement resolved = ((DartReference)element).resolve(); // todo this takes too much time
      final PsiElement elementParent = element.getParent();
      if (resolved != null && elementParent instanceof DartCallExpression) {
        final PsiElement parent = resolved.getParent();
        if (parent instanceof DartFunctionDeclarationWithBodyOrNative) {
          createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_CALL);
        }
        else if (parent instanceof DartMethodDeclaration) {
          final String callType =
            ((DartMethodDeclaration)parent).isStatic() ? DartSyntaxHighlighterColors.DART_STATIC_MEMBER_FUNCTION_CALL
                                                       : DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_FUNCTION_CALL;
          createInfoAnnotation(holder, element, callType);
        }
      }
      else if (resolved != null) {

        final PsiElement parent = resolved.getParent();
        if (parent instanceof DartVarAccessDeclaration) {
          final DartComponentType type = DartComponentType.typeOf(parent);
          if (type == DartComponentType.VARIABLE) {
            final PsiElement varParent = parent.getParent().getParent();
            if (varParent instanceof DartFile) {
              createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_TOP_LEVEL_VARIABLE_ACCESS);
            }
            else {
              createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE_ACCESS);
            }
          }
          else {
            if (((DartVarAccessDeclaration)parent).isStatic()) {
              createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_STATIC_MEMBER_VARIABLE_ACCESS);
            }
            else {
              createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_VARIABLE_ACCESS);
            }
          }
        }
        else if (resolved instanceof DartComponentName) componentName = (DartComponentName)resolved;
      }
    }
    return componentName;
  }

  private static void highlightDeclarationsAndInvocations(final @NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    if (element instanceof DartNewExpression) {
      final DartNewExpression newExpression = (DartNewExpression)element;
      final DartType type = newExpression.getType();
      createInfoAnnotation(holder, type, DartSyntaxHighlighterColors.DART_CONSTRUCTOR_CALL);
    }
    else if (element instanceof DartConstConstructorExpression) {
      final DartConstConstructorExpression constConstructorExpression = (DartConstConstructorExpression)element;
      final DartType type = constConstructorExpression.getType();
      createInfoAnnotation(holder, type, DartSyntaxHighlighterColors.DART_CONSTRUCTOR_CALL);
    }
    else if (element instanceof DartNamedConstructorDeclaration) {
      final DartNamedConstructorDeclaration decl = (DartNamedConstructorDeclaration)element;
      final PsiElement child = decl.getFirstChild();
      final DartComponentName name = decl.getComponentName();
      final TextRange textRange = new TextRange(child.getTextOffset(), name.getTextRange().getEndOffset());
      createInfoAnnotation(holder, textRange, DartSyntaxHighlighterColors.DART_CONSTRUCTOR_DECLARATION);
    }
    else if (element instanceof DartFactoryConstructorDeclaration) {
      final DartFactoryConstructorDeclaration decl = (DartFactoryConstructorDeclaration)element;
      final DartReference dartReference = PsiTreeUtil.findChildOfType(decl, DartReference.class);
      createInfoAnnotation(holder, dartReference, DartSyntaxHighlighterColors.DART_CONSTRUCTOR_DECLARATION);
    }
    // Constructors are just method declarations whose name matches the parent class
    else if (element instanceof DartMethodDeclaration) {
      final DartMethodDeclaration decl = (DartMethodDeclaration)element;
      final String methodName = decl.getName();
      final DartClassDefinition classDef = PsiTreeUtil.getParentOfType(decl, DartClassDefinition.class);
      if (classDef != null && methodName != null) {
        final String className = classDef.getName();
        if (className != null) {
          final String elementKind;
          if (className.equals(methodName)) {
            elementKind = DartSyntaxHighlighterColors.DART_CONSTRUCTOR_DECLARATION;
          }
          else {
            elementKind = isStatic(element) ? DartSyntaxHighlighterColors.DART_STATIC_MEMBER_FUNCTION
                                            : DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_FUNCTION;
          }
          createInfoAnnotation(holder, decl.getComponentName(), elementKind);
        }
      }
    }
    else if (element instanceof DartFunctionDeclarationWithBodyOrNative) {
      final DartFunctionDeclarationWithBodyOrNative decl = (DartFunctionDeclarationWithBodyOrNative)element;
      createInfoAnnotation(holder, decl.getComponentName(), DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_DECLARATION);
    }
  }

  private static void createInfoAnnotation(final @NotNull AnnotationHolder holder,
                                           final @Nullable PsiElement element,
                                           final @NotNull String attributeKey) {
    if (element != null) {
      createInfoAnnotation(holder, element, TextAttributesKey.find(attributeKey));
    }
  }

  private static void createInfoAnnotation(final @NotNull AnnotationHolder holder,
                                           final @Nullable PsiElement element,
                                           final @Nullable TextAttributesKey attributeKey) {
    if (element != null && attributeKey != null) {
      holder.createInfoAnnotation(element, null).setTextAttributes(attributeKey);
    }
  }

  private static void createInfoAnnotation(final @NotNull AnnotationHolder holder,
                                           final @NotNull TextRange textRange,
                                           final @NotNull String attributeKey) {
    holder.createInfoAnnotation(textRange, null).setTextAttributes(
      TextAttributesKey.find(attributeKey));
  }


  private static boolean isInSdkCore(final @NotNull DartSdk sdk, final @NotNull PsiFile psiFile) {
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    final VirtualFile parentFolder = virtualFile == null ? null : virtualFile.getParent();
    return parentFolder != null && parentFolder.getPath().equals(sdk.getHomePath() + "/lib/core");
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
    return element instanceof DartComponent && ((DartComponent)element).isStatic();
  }

  private static boolean isTopLevel(final PsiElement element) {
    return PsiTreeUtil.findFirstParent(element, new Condition<PsiElement>() {
      @Override
      public boolean value(final PsiElement element) {
        return element instanceof DartMethodDeclaration;
      }
    }) == null;
  }

  @Nullable
  private static TextAttributesKey getDeclarationAttributeByType(final @Nullable DartComponentType type,
                                                                 boolean isStatic,
                                                                 boolean isTopLevel) {
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
        if (isTopLevel) return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_DECLARATION);
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_FUNCTION);
      case VARIABLE:
        if (isTopLevel) return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_TOP_LEVEL_VARIABLE_DECLARATION);
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
