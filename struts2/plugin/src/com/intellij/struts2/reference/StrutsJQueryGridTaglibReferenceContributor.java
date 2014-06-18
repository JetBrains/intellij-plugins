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
 * Provides support for struts2-jquery Grid plugin taglib.
 * <p/>
 * <a href="http://code.google.com/p/struts2-jquery/wiki/Grid">Description</a>.
 *
 * @author Johannes Geppert
 */
@SuppressWarnings("SpellCheckingInspection")
public class StrutsJQueryGridTaglibReferenceContributor extends StrutsJQueryTaglibReferenceContributorBase {

  private static final String GRID = "grid";
  private static final String GRID_COLUMN = "gridColumn";

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_JQUERY_GRID_PLUGIN_URI;
  }

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {

    // <grid>
    registerBoolean("autoencode", registrar, GRID);
    registerBoolean("cellEdit", registrar, GRID);
    registerBoolean("editinline", registrar, GRID);
    registerBoolean("footerrow", registrar, GRID);
    registerBoolean("hiddengrid", registrar, GRID);
    registerBoolean("hidegrid", registrar, GRID);
    registerBoolean("hoverrows", registrar, GRID);
    registerBoolean("loadonce", registrar, GRID);
    registerBoolean("multiselect", registrar, GRID);
    registerBoolean("navigator", registrar, GRID);
    registerBoolean("navigatorAdd", registrar, GRID);
    registerBoolean("navigatorDelete", registrar, GRID);
    registerBoolean("navigatorEdit", registrar, GRID);
    registerBoolean("navigatorRefresh", registrar, GRID);
    registerBoolean("navigatorSearch", registrar, GRID);
    registerBoolean("navigatorView", registrar, GRID);
    registerBoolean("pager", registrar, GRID);
    registerBoolean("rownumbers", registrar, GRID);
    registerBoolean("scroll", registrar, GRID);
    registerBoolean("scrollrows", registrar, GRID);
    registerBoolean("shrinkToFit", registrar, GRID);
    registerBoolean("sortable", registrar, GRID);
    registerTags(new StaticStringValuesReferenceProvider(false, "asc", "desc"),
                 "sortorder", registrar, GRID);
    registerBoolean("userDataOnFooter", registrar, GRID);
    registerBoolean("viewrecords", registrar, GRID);

    // <gridColumn>
    registerTags(new StaticStringValuesReferenceProvider(false, "left", "center", "right"),
                 "align", registrar, GRID_COLUMN);
    registerBoolean("editable", registrar, GRID_COLUMN);
    registerTags(new StaticStringValuesReferenceProvider(false, "text", "textarea", "select", "checkbox",
                                                         "password", "button", "image", "file"),
                 "edittype", registrar, GRID_COLUMN);
    registerTags(new StaticStringValuesReferenceProvider("integer", "currency", "date", "checkbox"),
                 "formatter", registrar, GRID_COLUMN);
    registerBoolean("hidden", registrar, GRID_COLUMN);
    registerBoolean("hidedlg", registrar, GRID_COLUMN);
    registerBoolean("resizable", registrar, GRID_COLUMN);
    registerBoolean("search", registrar, GRID_COLUMN);
    registerBoolean("sortable", registrar, GRID_COLUMN);
  }
}
