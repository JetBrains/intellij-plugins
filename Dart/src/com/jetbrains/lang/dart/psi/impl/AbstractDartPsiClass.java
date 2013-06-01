package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
abstract public class AbstractDartPsiClass extends AbstractDartComponentImpl implements DartClass {
  public AbstractDartPsiClass(@NotNull ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public DartType getSuperClass() {
    final DartSuperclass superclass = PsiTreeUtil.getChildOfType(this, DartSuperclass.class);
    return superclass == null ? null : superclass.getType();
  }

  @NotNull
  @Override
  public List<DartType> getImplementsList() {
    final DartInterfaces interfaces = PsiTreeUtil.getChildOfType(this, DartInterfaces.class);
    if (interfaces != null) {
      return DartResolveUtil.getTypes(interfaces.getTypeList());
    }
    final DartSuperinterfaces superinterfaces = PsiTreeUtil.getChildOfType(this, DartSuperinterfaces.class);
    if (superinterfaces != null) {
      return DartResolveUtil.getTypes(superinterfaces.getTypeList());
    }
    return Collections.emptyList();
  }

  @NotNull
  @Override
  public List<DartType> getMixinsList() {
    final DartMixins mixins = PsiTreeUtil.getChildOfType(this, DartMixins.class);
    if (mixins != null) {
      return DartResolveUtil.getTypes(mixins.getTypeList());
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isInterface() {
    return DartComponentType.typeOf(this) == DartComponentType.INTERFACE;
  }

  @Override
  public boolean isGeneric() {
    return getTypeParameters() != null;
  }

  @NotNull
  @Override
  public List<DartComponent> getMethods() {
    final List<DartComponent> components = DartResolveUtil.findNamedSubComponents(this);
    return DartResolveUtil.filterComponentsByType(components, DartComponentType.METHOD);
  }

  @NotNull
  @Override
  public List<DartComponent> getFields() {
    final List<DartComponent> components = DartResolveUtil.findNamedSubComponents(this);
    return DartResolveUtil.filterComponentsByType(components, DartComponentType.FIELD);
  }

  @NotNull
  @Override
  public List<DartComponent> getConstructors() {
    final List<DartComponent> components = DartResolveUtil.getNamedSubComponents(this);
    final String className = getName();
    if (className == null) {
      return Collections.emptyList();
    }
    return ContainerUtil.filter(components, new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return DartComponentType.typeOf(component) == DartComponentType.CONSTRUCTOR;
      }
    });
  }

  @Override
  public List<DartOperator> getOperators() {
    return DartResolveUtil.findOperators(this);
  }

  @Nullable
  @Override
  public DartOperator findOperator(final String operator, @Nullable final DartClass rightDartClass) {
    return ContainerUtil.find(getOperators(), new Condition<PsiElement>() {
      @Override
      public boolean value(PsiElement element) {
        final DartUserDefinableOperator userDefinableOperator = PsiTreeUtil.getChildOfType(element, DartUserDefinableOperator.class);
        final boolean isGoodOperator = userDefinableOperator != null &&
                                       operator.equals(DartResolveUtil.getOperatorString(userDefinableOperator));
        if (rightDartClass == null) {
          return isGoodOperator;
        }
        final DartFormalParameterList formalParameterList = PsiTreeUtil.getChildOfType(element, DartFormalParameterList.class);
        return isGoodOperator && DartResolveUtil.checkParametersType(formalParameterList, rightDartClass);
      }
    });
  }

  @Override
  public DartComponent findFieldByName(@NotNull final String name) {
    return ContainerUtil.find(getFields(), new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return name.equals(component.getName());
      }
    });
  }

  @Override
  public DartComponent findMethodByName(@NotNull final String name) {
    return ContainerUtil.find(getMethods(), new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return name.equals(component.getName());
      }
    });
  }

  @Override
  public DartComponent findMemberByName(@NotNull String name) {
    final List<DartComponent> membersByName = findMembersByName(name);
    return membersByName.isEmpty() ? null : membersByName.iterator().next();
  }

  @NotNull
  @Override
  public List<DartComponent> findMembersByName(@NotNull final String name) {
    return ContainerUtil.filter(DartResolveUtil.findNamedSubComponents(false, this), new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        final DartClass dartClass = PsiTreeUtil.getParentOfType(component, DartClass.class);
        final String dartClassName = dartClass != null ? dartClass.getName() : null;
        if (dartClassName != null && dartClassName.equals(component.getName())) {
          return false;
        }
        return name.equals(component.getName());
      }
    });
  }

  @Override
  public DartComponent findNamedConstructor(final String name) {
    return ContainerUtil.find(getConstructors(), new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return name.equals(component.getName());
      }
    });
  }
}
