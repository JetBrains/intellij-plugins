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

package com.intellij.struts2.reference;

import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.struts2.StrutsConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Provides support for struts2-jquery Richtext editor plugin taglib.
 * <p/>
 * <a href="http://code.google.com/p/struts2-jquery/wiki/RichtextEditor">Description</a>.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsJQueryRichtextTaglibReferenceContributor extends StrutsJQueryTaglibReferenceContributorBase {

  private static final String CKEDITOR = "ckeditor";

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_URI;
  }

  @Override
  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {

    // common attributes

    installCSS(registrar, CKEDITOR);

    registerBoolean("disabled", registrar, CKEDITOR);

    installDraggable(registrar, CKEDITOR);
    installDroppable(registrar, CKEDITOR);

    installEffect(registrar, CKEDITOR);

    installErrorElementId(registrar, CKEDITOR);

    installEvents(registrar, CKEDITOR);

    installIndicator(registrar, CKEDITOR);

    installRequired(registrar, CKEDITOR);

    installResizable(registrar, CKEDITOR);

    installSelectable(registrar, CKEDITOR);

    installSortable(registrar, CKEDITOR);

    installTargets(registrar, CKEDITOR);

    // specific attributes

    // "readonly"
    registerBoolean("readonly", registrar, CKEDITOR);

    // "skin"
    registerTags(new StaticStringValuesReferenceProvider("kama", "office2003", "v2"),
                 "skin", registrar, CKEDITOR);
  }

}