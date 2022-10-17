// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.DroolsConstants;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.plugins.drools.lang.psi.util.DroolsElementsFactory;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.light.JavaIdentifier;
import com.intellij.psi.impl.light.LightMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PropertyUtilBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.IncorrectOperationException;
import java.util.HashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class DroolsPsiClassImpl extends DroolsPsiCompositeElementImpl implements DroolsPsiClass, DroolsTypeDeclaration {

  public DroolsPsiClassImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String getName() {
    return getTypeName().getText();
  }

  @Nullable
  @Override
  public String getQualifiedName() {
    String typeName = getTypeName().getText();
    if (StringUtil.getPackageName(typeName).isEmpty()) {
      String aPackage = DroolsResolveUtil.getCurrentPackage(PsiTreeUtil.getParentOfType(this, DroolsFile.class));
      typeName =  aPackage +"." +typeName;
    }
    return typeName;
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  public boolean isAnnotationType() {
    return false;
  }

  @Override
  public boolean isEnum() {
    return false;
  }

  @Nullable
  @Override
  public PsiReferenceList getExtendsList() {
    return null;
  }

  @Nullable
  @Override
  public PsiReferenceList getImplementsList() {
    return null;
  }

  @Override
  public PsiClassType @NotNull [] getExtendsListTypes() {
    PsiClass superClass = getSuperClass();
    return superClass == null ? PsiClassType.EMPTY_ARRAY : new PsiClassType[]{PsiTypesUtil.getClassType(superClass)};
  }

  @Override
  public PsiClassType @NotNull [] getImplementsListTypes() {
    if (isTraitable()) {
      final PsiClass aClass = JavaPsiFacade.getInstance(getProject()).findClass(DroolsConstants.TRAITS_THING, getResolveScope());
      if (aClass != null) {
        return new PsiClassType[]{PsiTypesUtil.getClassType(aClass)};
      }
    }
    return PsiClassType.EMPTY_ARRAY;
  }

  @Nullable
  @Override
  public PsiClass getSuperClass() {
    DroolsSuperType superType = getSuperType();
    if (superType != null) {
      Set<PsiClass> psiClasses = DroolsResolveUtil.resolveQualifiedIdentifier(superType.getQualifiedName().getQualifiedIdentifier());
      if (psiClasses.size() > 0) {
        return psiClasses.iterator().next();
      }
    }
    return null;
  }

  @Override
  public PsiClass @NotNull [] getInterfaces() {
    if (isTraitable()) {
      final PsiClass aClass = JavaPsiFacade.getInstance(getProject()).findClass(DroolsConstants.TRAITS_THING, getResolveScope());

      if (aClass != null) {
        return new PsiClass[]{aClass};
      }
    }

    return PsiClass.EMPTY_ARRAY;
  }

  @Override
  public PsiClass @NotNull [] getSupers() {
    return PsiClassImplUtil.getSupers(this);
  }

  @Override
  public PsiClassType @NotNull [] getSuperTypes() {
    return PsiClassImplUtil.getSuperTypes(this);
  }

  @Override
  public PsiField @NotNull [] getFields() {
    return findChildrenByClass(DroolsField.class);
  }

  @Override
  public PsiMethod @NotNull [] getMethods() {
    Set<PsiMethod> generatedMethods = new HashSet<>();
    PsiField[] fields = getFields();
    for (final PsiField field : fields) {
      generatedMethods.add(new GeneratedLightMethod(getManager(), PropertyUtilBase.generateGetterPrototype(field), field));
      generatedMethods.add(new GeneratedLightMethod(getManager(), PropertyUtilBase.generateSetterPrototype(field), field)) ;
    }

    return generatedMethods.toArray(PsiMethod.EMPTY_ARRAY);
  }

  @Override
  public PsiMethod @NotNull [] getConstructors() {
    return PsiMethod.EMPTY_ARRAY;
  }

  @Override
  public PsiClass @NotNull [] getInnerClasses() {
    return PsiClass.EMPTY_ARRAY;
  }

  @Override
  public PsiClassInitializer @NotNull [] getInitializers() {
    return PsiClassInitializer.EMPTY_ARRAY;
  }

  @Override
  public PsiField @NotNull [] getAllFields() {
    return PsiClassImplUtil.getAllFields(this);
  }

  @Override
  public PsiMethod @NotNull [] getAllMethods() {
    return PsiClassImplUtil.getAllMethods(this);
  }

  @Override
  public PsiClass @NotNull [] getAllInnerClasses() {
    return PsiClass.EMPTY_ARRAY;
  }

  @Nullable
  @Override
  public PsiField findFieldByName(@NonNls String name, boolean checkBases) {
    return PsiClassImplUtil.findFieldByName(this, name, checkBases);
  }

  @Nullable
  @Override
  public PsiMethod findMethodBySignature(@NotNull PsiMethod patternMethod, boolean checkBases) {
    return PsiClassImplUtil.findMethodBySignature(this, patternMethod, checkBases);
  }

  @Override
  public PsiMethod @NotNull [] findMethodsBySignature(@NotNull PsiMethod patternMethod, boolean checkBases) {
    return PsiClassImplUtil.findMethodsBySignature(this, patternMethod, checkBases);
  }

  @Override
  public PsiMethod @NotNull [] findMethodsByName(@NonNls String name, boolean checkBases) {
    return PsiClassImplUtil.findMethodsByName(this, name, checkBases);
  }

  @NotNull
  @Override
  public List<Pair<PsiMethod, PsiSubstitutor>> findMethodsAndTheirSubstitutorsByName(@NonNls @NotNull String name, boolean checkBases) {
    return PsiClassImplUtil.findMethodsAndTheirSubstitutorsByName(this, name, checkBases);
  }

  @NotNull
  @Override
  public List<Pair<PsiMethod, PsiSubstitutor>> getAllMethodsAndTheirSubstitutors() {
    return PsiClassImplUtil.getAllWithSubstitutorsByMap(this, PsiClassImplUtil.MemberType.METHOD);
  }

  @Nullable
  @Override
  public PsiClass findInnerClassByName(@NonNls String name, boolean checkBases) {
    return null;
  }

  @Nullable
  @Override
  public PsiElement getLBrace() {
    return null;
  }

  @Nullable
  @Override
  public PsiElement getRBrace() {
    return null;
  }

  @Nullable
  @Override
  public PsiIdentifier getNameIdentifier() {
    return new JavaIdentifier(getManager(), getTypeName());
  }

  @Override
  public PsiElement getScope() {
    return null;
  }

  @Override
  public boolean isInheritor(@NotNull PsiClass baseClass, boolean checkDeep) {
    if (isTraitable()) {
      return DroolsConstants.TRAITS_THING.equals(baseClass.getQualifiedName());
    }
    return false;
  }

  @Override
  public boolean isInheritorDeep(@NotNull PsiClass baseClass, @Nullable PsiClass classToByPass) {
    return false;
  }

  @Nullable
  @Override
  public PsiClass getContainingClass() {
    return null;
  }

  @NotNull
  @Override
  public Collection<HierarchicalMethodSignature> getVisibleSignatures() {
    return Collections.emptySet();
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    final DroolsTypeName oldIdentifier = getTypeName();

    final PsiElement nameIdentifier = DroolsElementsFactory.createDeclaredTypeNameIdentifier(name, getProject());
    if (nameIdentifier != null) {
      oldIdentifier.replace(nameIdentifier);
    }
    return this;
  }

  @Nullable
  @Override
  public PsiDocComment getDocComment() {
    return null;
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @Override
  public boolean hasTypeParameters() {
    return false;
  }

  @Nullable
  @Override
  public PsiTypeParameterList getTypeParameterList() {
    return null;
  }

  @Override
  public PsiTypeParameter @NotNull [] getTypeParameters() {
    return PsiTypeParameter.EMPTY_ARRAY;
  }

  @Nullable
  @Override
  public PsiModifierList getModifierList() {
    return null;
  }

  @Override
  public boolean hasModifierProperty(@PsiModifier.ModifierConstant @NonNls @NotNull String name) {
    return false;
  }

  public boolean isTraitable() {
    for (DroolsAnnotation annotation : getAnnotationList()) {
      if ("Traitable".equals(annotation.getIdentifier().getText())) return true;
    }

    return getTraitable() != null;
  }

  public class GeneratedLightMethod extends LightMethod {
    private final PsiField myField;

    public GeneratedLightMethod(PsiManagerEx manager, PsiMethod method, PsiField field) {
      super(manager, method, DroolsPsiClassImpl.this);
      myField = field;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
      return myField;
    }
  }
}
