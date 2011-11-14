package org.osmorc.manifest.impl;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightIdeaTestCase;
import org.junit.Test;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.lang.psi.ManifestFile;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link BundleManifestImpl}
 */
public class BundleManifestTest extends LightIdeaTestCase {

  private static final String Manifest1 =
    "Bundle-SymbolicName: foo.bar\nBundle-Version: 1.0.0\nExport-Package: foo.bar.baz;version= 1.0.0,foo.bar.bam;version= 1.0.0\n";

  private static final String Manifest2 =
    "Bundle-SymbolicName: foo.bar\nBundle-Version: 2.1.1\nExport-Package: foo.bar.baz;version= 1.0.0,foo.bar.bam;version= 2.1.1\n";

  private static final String Manifest3 =
    "Bundle-SymbolicName: foo.bam\nBundle-Version: 1.0.0\nExport-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\nRequire-Bundle: foo.bar";
  private static final String Manifest4 =
    "Bundle-SymbolicName: foo.bam\nBundle-Version: 1.0.0\nExport-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\nRequire-Bundle: foo.bar;bundle-version=1.0.0";

  private static final String Manifest5 =
    "Bundle-SymbolicName: foo.bam\nBundle-Version: 1.0.0\nExport-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\nRequire-Bundle: foo.bar;bundle-version=\"(2.0.0, 2.5.0]\"";

  private static final String Manifest6 =
    "Bundle-SymbolicName: foo.bam\nBundle-Version: 1.0.0\nExport-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\nRequire-Bundle: foo.bar;bundle-version=\"(2.0.0, 2.5.0]\",foo.bam,foo.baz;bundle-version=10.0.5";

  private static final String Manifest7 =
    "Bundle-SymbolicName: foo.bam\nBundle-Version: 1.0.0\nExport-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\nRequire-Bundle: foo.bar;bundle-version=\"(2.0.0, 2.5.0]\";visibility:=reexport";

  private static final String Manifest8 =
    "Bundle-SymbolicName: foo.bam\nBundle-Version: 1.0.0\nExport-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\nFragment-Host: foo.bar;bundle-version=\"(2.0.0, 2.5.0]\"";
  
