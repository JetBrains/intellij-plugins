// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.lexer.CfmlTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CfmlStringLiteralExpression extends CfmlCompositeElement implements CfmlExpression {
  public CfmlStringLiteralExpression(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public PsiType getPsiType() {
    return CfmlPsiUtil.getTypeByName(CommonClassNames.JAVA_LANG_STRING, getProject());
  }

  @Nullable
  public PsiElement getValueElement() {
    return findChildByType(CfmlTokenTypes.STRING_TEXT);
  }

  @NotNull
  public String getValue() {
    final ASTNode value = getNode().findChildByType(CfmlTokenTypes.STRING_TEXT);
    if (value != null) {
      return value.getText();
    }
    return "";
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    CfmlFunctionCallExpression functionCallEl = PsiTreeUtil.getParentOfType(this, CfmlFunctionCallExpression.class);
    if (functionCallEl != null && functionCallEl.isCreateObject()) {
      final String[] args = functionCallEl.getArgumentsAsStrings();
      final CfmlExpression[] expressions = functionCallEl.getArguments();
      final PsiElement referenceElement = findChildByType(CfmlTokenTypes.STRING_TEXT);

      if (referenceElement == null) {
        return super.getReferences();
      }

      if (args.length == 2 && "java".equals(args[0]) &&
          expressions.length == 2 && expressions[1] == this) {
        final JavaClassReferenceProvider provider = new JavaClassReferenceProvider();
        return provider.getReferencesByString(args[1], referenceElement, 0);
      }
      else if ((args.length == 2 && "component".equals(args[0]) &&
                expressions.length == 2 && expressions[1] == this) ||
               (args.length == 1 && expressions.length == 1 && expressions[0] == this)) {
        final ASTNode referenceNode = referenceElement.getNode();
        if (referenceNode != null) {
          return new PsiReference[]{new CfmlComponentReference(referenceNode)};
        }
      }
    }
    else if (functionCallEl != null && functionCallEl.isExpandPath()) {
      final PsiElement referenceElement = findChildByType(CfmlTokenTypes.STRING_TEXT);
      if (referenceElement == null) {
        return super.getReferences();
      }
      final ASTNode referenceNode = referenceElement.getNode();
      if (referenceNode != null) {
        return (new CfmlFileReferenceSet(this, 1)).getAllReferences();
      }
    }
    else if (functionCallEl != null && functionCallEl.isCreateFromJavaLoader()) {
      CfmlExpression[] expressions = functionCallEl.getArguments();
      if (expressions.length > 0 && expressions[0] == this) {
        PsiElement stringTextElement = findChildByType(CfmlTokenTypes.STRING_TEXT);
        if (stringTextElement != null) {
          // getting javaloader type
          PsiElement secondChild = functionCallEl.getFirstChild().getFirstChild();
          if (!(secondChild instanceof CfmlReferenceExpression)) {
            return super.getReferences();
          }
          PsiType type = ((CfmlReferenceExpression)secondChild).getPsiType();
          if (!(type instanceof CfmlJavaLoaderClassType)) {
            return super.getReferences();
          }
          final GlobalSearchScope ss = ((CfmlJavaLoaderClassType)type).getSearchScope();

          String possibleJavaClassName = stringTextElement.getText();
          final JavaClassReferenceProvider provider = new JavaClassReferenceProvider() {
            @Override
            public GlobalSearchScope getScope(Project project) {
              return ss;
            }
          };
          return provider.getReferencesByString(possibleJavaClassName, stringTextElement, 0);
        }
      }
    }
    else if (getParent() instanceof CfmlComponentConstructorCall || getParent() instanceof CfmlImport) {
      PsiElement stringTextElement = findChildByType(CfmlTokenTypes.STRING_TEXT);
      if (stringTextElement != null) {
        return new PsiReference[]{new CfmlComponentReference(stringTextElement.getNode(), this)};
      }
    }

    return super.getReferences();
  }
}
