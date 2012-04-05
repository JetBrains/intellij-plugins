package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class ExpressionBinding extends Binding {
  private final JSExpression expression;

  public ExpressionBinding(JSExpression expression) {
    this.expression = expression;
  }

  @Override
  protected int getType() {
    return BindingType.EXPRESSION;
  }

  @Override
  void write(PrimitiveAmfOutputStream out, BaseWriter writer, ValueReferenceResolver valueReferenceResolver)
    throws InvalidPropertyException {
    super.write(out, writer, valueReferenceResolver);

    // chain (not null expression.getQualifier()) supported only for binding from MXML, not from variable initializer value
    if (expression instanceof JSReferenceExpression) {
      writeReferenceExpression((JSReferenceExpression)expression, out, writer, valueReferenceResolver, true);
    }
    else {
      writeExpression(expression, out, writer, valueReferenceResolver);
    }
  }

  private static void writeExpression(JSExpression expression, PrimitiveAmfOutputStream out, BaseWriter writer,
                                      @Nullable ValueReferenceResolver valueReferenceResolver)
    throws InvalidPropertyException {
    if (expression instanceof JSLiteralExpression) {
      writeLiteralExpression((JSLiteralExpression)expression, out, writer);
    }
    else if (expression instanceof JSObjectLiteralExpression) {
      writeObjectExpression((JSObjectLiteralExpression)expression, out, writer, valueReferenceResolver);
    }
    else if (expression instanceof JSArrayLiteralExpression) {
      writeArrayLiteralExpression((JSArrayLiteralExpression)expression, out, writer, valueReferenceResolver);
    }
    else if (expression instanceof JSNewExpression) {
      writeCallExpression((JSNewExpression)expression, out, writer, valueReferenceResolver);
    }
    else if (expression instanceof JSReferenceExpression) {
      writeReferenceExpression((JSReferenceExpression)expression, out, writer, valueReferenceResolver, false);
    }
    else if (expression instanceof JSCallExpression) {
      writeCallExpression((JSCallExpression)expression, out, writer, valueReferenceResolver);
    }
    else {
      throw new UnsupportedOperationException(expression.getText());
    }
  }

  private static void writeCallExpression(JSCallExpression expression, PrimitiveAmfOutputStream out, BaseWriter writer,
                                          ValueReferenceResolver valueReferenceResolver) throws InvalidPropertyException {
    final JSReferenceExpression methodExpression = (JSReferenceExpression)expression.getMethodExpression();
    final PsiElement psiElement = resolveReferenceExpression(methodExpression, true);
    final JSExpression[] arguments;
    if (psiElement instanceof JSClass) {
      // IDEA-74060, {Number('20')}
      arguments = expression.getArguments();
      assert arguments.length == 1;
      writeExpression(arguments[0], out, writer, valueReferenceResolver);
      return;
    }
    else if (psiElement instanceof JSReferenceExpression) {
      writeReferenceExpression((JSReferenceExpression)psiElement, out, writer, valueReferenceResolver, false);
      return;
    }

    writeFunction((JSFunction)psiElement, writer, valueReferenceResolver, false, expression, methodExpression);
  }

  private static void writeFunction(JSFunction function,
                                    BaseWriter writer,
                                    @Nullable ValueReferenceResolver valueReferenceResolver,
                                    boolean isBinding) throws InvalidPropertyException {
    writeFunction(function, writer, valueReferenceResolver, isBinding, null, null);
  }

  private static void writeFunction(JSFunction function,
                                    BaseWriter writer,
                                    @Nullable ValueReferenceResolver valueReferenceResolver,
                                    boolean isBinding,
                                    @Nullable JSCallExpression expression,
                                    @Nullable JSReferenceExpression methodExpression) throws InvalidPropertyException {
    final PrimitiveAmfOutputStream out = writer.getOut();
    JSExpression[] arguments;
    final int rollbackPosition;
    final int start;
    if (function.isConstructor()) {
      assert expression != null;
      arguments = expression.getArguments();
      final JSClass jsClass = (JSClass)function.getParent();
      // text="{new String('newString')}"
      writer.newInstance(jsClass.getQualifiedName(), arguments.length, true);
      rollbackPosition = out.allocateShort();
      start = out.size();
    }
    else {
      out.write(ExpressionMessageTypes.CALL);
      rollbackPosition = out.allocateShort();
      start = out.size();
      // text="{resourceManager.getString('core', 'viewSource')}"
      if (methodExpression != null) {
        JSReferenceExpression qualifier = (JSReferenceExpression)methodExpression.getQualifier();
        while (qualifier != null) {
          writer.classOrPropertyName(qualifier.getReferencedName());
          qualifier = (JSReferenceExpression)qualifier.getQualifier();
        }
      }

      out.write(0);

      writer.classOrPropertyName(function.getName());

      if (function.isGetProperty()) {
        out.write(isBinding ? -2 : -1);
        return;
      }
      else {
        assert expression != null;
        arguments = expression.getArguments();
        out.write(arguments.length);
      }
    }

    for (JSExpression argument : arguments) {
      writeExpression(argument, out, writer, valueReferenceResolver);
    }

    out.putShort(out.size() - start, rollbackPosition);
  }

  static void writeArrayLiteralExpression(JSArrayLiteralExpression expression, PrimitiveAmfOutputStream out, BaseWriter writer,
                                          @Nullable ValueReferenceResolver valueReferenceResolver) throws InvalidPropertyException {
    JSExpression[] expressions = expression.getExpressions();
    writer.arrayHeader(expressions.length);
    for (JSExpression item : expressions) {
      writeExpression(item, out, writer, valueReferenceResolver);
    }
  }

  @NotNull
  private static PsiElement resolveReferenceExpression(JSReferenceExpression expression, boolean qualificatorSupported) throws InvalidPropertyException {
    if (!qualificatorSupported) {
      checkQualifier(expression);
    }

    final AccessToken token = ReadAction.start();
    final PsiElement element;
    try {
      element = expression.resolve();
    }
    finally {
      token.finish();
    }

    if (element == null) {
      throw new InvalidPropertyException(expression, "unresolved.variable.or.type", expression.getReferencedName());
    }

    return element;
  }

  private static void checkQualifier(JSReferenceExpression expression) {
    JSExpression qualifier = expression.getQualifier();
    if (qualifier != null && !(qualifier instanceof JSThisExpression)) {
      throw new UnsupportedOperationException(expression.getText());
    }
  }

  private static void writeReferenceExpression(JSReferenceExpression expression, PrimitiveAmfOutputStream out, BaseWriter writer,
                                               ValueReferenceResolver valueReferenceResolver, boolean qualificatorSupportedForMxmlBinding)
    throws InvalidPropertyException {
    final PsiElement element;
    List<String> qualifiers = null;
    JSExpression expressionQualifier = expression.getQualifier();
    JSReferenceExpression qualifier;
    if (expressionQualifier instanceof JSReferenceExpression) {
      qualifier = (JSReferenceExpression)expressionQualifier;
    }
    else if (expressionQualifier != null && !(expressionQualifier instanceof JSThisExpression)) {
      // we can skip "this."
      throw new IllegalArgumentException("unknown qualifier " + expressionQualifier.toString() + " " + expression.getText());
    }
    else {
      qualifier = null;
    }

    if (qualificatorSupportedForMxmlBinding && qualifier != null) {
      JSReferenceExpression topElement;
      qualifiers = new ArrayList<String>();
      do {
        qualifiers.add(qualifier.getReferencedName());
        topElement = qualifier;
      }
      while ((qualifier = (JSReferenceExpression)qualifier.getQualifier()) != null);
      element = resolveReferenceExpression(topElement, true);
    }
    else {
      element = resolveReferenceExpression(expression, false);
    }

    if (element instanceof JSClass) {
      // {VerticalAlign}
      if (qualifiers == null) {
        writer.classReference(((JSClass)element).getQualifiedName());
      }
      else {
        // check for {VerticalAlign.MIDDLE}
        PsiElement possibleVariable = resolveReferenceExpression(expression, true);
        if (possibleVariable instanceof JSVariable) {
          JSVariable variable = (JSVariable)possibleVariable;
          if (variable.isConst()) {
            JSExpression initializer = ((JSVariable)possibleVariable).getInitializer();
            if (initializer != null) {
              writeExpression(initializer, out, writer, valueReferenceResolver);
              return;
            }
            else {
              throw new InvalidPropertyException(expression, "const.without.initializer", expression.getText());
            }
          }
        }

        throw new UnsupportedOperationException(expression.getText());
      }
    }
    else if (element instanceof JSVariable) {
      checkQualifier(expression);
      VariableReference valueReference = valueReferenceResolver.getNullableValueReference((JSVariable)element);
      if (valueReference != null) {
        out.write(ExpressionMessageTypes.VARIABLE_REFERENCE);
        // may be already referenced, i.e. VariableReference created for this variable
        valueReference.write(out, writer, valueReferenceResolver);
      }
      else {
        writeJSVariable(((JSVariable)element), out, writer, valueReferenceResolver);
      }
    }
    else if (element instanceof JSFunction) {
      writeFunction((JSFunction)element, writer, valueReferenceResolver, true);
    }
    else {
      final String hostObjectId;
      if (qualifiers == null) {
        out.write(ExpressionMessageTypes.MXML_OBJECT_REFERENCE);
        hostObjectId = expression.getReferencedName();
      }
      else {
        out.write(ExpressionMessageTypes.MXML_OBJECT_CHAIN);
        writer.classOrPropertyName(expression.getReferencedName());
        for (int i = qualifiers.size() - 2 /* last qualifier is not included to chain, it is host object */; i >= 0; i--) {
          writer.classOrPropertyName(qualifiers.get(i));
        }
        writer.endObject();

        hostObjectId = qualifiers.get(qualifiers.size() - 1);
      }

      try {
        valueReferenceResolver.getValueReference(hostObjectId).write(out, writer, valueReferenceResolver);
      }
      catch (IllegalStateException e) {
        // getValueReference throws IllegalStateException if value reference is null
        throw new UnsupportedOperationException(expression.getText());
      }
    }
  }

  static void writeJSVariable(JSVariable variable, PrimitiveAmfOutputStream out, BaseWriter writer,
                              ValueReferenceResolver valueReferenceResolver) throws InvalidPropertyException {
    JSExpression initializer = variable.getInitializer();
    if (initializer == null) {
      MxmlWriter.LOG.warn("Unsupported variable without initializer: " + variable.getParent().getText() + ", write as null");
      out.write(Amf3Types.NULL);
    }
    else {
      writeExpression(initializer, out, writer, valueReferenceResolver);
    }
  }

  private static void writeLiteralExpression(JSLiteralExpression expression, PrimitiveAmfOutputStream out, BaseWriter writer) {
    if (expression.isNumericLiteral()) {
      out.writeAmfDouble(expression.getText());
    }
    else {
      final PsiElement firstChild = expression.getFirstChild();
      if (firstChild == null) {
        writer.string(XmlElementValueProvider.EMPTY);
        return;
      }

      final IElementType elementType = firstChild.getNode().getElementType();
      if (elementType == JSTokenTypes.TRUE_KEYWORD) {
        writer.getOut().write(Amf3Types.TRUE);
      }
      else if (elementType == JSTokenTypes.FALSE_KEYWORD) {
        writer.getOut().write(Amf3Types.FALSE);
      }
      else if (elementType == JSTokenTypes.NULL_KEYWORD) {
        writer.getOut().write(Amf3Types.NULL);
      }
      else {
        writer.string(StringUtil.stripQuotesAroundValue(expression.getText()));
      }
    }
  }

  private static void writeObjectExpression(JSObjectLiteralExpression expression, PrimitiveAmfOutputStream out, BaseWriter writer,
                                            ValueReferenceResolver valueReferenceResolver) throws InvalidPropertyException {
    JSProperty[] properties = expression.getProperties();
    out.write(ExpressionMessageTypes.SIMPLE_OBJECT);
    for (JSProperty property : properties) {
      writer.classOrPropertyName(property.getName());
      writeExpression(property.getValue(), out, writer, valueReferenceResolver);
    }
    writer.endObject();
  }
}
