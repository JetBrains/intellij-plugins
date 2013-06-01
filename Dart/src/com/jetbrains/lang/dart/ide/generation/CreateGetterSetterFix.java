package com.jetbrains.lang.dart.ide.generation;

import com.intellij.openapi.util.Condition;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReturnType;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartPresentableUtil;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class CreateGetterSetterFix extends BaseCreateMethodsFix {
  public enum Strategy {
    GETTER {
      @Override
      boolean accept(final String name, List<DartComponent> componentList) {
        return name.startsWith("_") && ContainerUtil.find(componentList, new Condition<DartComponent>() {
          @Override
          public boolean value(DartComponent component) {
            return component.isGetter() && DartPresentableUtil.setterGetterName(name).equals(component.getName());
          }
        }) == null;
      }
    }, SETTER {
      @Override
      boolean accept(final String name, List<DartComponent> componentList) {
        return name.startsWith("_") && ContainerUtil.find(componentList, new Condition<DartComponent>() {
          @Override
          public boolean value(DartComponent component) {
            return component.isSetter() && DartPresentableUtil.setterGetterName(name).equals(component.getName());
          }
        }) == null;
      }
    }, GETTERSETTER {
      @Override
      boolean accept(final String name, List<DartComponent> componentList) {
        return name.startsWith("_") && ContainerUtil.find(componentList, new Condition<DartComponent>() {
          @Override
          public boolean value(DartComponent component) {
            return (component.isGetter() || component.isSetter()) && DartPresentableUtil.setterGetterName(name).equals(component.getName());
          }
        }) == null;
      }
    };

    abstract boolean accept(String name, List<DartComponent> componentList);
  }

  private final Strategy myStratagy;

  public CreateGetterSetterFix(final DartClass haxeClass, Strategy strategy) {
    super(haxeClass);
    myStratagy = strategy;
  }

  @Override
  protected String buildFunctionsText(DartComponent namedComponent) {
    final DartReturnType returnType = PsiTreeUtil.getChildOfType(namedComponent, DartReturnType.class);
    final DartType dartType = returnType == null ? PsiTreeUtil.getChildOfType(namedComponent, DartType.class) : returnType.getType();
    final String typeText = DartPresentableUtil.buildTypeText(namedComponent, dartType);
    final StringBuilder result = new StringBuilder();
    if (myStratagy == Strategy.GETTER || myStratagy == Strategy.GETTERSETTER) {
      buildGetter(result, namedComponent.getName(), typeText);
    }
    if (myStratagy == Strategy.SETTER || myStratagy == Strategy.GETTERSETTER) {
      buildSetter(result, namedComponent.getName(), typeText);
    }
    return result.toString();
  }

  private static void buildGetter(StringBuilder result, String name, String typeText) {
    build(result, name, typeText, true);
  }

  private static void buildSetter(StringBuilder result, String name, String typeText) {
    build(result, name, typeText, false);
  }

  private static void build(StringBuilder result, String name, String typeText, boolean isGetter) {
    if (isGetter) {
      result.append(typeText);
      result.append(" ");
    }

    result.append(isGetter ? "get" : "set");
    result.append(" ");
    result.append(DartPresentableUtil.setterGetterName(name));
    if (!isGetter) {
      result.append("(");
      result.append(typeText);
      result.append(" value");
      result.append(")");
    }
    result.append(" => ");
    if (isGetter) {
      result.append(name);
      result.append(";");
    }
    else {
      result.append(name);
      result.append("=value;");
    }
    result.append("\n");
  }
}
