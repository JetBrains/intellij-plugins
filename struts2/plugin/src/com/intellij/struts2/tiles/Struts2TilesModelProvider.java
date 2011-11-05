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

package com.intellij.struts2.tiles;

import com.intellij.javaee.model.xml.Listener;
import com.intellij.javaee.model.xml.ParamValue;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.jsp.WebDirectoryUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts.StrutsPluginDomFactory;
import com.intellij.struts.StrutsProjectComponent;
import com.intellij.struts.TilesModel;
import com.intellij.struts.TilesModelProvider;
import com.intellij.struts.dom.tiles.TilesDefinitions;
import com.intellij.struts.psi.TilesModelImpl;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.util.xml.DomFileElement;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Provides Tiles-model configured via {@code org.apache.struts2.tiles.StrutsTilesListener} or
 * {@code org.apache.tiles.listener.TilesListener} in web.xml.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2TilesModelProvider implements TilesModelProvider {

  @NonNls
  private static final String STRUTS_TILES_LISTENER_CLASS = "org.apache.struts2.tiles.StrutsTilesListener";

  @NonNls
  private static final String TILES_LISTENER_CLASS = "org.apache.tiles.listener.TilesListener";

  @NonNls
  private static final String TILES_CONTEXT_PARAM = "org.apache.tiles.impl.BasicTilesContainer.DEFINITIONS_CONFIG";

  @NonNls
  private static final String DEFAULT_TILES_XML = "/WEB-INF/tiles.xml";

  @NonNls
  private static final String STRUTS2_TILES_MODEL = "struts2TilesModel";

  private static final Condition<ParamValue> TILES_CONTEXT_PARAM_CONDITION = new Condition<ParamValue>() {
    public boolean value(final ParamValue paramValue) {
      return Comparing.equal(TILES_CONTEXT_PARAM, paramValue.getParamName().getStringValue());
    }
  };

  @NotNull
  public Collection<TilesModel> computeModels(@NotNull final Module module) {
    final Project project = module.getProject();
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    final GlobalSearchScope moduleScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);

    // struts2-tiles plugin must be available
    final PsiClass strutsTilesListenerClass = facade.findClass(STRUTS_TILES_LISTENER_CLASS,
                                                               moduleScope);
    if (strutsTilesListenerClass == null) {
      return Collections.emptyList();
    }

    final PsiClass tilesListenerClass = facade.findClass(TILES_LISTENER_CLASS, moduleScope);

    final StrutsPluginDomFactory<TilesDefinitions, TilesModel> factory =
        StrutsProjectComponent.getInstance(project).getTilesFactory();

    final Set<TilesModel> struts2TilesModels = new HashSet<TilesModel>();
    final Consumer<Set<XmlFile>> consumer = new Consumer<Set<XmlFile>>() {
      public void consume(final Set<XmlFile> definitions) {
        final DomFileElement<TilesDefinitions> domFileElement = factory.createMergedModelRoot(definitions);
        if (domFileElement != null) {
          struts2TilesModels.add(new TilesModelImpl(definitions, domFileElement, STRUTS2_TILES_MODEL));
        }
      }
    };

    final WebDirectoryUtil webDirectoryUtil = WebDirectoryUtil.getWebDirectoryUtil(project);
    final Collection<WebFacet> webFacets = WebFacet.getInstances(module);
    for (final WebFacet webFacet : webFacets) {
      final WebApp webApp = webFacet.getRoot();
      if (webApp == null) {
        continue;
      }

      // determine configured tiles config files
      @NonNls final Set<String> tilesConfigNames = findConfiguredTilesPaths(webApp);

      // no configured paths? use default
      if (tilesConfigNames.isEmpty()) {
        tilesConfigNames.add(DEFAULT_TILES_XML);
      }

      // resolve to XmlFile
      final Set<XmlFile> tilesFileSet = new HashSet<XmlFile>();
      for (final String tilesPath : tilesConfigNames) {
        final PsiElement tilesXmlFile = webDirectoryUtil.findFileByPath(tilesPath, webFacet);
        if (tilesXmlFile instanceof XmlFile) {
          tilesFileSet.add((XmlFile) tilesXmlFile);
        }
      }

      final List<Listener> listenerList = webApp.getListeners();
      for (final Listener listener : listenerList) {
        final PsiClass listenerClass = listener.getListenerClass().getValue();
        if (strutsTilesListenerClass.equals(listenerClass) ||
            Comparing.equal(tilesListenerClass, listenerClass)) {
          consumer.consume(tilesFileSet);
        }
      }
    }

    return struts2TilesModels;
  }

  /**
   * Returns the configured tiles.xml file paths (if configured).
   *
   * @param webApp Web application.
   * @return File names.
   */
  private static Set<String> findConfiguredTilesPaths(final WebApp webApp) {
    @NonNls final Set<String> tilesConfigNames = new THashSet<String>();
    final ParamValue tilesParamValue = ContainerUtil.find(webApp.getContextParams(), TILES_CONTEXT_PARAM_CONDITION);
    if (tilesParamValue == null) {
      return tilesConfigNames;
    }

    final String paramValue = tilesParamValue.getParamValue().getStringValue();
    if (paramValue == null) {
      return tilesConfigNames;
    }

    for (final String file : StringUtil.split(paramValue, ",")) {
      tilesConfigNames.add(file.trim());
    }

    return tilesConfigNames;
  }

}