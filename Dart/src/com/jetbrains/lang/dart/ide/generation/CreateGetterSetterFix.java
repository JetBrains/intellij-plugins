// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReturnType;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class CreateGetterSetterFix extends BaseCreateMethodsFix<DartComponent> {
  public enum Strategy {
    GETTER(DartBundle.messagePointer("dart.fix.getter.none.found")) {
      @Override
      boolean accept(final String name, List<DartComponent> componentList) {
        return name.startsWith("_") &&
               ContainerUtil.find(componentList, component -> component.isGetter() &&
                                                              DartPresentableUtil.setterGetterName(name).equals(component.getName())) ==
               null;
      }
    },

    SETTER(DartBundle.messagePointer("dart.fix.setter.none.found")) {
      @Override
      boolean accept(final String name, List<DartComponent> componentList) {
        return name.startsWith("_") &&
               ContainerUtil.find(componentList, component -> component.isSetter() &&
                                                              DartPresentableUtil.setterGetterName(name).equals(component.getName())) ==
               null;
      }
    },

    GETTER_SETTER(DartBundle.messagePointer("dart.fix.getter.setter.none.found")) {
      @Override
      boolean accept(final String name, List<DartComponent> componentList) {
        return name.startsWith("_") &&
               ContainerUtil.find(componentList, component -> (component.isGetter() || component.isSetter()) &&
                                                              DartPresentableUtil.setterGetterName(name).equals(component.getName())) ==
               null;
      }
    };

    private final Supplier<@Nls String> myNothingFoundMessageSupplier;

    Strategy(Supplier<@Nls String> nothingFoundMessageSupplier) {
      myNothingFoundMessageSupplier = nothingFoundMessageSupplier;
    }

    abstract boolean accept(String name, List<DartComponent> componentList);
  }

  private final @NotNull Strategy myStrategy;

  public CreateGetterSetterFix(@NotNull DartClass dartClass, @NotNull Strategy strategy) {
    super(dartClass);
    myStrategy = strategy;
  }

  @Override
  protected @NotNull @NlsContexts.Command String getCommandName() {
    return switch (myStrategy) {
      case GETTER -> DartBundle.message("command.generate.getters");
      case SETTER -> DartBundle.message("command.generate.setters");
      case GETTER_SETTER -> DartBundle.message("command.generate.getters.and.setters");
    };
  }

  @Override
  protected @NotNull String getNothingFoundMessage() {
    return myStrategy.myNothingFoundMessageSupplier.get();
  }

  @Override
  protected Template buildFunctionsText(final TemplateManager templateManager, final DartComponent namedComponent) {
    final DartReturnType returnType = PsiTreeUtil.getChildOfType(namedComponent, DartReturnType.class);
    final DartType dartType = PsiTreeUtil.getChildOfType(namedComponent, DartType.class);
    final String typeText = returnType == null
                            ? DartPresentableUtil.buildTypeText(namedComponent, dartType, null)
                            : DartPresentableUtil.buildTypeText(namedComponent, returnType, null);
    final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
    template.setToReformat(true);
    if (myStrategy == Strategy.GETTER || myStrategy == Strategy.GETTER_SETTER) {
      buildGetter(template, namedComponent.getName(), typeText, namedComponent.isStatic());
    }
    if (myStrategy == Strategy.SETTER || myStrategy == Strategy.GETTER_SETTER) {
      buildSetter(template, namedComponent.getName(), typeText, namedComponent.isStatic());
    }
    return template;
  }

  private static void buildGetter(final Template template, final String name, final String typeText, final boolean isStatic) {
    build(template, name, typeText, isStatic, true);
  }

  private static void buildSetter(final Template template, final String name, final String typeText, final boolean isStatic) {
    build(template, name, typeText, isStatic, false);
  }

  private static void build(final Template template,
                            final String name,
                            final String typeText,
                            final boolean isStatic,
                            final boolean isGetter) {
    if (isStatic) {
      template.addTextSegment("static");
      template.addTextSegment(" ");
    }

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
    if (isGetter) {
      template.addTextSegment(" => ");
      template.addTextSegment(name);
      template.addTextSegment("; "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed
    }
    else {
      template.addTextSegment("{ ");
      template.addTextSegment(name);
      template.addTextSegment(" = value; "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed
      template.addTextSegment(" } "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed
    }
  }
}
