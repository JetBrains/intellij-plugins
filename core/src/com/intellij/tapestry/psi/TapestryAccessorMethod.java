package com.intellij.tapestry.psi;

import com.intellij.lang.StdLanguages;
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

/**
 * @author Alexey Chmutov
 *         Date: 19.10.2009
 *         Time: 18:27:02
 */
public class TapestryAccessorMethod extends LightElement implements PsiMethod {
  private static final Logger LOG = Logger.getInstance("#com.intellij.tapestry.psi.TapestryAccessorMethod");

  private final PsiField myProperty;

  private final boolean myGetterNotSetter;
  private final String myName;
  private LightParameterList myParameterList;
  private final PsiModifierList myModifierList;

  protected TapestryAccessorMethod(PsiField property, boolean getterNotSetter, @NotNull String name) {
    super(property.getManager(), StdLanguages.JAVA);
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

  public PsiDocComment getDocComment() {
    return null;
  }

  public boolean hasTypeParameters() {
    return false;
  }

  @Nullable
  public PsiTypeParameterList getTypeParameterList() {
    return null;
  }

  @NotNull
  public PsiTypeParameter[] getTypeParameters() {
    return PsiTypeParameter.EMPTY_ARRAY;
  }

  public PsiClass getContainingClass() {
    return myProperty.getContainingClass();
  }

  public PsiType getReturnType() {
    return myGetterNotSetter ? myProperty.getType() : PsiType.VOID;
  }

  public PsiTypeElement getReturnTypeElement() {
    return myProperty.getTypeElement();
  }

  @NotNull
  public PsiParameterList getParameterList() {
    if (myParameterList == null) {
      myParameterList = new LightParameterList(getManager(), new Computable<LightParameter[]>() {
        public LightParameter[] compute() {
          return myGetterNotSetter
                 ? EMPTY_PARAMETERS_ARRAY
                 : new LightParameter[]{
                   new LightParameter(getManager(), myProperty.getName(), null, myProperty.getType(), TapestryAccessorMethod.this)};
        }
      });
    }
    return myParameterList;
  }

  @Nullable
  public PsiIdentifier getNameIdentifier() {
    return myProperty.getNameIdentifier();
  }

  @NotNull
  public PsiModifierList getModifierList() {
    return myModifierList;
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    return null;
  }

  @NotNull
  public HierarchicalMethodSignature getHierarchicalMethodSignature() {
    return PsiSuperMethodImplUtil.getHierarchicalMethodSignature(this);
  }

  public PsiMethodReceiver getMethodReceiver() {
    return null;
  }

  public PsiType getReturnTypeNoResolve() {
    return getReturnType();
  }

  @Override
  public String toString() {
    return "AccessorMethod";
  }

  @NotNull
  public PsiReferenceList getThrowsList() {
    return new LightEmptyImplementsList(getManager());
  }

  @Nullable
  public PsiCodeBlock getBody() {
    return null;
  }

  public boolean isConstructor() {
    return false;
  }

  public boolean isVarArgs() {
    return false;
  }

  @NotNull
  public MethodSignature getSignature(@NotNull PsiSubstitutor substitutor) {
    return MethodSignatureBackedByPsiMethod.create(this, substitutor);
  }

  @NotNull
  public PsiMethod[] findSuperMethods() {
    return PsiMethod.EMPTY_ARRAY;
  }

  @NotNull
  public PsiMethod[] findSuperMethods(boolean checkAccess) {
    return PsiMethod.EMPTY_ARRAY;
  }

  @NotNull
  public PsiMethod[] findSuperMethods(PsiClass parentClass) {
    return PsiMethod.EMPTY_ARRAY;
  }

  @NotNull
  public List<MethodSignatureBackedByPsiMethod> findSuperMethodSignaturesIncludingStatic(boolean checkAccess) {
    return Collections.emptyList();
  }

  @NotNull
  public PsiMethod[] findDeepestSuperMethods() {
    return PsiMethod.EMPTY_ARRAY;
  }

  @Nullable
  public PsiMethod findDeepestSuperMethod() {
    return null;
  }

  public boolean hasModifierProperty(@NonNls @NotNull String name) {
    return getModifierList().hasModifierProperty(name);
  }

  public boolean isDeprecated() {
    return false;
  }

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

    public void accept(@NotNull PsiElementVisitor visitor) {
      if (visitor instanceof JavaElementVisitor) {
        ((JavaElementVisitor)visitor).visitParameter(this);
      }
    }

    public String toString() {
      return "Light Parameter";
    }

    public boolean isVarArgs() {
      return false;
    }

    @NotNull
    public String getName() {
      return StringUtil.notNullize(myName);
    }
  }

  public static class LightParameterList extends LightElement implements PsiParameterList {
    private final Computable<LightParameter[]> myParametersComputation;
    private LightParameter[] myParameters = null;

    protected LightParameterList(PsiManager manager, Computable<LightParameter[]> parametersComputation) {
      super(manager, StdLanguages.JAVA);
      myParametersComputation = parametersComputation;
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
      if (visitor instanceof JavaElementVisitor) {
        ((JavaElementVisitor)visitor).visitParameterList(this);
      }
    }

    @NotNull
    public PsiParameter[] getParameters() {
      if (myParameters == null) {
        myParameters = myParametersComputation.compute();
      }

      return myParameters;
    }

    public int getParameterIndex(PsiParameter parameter) {
      LOG.assertTrue(parameter.getParent() == this);
      return PsiImplUtil.getParameterIndex(parameter, this);
    }

    public int getParametersCount() {
      return getParameters().length;
    }

    public String toString() {
      return "Light Parameter List";
    }
  }
}
