package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author: Fedor.Korotkov
 */
abstract public class AbstractDartComponentImpl extends DartPsiCompositeElementImpl implements DartComponent {
  public AbstractDartComponentImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String getName() {
    final DartComponentName name = getComponentName();
    if (name != null) {
      return name.getText();
    }
    return super.getName();
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    final DartComponentName componentName = getComponentName();
    if (componentName != null) {
      componentName.setName(name);
    }
    return this;
  }

  @Nullable
  @Override
  public PsiElement getNameIdentifier() {
    return getComponentName();
  }

  @Override
  public Icon getIcon(int flags) {
    final DartComponentType type = DartComponentType.typeOf(this);
    return type == null ? super.getIcon(flags) : type.getIcon();
  }

  @Override
  public boolean isStatic() {
    return DartResolveUtil.getDeclarationTypes(this).contains(DartTokenTypes.STATIC);
  }

  @Override
  public boolean isPublic() {
    final String name = getName();
    return name != null && !name.startsWith("_");
  }

  @Override
  public boolean isSetter() {
    return DartResolveUtil.getDeclarationTypes(this).contains(DartTokenTypes.SET);
  }

  @Override
  public boolean isGetter() {
    return DartResolveUtil.getDeclarationTypes(this).contains(DartTokenTypes.GET);
  }

  @Override
  public boolean isAbstract() {
    return DartResolveUtil.getDeclarationTypes(this).contains(DartTokenTypes.ABSTRACT);
  }

  @Override
  public ItemPresentation getPresentation() {
    return new ItemPresentation() {
      @Override
      public String getPresentableText() {
        final StringBuilder result = new StringBuilder();
        result.append(getComponentName());
        final DartComponentType type = DartComponentType.typeOf(AbstractDartComponentImpl.this);
        if ((type == DartComponentType.METHOD || type == DartComponentType.FUNCTION || type == DartComponentType.CONSTRUCTOR) &&
            !(isGetter() || isSetter())) {
          final String parameterList = DartPresentableUtil.getPresentableParameterList(AbstractDartComponentImpl.this);
          result.append("(").append(parameterList).append(")");
        }
        if (type == DartComponentType.METHOD || type == DartComponentType.FIELD || type == DartComponentType.FUNCTION) {
          final DartReturnType returnType = PsiTreeUtil.getChildOfType(AbstractDartComponentImpl.this, DartReturnType.class);
          final DartType dartType =
            returnType == null ? PsiTreeUtil.getChildOfType(AbstractDartComponentImpl.this, DartType.class) : returnType.getType();
          if (dartType != null) {
            result.append(":");
            result.append(DartPresentableUtil.buildTypeText(AbstractDartComponentImpl.this, dartType));
          }
        }
        return result.toString();
      }

      @Nullable
      private String getComponentName() {
        String name = getName();
        if (DartComponentType.typeOf(AbstractDartComponentImpl.this) == DartComponentType.CONSTRUCTOR) {
          DartClass dartClass = PsiTreeUtil.getParentOfType(AbstractDartComponentImpl.this, DartClass.class);
          if (dartClass == null) {
            return name;
          }
          return StringUtil.isEmpty(name) ? dartClass.getName() : dartClass.getName() + "" + name;
        }
        return name;
      }

      @Override
      public String getLocationString() {
        if (!isValid()) {
          return "";
        }
        if (!(AbstractDartComponentImpl.this instanceof DartClass)) {
          final DartClass dartClass = PsiTreeUtil.getParentOfType(AbstractDartComponentImpl.this, DartClass.class);
          if (dartClass != null) {
            return dartClass.getName();
          }
        }
        DartExecutionScope root = PsiTreeUtil.getTopmostParentOfType(AbstractDartComponentImpl.this, DartExecutionScope.class);
        DartPartOfStatement partOfStatement = PsiTreeUtil.getChildOfType(root, DartPartOfStatement.class);
        return partOfStatement == null ? null : partOfStatement.getLibraryId().getCanonicalText();
      }

      @Override
      public Icon getIcon(boolean open) {
        return AbstractDartComponentImpl.this.getIcon(0);
      }
    };
  }
}
