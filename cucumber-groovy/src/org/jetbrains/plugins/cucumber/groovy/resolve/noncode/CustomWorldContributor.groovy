package org.jetbrains.plugins.cucumber.groovy.resolve.noncode

import com.intellij.psi.PsiType
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.cucumber.groovy.GrCucumberCommonClassNames
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMember
import org.jetbrains.plugins.groovy.lang.psi.util.GroovyCommonClassNames
import org.jetbrains.plugins.groovy.lang.resolve.NonCodeMembersContributor
import org.jetbrains.plugins.groovy.lang.resolve.ResolveUtil
/**
 * @author Max Medvedev
 */
class CustomWorldContributor extends NonCodeMembersContributor {

  @Override
  void processDynamicElements(PsiType qualifierType, PsiScopeProcessor processor, GroovyPsiElement place, ResolveState state) {
    if (place instanceof GrReferenceExpression && place.qualifier == null && qualifierType.equalsToText(GroovyCommonClassNames.GROOVY_LANG_CLOSURE)) {
      final GrClosableBlock closureContainer = PsiTreeUtil.getParentOfType(place, GrClosableBlock, true, GrMember)
      if (closureContainer != null) {
        final parent = closureContainer.parent
        if (parent instanceof GrArgumentList && isLastArg(parent, closureContainer)) {
          parent = parent.parent
        }

        if (parent instanceof GrMethodCall && (GrCucumberUtil.isStepDefinition(parent) || GrCucumberUtil.isHook(parent))) {
          doProcessDynamicMethods(processor, parent, place, state)
        }
      }
    }
  }

  private static void doProcessDynamicMethods(PsiScopeProcessor processor, GrMethodCall stepDef, GroovyPsiElement place, ResolveState state) {
    final file = stepDef.containingFile
    if (file instanceof GroovyFile) {
      final worldType = getWorldType(file)
      if (worldType != null) {
        ResolveUtil.processAllDeclarations(worldType, processor, state, place)
      }
    }
  }

  @Nullable
  private static PsiType getWorldType(GroovyFile stepFile) {
    for (statement in stepFile.statements) {
      if (statement instanceof GrMethodCall && isWorldDeclaration(statement)) {
        final GrClosableBlock closure = getClosureArg(statement)
        return closure?.returnType
      }
    }
    return null
  }

  @Nullable
  private static GrClosableBlock getClosureArg(GrMethodCall methodCall) {
    final GrClosableBlock[] closures = methodCall.closureArguments
    if (closures.length == 1) return closures[0]
    if (closures.length > 1) return null  // if there is more than one closure arg something went wrong

    final GrExpression[] args = methodCall.expressionArguments

    if (args.length == 0) return null

    final last = args.last()
    if (last instanceof GrClosableBlock) {
      return last
    }
    return null
  }

  private static boolean isWorldDeclaration(GrMethodCall methodCall) {
    final invoked = methodCall.invokedExpression
    if (invoked instanceof GrReferenceExpression) {
      final method = methodCall.resolveMethod()
      final qname = method?.containingClass?.qualifiedName
      return 'World' == method?.name && GrCucumberCommonClassNames.CUCUMBER_RUNTIME_GROOVY_HOOKS == qname
    }
    return false
  }

  private static boolean isLastArg(GrArgumentList list, GrClosableBlock block) {
    final exprs = list.expressionArguments
    return exprs && exprs.last() == block
  }
}
