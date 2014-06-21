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
 * Provides support for struts2-jquery Richtext editor plugin taglib.
 * <p/>
 * <a href="http://code.google.com/p/struts2-jquery/wiki/RichtextEditor">Description</a>.
 *
 * @author Yann C&eacute;bron
 */
@SuppressWarnings("SpellCheckingInspection")
public class StrutsJQueryRichtextTaglibReferenceContributor extends StrutsJQueryTaglibReferenceContributorBase {

  private static final String CKEDITOR = "ckeditor";
  private static final String TINYMCE = "tinymce";

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_URI;
  }

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {

    // common attributes

    installCSS(registrar, CKEDITOR, TINYMCE);

    registerBoolean("disabled", registrar, CKEDITOR, TINYMCE);

    installDraggable(registrar, CKEDITOR, TINYMCE);
    installDroppable(registrar, CKEDITOR, TINYMCE);

    installEffect(registrar, CKEDITOR, TINYMCE);

    installErrorElementId(registrar, CKEDITOR, TINYMCE);

    installEvents(registrar, CKEDITOR, TINYMCE);

    installIndicator(registrar, CKEDITOR, TINYMCE);

    installRequired(registrar, CKEDITOR, TINYMCE);

    installResizable(registrar, CKEDITOR, TINYMCE);

    installSelectable(registrar, CKEDITOR, TINYMCE);

    installSortable(registrar, CKEDITOR, TINYMCE);

    installTargets(registrar, CKEDITOR, TINYMCE);

    // specific attributes

    // "readonly"
    registerBoolean("readonly", registrar, CKEDITOR, TINYMCE);

    // "skin"
    registerTags(new StaticStringValuesReferenceProvider("kama", "moono"),
                 "skin", registrar, CKEDITOR);

    // "editorTheme"
    registerTags(new StaticStringValuesReferenceProvider("simple", "advanced"),
                 "editorTheme", registrar, TINYMCE);

    // "editorSkin"
    registerTags(new StaticStringValuesReferenceProvider("default", "highcontrast", "o2k7"),
                 "editorSkin", registrar, TINYMCE);
  }
}