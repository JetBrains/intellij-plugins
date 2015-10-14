package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.CidrStackFrame;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrDebuggerTypesHelper;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrMemberValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import com.jetbrains.cidr.execution.debugger.evaluation.EvaluationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.RubyMotionSymbolProvider;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtilImpl;
import org.jetbrains.plugins.ruby.motion.symbols.StructSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.ScopeVariable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import java.util.Collections;

/**
* @author Dennis.Ushakov
*/
class MotionDebuggerTypesHelper extends CidrDebuggerTypesHelper {
  public MotionDebuggerTypesHelper(CidrDebugProcess process) {
    super(process);
  }

  @Override
  public PsiElement resolveToDeclaration(XSourcePosition position, LLValue var) {
    final String name = var.getName();
    final VirtualFile file = position.getFile();
    final int offset = position.getOffset();
    final PsiFile psiFile = PsiManager.getInstance(myProcess.getProject()).findFile(file);
    if (psiFile == null) return null;

    final PsiElement element = RubyPsiUtil.getInstance().getSignificantLeafToTheRight(psiFile.findElementAt(offset));
    final RContainer container = element != null ? RubyPsiUtil.getInstance().getParentContainerOrSelf(element) : null;
    if (container == null) return null;

    ScopeVariable variable = null;
    for (ScopeVariable scopeVariable : container.getScope().getAllDeclaredVariables()) {
      if (name.equals(scopeVariable.getName())) {
        variable = scopeVariable;
        break;
      }
    }
    if (variable == null) return null;
    final RPsiElement item = ContainerUtil.getFirstItem(variable.getDeclarations());
    if (item == null) return null;
    return item;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public Boolean isImplicitContextVariable(@NotNull XSourcePosition position, @NotNull LLValue var) {
    return "self".equals(var.getName()) && findContainer(position) != null;
  }

  @Override
  public XSourcePosition computeSourcePosition(CidrMemberValue value) {
    return null;
  }

  @Nullable
  @Override
  public XSourcePosition resolveProperty(@NotNull CidrMemberValue value, @Nullable String dynamicTypeName) {
    return null;
  }

  @Override
  public boolean hasStructType(@NotNull CidrPhysicalValue value, EvaluationContext context) throws ExecutionException {
    if (super.hasStructType(value, context)) {
      return true;
    }
    final CidrStackFrame.CachedTypeInfo typeInfo = value.getFrame().getTypeInfo(value.getType());
    if (typeInfo.isStruct() != null) {
      return typeInfo.isStruct();
    }
    final String type = value.getType();
    final Module module = RubyMotionUtil.getInstance().getModuleWithMotionSupport(myProcess.getProject());
    if (module == null) return false;
    final Symbol symbol = ApplicationManager.getApplication().runReadAction(new Computable<Symbol>() {
      @Override
      public Symbol compute() {
        return RubyMotionSymbolProvider.findClassOrStruct(module, ((RubyMotionUtilImpl)RubyMotionUtil.getInstance()).getFrameworks(module), Collections.singletonList(type));
      }
    });
    final boolean isStruct = symbol instanceof StructSymbol;
    typeInfo.setStruct(isStruct);
    return isStruct;
  }

  @Nullable
  private RContainer findContainer(XSourcePosition pos) {
    if (pos == null) return null;

    PsiFile psiFile = PsiManager.getInstance(myProcess.getProject()).findFile(pos.getFile());
    final PsiElement element = psiFile != null ? psiFile.findElementAt(pos.getOffset()) : null;
    return element != null ? RubyPsiUtil.getInstance().getParentContainerOrSelf(element) : null;
  }
}
