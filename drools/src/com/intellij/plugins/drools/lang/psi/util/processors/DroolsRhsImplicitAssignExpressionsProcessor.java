package com.intellij.plugins.drools.lang.psi.util.processors;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.DroolsJavaRhsStatement;
import com.intellij.plugins.drools.lang.psi.DroolsSimpleRhsStatement;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightClass;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightVariable;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public final class DroolsRhsImplicitAssignExpressionsProcessor implements DroolsDeclarationsProcessor {
  private static DroolsRhsImplicitAssignExpressionsProcessor myInstance;

  private DroolsRhsImplicitAssignExpressionsProcessor() {
  }

  public static DroolsRhsImplicitAssignExpressionsProcessor getInstance() {
    if (myInstance == null) {
      myInstance = new DroolsRhsImplicitAssignExpressionsProcessor();
    }
    return myInstance;
  }

  @Override
  public boolean processElement(@NotNull PsiScopeProcessor processor,
                                @NotNull ResolveState state,
                                PsiElement lastParent,
                                @NotNull PsiElement place, @NotNull DroolsFile droolsFile) {
    for (DroolsLightVariable psiVariable : getLocalVariables(place, droolsFile)) {
      if (!processor.execute(psiVariable, state)) return false;
    }
    return true;
  }

  @NotNull
  public static Set<DroolsLightVariable> getLocalVariables(final PsiElement place) {
    final PsiFile file = place.getContainingFile();
    return file instanceof  DroolsFile ? getLocalVariables(place, (DroolsFile)file) : Collections.emptySet();
  }

  @NotNull
  public static Set<DroolsLightVariable> getLocalVariables(final PsiElement place, final DroolsFile droolsFile) {
    final Set<DroolsLightVariable> psiVariables = new HashSet<>();
    DroolsSimpleRhsStatement rhsStatement = PsiTreeUtil.getParentOfType(place, DroolsSimpleRhsStatement.class, false);
    if (rhsStatement != null) {
      DroolsJavaRhsStatement statement = PsiTreeUtil.getPrevSiblingOfType(rhsStatement, DroolsJavaRhsStatement.class);
      while (statement != null) {
        processStatement(droolsFile, psiVariables, statement);
        statement = PsiTreeUtil.getPrevSiblingOfType(statement, DroolsJavaRhsStatement.class);
      }

      if (rhsStatement instanceof DroolsJavaRhsStatement) {
        processStatement(droolsFile, psiVariables, (DroolsJavaRhsStatement)rhsStatement);
      }
    }
    return psiVariables;
  }

  private static void processStatement(final DroolsFile droolsFile, final Set<DroolsLightVariable> psiVariables, DroolsJavaRhsStatement statement) {
    statement.acceptChildren(new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (element instanceof PsiAssignmentExpression) {
          final PsiExpression expression = ((PsiAssignmentExpression)element).getLExpression();
          if (expression instanceof PsiReferenceExpression) {
            final String name = ((PsiReferenceExpression)expression).getQualifiedName();
            if (StringUtil.isNotEmpty(name)) {
              final PsiExpression rExpression = ((PsiAssignmentExpression)element).getRExpression();
              if (rExpression != null) {
                PsiType type = rExpression.getType();
                if (type instanceof PsiClassType) {
                  final PsiClass resolve = ((PsiClassType)type).resolve();
                  if (resolve != null) {
                    type = JavaPsiFacade.getInstance(element.getProject()).getElementFactory().createType(new DroolsLightClass(resolve));
                  }
                }
                if (type != null) {
                  psiVariables.add(new DroolsLightVariable(name, type, droolsFile));
                }
              }
            }
          }
        }
        element.acceptChildren(this);
      }
    });
  }
}
