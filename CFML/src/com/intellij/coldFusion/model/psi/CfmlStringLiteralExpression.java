/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * @author vnikolaenko
 */
public class CfmlStringLiteralExpression extends CfmlCompositeElement implements CfmlExpression {
  public CfmlStringLiteralExpression(@NotNull final ASTNode node) {
    super(node);
  }

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

  @NotNull
  @Override
  public PsiReference[] getReferences() {
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

    return super.getReferences();    //To change body of overridden methods use File | Settings | File Templates.
  }
}
