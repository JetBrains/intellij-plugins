package com.jetbrains.lang.dart.ide.documentation;


import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.petebevin.markdown.MarkdownProcessor;
import org.jetbrains.annotations.NonNls;

import java.util.Iterator;
import java.util.List;

public class DartDocUtil {

  @NonNls public static final String RIGHT_ARROW = " \u2192 ";

  public static String generateDoc(final PsiElement element) {
    if (!(element instanceof DartComponent) && !(element.getParent() instanceof DartComponent)) {
      return null;
    }
    final DartComponent namedComponent = (DartComponent)(element instanceof DartComponent ? element : element.getParent());
    final StringBuilder builder = new StringBuilder();
    final DartComponentType type = DartComponentType.typeOf(namedComponent);
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
    else if (type == DartComponentType.FIELD || type == DartComponentType.METHOD) {
      final DartClass haxeClass = PsiTreeUtil.getParentOfType(namedComponent, DartClass.class);
      assert haxeClass != null;
      builder.append(haxeClass.getName());
      builder.append(" ");
      builder.append(type.toString().toLowerCase());
      builder.append(" ");
      builder.append(namedComponent.getName());
    }
    final PsiComment comment = DartResolveUtil.findDocumentation(namedComponent);
    if (comment != null) {
      builder.append("<br/><br/>");
      final String commentText = DartPresentableUtil.unwrapCommentDelimiters(comment.getText());
      final MarkdownProcessor processor = new MarkdownProcessor();
      builder.append(processor.markdown(commentText));
    }
    return builder.toString();
  }

  protected static void appendClassSignature(final StringBuilder builder, final DartClass dartClass) {
    if (isAbstract(dartClass)) {
      builder.append("abstract ");
    }
    builder.append("class <b>").append(dartClass.getName()).append("</b>");
    final DartTypeParameters typeParameters = dartClass.getTypeParameters();
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

    final DartType superClass = dartClass.getSuperClass();
    if (superClass != null) {
      final PsiElement resolved = superClass.resolveReference();
      if (resolved != null) {
        builder.append(" extends ").append(resolved.getText());
      }
    }

    final List<DartType> implementsList = dartClass.getImplementsList();
    if (!implementsList.isEmpty()) {
      builder.append(" implements ");
      for (Iterator<DartType> iter = implementsList.iterator(); iter.hasNext(); ) {
        final DartType implementedType = iter.next();
        final PsiElement resolvedReference = implementedType.resolveReference();
        if (resolvedReference != null) {
          builder.append(implementedType.getText());
          if (iter.hasNext()) {
            builder.append(",");
          }
        }
      }
    }
  }

  private static void appendFunctionSignature(final StringBuilder builder, final DartComponent function, final DartReturnType returnType) {
    builder.append(function.getName());
    builder.append('(');
    builder.append(DartPresentableUtil.getPresentableParameterList(function));
    builder.append(')');
    if (returnType != null) {
      builder.append(' ');
      builder.append(RIGHT_ARROW);
      builder.append(' ');
      builder.append(DartPresentableUtil.buildTypeText(null, returnType, null));
    }
  }

  // isAbstract does not work for classes :/
  private static boolean isAbstract(final DartClass cls) {
    return "abstract".equals(cls.getFirstChild().getText());
  }

}
