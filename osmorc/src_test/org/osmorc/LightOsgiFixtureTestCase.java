// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacetType;

import java.io.File;

public abstract class LightOsgiFixtureTestCase extends LightCodeInsightFixtureTestCase {
  private static final DefaultLightProjectDescriptor OSGi_DESCRIPTOR = new DefaultLightProjectDescriptor() {
    @Override
    public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
      super.configureModule(module, model, contentEntry);

      String libPath = PluginPathManager.getPluginHomePath("osmorc") + "/lib";
      PsiTestUtil.addLibrary(module, model, "osgi.core", libPath, "org.apache.felix.framework-4.2.1.jar");
      PsiTestUtil.addLibrary(module, model, "plexus", libPath, "plexus-utils-3.0.10.jar");

      String annotationsPath = PathManager.getJarPathForClass(NotNull.class);
      assertNotNull(annotationsPath);
      File annotations = new File(annotationsPath);
      PsiTestUtil.addLibrary(module, model, "annotations", annotations.getParent(), annotations.getName());

      FacetManager.getInstance(module).addFacet(OsmorcFacetType.getInstance(), "OSGi", null);
    }
  };

  protected OsmorcFacet myFacet;
  protected OsmorcFacetConfiguration myConfiguration;

  @NotNull
  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return OSGi_DESCRIPTOR;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myFacet = OsmorcFacet.getInstance(myModule);
    assertNotNull(myFacet);

    myConfiguration = myFacet.getConfiguration();
    myConfiguration.setUseProjectDefaultManifestFileLocation(false);
    myConfiguration.setManifestLocation("META-INF/MANIFEST.MF");
    myConfiguration.setManifestGenerationMode(ManifestGenerationMode.Manually);
    myConfiguration.setBndFileLocation("bnd.bnd");
  }
}
