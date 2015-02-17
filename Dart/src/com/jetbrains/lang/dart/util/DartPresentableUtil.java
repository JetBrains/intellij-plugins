package com.jetbrains.lang.dart.util;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DartPresentableUtil {

  @NonNls public static final String RIGHT_ARROW = UIUtil.rightArrow();
  @NonNls private static final String SPACE = " ";

  public static String setterGetterName(String name) {
    return name.startsWith("_") ? name.substring(1) : name;
  }

  @NotNull
  public static String getPresentableParameterList(DartComponent element) {
    return getPresentableParameterList(element, new DartGenericSpecialization());
  }

  @NotNull
  public static String getPresentableParameterList(DartComponent element, DartGenericSpecialization specialization) {
    return getPresentableParameterList(element, specialization, false);
  }

  @NotNull
  public static String getPresentableParameterList(DartComponent element, DartGenericSpecialization specialization,
                                                   boolean functionalStyleSignatures) {
    final StringBuilder result = new StringBuilder();
    final DartFormalParameterList parameterList = PsiTreeUtil.getChildOfType(element, DartFormalParameterList.class);
    if (parameterList == null) {
      return "";
    }
    final List<DartNormalFormalParameter> list = parameterList.getNormalFormalParameterList();
    for (int i = 0, size = list.size(); i < size; i++) {
      result.append(getPresentableNormalFormalParameter(list.get(i), specialization, functionalStyleSignatures));
      if (i < size - 1) {
        result.append(", ");
      }
    }
    final DartNamedFormalParameters namedFormalParameters = parameterList.getNamedFormalParameters();
    if (namedFormalParameters != null) {
      if (!list.isEmpty()) {
        result.append(", ");
      }

      final boolean isOptional = isOptionalParameterList(namedFormalParameters);
      result.append(isOptional ? '{' : '[');

      List<DartDefaultFormalNamedParameter> list1 = namedFormalParameters.getDefaultFormalNamedParameterList();
      for (int i = 0, size = list1.size(); i < size; i++) {
        if (i > 0) {
          result.append(", ");
        }
        DartDefaultFormalNamedParameter formalParameter = list1.get(i);
        result.append(
          getPresentableNormalFormalParameter(formalParameter.getNormalFormalParameter(), specialization, functionalStyleSignatures));
      }
      result.append(isOptional ? '}' : ']');
    }
    return result.toString();
  }

  private static boolean isOptionalParameterList(final @NotNull DartNamedFormalParameters parameters) {
    // Workaround for the lack of distinction between named and optional params in the grammar
    final PsiElement firstChild = parameters.getFirstChild();
    return firstChild != null && "{".equals(firstChild.getText());
  }

  public static String getPresentableNormalFormalParameter(DartNormalFormalParameter parameter, DartGenericSpecialization specialization) {
    return getPresentableNormalFormalParameter(parameter, specialization, false);
  }

  public static String getPresentableNormalFormalParameter(DartNormalFormalParameter parameter, DartGenericSpecialization specialization,
                                                           boolean functionalStyleSignature) {
    final StringBuilder result = new StringBuilder();

    final DartFunctionSignature functionSignature = parameter.getFunctionSignature();
    final DartFieldFormalParameter fieldFormalParameter = parameter.getFieldFormalParameter();
    final DartSimpleFormalParameter simpleFormalParameter = parameter.getSimpleFormalParameter();

    if (functionSignature != null) {
      final DartReturnType returnType = functionSignature.getReturnType();
      if (!functionalStyleSignature && returnType != null) {
        result.append(buildTypeText(PsiTreeUtil.getParentOfType(parameter, DartComponent.class), returnType, specialization));
        result.append(SPACE);
      }
      result.append(functionSignature.getName());
      result.append("(");
      result.append(getPresentableParameterList(functionSignature, specialization, functionalStyleSignature));
      result.append(")");
      if (functionalStyleSignature && returnType != null) {
        result.append(SPACE);
        result.append(RIGHT_ARROW);
        result.append(SPACE);
        result.append(buildTypeText(PsiTreeUtil.getParentOfType(parameter, DartComponent.class), returnType, specialization));
      }
    }
    else if (fieldFormalParameter != null) {
      final DartType type = fieldFormalParameter.getType();
      if (type != null) {
        result.append(buildTypeText(PsiTreeUtil.getParentOfType(parameter, DartComponent.class), type, specialization));
        result.append(SPACE);
      }
      result.append(fieldFormalParameter.getReferenceExpression().getText());
    }
    else if (simpleFormalParameter != null) {
      final DartType type = simpleFormalParameter.getType();
      if (type != null) {
        result.append(buildTypeText(PsiTreeUtil.getParentOfType(parameter, DartComponent.class), type, specialization));
        result.append(SPACE);
      }
      result.append(simpleFormalParameter.getComponentName().getText());
    }

    return result.toString();
  }


  public static String buildTypeText(final @Nullable DartComponent element,
                                     final @Nullable DartReturnType returnType,
                                     final @Nullable DartGenericSpecialization specializations) {
    if (returnType == null) return "";
    return returnType.getNode().findChildByType(DartTokenTypes.VOID) == null
           ? buildTypeText(element, returnType.getType(), specializations)
           : "void";
  }

  public static String buildTypeText(final @Nullable DartComponent element,
                                     final @Nullable DartType type,
                                     final @Nullable DartGenericSpecialization specializations) {
    if (type == null) {
      return "";
    }
    final StringBuilder result = new StringBuilder();
    final String typeText = type.getReferenceExpression().getText();
    if (specializations != null && specializations.containsKey(element, typeText)) {
      final DartClass haxeClass = specializations.get(element, typeText).getDartClass();
      result.append(haxeClass == null ? typeText : haxeClass.getName());
    }
    else {
      result.append(typeText);
    }
    final DartTypeArguments typeArguments = type.getTypeArguments();
    if (typeArguments != null) {
      result.append("<");
      List<DartType> list = typeArguments.getTypeList().getTypeList();
      for (int i = 0; i < list.size(); i++) {
        if (i > 0) {
          result.append(", ");
        }
        DartType typeListPart = list.get(i);
        result.append(buildTypeText(element, typeListPart, specializations));
      }
      result.append(">");
    }
    return result.toString();
  }

  public static void appendArgumentList(@NotNull Template result, @NotNull DartArgumentList argumentList) {
    final List<DartNamedArgument> namedArgumentList = argumentList.getNamedArgumentList();
    final Set<String> additionalUsedNamed = new THashSet<String>();
    for (DartNamedArgument namedArgument : namedArgumentList) {
      additionalUsedNamed.add(namedArgument.getParameterReferenceExpression().getText());
    }

    boolean needComma = false;
    for (DartExpression expression : argumentList.getExpressionList()) {
      if (needComma) {
        result.addTextSegment(", ");
      }
      if (expression instanceof DartReference) {
        DartClass dartClass = ((DartReference)expression).resolveDartClass().getDartClass();
        if (dartClass != null) {
          final String name = dartClass.getName();
          if (name != null) {
            result.addTextSegment(name);
            result.addTextSegment(" ");
          }
        }
      }

      Collection<String> suggestedNames = DartNameSuggesterUtil.getSuggestedNames(expression, additionalUsedNamed);
      String parameterName = suggestedNames.iterator().next();
      additionalUsedNamed.add(parameterName);

      result.addVariable(getExpression(parameterName), true);

      needComma = true;
    }
    if (namedArgumentList.isEmpty()) {
      return;
    }
    if (needComma) {
      result.addTextSegment(", ");
      needComma = false;
    }
    result.addTextSegment("{");
    for (DartNamedArgument namedArgument : namedArgumentList) {
      if (needComma) {
        result.addTextSegment(", ");
      }

      DartExpression expression = namedArgument.getExpression();
      if (expression instanceof DartReference) {
        DartClass dartClass = ((DartReference)expression).resolveDartClass().getDartClass();
        if (dartClass != null) {
          final String name = dartClass.getName();
          if (name != null) {
            result.addTextSegment(name);
            result.addTextSegment(SPACE);
          }
        }
      }

      result.addVariable(getExpression(namedArgument.getParameterReferenceExpression().getText()), true);
      needComma = true;
    }
    result.addTextSegment("}");
  }

  public static Expression getExpression(String parameterName) {
    return new DartTemplateExpression(parameterName);
  }

  public static String buildClassText(@NotNull DartClass dartClass, DartGenericSpecialization specialization) {
    StringBuilder result = new StringBuilder();
    result.append(dartClass.getName());
    DartTypeParameters typeParameters = PsiTreeUtil.getChildOfType(dartClass, DartTypeParameters.class);
    if (typeParameters != null) {
      result.append("<");
      for (DartTypeParameter typeParameter : typeParameters.getTypeParameterList()) {
        DartComponentName componentName = typeParameter.getComponentName();
        String typeParamName = componentName.getText();
        DartClassResolveResult resolveResult = specialization.get(dartClass, typeParamName);
        DartClass paramDartClass = resolveResult.getDartClass();
        if (paramDartClass == null) {
          result.append(typeParamName);
        }
        else {
          result.append(buildClassText(paramDartClass, resolveResult.getSpecialization()));
        }
      }
      result.append(">");
    }
    return result.toString();
  }

  private static class DartTemplateExpression extends Expression {
    private final TextResult myResult;

    public DartTemplateExpression(String text) {
      myResult = new TextResult(text);
    }

    @Nullable
    @Override
    public Result calculateResult(ExpressionContext context) {
      return myResult;
    }

    @Nullable
    @Override
    public Result calculateQuickResult(ExpressionContext context) {
      return myResult;
    }

    @Nullable
    @Override
    public LookupElement[] calculateLookupItems(ExpressionContext context) {
      return LookupElement.EMPTY_ARRAY;
    }
  }

  @Nullable
  public static String findLastQuotedWord(@NotNull String text) {
    return findLastQuotedWord(text, '\'');
  }

  @Nullable
  public static String findLastDoubleQuotedWord(@NotNull String text) {
    return findLastQuotedWord(text, '"');
  }

  @Nullable
  public static String findLastQuotedWord(@NotNull String text, char quote) {
    int j = text.lastIndexOf(quote);
    if (j == -1) {
      return null;
    }
    text = text.substring(0, j);
    int i = text.lastIndexOf(quote);
    if (i == -1) {
      return null;
    }
    return text.substring(i + 1);
  }

  @Nullable
  public static String findFirstQuotedWord(@NotNull String text) {
    return findFirstQuotedWord(text, '\'');
  }

  @Nullable
  public static String findFirstQuotedWord(@NotNull String text, char quote) {
    int i = text.indexOf(quote);
    if (i == -1) {
      return null;
    }
    text = text.substring(i + 1);
    int j = text.indexOf(quote);
    if (j == -1) {
      return null;
    }
    return text.substring(0, j);
  }
}
