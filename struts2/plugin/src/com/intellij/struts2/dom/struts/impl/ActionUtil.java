/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helper methods for {@link ActionImpl}.
 *
 * @author Yann C&eacute;bron
 */
final class ActionUtil {

  private ActionUtil() {
  }

  /**
   * Does the given path match the Action's path (including support for wildcards and bang notation).
   *
   * @param actionPath Path of Action.
   * @param checkPath  Path to check.
   * @return true if matched.
   */
  static boolean matchesPath(@NotNull @NonNls final String actionPath,
                             @NotNull @NonNls final String checkPath) {
    // strip everything behind "!"
    final int bangIdx = StringUtil.indexOf(checkPath, '!');
    final String strippedCheckPath = bangIdx == -1 ? checkPath : checkPath.substring(0, bangIdx);

    // do we have any wildcard-markers in our path? no --> exact compare
    if (StringUtil.indexOf(actionPath, '*') == -1) {
      return Comparing.equal(strippedCheckPath, actionPath);
    }

    return Pattern.matches(StringUtil.replace(actionPath, "*", "[^/]*"), strippedCheckPath);
  }

  /**
   * Returns all suitable action methods for the given Action class.
   *
   * @param actionClass Action class to search for action methods.
   * @param methodName  (Optional) Method name.
   * @return Methods suitable for action execution.
   */
  static List<PsiMethod> findActionMethods(@NotNull final PsiClass actionClass,
                                           @Nullable final String methodName) {
    final Module module = ModuleUtil.findModuleForPsiElement(actionClass);
    if (module == null) {
      return Collections.emptyList();
    }

    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);

    final PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(actionClass.getProject()).getElementFactory();
    final PsiClassType stringType =
        psiElementFactory.createTypeByFQClassName(CommonClassNames.JAVA_LANG_STRING, scope);
    final PsiClassType resultType =
        psiElementFactory.createTypeByFQClassName("com.opensymphony.xwork2.Result", scope);

    final boolean searchForMethod = methodName != null;

    final List<PsiMethod> actionMethods = new SmartList<PsiMethod>();
    for (final PsiMethod psiMethod : actionClass.getAllMethods()) {
      final String psiMethodName = psiMethod.getName();

      if (searchForMethod &&
          !Comparing.equal(psiMethodName, methodName)) {
        continue;
      }

      if (psiMethod.isConstructor()) {
        continue;
      }

      // only public non-static concrete methods
      final PsiModifierList modifiers = psiMethod.getModifierList();
      if (!modifiers.hasModifierProperty(PsiModifier.PUBLIC) ||
          modifiers.hasModifierProperty(PsiModifier.STATIC) ||
          modifiers.hasModifierProperty(PsiModifier.ABSTRACT)) {
        continue;
      }

      // no parameters
      if (psiMethod.getParameterList().getParametersCount() != 0) {
        continue;
      }

      // skip "toString()"
      if (Comparing.equal(psiMethodName, "toString")) {
        continue;
      }

      // do not include simple getters (with underlying field)
      if (PropertyUtil.isSimplePropertyGetter(psiMethod) &&
          actionClass.findFieldByName(PropertyUtil.getPropertyName(psiMethod), true) != null) {
        continue;
      }

      // return type "java.lang.String" or "com.opensymphony.xwork2.Result"
      final PsiType type = psiMethod.getReturnType();
      if (type != null &&
          type instanceof PsiClassType &&
          (type.equals(stringType) || type.equals(resultType))) {
        actionMethods.add(psiMethod);

        // stop on first hit when searching for name
        if (searchForMethod) {
          return actionMethods;
        }
      }

    }

    return actionMethods;
  }

}