// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.resolve.noncode;

import com.intellij.psi.*;
import com.intellij.psi.impl.cache.CacheManager;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberCommonClassNames;
import org.jetbrains.plugins.cucumber.groovy.resolve.CustomWorldType;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;

@NotNullByDefault
public final class CustomWorldContributor extends NonCodeMembersContributor {

  @Override
  public void processDynamicElements(PsiType qualifierType,
                                     PsiScopeProcessor processor,
                                     PsiElement place,
                                     ResolveState state) {
    if (qualifierType instanceof CustomWorldType type) {
      doProcessDynamicMethods(processor, place, state, type.getStepFile());
    }
  }

  private static void doProcessDynamicMethods(PsiScopeProcessor processor,
                                              PsiElement place,
                                              ResolveState state,
                                              PsiFile stepFile) {
    if (stepFile instanceof GroovyFile groovyStepFile) {
      final PsiType worldType = getWorldType(groovyStepFile);
      if (worldType != null) {
        ResolveUtil.processAllDeclarations(worldType, processor, state, place);
      }
      else {
        GlobalSearchScope scope = GlobalSearchScope.getScopeRestrictedByFileTypes(stepFile.getResolveScope(),
                                                                                  GroovyFileType.getGroovyEnabledFileTypes());
        PsiFile[] files = CacheManager.getInstance(place.getProject()).getFilesWithWord("World", UsageSearchContext.IN_CODE, scope, true);
        for (PsiFile file : files) {
          if (file instanceof GroovyFile groovyFile) {
            final PsiType type = getWorldType(groovyFile);
            if (type != null) {
              if (!ResolveUtil.processAllDeclarations(type, processor, state, place)) {
                return;
              }
            }
          }
        }
      }
    }
  }

  private static @Nullable PsiType getWorldType(GroovyFile stepFile) {
    return CachedValuesManager.getCachedValue(stepFile, () -> {
      for (GrStatement statement : stepFile.getStatements()) {
        if (statement instanceof GrMethodCall call && isWorldDeclaration(call)) {
          final GrClosableBlock closure = getClosureArg(call);
          return CachedValueProvider.Result.create(closure == null ? null : closure.getReturnType(), stepFile);
        }
      }
      return CachedValueProvider.Result.create(null, stepFile);
    });
  }

  private static @Nullable GrClosableBlock getClosureArg(GrMethodCall methodCall) {
    final GrClosableBlock[] closures = methodCall.getClosureArguments();
    if (closures.length == 1) return closures[0];
    if (closures.length > 1) return null;
    final GrExpression[] args = methodCall.getExpressionArguments();
    if (args.length == 0) return null;
    final GrExpression last = DefaultGroovyMethods.last(args);
    if (last instanceof GrClosableBlock block) {
      return block;
    }

    return null;
  }

  private static boolean isWorldDeclaration(GrMethodCall methodCall) {
    final GrExpression invoked = methodCall.getInvokedExpression();
    if (invoked instanceof GrReferenceExpression) {
      final PsiMethod method = methodCall.resolveMethod();
      final PsiClass clazz = method == null ? null : method.getContainingClass();
      final String qname = clazz == null ? null : clazz.getQualifiedName();
      return method != null && "World".equals(method.getName()) && GrCucumberCommonClassNames.isHookClassName(qname);
    }

    return false;
  }
}
