/*
 * Copyright 2020 The authors
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
package com.intellij.struts2;

import com.intellij.facet.FacetManager;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.javaee.DeploymentDescriptorsConstants;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.facet.WebFacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.struts2.facet.StrutsFacetType;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.MavenDependencyUtil;
import com.intellij.util.SmartList;
import com.intellij.util.descriptors.ConfigFileInfoSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Re-use (static) instances across as many tests as possible.
 * <p/>
 * Make sure to update equals/hashCode for any modifications in this class.
 *
 * @author Yann C&eacute;bron
 */
public final class Struts2ProjectDescriptorBuilder extends DefaultLightProjectDescriptor {

  private boolean addStrutsLibrary;
  private boolean addStrutsFacet;
  private boolean addWebFacet;

  private final List<String> mavenLibs = new SmartList<>();
  private final List<Callback> callbacks = new SmartList<>();

  public Struts2ProjectDescriptorBuilder withWebModuleType() {
    addWebFacet = true;
    return this;
  }

  public Struts2ProjectDescriptorBuilder withStrutsLibrary() {
    addStrutsLibrary = true;
    return this;
  }

  public Struts2ProjectDescriptorBuilder withStrutsConvention() {
    withMavenLibrary("org.apache.struts:struts2-convention-plugin:2.3.1");
    return this;
  }

  public Struts2ProjectDescriptorBuilder withStrutsFacet() {
    addStrutsFacet = true;
    return this;
  }

  public Struts2ProjectDescriptorBuilder withCallback(Callback callback) {
    callbacks.add(callback);
    return this;
  }

  public Struts2ProjectDescriptorBuilder withMavenLibrary(String coordinates) {
    mavenLibs.add(coordinates);
    return this;
  }

  public LightProjectDescriptor build() {
    return this;
  }

  @Override
  public Sdk getSdk() {
    return IdeaTestUtil.getMockJdk18();
  }

  @Override
  public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
    super.configureModule(module, model, contentEntry);

    if (addWebFacet) {
      IdeaTestUtil.addWebJarsToModule(model);
    }

    if (addStrutsLibrary) {
      BasicLightHighlightingTestCase.addStrutsJars(model);
    }

    for (String lib : mavenLibs) {
      MavenDependencyUtil.addFromMaven(model, lib);
    }

    final WebFacet webFacet = FacetUtil.addFacet(module, WebFacetType.getInstance());
    if (addStrutsFacet) {
      FacetManager.getInstance(module).addFacet(StrutsFacetType.getInstance(), "struts2", null);
    }

    if (addWebFacet) {
      final String sourceRootUrl = model.getSourceRootUrls()[0];
      webFacet.addWebRoot(sourceRootUrl, "/");

      final ConfigFileInfoSet descriptors = webFacet.getDescriptorsContainer().getConfiguration();
      descriptors.addConfigFile(DeploymentDescriptorsConstants.WEB_XML_META_DATA, sourceRootUrl + "/WEB-INF/web.xml");

      for (String url : ModuleRootManager.getInstance(module).getSourceRootUrls()) {
        webFacet.addWebSourceRoot(url);
      }
    }

    for (Callback callback : callbacks) {
      callback.configureModule(module, model, contentEntry);
    }
  }


  /**
   * Performs custom initialization.
   */
  public abstract static class Callback {

    protected abstract void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry);

    @Override
    public final int hashCode() {
      return this.getClass().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
      return obj instanceof Callback && obj.hashCode() == hashCode();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Struts2ProjectDescriptorBuilder builder = (Struts2ProjectDescriptorBuilder)o;

    if (addStrutsLibrary != builder.addStrutsLibrary) return false;
    if (addStrutsFacet != builder.addStrutsFacet) return false;
    if (addWebFacet != builder.addWebFacet) return false;
    if (!callbacks.equals(builder.callbacks)) return false;
    if (!mavenLibs.equals(builder.mavenLibs)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = (addStrutsLibrary ? 1 : 0);
    result = 31 * result + (addStrutsFacet ? 1 : 0);
    result = 31 * result + (addWebFacet ? 1 : 0);
    result = 31 * result + mavenLibs.hashCode();
    result = 31 * result + callbacks.hashCode();
    return result;
  }
}
