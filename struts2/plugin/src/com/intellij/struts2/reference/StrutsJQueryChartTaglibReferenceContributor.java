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
 * Provides support for struts2-jquery Chart plugin taglib.
 * <p/>
 * <a href="http://code.google.com/p/struts2-jquery/wiki/Chart">Description</a>.
 *
 * @author Johannes Geppert
 */
@SuppressWarnings("SpellCheckingInspection")
public class StrutsJQueryChartTaglibReferenceContributor extends StrutsJQueryTaglibReferenceContributorBase {

  private static final String CHART = "chart";
  private static final String CHART_DATA = "chartData";

  @NotNull
  @Override
  protected String getNamespace() {
    return StrutsConstants.TAGLIB_JQUERY_CHART_PLUGIN_URI;
  }

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {

    // common attributes
    installCSS(registrar, CHART);
    installEvents(registrar, CHART);
    installIndicator(registrar, CHART);
    installResizable(registrar, CHART);
    installSelectable(registrar, CHART);
    installSortable(registrar, CHART);

    // specific attributes
    registerBoolean("pie", registrar, CHART);
    registerBoolean("pieLabel", registrar, CHART);
    registerBoolean("legendShow", registrar, CHART);
    registerTags(new StaticStringValuesReferenceProvider("ne", "nw", "se", "sw"),
                 "legendPosition", registrar, CHART);

    registerBoolean("deferredLoading", registrar, CHART_DATA);
    registerBoolean("clickable", registrar, CHART_DATA);
    registerBoolean("hoverable", registrar, CHART_DATA);
    registerBoolean("curvedLines", registrar, CHART_DATA);
    registerBoolean("curvedLinesFill", registrar, CHART_DATA);
  }
}
