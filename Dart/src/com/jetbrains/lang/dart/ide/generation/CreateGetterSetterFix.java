package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReturnType;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartPresentableUtil;

import java.util.List;

public class CreateGetterSetterFix extends BaseCreateMethodsFix<DartComponent> {
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

  public CreateGetterSetterFix(final DartClass dartClass, Strategy strategy) {
    super(dartClass);
    myStratagy = strategy;
  }

  @Override
  protected Template buildFunctionsText(TemplateManager templateManager, DartComponent namedComponent) {
    final DartReturnType returnType = PsiTreeUtil.getChildOfType(namedComponent, DartReturnType.class);
    final DartType dartType = PsiTreeUtil.getChildOfType(namedComponent, DartType.class);
    final String typeText = returnType == null ? DartPresentableUtil.buildTypeText(namedComponent, dartType, null)
                                               : DartPresentableUtil.buildTypeText(namedComponent, returnType, null);
    final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
    template.setToReformat(true);
    if (myStratagy == Strategy.GETTER || myStratagy == Strategy.GETTERSETTER) {
      buildGetter(template, namedComponent.getName(), typeText);
    }
    if (myStratagy == Strategy.SETTER || myStratagy == Strategy.GETTERSETTER) {
      buildSetter(template, namedComponent.getName(), typeText);
    }
    return template;
  }

  private static void buildGetter(Template template, String name, String typeText) {
    build(template, name, typeText, true);
  }

  private static void buildSetter(Template template, String name, String typeText) {
    build(template, name, typeText, false);
  }

  private static void build(Template template, String name, String typeText, boolean isGetter) {
    if (isGetter) {
      template.addTextSegment(typeText);
      template.addTextSegment(" ");
    }

    template.addTextSegment(isGetter ? "get" : "set");
    template.addTextSegment(" ");
    template.addEndVariable();
    template.addTextSegment(DartPresentableUtil.setterGetterName(name));
    if (!isGetter) {
      template.addTextSegment("(");
      template.addTextSegment(typeText);
      template.addTextSegment(" value");
      template.addTextSegment(")");
    }
    template.addTextSegment(" => ");
    if (isGetter) {
      template.addTextSegment(name);
      template.addTextSegment(";");
    }
    else {
      template.addTextSegment(name);
      template.addTextSegment("=value;\n");
    }
  }
}
