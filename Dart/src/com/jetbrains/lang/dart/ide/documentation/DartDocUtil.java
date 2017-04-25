package com.jetbrains.lang.dart.ide.documentation;


import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
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
    return generateDoc(signatureHtml, true, docText, containingLibraryName, containingClassDescription, null, null, false);
  }

  public static String generateDoc(@Nullable final String signature,
                                   final boolean signatureIsHtml,
                                   @Nullable final String docText,
                                   @Nullable final String containingLibraryName,
                                   @Nullable final String containingClassDescription,
                                   @Nullable final String staticType,
                                   @Nullable final String propagatedType,
                                   final boolean compactPresentation) {
    final boolean hasContainingLibraryName = !StringUtil.isEmpty(containingLibraryName);
    final boolean hasContainingClassDescription = !StringUtil.isEmpty(containingClassDescription);
    final boolean hasStaticType = !StringUtil.isEmpty(staticType);
    final boolean hasPropagatedType = !StringUtil.isEmpty(propagatedType);
    // generate
    final StringBuilder builder = new StringBuilder();
    builder.append("<code>");
    if (signature != null) {
      if (signatureIsHtml) {
        builder.append(signature);
      }
      else {
        builder.append(StringUtil.escapeXml(signature));
      }
      builder.append("<br>");
    }
    if (hasContainingLibraryName || hasContainingClassDescription) {
      builder.append("<br>");
      if (hasContainingLibraryName) {
        builder.append("<b>Containing library:</b> ");
        builder.append(StringUtil.escapeXml(containingLibraryName));
        builder.append("<br>");
      }
      if (hasContainingClassDescription) {
        builder.append("<b>Containing class:</b> ");
        builder.append(StringUtil.escapeXml(containingClassDescription));
        builder.append("<br>");
      }
    }
    if (hasStaticType || hasPropagatedType) {
      if (!compactPresentation) {
        builder.append("<br>");
      }

      if (hasStaticType) {
        builder.append("<b>Static type:</b> ");
        builder.append(StringUtil.escapeXml(staticType));
        builder.append("<br>");
      }
      if (hasPropagatedType) {
        builder.append("<b>Propagated type:</b> ");
        builder.append(StringUtil.escapeXml(propagatedType));
        builder.append("<br>");
      }
    }
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
      return getSingleLineDocCommentsText(siblingComments.toArray(new PsiComment[siblingComments.size()]));
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
      builder.append(StringUtil.escapeXml(expression.getText()));
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

  private static void appendClassSignature(final StringBuilder builder, final DartClass dartClass) {
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
      builder.append(" extends ").append(StringUtil.escapeXml(superClass.getText()));
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

  private static void appendDartTypeList(final StringBuilder builder, final List<DartType> dartTypes) {
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
      returnType == null ? "dynamic" : StringUtil.escapeXml(DartPresentableUtil.buildTypeText(null, returnType, null));
    appendFunctionSignature(builder, function, returnString);
  }

  private static void appendFunctionSignature(final StringBuilder builder, final DartComponent function, final String returnType) {
    builder.append("<b>").append(function.getName()).append("</b>");
    if (!function.isGetter()) {
      builder.append('(');
      builder.append(StringUtil.escapeXml(
        DartPresentableUtil.getPresentableParameterList(function, new DartGenericSpecialization(), true, true, false)));
      builder.append(')');
    }
    builder.append(' ');
    builder.append(DartPresentableUtil.RIGHT_ARROW);
    builder.append(' ');
    builder.append(returnType);
  }
}
