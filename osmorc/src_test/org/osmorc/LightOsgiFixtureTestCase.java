// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.osmorc;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacetType;

import java.io.File;

import static com.intellij.project.IntelliJProjectConfiguration.getModuleLibrary;

public abstract class LightOsgiFixtureTestCase extends LightJavaCodeInsightFixtureTestCase {
  private static final DefaultLightProjectDescriptor OSGi_DESCRIPTOR = new DefaultLightProjectDescriptor() {
    @Override
    public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel model, @NotNull ContentEntry contentEntry) {
      super.configureModule(module, model, contentEntry);

      PsiTestUtil.addProjectLibrary(model, "bndlib", getModuleLibrary("intellij.osgi.jps", "bndlib").getClassesPaths());
      PsiTestUtil.addProjectLibrary(model, "bndlib-repository", getModuleLibrary("intellij.osgi.jps", "bndlib-repository").getClassesPaths());
      PsiTestUtil.addProjectLibrary(model, "plexus-utils", getModuleLibrary("intellij.libraries.plexus.utils", "plexus-utils").getClassesPaths());

      String annotationsPath = PathManager.getJarPathForClass(NotNull.class);
      assertNotNull(annotationsPath);
      File annotations = new File(annotationsPath);
      PsiTestUtil.addLibrary(model, "annotations", annotations.getParent(), annotations.getName());

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

    myFacet = OsmorcFacet.getInstance(getModule());
    assertNotNull(myFacet);

    myConfiguration = myFacet.getConfiguration();
    myConfiguration.setUseProjectDefaultManifestFileLocation(false);
    myConfiguration.setManifestLocation("META-INF/MANIFEST.MF");
    myConfiguration.setManifestGenerationMode(ManifestGenerationMode.Manually);
    myConfiguration.setBndFileLocation("bnd.bnd");
  }
}