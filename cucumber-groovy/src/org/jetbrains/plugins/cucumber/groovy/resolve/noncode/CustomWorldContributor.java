package org.jetbrains.plugins.cucumber.groovy.resolve.noncode;

import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberCommonClassNames;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMember;
import org.jetbrains.plugins.groovy.lang.psi.util.GroovyCommonClassNames;
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor;
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil;

import java.util.List;

public class CustomWorldContributor extends NonCodeMembersContributor {
  @Override
  public void processDynamicElements(@NotNull PsiType qualifierType,
                                     @NotNull PsiScopeProcessor processor,
                                     @Nullable PsiElement place,
                                     @NotNull ResolveState state) {
    if (place instanceof GrReferenceExpression &&
        ((GrReferenceExpression)place).getQualifier() == null &&
        qualifierType.equalsToText(GroovyCommonClassNames.GROOVY_LANG_CLOSURE)) {

      final GrClosableBlock closureContainer = PsiTreeUtil.getParentOfType(place, GrClosableBlock.class, true, GrMember.class);
      if (closureContainer != null) {
        PsiElement parent = closureContainer.getParent();
        if (parent instanceof GrArgumentList && isLastArg((GrArgumentList)parent, closureContainer)) {
          parent = parent.getParent();
        }

        if (parent instanceof GrMethodCall && (GrCucumberUtil.isStepDefinition(parent) || GrCucumberUtil.isHook((GrMethodCall)parent))) {
          doProcessDynamicMethods(processor, place, state, parent.getContainingFile());
        }
      }
    }
  }

  private static void doProcessDynamicMethods(@NotNull PsiScopeProcessor processor,
                                              @NotNull PsiElement place,
                                              @NotNull ResolveState state,
                                              final PsiFile stepFile) {
    if (stepFile instanceof GroovyFile) {
      final PsiType worldType = getWorldType((GroovyFile)stepFile);
      if (worldType != null) {
        ResolveUtil.processAllDeclarations(worldType, processor, state, place);
      }
      else {
        final PsiDirectory directory = stepFile.getContainingDirectory();
        if (directory != null) {
          final List<PsiFile> otherStepFiles = CucumberStepsIndex.getInstance(place.getProject()).gatherStepDefinitionsFilesFromDirectory(directory, false);
          for (PsiFile otherFile : otherStepFiles) {
            if (otherFile instanceof GroovyFile) {
              final PsiType type = getWorldType((GroovyFile)otherFile);
              if (type != null) {
                ResolveUtil.processAllDeclarations(type, processor, state, place);
              }
            }
          }
        }
      }
    }
  }

  @Nullable
  private static PsiType getWorldType(@NotNull final GroovyFile stepFile) {
    return CachedValuesManager.getManager(stepFile.getProject()).getCachedValue(stepFile, new CachedValueProvider<PsiType>() {
      @Nullable
      @Override
      public Result<PsiType> compute() {
        for (GrStatement statement : stepFile.getStatements()) {
          if (statement instanceof GrMethodCall && isWorldDeclaration((GrMethodCall)statement)) {
            final GrClosableBlock closure = getClosureArg((GrMethodCall)statement);
            return Result.create(closure == null ? null : closure.getReturnType(), stepFile);
          }
        }
        return Result.create(null, stepFile);
      }
    });
  }

  @Nullable
  private static GrClosableBlock getClosureArg(@NotNull GrMethodCall methodCall) {
    final GrClosableBlock[] closures = methodCall.getClosureArguments();
    if (closures.length == 1) return closures[0];
    if (closures.length > 1) return null;
    final GrExpression[] args = methodCall.getExpressionArguments();
    if (args.length == 0) return null;
    final GrExpression last = DefaultGroovyMethods.last(args);
    if (last instanceof GrClosableBlock) {
      return (GrClosableBlock)last;
    }

    return null;
  }

  private static boolean isWorldDeclaration(@NotNull GrMethodCall methodCall) {
    final GrExpression invoked = methodCall.getInvokedExpression();
    if (invoked instanceof GrReferenceExpression) {
      final PsiMethod method = methodCall.resolveMethod();
      final PsiClass clazz = method == null ? null : method.getContainingClass();
      final String qname = clazz == null ? null : clazz.getQualifiedName();
      return method!= null && "World".equals(method.getName()) && GrCucumberCommonClassNames.isHookClassName(qname);
    }

    return false;
  }

  private static boolean isLastArg(@NotNull GrArgumentList list, @NotNull GrClosableBlock block) {
    final GrExpression[] exprs = list.getExpressionArguments();
    return exprs.length > 0 && exprs[exprs.length - 1] == block;
  }
}
