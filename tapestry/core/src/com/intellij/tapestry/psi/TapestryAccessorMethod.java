package com.intellij.tapestry.psi;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.impl.PsiSuperMethodImplUtil;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.impl.light.LightEmptyImplementsList;
import com.intellij.psi.impl.light.LightModifierList;
import com.intellij.psi.impl.light.LightVariableBase;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Alexey Chmutov
 */
public class TapestryAccessorMethod extends LightElement implements PsiMethod {
  private static final Logger LOG = Logger.getInstance(TapestryAccessorMethod.class);

  private final PsiField myProperty;

  private final boolean myGetterNotSetter;
  private final String myName;
  private LightParameterList myParameterList;
  private final PsiModifierList myModifierList;

  protected TapestryAccessorMethod(PsiField property, boolean getterNotSetter, @NotNull String name) {
    super(property.getManager(), JavaLanguage.INSTANCE);
    myProperty = property;
    myGetterNotSetter = getterNotSetter;
    myName = name;
    myModifierList = new LightModifierList(getManager()) {
      @Override
      public boolean hasModifierProperty(@NotNull String name) {
        return PsiModifier.PUBLIC.equals(name);
      }

      @Override
      public boolean hasExplicitModifier(@NotNull String name) {
        return PsiModifier.PUBLIC.equals(name);
      }

      @Override
      public String getText() {
        return PsiModifier.PUBLIC;
      }
    };
  }

  @Override
  @NotNull
  public PsiElement getNavigationElement() {
    return getProperty();
  }

  public PsiField getProperty() {
    return myProperty;
  }

  public boolean isGetter() {
    return myGetterNotSetter;
  }

  @Override
  public PsiDocComment getDocComment() {
    return null;
  }

  @Override
  public boolean hasTypeParameters() {
    return false;
  }

  @Override
  @Nullable
  public PsiTypeParameterList getTypeParameterList() {
    return null;
  }

  @Override
  public PsiTypeParameter @NotNull [] getTypeParameters() {
    return PsiTypeParameter.EMPTY_ARRAY;
  }

  @Override
  public PsiClass getContainingClass() {
    return myProperty.getContainingClass();
  }

  @Override
  public PsiType getReturnType() {
    return myGetterNotSetter ? myProperty.getType() : PsiType.VOID;
  }

  @Override
  public PsiTypeElement getReturnTypeElement() {
    return myProperty.getTypeElement();
  }

  @Override
  @NotNull
  public PsiParameterList getParameterList() {
    if (myParameterList == null) {
      myParameterList = new LightParameterList(getManager(), () -> myGetterNotSetter
             ? EMPTY_PARAMETERS_ARRAY
             : new LightParameter[]{
               new LightParameter(getManager(), myProperty.getName(), null, myProperty.getType(), this)});
    }
    return myParameterList;
  }

  @Override
  @Nullable
  public PsiIdentifier getNameIdentifier() {
    return myProperty.getNameIdentifier();
  }

  @Override
  @NotNull
  public PsiModifierList getModifierList() {
    return myModifierList;
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    return null;
  }

  @Override
  @NotNull
  public HierarchicalMethodSignature getHierarchicalMethodSignature() {
    return PsiSuperMethodImplUtil.getHierarchicalMethodSignature(this);
  }

  @Override
  public String toString() {
    return "AccessorMethod";
  }

  @Override
  @NotNull
  public PsiReferenceList getThrowsList() {
    return new LightEmptyImplementsList(getManager());
  }

  @Override
  @Nullable
  public PsiCodeBlock getBody() {
    return null;
  }

  @Override
  public boolean isConstructor() {
    return false;
  }

  @Override
  public boolean isVarArgs() {
    return false;
  }

  @Override
  @NotNull
  public MethodSignature getSignature(@NotNull PsiSubstitutor substitutor) {
    return MethodSignatureBackedByPsiMethod.create(this, substitutor);
  }

  @Override
  public PsiMethod @NotNull [] findSuperMethods() {
    return PsiMethod.EMPTY_ARRAY;
  }

  @Override
  public PsiMethod @NotNull [] findSuperMethods(boolean checkAccess) {
    return PsiMethod.EMPTY_ARRAY;
  }

  @Override
  public PsiMethod @NotNull [] findSuperMethods(PsiClass parentClass) {
    return PsiMethod.EMPTY_ARRAY;
  }

  @Override
  @NotNull
  public List<MethodSignatureBackedByPsiMethod> findSuperMethodSignaturesIncludingStatic(boolean checkAccess) {
    return Collections.emptyList();
  }

  @Override
  public PsiMethod @NotNull [] findDeepestSuperMethods() {
    return PsiMethod.EMPTY_ARRAY;
  }

  @Override
  @Nullable
  public PsiMethod findDeepestSuperMethod() {
    return null;
  }

  @Override
  public boolean hasModifierProperty(@NonNls @NotNull String name) {
    return getModifierList().hasModifierProperty(name);
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @Override
  @NotNull
  public String getName() {
    return myName;
  }

  private static final LightParameter[] EMPTY_PARAMETERS_ARRAY = new LightParameter[0];

  public static class LightParameter extends LightVariableBase implements PsiParameter {
    private final String myName;

    public LightParameter(PsiManager manager, String name, PsiIdentifier nameIdentifier, @NotNull PsiType type, PsiElement scope) {
      super(manager, nameIdentifier, type, false, scope);
      myName = name;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
      if (visitor instanceof JavaElementVisitor) {
        ((JavaElementVisitor)visitor).visitParameter(this);
      }
      else {
        visitor.visitElement(this);
      }
    }

    @Override
    public String toString() {
      return "Light Parameter";
    }

    @Override
    public boolean isVarArgs() {
      return false;
    }

    @Override
    @NotNull
    public String getName() {
      return StringUtil.notNullize(myName);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TapestryAccessorMethod)) return false;
    TapestryAccessorMethod method = (TapestryAccessorMethod)o;
    return myGetterNotSetter == method.myGetterNotSetter &&
           myProperty.equals(method.myProperty) &&
           myName.equals(method.myName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myProperty, myGetterNotSetter, myName);
  }

  public static class LightParameterList extends LightElement implements PsiParameterList {
    private final Computable<LightParameter[]> myParametersComputation;
    private LightParameter[] myParameters = null;

    protected LightParameterList(PsiManager manager, Computable<LightParameter[]> parametersComputation) {
      super(manager, JavaLanguage.INSTANCE);
      myParametersComputation = parametersComputation;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
      if (visitor instanceof JavaElementVisitor) {
        ((JavaElementVisitor)visitor).visitParameterList(this);
      }
      else {
        visitor.visitElement(this);
      }
    }

    @Override
    public PsiParameter @NotNull [] getParameters() {
      if (myParameters == null) {
        myParameters = myParametersComputation.compute();
      }

      return myParameters;
    }

    @Override
    public int getParameterIndex(@NotNull PsiParameter parameter) {
      LOG.assertTrue(parameter.getParent() == this);
      return PsiImplUtil.getParameterIndex(parameter, this);
    }

    @Override
    public int getParametersCount() {
      return getParameters().length;
    }

    @Override
    public String toString() {
      return "Light Parameter List";
    }
  }
}
