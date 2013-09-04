package org.osmorc.manifest.impl;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightIdeaTestCase;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.manifest.BundleManifest;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link BundleManifestImpl}.
 */
public class BundleManifestTest extends LightIdeaTestCase {
  private static final String Manifest1 =
    "Bundle-SymbolicName: foo.bar\n" +
    "Bundle-Version: 1.0.0\n" +
    "Export-Package: foo.bar.baz;version= 1.0.0,foo.bar.bam;version= 1.0.0\n";

  private static final String Manifest2 =
    "Bundle-SymbolicName: foo.bar\n" +
    "Bundle-Version: 2.1.1\n" +
    "Export-Package: foo.bar.baz;version= 1.0.0,foo.bar.bam;version= 2.1.1\n";

  private static final String Manifest3 =
    "Bundle-SymbolicName: foo.bam\n" +
    "Bundle-Version: 1.0.0\n" +
    "Export-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\n" +
    "Require-Bundle: foo.bar";

  private static final String Manifest4 =
    "Bundle-SymbolicName: foo.bam\n" +
    "Bundle-Version: 1.0.0\n" +
    "Export-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\n" +
    "Require-Bundle: foo.bar;bundle-version=1.0.0\n";

  private static final String Manifest5 =
    "Bundle-SymbolicName: foo.bam\n" +
    "Bundle-Version: 1.0.0\n" +
    "Export-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\n" +
    "Require-Bundle: foo.bar;bundle-version=\"(2.0.0, 2.5.0]\"\n";

  private static final String Manifest6 =
    "Bundle-SymbolicName: foo.bam\n" +
    "Bundle-Version: 1.0.0\n" +
    "Export-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\n" +
    "Require-Bundle: foo.bar;bundle-version=\"(2.0.0, 2.5.0]\",foo.bam,foo.baz;bundle-version=10.0.5\n";

  private static final String Manifest7 =
    "Bundle-SymbolicName: foo.bam\n" +
    "Bundle-Version: 1.0.0\n" +
    "Export-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\n" +
    "Require-Bundle: foo.bar;bundle-version=\"(2.0.0, 2.5.0]\";visibility:=reexport\n";

  private static final String Manifest8 =
    "Bundle-SymbolicName: foo.bam\n" +
    "Bundle-Version: 1.0.0\n" +
    "Export-Package: foo.bam.baz;version= 1.0.0,foo.bam.bam;version= 2.1.1\n" +
    "Fragment-Host: foo.bar;bundle-version=\"(2.0.0, 2.5.0]\"\n";

  public void testExportsPackage() {
    BundleManifest bundleManifest = getManifest(Manifest1);

    assertThat(bundleManifest.getBundleSymbolicName(), equalTo("foo.bar"));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz"), is(true));
    assertThat(bundleManifest.exportsPackage("foo.bar.bam"), is(true));
    assertThat(bundleManifest.exportsPackage("naff.blah"), is(false));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz;version=1.0.0"), is(true));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz;version=2.0.0"), is(false));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz;version=\"[1.0.0, 2.0.0)\""), is(true));
    assertThat(bundleManifest.exportsPackage("foo.bar.baz;version=\"[1.1.0, 2.0.0)\""), is(false));
  }

  public void testSimpleRequireBundle() {
    BundleManifest providerManifest1 = getManifest(Manifest1);
    BundleManifest providerManifest2 = getManifest(Manifest1);
    BundleManifest requestorManifest = getManifest(Manifest3);

    List<String> requiredBundles = requestorManifest.getRequiredBundles();
    assertThat(requiredBundles.size(), is(1));
    assertThat(providerManifest1.isRequiredBundle(requiredBundles.get(0)), is(true));
    assertThat(providerManifest2.isRequiredBundle(requiredBundles.get(0)), is(true));
  }

  public void testVersionedRequireBundle() {
    BundleManifest providerManifest1 = getManifest(Manifest1);
    BundleManifest providerManifest2 = getManifest(Manifest2);
    BundleManifest requestorManifest = getManifest(Manifest4);

    List<String> requiredBundles = requestorManifest.getRequiredBundles();
    assertThat(requiredBundles.size(), is(1));
    assertThat(providerManifest1.isRequiredBundle(requiredBundles.get(0)), is(true));
    assertThat(providerManifest2.isRequiredBundle(requiredBundles.get(0)), is(true));  // 1.0.0 is implicit [1.0.0, INF], hence true
  }

  public void testVersionRangeRequireBundle() {
    BundleManifest providerManifest1 = getManifest(Manifest1);
    BundleManifest providerManifest2 = getManifest(Manifest2);
    BundleManifest requestorManifest = getManifest(Manifest5);

    List<String> requiredBundles = requestorManifest.getRequiredBundles();
    assertThat(requiredBundles.size(), is(1));
    assertThat(providerManifest1.isRequiredBundle(requiredBundles.get(0)), is(false));
    assertThat(providerManifest2.isRequiredBundle(requiredBundles.get(0)), is(true));
  }

  public void testMultipleRequirements() {
    BundleManifest requestorManifest = getManifest(Manifest6);

    List<String> requiredBundles = requestorManifest.getRequiredBundles();
    assertThat(requiredBundles.size(), is(3));
    assertThat(requiredBundles.get(0), equalTo("foo.bar;bundle-version=\"(2.0.0, 2.5.0]\""));
    assertThat(requiredBundles.get(1), equalTo("foo.bam"));
    assertThat(requiredBundles.get(2), equalTo("foo.baz;bundle-version=10.0.5"));
  }

  public void testReexport() {
    BundleManifest providerManifest = getManifest(Manifest2);
    BundleManifest requestorManifest1 = getManifest(Manifest5);
    BundleManifest requestorManifest2 = getManifest(Manifest7);

    assertThat(requestorManifest1.reExportsBundle(providerManifest), is(false));
    assertThat(requestorManifest2.reExportsBundle(providerManifest), is(true));
  }

  public void testFragmentBundles() {
    BundleManifest potentialHost1 = getManifest(Manifest1);
    BundleManifest potentialHost2 = getManifest(Manifest2);
    BundleManifest fragment = getManifest(Manifest8);

    assertThat(fragment.isFragmentBundle(), is(true));
    assertThat(potentialHost1.isFragmentBundle(), is(false));
    assertThat(potentialHost2.isFragmentBundle(), is(false));
    assertThat(potentialHost1.isFragmentHostFor(fragment), is(false));
    assertThat(potentialHost2.isFragmentHostFor(fragment), is(true));
  }

  private static BundleManifest getManifest(String text) {
    PsiFile file = createLightFile("MANIFEST.MF", text);
    return new BundleManifestImpl((ManifestFile)file);
  }
}
