// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.documentation;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartGenericSpecialization;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import com.petebevin.markdown.MarkdownProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DartDocUtil {

  public static final String SINGLE_LINE_DOC_COMMENT = "///";
  private static final String NBSP = "&nbsp;";
  private static final String GREATER_THAN = "&gt;";
  private static final String LESS_THAN = "&lt;";

  public static String generateDoc(final PsiElement element) {
    if (!(element instanceof DartComponent) && !(element.getParent() instanceof DartComponent)) {
      return null;
    }
    final DartComponent namedComponent = (DartComponent)(element instanceof DartComponent ? element : element.getParent());

    final String signatureHtml;
    {
      final StringBuilder builder = new StringBuilder();
      appendSignature(namedComponent, builder);
      signatureHtml = builder.toString();
    }

    final String containingLibraryName;
    final PsiFile file = element.getContainingFile();
    if (file != null) {
      containingLibraryName = DartResolveUtil.getLibraryName(file);
    }
    else {
      containingLibraryName = null;
    }

    final String containingClassDescription;
    final DartClass dartClass = PsiTreeUtil.getParentOfType(namedComponent, DartClass.class);
    if (dartClass != null) {
      final StringBuilder builder = new StringBuilder();
      builder.append(dartClass.getName());
      appendTypeParams(builder, dartClass.getTypeParameters());
      containingClassDescription = builder.toString();
    }
    else {
      containingClassDescription = null;
    }

    final String docText = getDocumentationText(namedComponent);
    return generateDoc(signatureHtml, true, docText, containingLibraryName, containingClassDescription, null, false);
  }

  private static String formatSignature(@NotNull final String signature) {
    final int offsetToOpenParen = signature.indexOf('(');

    // If this signature doesn't have a '(', return
    if (offsetToOpenParen <= 0) {
      return StringUtil.escapeXmlEntities(signature);
    }

    String[] strings = signatureSplit(signature);
    if (strings.length == 1) {
      return StringUtil.escapeXmlEntities(signature);
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < strings.length; i++) {
      stringBuilder.append(StringUtil.escapeXmlEntities(strings[i]));
      if (i + 1 != strings.length) {
        stringBuilder.append(",<br>");
        stringBuilder.append(StringUtil.repeat(NBSP, offsetToOpenParen + 1));
      }
    }
    return stringBuilder.toString();
  }

  /**
   * Split around the ", " pattern, when not in a generic or function parameter (inside a nested parenthesize.)
   */
  private static String[] signatureSplit(@NotNull final String str) {
    List<String> result = new SmartList<>();

    int beginningOffset = 0;
    int genericDepth = 0;
    int parenDepth = 0;
    for (int i = 0; i < str.length(); i++) {
      final char c = str.charAt(i);
      if (c == '<') {
        genericDepth++;
      }
      else if (c == '>') {
        genericDepth = Math.max(genericDepth - 1, 0);
      }
      else if (c == '(') {
        parenDepth++;
      }
      else if (c == ')') {
        parenDepth = Math.max(parenDepth - 1, 0);
      }
      else if (c == ',' && genericDepth == 0 && parenDepth == 1) {
        result.add(str.substring(beginningOffset, i));
        beginningOffset = i + 1;
        while (beginningOffset + 1 < str.length() && str.charAt(beginningOffset) == ' ') {
          beginningOffset++;
        }
      }
    }
    result.add(str.substring(beginningOffset));
    return ArrayUtil.toStringArray(result);
  }

  @NotNull
  public static String generateDoc(@Nullable final String signature,
                                   final boolean signatureIsHtml,
                                   @Nullable final String docText,
                                   @Nullable final String containingLibraryName,
                                   @Nullable final String containingClassDescription,
                                   @Nullable final String staticType,
                                   final boolean compactPresentation) {
    final boolean hasContainingLibraryName = StringUtil.isNotEmpty(containingLibraryName);
    final boolean hasContainingClassDescription = StringUtil.isNotEmpty(containingClassDescription);
    final boolean hasStaticType = StringUtil.isNotEmpty(staticType);
    // generate
    final StringBuilder builder = new StringBuilder();
    builder.append("<code>");
    if (hasContainingLibraryName) {
      builder.append("<b>");
      builder.append(StringUtil.escapeXmlEntities(containingLibraryName));
      builder.append("</b>");
      builder.append("<br>");
    }
    if (signature != null) {
      if (signatureIsHtml) {
        builder.append(signature);
      }
      else {
        builder.append(formatSignature(signature));
      }
      builder.append("<br>");
    }
    if (hasContainingClassDescription) {
      builder.append("<br>");
      builder.append("<b>Containing class:</b> ");
      builder.append(StringUtil.escapeXmlEntities(containingClassDescription));
      builder.append("<br>");
    }
    if (hasStaticType) {
      if (!compactPresentation) {
        builder.append("<br>");
      }
      builder.append("<b>Type:</b> ");
      builder.append(StringUtil.escapeXmlEntities(staticType));
      builder.append("<br>");
    }
    builder.append("<br>");
    builder.append("</code>\n");
    if (docText != null) {
      final MarkdownProcessor processor = new MarkdownProcessor();
      builder.append(processor.markdown(docText.trim()));
    }
    // done
    return builder.toString().trim();
  }

  @Nullable
  public static String getSignature(@NotNull PsiElement element) {
    if (!(element instanceof DartComponent)) {
      element = element.getParent();
    }
    if (element instanceof DartComponent) {
      final StringBuilder sb = new StringBuilder();
      appendSignature((DartComponent)element, sb);
      if (sb.length() > 0) return sb.toString();
    }
    return null;
  }

  private static void appendSignature(final DartComponent namedComponent, final StringBuilder builder) {
    if (namedComponent instanceof DartClass) {
      appendClassSignature(builder, (DartClass)namedComponent);
    }
    else if (namedComponent instanceof DartFunctionDeclarationWithBodyOrNative) {
      appendFunctionSignature(builder, namedComponent, ((DartFunctionDeclarationWithBodyOrNative)namedComponent).getReturnType());
    }
    else if (namedComponent instanceof DartFunctionTypeAlias) {
      builder.append("typedef ");
      appendFunctionSignature(builder, namedComponent, ((DartFunctionTypeAlias)namedComponent).getReturnType());
    }
    else if (namedComponent.isConstructor()) {
      appendConstructorSignature(builder, namedComponent, PsiTreeUtil.getParentOfType(namedComponent, DartClass.class));
    }
    else if (namedComponent instanceof DartMethodDeclaration) {
      appendFunctionSignature(builder, namedComponent, ((DartMethodDeclaration)namedComponent).getReturnType());
    }
    else if (namedComponent instanceof DartVarAccessDeclaration) {
      appendVariableSignature(builder, namedComponent, ((DartVarAccessDeclaration)namedComponent).getType());
    }
    else if (namedComponent instanceof DartGetterDeclaration) {
      builder.append("get ");
      appendFunctionSignature(builder, namedComponent, ((DartGetterDeclaration)namedComponent).getReturnType());
    }
    else if (namedComponent instanceof DartSetterDeclaration) {
      builder.append("set ");
      appendFunctionSignature(builder, namedComponent, ((DartSetterDeclaration)namedComponent).getReturnType());
    }
    else if (namedComponent instanceof DartEnumConstantDeclaration) {
      builder.append(((DartEnumDefinition)namedComponent.getParent()).getName()).append(" ");
      builder.append("<b>").append(namedComponent.getName()).append("</b>");
    }
  }


  @Nullable
  private static String getDocumentationText(final DartComponent dartComponent) {
    // PSI is not perfect currently, doc comment may be not part of the corresponding DartComponent element, so docs are searched for in several places:
    // - direct child of this DartComponent
    // - previous sibling (or previous sibling of parent element if this element is first child of its parent DartClassMembers)
    // Consequent line doc comments (///) are joined

    // 1. Look for multiline doc comment as direct child
    final DartDocComment multilineComment = PsiTreeUtil.getChildOfType(dartComponent, DartDocComment.class);
    if (multilineComment != null) return getMultilineDocCommentText(multilineComment);

    // 2. Look for single line doc comments as direct children
    final PsiComment[] childComments = PsiTreeUtil.getChildrenOfType(dartComponent, PsiComment.class);
    if (childComments != null) {
      //
      final String docText = getSingleLineDocCommentsText(childComments);
      if (docText != null) return docText;
    }

    PsiElement anchorElement = dartComponent;

    final PsiElement parent = dartComponent.getParent();
    if (parent instanceof DartClassMembers && parent.getFirstChild() == dartComponent ||
        dartComponent instanceof DartVarAccessDeclaration) {
      anchorElement = parent;
    }

    // 3. Look for multiline doc comment or line doc comments as previous siblings
    final List<PsiComment> siblingComments = new ArrayList<>();
    PsiElement previous = anchorElement;
    while ((previous = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpaces(previous, true)) instanceof PsiComment) {
      if (previous instanceof DartDocComment) {
        return getMultilineDocCommentText((DartDocComment)previous);
      }
      siblingComments.add(0, (PsiComment)previous);
    }

    if (!siblingComments.isEmpty()) {
      return getSingleLineDocCommentsText(siblingComments.toArray(new PsiComment[0]));
    }

    return null;
  }

  @NotNull
  private static String getMultilineDocCommentText(final @NotNull DartDocComment docComment) {
    final StringBuilder buf = new StringBuilder();
    boolean afterAsterisk = false;

    for (PsiElement child = docComment.getFirstChild(); child != null; child = child.getNextSibling()) {
      final IElementType elementType = child.getNode().getElementType();
      final String text = child.getText();

      if (elementType != DartTokenTypesSets.MULTI_LINE_DOC_COMMENT_START &&
          elementType != DartTokenTypesSets.DOC_COMMENT_LEADING_ASTERISK &&
          elementType != DartTokenTypesSets.MULTI_LINE_COMMENT_END) {
        int newLinesCount;
        if (child instanceof PsiWhiteSpace && (newLinesCount = StringUtil.countNewLines(text)) > 0) {
          buf.append(StringUtil.repeatSymbol('\n', newLinesCount));
        }
        else {
          if (afterAsterisk && text.startsWith(" ")) {
            buf.append(text.substring(1));
          }
          else {
            buf.append(text);
          }
        }
      }

      afterAsterisk = elementType == DartTokenTypesSets.DOC_COMMENT_LEADING_ASTERISK;
    }

    return buf.toString();
  }

  @Nullable
  private static String getSingleLineDocCommentsText(final @NotNull PsiComment[] comments) {
    StringBuilder buf = null;

    for (PsiComment comment : comments) {
      if (comment.getNode().getElementType() == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) {
        if (buf == null) {
          buf = new StringBuilder();
        }
        else {
          buf.append('\n');
        }

        final String text = comment.getText();
        if (text.startsWith(SINGLE_LINE_DOC_COMMENT + " ")) {
          buf.append(StringUtil.trimStart(text, SINGLE_LINE_DOC_COMMENT + " "));
        }
        else {
          buf.append(StringUtil.trimStart(text, SINGLE_LINE_DOC_COMMENT));
        }
      }
    }

    return buf == null ? null : buf.toString();
  }

  private static void appendConstructorSignature(final StringBuilder builder, final DartComponent component, final DartClass dartClass) {
    if (component instanceof DartNamedConstructorDeclaration || component instanceof DartFactoryConstructorDeclaration) {
      builder.append("<b>").append(dartClass.getName()).append(".</b>");
    }
    appendFunctionSignature(builder, component, dartClass.getName());
  }

  private static void appendVariableSignature(@NotNull final StringBuilder builder,
                                              @NotNull final DartComponent component,
                                              @Nullable final DartType type) {
    if (type == null) {
      builder.append("var ");
    }
    else {
      appendDartType(builder, type);
      builder.append(" ");
    }
    builder.append("<b>").append(component.getName()).append("</b>");
  }

  private static void appendDartType(@NotNull final StringBuilder builder, @NotNull final DartType type) {
    final DartReferenceExpression expression = type.getReferenceExpression();
    if (expression != null) {
      builder.append(StringUtil.escapeXmlEntities(expression.getText()));
      appendTypeArguments(builder, type);
    }
    else {
      builder.append("Function"); // functionType
    }
  }

  private static void appendTypeArguments(final @NotNull StringBuilder builder, final @NotNull DartType type) {
    final DartTypeArguments typeArguments = type.getTypeArguments();
    if (typeArguments != null) {
      final DartTypeList typeList = typeArguments.getTypeList();
      final List<DartType> children = typeList.getTypeList();
      if (!children.isEmpty()) {
        builder.append("&lt;");
        appendDartTypeList(builder, children);
        builder.append("&gt;");
      }
    }
  }

  private static void appendClassSignature(final StringBuilder builder, @NotNull final DartClass dartClass) {
    if (dartClass.isEnum()) {
      builder.append("enum <b>").append(dartClass.getName()).append("</b>");
      return;
    }

    if (dartClass.isAbstract()) {
      builder.append("abstract ");
    }

    builder.append("class <b>").append(dartClass.getName()).append("</b>");
    appendTypeParams(builder, dartClass.getTypeParameters());

    final List<DartType> mixins = dartClass.getMixinsList();
    final DartType superClass = dartClass.getSuperClass();
    if (superClass != null) {
      builder.append(" extends ").append(StringUtil.escapeXmlEntities(superClass.getText()));
    }

    if (!mixins.isEmpty()) {
      builder.append(" with ");
      appendDartTypeList(builder, mixins);
    }

    final List<DartType> implementsList = dartClass.getImplementsList();
    if (!implementsList.isEmpty()) {
      builder.append(" implements ");
      appendDartTypeList(builder, implementsList);
    }
  }

  private static void appendDartTypeList(final StringBuilder builder, @NotNull final List<DartType> dartTypes) {
    for (Iterator<DartType> iter = dartTypes.iterator(); iter.hasNext(); ) {
      appendDartType(builder, iter.next());
      if (iter.hasNext()) {
        builder.append(", ");
      }
    }
  }

  private static void appendTypeParams(final StringBuilder builder, final DartTypeParameters typeParameters) {
    if (typeParameters != null) {
      final List<DartTypeParameter> parameters = typeParameters.getTypeParameterList();
      if (!parameters.isEmpty()) {
        builder.append("&lt;");
        for (Iterator<DartTypeParameter> iter = parameters.iterator(); iter.hasNext(); ) {
          builder.append(iter.next().getText());
          if (iter.hasNext()) {
            builder.append(", ");
          }
        }
        builder.append("&gt;");
      }
    }
  }

  private static void appendFunctionSignature(final StringBuilder builder, final DartComponent function, final DartReturnType returnType) {
    final String returnString =
      returnType == null ? "dynamic" : StringUtil.escapeXmlEntities(DartPresentableUtil.buildTypeText(null, returnType, null));
    appendFunctionSignature(builder, function, returnString);
  }

  private static void appendFunctionSignature(@NotNull final StringBuilder builder,
                                              @NotNull final DartComponent function,
                                              final String returnType) {
    builder.append("<b>").append(function.getName()).append("</b>");
    if (!function.isGetter()) {
      builder.append('(');
      builder.append(StringUtil.escapeXmlEntities(
        DartPresentableUtil.getPresentableParameterList(function, new DartGenericSpecialization(), true, true, false)));
      builder.append(')');
    }
    builder.append(' ');
    builder.append(DartPresentableUtil.RIGHT_ARROW);
    builder.append(' ');
    builder.append(returnType);
  }
}
