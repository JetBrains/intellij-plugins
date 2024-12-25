// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.*;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDartPsiClass extends AbstractDartComponentImpl implements DartClass {

  private CachedValue<Map<String, List<DartComponent>>> myMembersCache;

  public AbstractDartPsiClass(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public boolean isEnum() {
    return this instanceof DartEnumDefinition;
  }

  @Override
  public @NotNull List<DartEnumConstantDeclaration> getEnumConstantDeclarationList() {
    return Collections.emptyList();
  }

  @Override
  public @Nullable DartTypeParameters getTypeParameters() {
    return null;
  }

  @Override
  public @NotNull DartClassResolveResult getSuperClassResolvedOrObjectClass() {
    if (DartResolveUtil.OBJECT.equals(getName())) return DartClassResolveResult.EMPTY;

    final DartType superClass = getSuperClass();
    return superClass != null ? DartResolveUtil.resolveClassByType(superClass)
                              : DartResolveUtil.findCoreClass(this, DartResolveUtil.OBJECT);
  }

  @Override
  public @Nullable DartType getSuperClass() {
    final DartSuperclass superclass = PsiTreeUtil.getChildOfType(this, DartSuperclass.class);
    if (superclass != null) return superclass.getType();

    final DartMixinApplication mixinApp = PsiTreeUtil.getChildOfType(this, DartMixinApplication.class);
    if (mixinApp != null) return mixinApp.getType();

    return null;
  }

  @Override
  public @NotNull List<DartType> getImplementsList() {
    final DartMixinApplication mixinApp = PsiTreeUtil.getChildOfType(this, DartMixinApplication.class);
    final DartInterfaces interfaces = mixinApp != null ? mixinApp.getInterfaces() : PsiTreeUtil.getChildOfType(this, DartInterfaces.class);
    if (interfaces != null) return DartResolveUtil.getTypes(interfaces.getTypeList());

    return Collections.emptyList();
  }

  @Override
  public @NotNull List<DartType> getMixinsList() {
    final DartMixinApplication mixinApp = PsiTreeUtil.getChildOfType(this, DartMixinApplication.class);

    final DartMixins mixins = PsiTreeUtil.getChildOfType(mixinApp != null ? mixinApp : this, DartMixins.class);
    if (mixins != null) {
      return DartResolveUtil.getTypes(mixins.getTypeList());
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isGeneric() {
    return getTypeParameters() != null;
  }

  @Override
  public @NotNull List<DartComponent> getMethods() {
    final List<DartComponent> components = DartResolveUtil.findNamedSubComponents(this);
    return DartResolveUtil.filterComponentsByType(components, DartComponentType.METHOD);
  }

  @Override
  public @NotNull List<DartComponent> getFields() {
    final List<DartComponent> components = DartResolveUtil.findNamedSubComponents(this);
    return DartResolveUtil.filterComponentsByType(components, DartComponentType.FIELD);
  }

  @Override
  public @NotNull List<DartComponent> getConstructors() {
    final List<DartComponent> components = DartResolveUtil.getNamedSubComponents(this);
    final String className = getName();
    if (className == null) {
      return Collections.emptyList();
    }
    return ContainerUtil.filter(components, component -> DartComponentType.typeOf(component) == DartComponentType.CONSTRUCTOR);
  }

  @Override
  public List<DartMethodDeclaration> getOperators() {
    return DartResolveUtil.findOperators(this);
  }

  @Override
  public @Nullable DartMethodDeclaration findOperator(final String operator, final @Nullable DartClass rightDartClass) {
    return ContainerUtil.find(getOperators(), (Condition<PsiElement>)element -> {
      if (element instanceof DartMethodDeclaration method) {
        if (method.isOperator() && operator.equals(method.getName())) {
          if (rightDartClass == null) {
            return true;
          }
          final DartFormalParameterList formalParameterList = PsiTreeUtil.getChildOfType(element, DartFormalParameterList.class);
          return DartResolveUtil.checkParametersType(formalParameterList, rightDartClass);
        }
      }
      return false;
    });
  }

  @Override
  public DartComponent findFieldByName(final @NotNull String name) {
    return ContainerUtil.find(getFields(), component -> name.equals(component.getName()));
  }

  @Override
  public DartComponent findMethodByName(final @NotNull String name) {
    return ContainerUtil.find(getMethods(), component -> name.equals(component.getName()));
  }

  @Override
  public DartComponent findMemberByName(@NotNull String name) {
    final List<DartComponent> membersByName = findMembersByName(name);
    return membersByName.isEmpty() ? null : membersByName.iterator().next();
  }

  @Override
  public @NotNull List<DartComponent> findMembersByName(final @NotNull String name) {
    ensureMembersCacheInitialized();
    final List<DartComponent> components = myMembersCache.getValue().get(name);
    return components == null ? Collections.emptyList() : components;
  }

  private void ensureMembersCacheInitialized() {
    if (myMembersCache == null) {
      myMembersCache = CachedValuesManager.getManager(getProject()).createCachedValue(
        () -> {
          final Map<String, List<DartComponent>> nameToMembers = new HashMap<>();

          for (DartComponent component : DartResolveUtil.findNamedSubComponents(false, this)) {
            final String componentName = component.getName();

            final DartClass dartClass = PsiTreeUtil.getParentOfType(component, DartClass.class);
            final String dartClassName = dartClass != null ? dartClass.getName() : null;
            if (dartClassName != null && dartClassName.equals(componentName)) {
              continue;
            }

            List<DartComponent> components = nameToMembers.computeIfAbsent(componentName, k -> new SmartList<>());
            components.add(component);
          }

          return new CachedValueProvider.Result<>(nameToMembers, PsiModificationTracker.MODIFICATION_COUNT);
        }, false);
    }
  }

  @Override
  public DartComponent findNamedConstructor(final String name) {
    return ContainerUtil.find(getConstructors(), component -> name.equals(component.getName()));
  }
}
