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

  public static String generateDoc(final PsiElement element) {
    if (!(element instanceof DartComponent) && !(element.getParent() instanceof DartComponent)) {
      return null;
    }
    final DartComponent namedComponent = (DartComponent)(element instanceof DartComponent ? element : element.getParent());
    final StringBuilder builder = new StringBuilder();

    appendLibraryName(builder, element);

    builder.append("<code>");

    for (DartMetadata metadata : namedComponent.getMetadataList()) {
      builder.append(StringUtil.join(metadata.getText(), "<br/>"));
    }

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
      appendDeclaringClass(builder, namedComponent);
      appendConstructorSignature(builder, namedComponent, PsiTreeUtil.getParentOfType(namedComponent, DartClass.class));
    }
    else if (namedComponent instanceof DartMethodDeclaration) {
      appendDeclaringClass(builder, namedComponent);
      appendFunctionSignature(builder, namedComponent, ((DartMethodDeclaration)namedComponent).getReturnType());
    }
    else if (namedComponent instanceof DartVarAccessDeclaration) {
      appendDeclaringClass(builder, namedComponent);
      appendVariableSignature(builder, namedComponent, ((DartVarAccessDeclaration)namedComponent).getType());
    }
    else if (namedComponent instanceof DartGetterDeclaration) {
      appendDeclaringClass(builder, namedComponent);
      builder.append("get ");
      appendFunctionSignature(builder, namedComponent, ((DartGetterDeclaration)namedComponent).getReturnType());
    }
    else if (namedComponent instanceof DartSetterDeclaration) {
      appendDeclaringClass(builder, namedComponent);
      builder.append("set ");
      appendFunctionSignature(builder, namedComponent, ((DartSetterDeclaration)namedComponent).getReturnType());
    }

    builder.append("</code>");

    final String docText = getDocumentationText(namedComponent);
    if (docText != null) {
      builder.append("<br/><br/>");
      final MarkdownProcessor processor = new MarkdownProcessor();
      builder.append(processor.markdown(docText));
    }

    return builder.toString();
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

    final PsiElement parent = dartComponent.getParent();
    final PsiElement anchorElement = parent instanceof DartClassMembers && parent.getFirstChild() == dartComponent ? parent : dartComponent;

    // 3. Look for multiline doc comment or line doc comments as previous siblings
    final List<PsiComment> siblingComments = new ArrayList<PsiComment>();
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

        buf.append(StringUtil.trimStart(comment.getText(), "///").trim());
      }
    }

    return buf == null ? null : buf.toString();
  }

  private static void appendConstructorSignature(final StringBuilder builder, final DartComponent component, final DartClass type) {
    if (component instanceof DartNamedConstructorDeclaration || component instanceof DartFactoryConstructorDeclaration) {
      builder.append("<b>").append(type.getName()).append(".</b>");
    }
    appendFunctionSignature(builder, component, type.getName());
  }

  private static void appendDeclaringClass(final StringBuilder builder, final DartComponent namedComponent) {
    final DartClass dartClass = PsiTreeUtil.getParentOfType(namedComponent, DartClass.class);
    if (dartClass != null) {
      builder.append(dartClass.getName());
      builder.append("<br/><br/>");
    }
  }

  private static void appendVariableSignature(final StringBuilder builder, final DartComponent component, final DartType type) {
    if (type == null) {
      builder.append("var ");
    } else {
      builder.append(type.getReferenceExpression().getText()).append(" ");
    }
    builder.append("<b>").append(component.getName()).append("</b>");
  }

  private static void appendClassSignature(final StringBuilder builder, final DartClass dartClass) {

    if (dartClass.isAbstract()) {
      builder.append("abstract ");
    }

    builder.append("class <b>").append(dartClass.getName()).append("</b>");
    appendTypeParams(builder, dartClass.getTypeParameters());

    final List<DartType> mixins = dartClass.getMixinsList();
    final DartType superClass = dartClass.getSuperClass();
    if (superClass != null) {
      builder.append("<br/>extends ").append(StringUtil.escapeXml(superClass.getText()));
    }

    if (!mixins.isEmpty()) {
      builder.append("<br/>with ");
      for (Iterator<DartType> iter = mixins.iterator(); iter.hasNext(); ) {
        builder.append(StringUtil.escapeXml(iter.next().getText()));
        if (iter.hasNext()) {
          builder.append(", ");
        }
      }
    }

    final List<DartType> implementsList = dartClass.getImplementsList();
    if (!implementsList.isEmpty()) {
      builder.append("<br/>implements ");
      for (Iterator<DartType> iter = implementsList.iterator(); iter.hasNext(); ) {
        builder.append(iter.next().getText());
        if (iter.hasNext()) {
          builder.append(", ");
        }
      }
    }
  }

  private static void appendLibraryName(final StringBuilder builder, final PsiElement element) {
    final PsiFile file = element.getContainingFile();
    if (file != null) {
      final String libraryName = DartResolveUtil.getLibraryName(file);
      if (libraryName != null) {
        builder.append(StringUtil.join("<code><small><b>", libraryName, "</b></small></code><br/><br/>"));
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
            builder.append(",");
          }
        }
        builder.append("&gt;");
      }
    }
  }

  private static void appendFunctionSignature(final StringBuilder builder, final DartComponent function, final DartReturnType returnType) {
    final String returnString = returnType == null ? "void" : StringUtil.escapeXml(
      DartPresentableUtil.buildTypeText(null, returnType, null));
    appendFunctionSignature(builder, function, returnString);
  }

  private static void appendFunctionSignature(final StringBuilder builder, final DartComponent function, final String returnType) {
    builder.append("<b>").append(function.getName()).append("</b>");
    builder.append('(');
    builder.append(StringUtil.escapeXml(DartPresentableUtil.getPresentableParameterList(function, new DartGenericSpecialization(), true)));
    builder.append(')');
    builder.append(' ');
    builder.append(DartPresentableUtil.RIGHT_ARROW);
    builder.append(' ');
    builder.append(returnType);
  }
}
