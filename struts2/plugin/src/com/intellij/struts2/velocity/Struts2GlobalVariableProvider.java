/*
 * Copyright 2010 The authors
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

package com.intellij.struts2.velocity;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.IncorrectOperationException;
import com.intellij.velocity.VtlGlobalVariableProvider;
import com.intellij.velocity.psi.VtlVariable;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.files.VtlFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Exposes implicit S2-variables.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2GlobalVariableProvider extends VtlGlobalVariableProvider {

  @NotNull
  @Override
  public Collection<? extends VtlVariable> getGlobalVariables(final VtlFile file) {
    final Module module = ModuleUtil.findModuleForPsiElement(file);
    if (module == null) {
      return Collections.emptySet();
    }

    if (StrutsFacet.getInstance(module) == null) {
      return Collections.emptySet();
    }

    final List<MyVtlVariable> result = new ArrayList<MyVtlVariable>();
    result.add(new MyVtlVariable("response", file, "javax.servlet.http.HttpServletResponse"));
    result.add(new MyVtlVariable("res", file, "javax.servlet.http.HttpServletResponse"));
    result.add(new MyVtlVariable("request", file, "javax.servlet.http.HttpServletRequest"));
    result.add(new MyVtlVariable("req", file, "javax.servlet.http.HttpServletRequest"));
    result.add(new MyVtlVariable("application", file, "javax.servlet.ServletContext"));
    result.add(new MyVtlVariable("session", file, "javax.servlet.http.HttpSession"));
    result.add(new MyVtlVariable("base", file, CommonClassNames.JAVA_LANG_STRING));
    result.add(new MyVtlVariable("stack", file, "com.opensymphony.xwork2.util.ValueStack"));
    result.add(new MyVtlVariable("action", file, "com.opensymphony.xwork2.ActionInvocation"));

    // TODO: taglibs->macros, Action-properties

    return result;
  }


  private static class MyVtlVariable extends FakePsiElement implements VtlVariable {

    private PsiType myType;
    private final String name;
    private final PsiElement parent;

    private MyVtlVariable(final String name, final PsiElement parent, final String fqnClassName) {
      this.name = name;
      this.parent = parent;
      this.myType = createType(parent, fqnClassName);
    }

    private static PsiType createType(final PsiElement parent, final String fqnClassName) {
      try {
        return JavaPsiFacade.getInstance(parent.getProject()).getElementFactory().createTypeFromText(fqnClassName, parent);
      } catch (IncorrectOperationException e) {
        return null;
      }
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Icon getIcon(final boolean b) {
      return StrutsIcons.STRUTS_VARIABLE_ICON;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
      return GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope) super.getUseScope(), VtlFileType.INSTANCE);
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
      return parent;
    }

    @Override
    public PsiType getPsiType() {
      return myType;
    }

    @Override
    public PsiElement getParent() {
      return parent;
    }
  }

}