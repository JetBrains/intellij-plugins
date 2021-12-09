/*
 * Copyright 2014 The authors
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

import com.intellij.javaee.web.WebCommonClassNames;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.CommonClassNames;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.velocity.VtlGlobalVariableProvider;
import com.intellij.velocity.psi.VtlLightVariable;
import com.intellij.velocity.psi.VtlVariable;
import com.intellij.velocity.psi.files.VtlFile;
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
final class Struts2GlobalVariableProvider extends VtlGlobalVariableProvider {
  @NotNull
  @Override
  public Collection<VtlVariable> getGlobalVariables(@NotNull final VtlFile file) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(file);
    if (module == null) {
      return Collections.emptySet();
    }

    if (StrutsFacet.getInstance(module) == null) {
      return Collections.emptySet();
    }

    final List<VtlVariable> result = new ArrayList<>();
    result.add(new MyVtlVariable("response", file, WebCommonClassNames.JAVAX_HTTP_SERVLET_RESPONSE));
    result.add(new MyVtlVariable("res", file, WebCommonClassNames.JAVAX_HTTP_SERVLET_RESPONSE));
    result.add(new MyVtlVariable("request", file, WebCommonClassNames.JAVAX_HTTP_SERVLET_REQUEST));
    result.add(new MyVtlVariable("req", file, WebCommonClassNames.JAVAX_HTTP_SERVLET_REQUEST));
    result.add(new MyVtlVariable("application", file, WebCommonClassNames.JAVAX_SERVLET_CONTEXT));
    result.add(new MyVtlVariable("session", file, WebCommonClassNames.JAVAX_HTTP_SESSION));
    result.add(new MyVtlVariable("base", file, CommonClassNames.JAVA_LANG_STRING));
    result.add(new MyVtlVariable("stack", file, "com.opensymphony.xwork2.util.ValueStack"));
    result.add(new MyVtlVariable("action", file, "com.opensymphony.xwork2.ActionInvocation"));

    // TODO: taglibs->macros, Action-properties

    return result;
  }


  private static final class MyVtlVariable extends VtlLightVariable {

    private MyVtlVariable(final String name, final VtlFile parent, final String fqnClassName) {
      super(name, parent, fqnClassName);
    }

    @Override
    public Icon getIcon(final boolean b) {
      return StrutsIcons.STRUTS_VARIABLE;
    }
  }

}