  @Test
  public void testExportsPackage() {
    PsiFile lightFile = createLightFile("MANIFEST.MF", Manifest1);
    BundleManifest bundleManifest = new BundleManifestImpl((ManifestFile)lightFile);

    assertThat(bundleManifest.getBundleSymbolicName(), equalTo("foo.bar"));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz"), is(true));
    assertThat(bundleManifest.exportsPackage("foo.bar.bam"), is(true));
    assertThat(bundleManifest.exportsPackage("narf.blah"), is(false));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz;version=1.0.0"), is(true));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz;version=2.0.0"), is(false));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz;version=\"[1.0.0, 2.0.0)\""), is(true));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz;version=\"[1.1.0, 2.0.0)\""), is(false));
  }

  @Test
  public void testSimpleRequireBundle() {
    PsiFile provider1 = createLightFile("MANIFEST.MF", Manifest1);
    BundleManifestImpl providerManifest1 = new BundleManifestImpl((ManifestFile)provider1);

    PsiFile provider2 = createLightFile("MANIFEST2.MF", Manifest1);
    BundleManifestImpl providerManifest2 = new BundleManifestImpl((ManifestFile)provider2);

    PsiFile requestor = createLightFile("MANIFEST3.MF", Manifest3);
    BundleManifestImpl requestorManifest = new BundleManifestImpl((ManifestFile)requestor);

    List<String> requiredBundles = requestorManifest.getRequiredBundles();
    assertThat(requiredBundles.size(), is(1));
    assertThat(providerManifest1.isRequiredBundle(requiredBundles.iterator().next()), is(true));
    assertThat(providerManifest2.isRequiredBundle(requiredBundles.iterator().next()), is(true));
  }

  @Test
  public void testVersionedRequireBundle() {
    PsiFile provider1 = createLightFile("MANIFEST.MF", Manifest1);
    BundleManifestImpl providerManifest1 = new BundleManifestImpl((ManifestFile)provider1);

    PsiFile provider2 = createLightFile("MANIFEST2.MF", Manifest2);
    BundleManifestImpl providerManifest2 = new BundleManifestImpl((ManifestFile)provider2);

    PsiFile requestor = createLightFile("MANIFEST4.MF", Manifest4);
    BundleManifestImpl requestorManifest = new BundleManifestImpl((ManifestFile)requestor);

    List<String> requiredBundles = requestorManifest.getRequiredBundles();
    assertThat(requiredBundles.size(), is(1));
    assertThat(providerManifest1.isRequiredBundle(requiredBundles.iterator().next()), is(true));
    assertThat(providerManifest2.isRequiredBundle(requiredBundles.iterator().next()), is(true)); // 1.0.0 is implict [1.0.0, infinity], so should be true
  }

  @Test
  public void testVersionRangeRequireBundle() {
    PsiFile provider1 = createLightFile("MANIFEST.MF", Manifest1);
    BundleManifestImpl providerManifest1 = new BundleManifestImpl((ManifestFile)provider1);

    PsiFile provider2 = createLightFile("MANIFEST2.MF", Manifest2);
    BundleManifestImpl providerManifest2 = new BundleManifestImpl((ManifestFile)provider2);

    PsiFile requestor = createLightFile("MANIFEST5.MF", Manifest5);
    BundleManifestImpl requestorManifest = new BundleManifestImpl((ManifestFile)requestor);

    List<String> requiredBundles = requestorManifest.getRequiredBundles();
    assertThat(requiredBundles.size(), is(1));
    assertThat(providerManifest1.isRequiredBundle(requiredBundles.iterator().next()), is(false));
    assertThat(providerManifest2.isRequiredBundle(requiredBundles.iterator().next()), is(true));
  }

  @Test
  public void testMultipleRequirements() {
    PsiFile requestor = createLightFile("MANIFEST6.MF", Manifest6);
    BundleManifestImpl requestorManifest = new BundleManifestImpl((ManifestFile)requestor);
    List<String> requiredBundles = requestorManifest.getRequiredBundles();
    assertThat(requiredBundles.size(), is(3));
    assertThat(requiredBundles.get(0), equalTo("foo.bar;bundle-version=\"(2.0.0, 2.5.0]\""));
    assertThat(requiredBundles.get(1), equalTo("foo.bam"));
    assertThat(requiredBundles.get(2), equalTo("foo.baz;bundle-version=10.0.5"));
  }

  @Test
  public void testReexport() {
    PsiFile provider = createLightFile("MANIFEST2.MF", Manifest2);
    BundleManifestImpl providerManifest = new BundleManifestImpl((ManifestFile)provider);

    PsiFile requestor1 = createLightFile("MANIFEST5.MF", Manifest5);
    BundleManifestImpl requestorManifest1 = new BundleManifestImpl((ManifestFile)requestor1);

    PsiFile requestor2 = createLightFile("MANIFEST7.MF", Manifest7);
    BundleManifestImpl requestorManifest2 = new BundleManifestImpl((ManifestFile)requestor2);

    assertThat(requestorManifest1.reExportsBundle(providerManifest), is(false));
    assertThat(requestorManifest2.reExportsBundle(providerManifest), is(true));
  }


  @Test
  public void testFragmenBundles() {
    PsiFile potentialHost1File = createLightFile("MANIFEST.MF", Manifest1);
    BundleManifestImpl potentialHost1 = new BundleManifestImpl((ManifestFile)potentialHost1File);

    PsiFile potentialHost2File = createLightFile("MANIFEST2.MF", Manifest2);
    BundleManifestImpl potentialHost2 = new BundleManifestImpl((ManifestFile)potentialHost2File);

    PsiFile fragmenFile = createLightFile("MANIFEST8.MF", Manifest8);
    BundleManifestImpl fragment = new BundleManifestImpl((ManifestFile)fragmenFile);

    assertThat(fragment.isFragmentBundle(), is(true));
    assertThat(potentialHost1.isFragmentBundle(), is(false));
    assertThat(potentialHost2.isFragmentBundle(), is(false));
    assertThat(potentialHost1.isFragmentHostFor(fragment), is(false));
    assertThat(potentialHost2.isFragmentHostFor(fragment), is(true));
  }
}
