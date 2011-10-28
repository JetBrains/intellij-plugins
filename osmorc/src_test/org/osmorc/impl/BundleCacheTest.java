package org.osmorc.impl;

import com.intellij.testFramework.LightIdeaTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;
import org.osmorc.manifest.impl.AbstractManifestHolderImpl;
import org.osmorc.manifest.impl.BundleManifestImpl;
import org.osmorc.manifest.lang.psi.ManifestFile;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for the bundle cache
 */
public class BundleCacheTest extends LightIdeaTestCase {


  private BundleCache myCache;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    myCache = new BundleCache();
    myCache.updateWith(makeManifestHolder("MANIFEST.MF", "foo.bar", "1.0.0", "foo.bar", ""));
    myCache.updateWith(makeManifestHolder("MANIFEST2.MF", "foo.bam", "1.2.0", "foo.bam; version=1.2.0", "foo.bar"));
    myCache.updateWith(makeManifestHolder("MANIFEST3.MF", "foo.bam", "1.2.3", "foo.bam; version=1.2.3", "foo.bar"));
    myCache.updateWith(
      makeManifestHolder("MANIFEST4.MF", "foo.baz", "1.2.1", "foo.baz; version=1.2.0", "foo.bar, foo.bam;version=\"[1.2.3,2.0.0)\""));
  }


  @Test
  public void testBundleLookupWithMoreThanOneResult() {
    List<ManifestHolder> manifestHolders = myCache.whoIs("foo.bam");
    assertThat(manifestHolders, notNullValue());
    assertThat(manifestHolders.size(), is(2));
  }


  @Test
  public void testSingleBundleLookup() throws ManifestHolderDisposedException {
    List<ManifestHolder> manifestHolders = myCache.whoIs("foo.bar");
    assertThat(manifestHolders, notNullValue());
    assertThat(manifestHolders.size(), is(1));
    assertThat(manifestHolders.get(0).getBundleManifest().getBundleSymbolicName(), equalTo("foo.bar"));
  }

  /**
   * Creates a manifest holder
   *
   * @param manifestName name of the manifest file
   * @param symbolicName bundle symbolic name
   * @param version      version
   * @param exports      export packages
   * @param imports      import packages.
   * @return
   */

  @NotNull
  private static ManifestHolder makeManifestHolder(@NotNull String manifestName,
                                                   @NotNull String symbolicName,
                                                   @NotNull String version,
                                                   @NotNull String exports,
                                                   @NotNull String imports) {

    String myManifest = "Bundle-SymbolicName: " + symbolicName + "\n" +
                        "Bundle-Version: " + version + "\n" +
                        "Export-Package: " + exports + "\n" +
                        "Import-Package: " + imports + "\n";

    final ManifestFile lightFile = (ManifestFile)createLightFile(manifestName, myManifest);
    return new AbstractManifestHolderImpl() {
      BundleManifest manifest = new BundleManifestImpl(lightFile);

      @NotNull
      @Override
      public BundleManifest getBundleManifest() {
        return manifest;
      }

      @Override
      public Object getBoundObject() {
        return null;
      }
    };
  }
}
