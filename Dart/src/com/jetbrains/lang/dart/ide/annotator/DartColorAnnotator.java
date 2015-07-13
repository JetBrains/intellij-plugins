package com.jetbrains.lang.dart.ide.annotator;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
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
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DartColorAnnotator implements Annotator {
  private static final Set<String> BUILT_IN_TYPES_HIGHLIGHTED_AS_KEYWORDS =
    new THashSet<String>(Arrays.asList("int", "num", "bool", "double"));

  @Override
  public void annotate(final @NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    if (holder.isBatchMode()) return;

    final DartSdk sdk = DartSdk.getDartSdk(element.getProject());

    if (DartTokenTypes.COLON == element.getNode().getElementType() && element.getParent() instanceof DartTernaryExpression) {
      createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.OPERATION_SIGN);
      return;
    }

    if (DartTokenTypesSets.BUILT_IN_IDENTIFIERS.contains(element.getNode().getElementType())) {
      if (element.getNode().getTreeParent().getElementType() != DartTokenTypes.ID) {
        createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_KEYWORD);
        return;
      }
    }

    // sync* and async*
    if (DartTokenTypes.MUL == element.getNode().getElementType()) {
      final ASTNode previous = element.getNode().getTreePrev();
      if (previous != null && (previous.getElementType() == DartTokenTypes.SYNC ||
                               previous.getElementType() == DartTokenTypes.ASYNC ||
                               previous.getElementType() == DartTokenTypes.YIELD)) {
        createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_KEYWORD);
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

    if (element instanceof DartSymbolLiteralExpression) {
      createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.SYMBOL_LITERAL);
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
      componentName = highlightReference((DartReference)element, holder);
    }

    if (componentName != null) {
      if (BUILT_IN_TYPES_HIGHLIGHTED_AS_KEYWORDS.contains(componentName.getName()) &&
          sdk != null && isInSdkCore(sdk, componentName.getContainingFile())) {
        createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_BUILTIN);
      }
      else {
        createInfoAnnotation(holder, element, getDeclarationAttributeByType(componentName));
      }
    }
    else {
      highlightDeclarationsAndInvocations(element, holder);
    }
  }

  private static DartComponentName highlightReference(final DartReference element, final AnnotationHolder holder) {
    DartComponentName componentName = null;
    final DartReference[] references = PsiTreeUtil.getChildrenOfType(element, DartReference.class);
    boolean chain = references != null && references.length > 1;
    if (!chain) {
      final PsiElement resolved = element.resolve(); // todo this takes too much time
      if (resolved != null) {
        final PsiElement parent = resolved.getParent();
        final DartComponent parentComponent = parent instanceof DartComponent ? (DartComponent)parent : null;
        if (parent instanceof DartFunctionDeclarationWithBodyOrNative) {
          createInfoAnnotation(holder, element, DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_CALL);
        }
        else if (parent instanceof DartGetterDeclaration || parent instanceof DartSetterDeclaration) {
          final String key = parentComponent.isUnitMember()
                             ? DartSyntaxHighlighterColors.DART_TOP_LEVEL_VARIABLE_ACCESS
                             : parentComponent.isStatic()
                               ? DartSyntaxHighlighterColors.DART_STATIC_MEMBER_VARIABLE_ACCESS
                               : DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_VARIABLE_ACCESS;
          createInfoAnnotation(holder, element, key);
        }
        else if (parent instanceof DartMethodDeclaration) {
          final String callType = getCallKind((DartMethodDeclaration)parent, element);
          createInfoAnnotation(holder, element, callType);
        }
        else if (parent instanceof DartVarAccessDeclaration || parent instanceof DartVarDeclarationListPart) {
          final DartComponentType type = DartComponentType.typeOf(parent);
          if (type == DartComponentType.VARIABLE) {
            final String key = parentComponent.isUnitMember()
                               ? DartSyntaxHighlighterColors.DART_TOP_LEVEL_VARIABLE_ACCESS
                               : DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE_ACCESS;
            createInfoAnnotation(holder, element, key);
          }
          else {
            final String key = parentComponent.isStatic()
                               ? DartSyntaxHighlighterColors.DART_STATIC_MEMBER_VARIABLE_ACCESS
                               : DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_VARIABLE_ACCESS;
            createInfoAnnotation(holder, element, key);
          }
        }
        else if (resolved instanceof DartComponentName) componentName = (DartComponentName)resolved;
      }
    }
    return componentName;
  }

  private static String getCallKind(final DartMethodDeclaration decl, final PsiElement reference) {
    if (decl.isAbstract()) return DartSyntaxHighlighterColors.DART_ABSTRACT_MEMBER_FUNCTION_CALL;
    if (decl.isStatic()) return DartSyntaxHighlighterColors.DART_STATIC_MEMBER_FUNCTION_CALL;
    if (isInherited(decl, reference)) return DartSyntaxHighlighterColors.DART_INHERITED_MEMBER_FUNCTION_CALL;
    return DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_FUNCTION_CALL;
  }

  private static boolean isInherited(final DartMethodDeclaration decl, final PsiElement reference) {
    final DartClass referencedClass = getClass(reference);
    if (referencedClass == null) return false;
    final DartClass declaringClass = PsiTreeUtil.getParentOfType(decl, DartClass.class);
    return !isEquivalentTo(declaringClass, referencedClass);
  }

  private static boolean isEquivalentTo(final @Nullable DartClass cls1, final @Nullable DartClass cls2) {
    if (cls1 == null || cls2 == null) return false;
    final PsiElement id1 = cls1.getNameIdentifier();
    final PsiElement id2 = cls2.getNameIdentifier();
    if (id1 == null || id2 == null) return false;
    final String id1Text = id1.getText();
    final String id2Text = id2.getText();
    return !(id1Text == null || id2Text == null) && id1Text.equals(id2Text);
  }

  private static DartClass getClass(final PsiElement reference) {
    if (reference == null) return null;
    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);
    if (leftReference != null) {
      final DartClassResolveResult resolveResult = DartResolveUtil.getDartClassResolveResult(leftReference);
      return resolveResult.getDartClass();
    }
    return PsiTreeUtil.getParentOfType(reference, DartClass.class);
  }

  private static void highlightDeclarationsAndInvocations(final @NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    if (element instanceof DartNewExpression) {
      final DartNewExpression newExpression = (DartNewExpression)element;
      final DartType type = newExpression.getType();
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
      final DartClass classDef = PsiTreeUtil.getParentOfType(decl, DartClass.class);
      if (classDef != null && methodName != null) {
        final String className = classDef.getName();
        if (className != null) {
          final String elementKind;
          if (className.equals(methodName)) {
            elementKind = DartSyntaxHighlighterColors.DART_CONSTRUCTOR_DECLARATION;
          }
          else {
            elementKind = isStatic(element)
                          ? DartSyntaxHighlighterColors.DART_STATIC_MEMBER_FUNCTION
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
    holder.createInfoAnnotation(textRange, null).setTextAttributes(TextAttributesKey.find(attributeKey));
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
      final TextAttributesKey attribute =
        rangeAndValidity.second ? DartSyntaxHighlighterColors.VALID_STRING_ESCAPE : DartSyntaxHighlighterColors.INVALID_STRING_ESCAPE;
      if (rangeAndValidity.second) {
        holder.createInfoAnnotation(range, null).setTextAttributes(attribute);
      }
      else {
        holder.createErrorAnnotation(range, DartBundle.message("dart.color.settings.description.invalid.string.escape"))
          .setTextAttributes(attribute);
      }
    }
  }

  private static boolean isPropertyAccessorName(DartComponentName name) {
    DartComponent component = (DartComponent)name.getParent();
    return component.isGetter() || component.isSetter();
  }

  private static boolean isStatic(final PsiElement element) {
    return element instanceof DartComponent && ((DartComponent)element).isStatic();
  }

  private static boolean isInFunctionBody(PsiElement element) {
    return element != null && PsiTreeUtil.getParentOfType(element, DartFunctionBody.class) != null;
  }

  @Nullable
  private static TextAttributesKey getDeclarationAttributeByType(@NotNull final DartComponentName componentName) {
    DartComponentType type = DartComponentType.typeOf(componentName);
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
        if (isPropertyAccessorName(componentName)) {
          return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_TOP_LEVEL_VARIABLE_DECLARATION);
        }
        else {
          return isInFunctionBody(componentName)
                 ? TextAttributesKey.find(DartSyntaxHighlighterColors.DART_FUNCTION)
                 : TextAttributesKey.find(DartSyntaxHighlighterColors.DART_TOP_LEVEL_FUNCTION_DECLARATION);
        }
      case VARIABLE:
        return isInFunctionBody(componentName)
               ? TextAttributesKey.find(DartSyntaxHighlighterColors.DART_LOCAL_VARIABLE)
               : TextAttributesKey.find(DartSyntaxHighlighterColors.DART_TOP_LEVEL_VARIABLE_DECLARATION);
      case LABEL:
        return TextAttributesKey.find(DartSyntaxHighlighterColors.DART_LABEL);
      case FIELD:
        return isStatic(componentName.getParent())
               ? TextAttributesKey.find(DartSyntaxHighlighterColors.DART_STATIC_MEMBER_VARIABLE)
               : TextAttributesKey.find(DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_VARIABLE);
      case METHOD: {
        if (isPropertyAccessorName(componentName)) {
          return isStatic(componentName.getParent())
                 ? TextAttributesKey.find(DartSyntaxHighlighterColors.DART_STATIC_MEMBER_VARIABLE)
                 : TextAttributesKey.find(DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_VARIABLE);
        }
        else {
          return isStatic(componentName.getParent())
                 ? TextAttributesKey.find(DartSyntaxHighlighterColors.DART_STATIC_MEMBER_FUNCTION)
                 : TextAttributesKey.find(DartSyntaxHighlighterColors.DART_INSTANCE_MEMBER_FUNCTION);
        }
      }
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
