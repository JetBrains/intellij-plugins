package org.osmorc.impl;

import com.intellij.testFramework.LightIdeaTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.junit.Before;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderDisposedException;
import org.osmorc.testutil.ManifestMaker;
import org.osmorc.testutil.TestManifestHolder;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.osmorc.testutil.ManifestMaker.bundleSymbolicName;

/**
 * Test for {@link org.osmorc.impl.BundleCache}.
 */
public class BundleCacheTest extends LightIdeaTestCase {
  private BundleCache myCache;
  private ManifestHolder myFragmentHolder;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    myCache = new BundleCache();
    myCache.updateWith(makeManifestHolder(
      "MANIFEST.MF",
      bundleSymbolicName("foo.bar").bundleVersion("1.0.0").exportPackages("foo.bar")));
    myCache.updateWith(makeManifestHolder(
      "MANIFEST2.MF",
      bundleSymbolicName("foo.bam").bundleVersion("1.2.0").exportPackages("foo.bam;version=1.2.0").importPackages("foo.bar")));
    myCache.updateWith(makeManifestHolder(
      "MANIFEST3.MF",
      bundleSymbolicName("foo.bam").bundleVersion("1.2.3").exportPackages("foo.bam; version=1.2.3").importPackages("foo.bar")));
    myCache.updateWith(makeManifestHolder(
      "MANIFEST4.MF",
      bundleSymbolicName("foo.baz").bundleVersion("1.2.1").exportPackages("foo.baz;version=1.2.0")
        .importPackages("foo.bar", "foo.bam;version=\"[1.2.3,2.0.0)\"")));
    myCache.updateWith(myFragmentHolder = makeManifestHolder(
      "MANIFEST5.MF",
      bundleSymbolicName("foo.bar.naff").bundleVersion("1.0.1").exportPackages("foo.bar.naff;version=1.0.1").importPackages("foo.bar")
        .fragmentHost("foo.bar")));
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

  public void testConcurrentUpdates() {
    Thread updateThread1 = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          myCache.updateWith(makeManifestHolder("MANIFEST_T1_" + i + ".MF",  bundleSymbolicName("foo.bar.t1").bundleVersion("1.0."+i)));
        }
        System.out.println("T1 done");
      }
    });
    Thread updateThread2 = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 1000; i++) {
          myCache.updateWith(makeManifestHolder("MANIFEST_T2_" + i + ".MF", bundleSymbolicName("foo.bar.t2").bundleVersion("1.0."+i)));
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

  @NotNull
  private static ManifestHolder makeManifestHolder(@NotNull String fileName, @NotNull ManifestMaker builder) {
    return new TestManifestHolder((ManifestFile)createLightFile(fileName, builder.toString()));
  }

  @NotNull
  private static BundleManifest getManifest(ManifestHolder manifestHolder) throws ManifestHolderDisposedException {
    BundleManifest manifest = manifestHolder.getBundleManifest();
    assertNotNull(manifestHolder.toString(), manifest);
    return manifest;
  }
}
