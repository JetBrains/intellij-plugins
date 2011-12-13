package org.osmorc.testutil;

import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.impl.AbstractManifestHolderImpl;
import org.osmorc.manifest.impl.BundleManifestImpl;
import org.osmorc.manifest.lang.psi.ManifestFile;

/**
 * Manifest holder which wraps an arbitrary manifest file. You can create files in thest using {@link com.intellij.testFramework.LightIdeaTestCase#createLightFile(String, String)}.
 */
public class TestManifestHolder extends AbstractManifestHolderImpl {
  BundleManifest manifest;

  public TestManifestHolder(ManifestFile lightFile) {
    manifest = new BundleManifestImpl(lightFile);
  }

  @NotNull
  @Override
  public BundleManifest getBundleManifest() {
    return manifest;
  }

  @Override
  public Object getBoundObject() {
    return null;
  }

  @Override
  public boolean isDisposed() {
    return false;
  }
}
