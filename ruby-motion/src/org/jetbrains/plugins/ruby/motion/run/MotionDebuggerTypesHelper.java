/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrDebuggerTypesHelper;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrMemberValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.ScopeVariable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

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

    final PsiElement element = RubyPsiUtil.getSignificantLeafToTheRight(psiFile.findElementAt(offset));
    final RContainer container = element != null ? RubyPsiUtil.getParentContainerOrSelf(element) : null;
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

  @Nullable
  @Override
  protected PsiReference createReferenceFromText(@NotNull LLValue var, @NotNull PsiElement context) {
    return null;
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
  public XSourcePosition computeSourcePosition(@NotNull CidrMemberValue value) {
    return null;
  }

  @Nullable
  @Override
  public XSourcePosition resolveProperty(@NotNull CidrMemberValue value, @Nullable String dynamicTypeName) {
    return null;
  }

  @Nullable
  private RContainer findContainer(XSourcePosition pos) {
    if (pos == null) return null;

    PsiFile psiFile = PsiManager.getInstance(myProcess.getProject()).findFile(pos.getFile());
    final PsiElement element = psiFile != null ? psiFile.findElementAt(pos.getOffset()) : null;
    return element != null ? RubyPsiUtil.getParentContainerOrSelf(element) : null;
  }
}
