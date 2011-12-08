package org.osmorc.impl;

import com.intellij.testFramework.LightIdeaTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;
import org.osmorc.manifest.impl.AbstractManifestHolderImpl;
import org.osmorc.manifest.impl.BundleManifestImpl;
import org.osmorc.manifest.lang.psi.ManifestFile;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for the bundle cache
 */
public class BundleCacheTest extends LightIdeaTestCase {


  private BundleCache myCache;
  private ManifestHolder myFragmentHolder;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    myCache = new BundleCache();
    myCache.updateWith(makeManifestHolder("MANIFEST.MF", "foo.bar", "1.0.0", "foo.bar", "", null));
    myCache.updateWith(makeManifestHolder("MANIFEST2.MF", "foo.bam", "1.2.0", "foo.bam; version=1.2.0", "foo.bar", null));
    myCache.updateWith(makeManifestHolder("MANIFEST3.MF", "foo.bam", "1.2.3", "foo.bam; version=1.2.3", "foo.bar", null));
    myCache.updateWith(
      makeManifestHolder("MANIFEST4.MF", "foo.baz", "1.2.1", "foo.baz; version=1.2.0", "foo.bar, foo.bam;version=\"[1.2.3,2.0.0)\"", null));

    myFragmentHolder =
      makeManifestHolder("MANIFEST5.MF", "foo.bar.narf", "1.0.1", "foo.bar.narf;version=1.0.1", "foo.bar", "foo.bar");
    myCache.updateWith(myFragmentHolder);
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

  @Test
  public void testFragmentHost() throws ManifestHolderDisposedException {
    Set<ManifestHolder> fragmentHosts = myCache.getFragmentHosts(myFragmentHolder);
    assertThat(fragmentHosts, notNullValue());
    assertThat(fragmentHosts.size(), is(1));
    assertThat(fragmentHosts.iterator().next().getBundleManifest().getBundleSymbolicName(), equalTo("foo.bar"));
  }

  @Test
  public void testRequiredBundleWithVersion() throws ManifestHolderDisposedException {
    ManifestHolder manifestHolder = myCache.whoIsRequiredBundle("foo.bam;bundle-version=1.2.0");
    assertThat(manifestHolder, notNullValue());
    BundleManifest bundleManifest = manifestHolder.getBundleManifest();
    assertThat(bundleManifest.getBundleSymbolicName(), equalTo("foo.bam"));

    // should pick largest available version
    assertThat(bundleManifest.getBundleVersion().toString(), equalTo("1.2.3"));
  }

  @Test
  public void testRequiredBundleWithVersionRange() throws ManifestHolderDisposedException {
    ManifestHolder manifestHolder = myCache.whoIsRequiredBundle("foo.bam;bundle-version=\"[1.2.0,1.2.3)\"");
    assertThat(manifestHolder, notNullValue());
    BundleManifest bundleManifest = manifestHolder.getBundleManifest();
    assertThat(bundleManifest.getBundleSymbolicName(), equalTo("foo.bam"));
    // 1.2.3 is not allowed by version range.
    assertThat(bundleManifest.getBundleVersion().toString(), equalTo("1.2.0"));
  }

  @Test
  public void testRequiredBundleWithoutVersion() throws ManifestHolderDisposedException {
    ManifestHolder manifestHolder = myCache.whoIsRequiredBundle("foo.bam");
    assertThat(manifestHolder, notNullValue());
    BundleManifest bundleManifest = manifestHolder.getBundleManifest();
    assertThat(bundleManifest.getBundleSymbolicName(), equalTo("foo.bam"));

    // should pick largest available version
    assertThat(bundleManifest.getBundleVersion().toString(), equalTo("1.2.3"));
  }

  @Test
  public void testConcurrentUpdates() {
    Thread updateThread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          myCache.updateWith(makeManifestHolder("MANIFEST_T1_" + i + ".MF", "foo.bar.t1", "1.0." + i, "", "", null));
        }
        System.out.println("T1 done");
      }
    });
    Thread updateThread2 = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          myCache.updateWith(makeManifestHolder("MANIFEST_T2_" + i + ".MF", "foo.bar.t2", "1.0." + i, "", "", null));
        }
        System.out.println("T2 done");
      }
    });
    CancelableRunnable cancelableRunnable = new CancelableRunnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000 && !requestStop; i++) {
          myCache.whoIs("foo.bar.t1");
        }
        System.out.println("T3 done");
      }
    };
    Thread readThread = new Thread(cancelableRunnable);

    updateThread1.start();
    updateThread2.start();
    readThread.start();

    long elapsed = 0;
    long last = System.currentTimeMillis();
    while (updateThread1.isAlive() || updateThread2.isAlive()) {
      elapsed += System.currentTimeMillis() - last;
      last = System.currentTimeMillis();
      if (elapsed > 10000) { // should be easily doable within 10 seconds
        fail("Probable deadlock in BundleCache");
      }
      try {
        //noinspection BusyWait
        Thread.sleep(500);
      }
      catch (InterruptedException ignore) {
        // ok
      }
    }
    cancelableRunnable.requestStop = true;
  }

  private static abstract class CancelableRunnable implements Runnable {
    public volatile boolean requestStop = false;
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
                                                   @NotNull String imports,
                                                   @Nullable String fragmentHost) {

    String myManifest = "Bundle-SymbolicName: " + symbolicName + "\n" +
                        "Bundle-Version: " + version + "\n" +
                        "Export-Package: " + exports + "\n" +
                        "Import-Package: " + imports + "\n";
    if (fragmentHost != null) {
      myManifest += "Fragment-Host: " + fragmentHost + "\n";
    }

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

      @Override
      public boolean isDisposed() {
        return false;
      }
    };
  }
}
