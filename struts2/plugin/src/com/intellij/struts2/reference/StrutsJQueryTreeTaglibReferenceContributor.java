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
 * Provides support for struts2-jquery Tree plugin taglib.
 * <p/>
 * <a href="http://code.google.com/p/struts2-jquery/wiki/Tree">Description</a>.
 *
 * @author Johannes Geppert
 */
@SuppressWarnings("SpellCheckingInspection")
public class StrutsJQueryTreeTaglibReferenceContributor extends StrutsJQueryTaglibReferenceContributorBase {

  private static final String TREE = "tree";
  private static final String TREE_ITEM = "treeItem";

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_JQUERY_TREE_PLUGIN_URI;
  }

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {

    // <tree>
    registerBoolean("openAllOnLoad", registrar, TREE);
    registerTags(new StaticStringValuesReferenceProvider("default", "apple"),
                 "jstreetheme", registrar, TREE);
    registerTags(Holder.HTML_ID_REFERENCE_PROVIDER, "nodeTargets", registrar, TREE);

    // <treeItem>
    registerBoolean("button", registrar, TREE_ITEM);
  }
}
