package org.osmorc.impl;

import com.intellij.testFramework.LightIdeaTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.TestManifestHolder;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link org.osmorc.impl.BundleCache}.
 */
public class BundleCacheTest extends LightIdeaTestCase {
  private BundleCache myCache;
  private ManifestHolder myFragmentHolder;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    myCache = new BundleCache();
    myCache.updateWith(makeManifestHolder(
      "MANIFEST.MF",
      "Bundle-SymbolicName: foo.bar\n" +
      "Bundle-Version: 1.0.0\n" +
      "Export-Package: foo.bar"));
    myCache.updateWith(makeManifestHolder(
      "MANIFEST2.MF",
      "Bundle-SymbolicName: foo.bam\n" +
      "Bundle-Version: 1.2.0\n" +
      "Export-Package: foo.bam;version=1.2.0\n" +
      "Import-Package: foo.bar"));
    myCache.updateWith(makeManifestHolder(
      "MANIFEST3.MF",
      "Bundle-SymbolicName: foo.bam\n" +
      "Bundle-Version: 1.2.3\n" +
      "Export-Package: foo.bam; version=1.2.3\n" +
      "Import-Package: foo.bar"));
    myCache.updateWith(makeManifestHolder(
      "MANIFEST4.MF",
      "Bundle-SymbolicName: foo.baz\n" +
      "Bundle-Version: 1.2.1\n" +
      "Export-Package: foo.baz;version=1.2.0\n" +
      "Import-Package: foo.bar, foo.bam;version=\"[1.2.3,2.0.0)\""));
    myCache.updateWith(myFragmentHolder = makeManifestHolder(
      "MANIFEST5.MF",
      "Bundle-SymbolicName: foo.bar.naff\n" +
      "Bundle-Version: 1.0.1\n" +
      "Export-Package: foo.bar.naff;version=1.0.1\n" +
      "Import-Package: foo.bar\n" +
      "Fragment-Host: foo.bar"));
  }

  public void testBundleLookupWithMoreThanOneResult() {
    List<ManifestHolder> manifestHolders = myCache.whoIs("foo.bam");
    assertThat(manifestHolders, notNullValue());
    assertThat(manifestHolders.size(), is(2));
  }

  public void testSingleBundleLookup() throws ManifestHolderDisposedException {
    List<ManifestHolder> manifestHolders = myCache.whoIs("foo.bar");
    assertThat(manifestHolders, notNullValue());
    assertThat(manifestHolders.size(), is(1));
    assertThat(getManifest(manifestHolders.get(0)).getBundleSymbolicName(), equalTo("foo.bar"));
  }

  public void testFragmentHost() throws ManifestHolderDisposedException {
    Set<ManifestHolder> fragmentHosts = myCache.getFragmentHosts(myFragmentHolder);
    assertThat(fragmentHosts, notNullValue());
    assertThat(fragmentHosts.size(), is(1));
    assertThat(getManifest(fragmentHosts.iterator().next()).getBundleSymbolicName(), equalTo("foo.bar"));
  }

  public void testRequiredBundleWithVersion() throws ManifestHolderDisposedException {
    ManifestHolder manifestHolder = myCache.whoIsRequiredBundle("foo.bam;bundle-version=1.2.0");
    assertThat(manifestHolder, notNullValue());
    BundleManifest bundleManifest = getManifest(manifestHolder);
    assertThat(bundleManifest.getBundleSymbolicName(), equalTo("foo.bam"));

    // should pick largest available version
    assertThat(bundleManifest.getBundleVersion().toString(), equalTo("1.2.3"));
  }

  public void testRequiredBundleWithVersionRange() throws ManifestHolderDisposedException {
    ManifestHolder manifestHolder = myCache.whoIsRequiredBundle("foo.bam;bundle-version=\"[1.2.0,1.2.3)\"");
    assertThat(manifestHolder, notNullValue());
    BundleManifest bundleManifest = getManifest(manifestHolder);
    assertThat(bundleManifest.getBundleSymbolicName(), equalTo("foo.bam"));
    // 1.2.3 is not allowed by version range.
    assertThat(bundleManifest.getBundleVersion().toString(), equalTo("1.2.0"));
  }

  public void testRequiredBundleWithoutVersion() throws ManifestHolderDisposedException {
    ManifestHolder manifestHolder = myCache.whoIsRequiredBundle("foo.bam");
    assertThat(manifestHolder, notNullValue());
    BundleManifest bundleManifest = getManifest(manifestHolder);
    assertThat(bundleManifest.getBundleSymbolicName(), equalTo("foo.bam"));

    // should pick largest available version
    assertThat(bundleManifest.getBundleVersion().toString(), equalTo("1.2.3"));
  }

  private static ManifestHolder makeManifestHolder(String fileName, String text) {
    return new TestManifestHolder((ManifestFile)createLightFile(fileName, text));
  }

  @NotNull
  private static BundleManifest getManifest(ManifestHolder manifestHolder) throws ManifestHolderDisposedException {
    BundleManifest manifest = manifestHolder.getBundleManifest();
    assertNotNull(manifestHolder.toString(), manifest);
    return manifest;
  }
}
