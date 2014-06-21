/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.reference;

import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.struts2.StrutsConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Provides support for struts2-bootstrap plugin taglib.
 * <p/>
 * <a href="http://code.google.com/p/struts2-bootstrap/">struts2-bootstrap plugin homepage</a>.
 *
 * @author Johannes Geppert
 */
public class StrutsBootstrapTaglibReferenceContributor extends StrutsTaglibReferenceContributorBase {

  private static final String HEAD = "head";

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_BOOTSTRAP_PLUGIN_PREFIX;
  }

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    registerBoolean("compressed", registrar, HEAD);
    registerBoolean("includeStyles", registrar, HEAD);
    registerBoolean("includeStylesResponsive", registrar, HEAD);
    registerBoolean("includeScripts", registrar, HEAD);
    registerBoolean("includeScriptsValidation", registrar, HEAD);
  }
}
