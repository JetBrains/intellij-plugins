package org.osmorc.impl;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.testFramework.LightIdeaTestCase;
import org.hamcrest.CoreMatchers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.impl.ManifestHolderRegistryImpl;
import org.osmorc.manifest.lang.psi.ManifestFile;
import org.osmorc.testutil.TestManifestHolder;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.osmorc.testutil.ManifestMaker.bundleSymbolicName;

/**
 * Test case for the bundle manager.
 */
public class BundleManagerTest extends LightIdeaTestCase {

  private MyBundleManager myBundleManager;
  private Library myFakeJarLibrary;
  public static final String FAKE_JAR_NAME = "fakeJar-1.0.0.jar";

  public void setUp() throws Exception {
    super.setUp();
    myBundleManager = new MyBundleManager(new ManifestHolderRegistryImpl(getProject()), getProject());

    myBundleManager.addManifestHolder(makeHolder("Manifest.MF", bundleSymbolicName("org.eclipse.ui").bundleVersion("3.4.1")
      .requireBundle("org.eclipse.ui.workbench;bundle-version=\"[3.3.0,4.0.0)\";visibility:=reexport").toString(), "org.eclipse.ui"));

    myBundleManager.addManifestHolder(makeHolder("Manifest2.MF", bundleSymbolicName("org.eclipse.ui.workbench").bundleVersion("3.4.1")
      .requireBundle("org.eclipse.core.expressions;bundle-version=\"[3.2.0,4.0.0)\";visibility:=reexport").toString(),
                                                 "org.eclipse.ui.workbench"));

    myBundleManager.addManifestHolder(makeHolder("Manifest3.MF", bundleSymbolicName("org.eclipse.core.expressions").bundleVersion("3.4.1")
      .toString(), "org.eclipse.core.expressions"));

    myBundleManager.addManifestHolder(makeHolder("Manifest4.MF", bundleSymbolicName("org.eclipse.circular.one").bundleVersion("3.4.1")
      .requireBundle("org.eclipse.circular.two;bundle-version=\"[3.2.0, 4.0.0)\";visibility:=reexport").toString(),
                                                 "org.eclipse.circular.one"));

    myBundleManager.addManifestHolder(makeHolder("Manifest5.MF", bundleSymbolicName("org.eclipse.circular.two").bundleVersion("3.4.1")
      .requireBundle("org.eclipse.circular.one;bundle-version=\"[3.2.0, 4.0.0)\";visibility:=reexport").toString(),
                                                 "org.eclipse.circular.two"));


    myBundleManager.addManifestHolder(makeHolder("Manifest6.MF", bundleSymbolicName("org.eclipse.fragment.host").bundleVersion("2.0.0")
      .requireBundle("org.eclipse.ui.workbench;bundle-version=\"[3.3.0,4.0.0)\";visibility:=reexport").toString(),
                                                 "org.eclipse.fragment.host"));

    myBundleManager.addManifestHolder(makeHolder("Manifest7.MF", bundleSymbolicName("org.eclipse.fragment.fragment").bundleVersion("2.0.0")
      .requireBundle("org.eclipse.circular.one;bundle-version=\"[3.3.0,4.0.0)\";visibility:=reexport")
      .fragmentHost("org.eclipse.fragment.host").toString(), "org.eclipse.fragment.fragment"));

    // this is inserted to trick the bundle manager in taking an older version of the fragment, which would lead to different (and wrong) dependencies.
    myBundleManager.addManifestHolder(makeHolder("Manifest8.MF", bundleSymbolicName("org.eclipse.fragment.fragment").bundleVersion("1.0.0")
      .requireBundle("org.eclipse.ui;bundle-version=\"[3.3.0,4.0.0)\";visibility:=reexport")
      .fragmentHost("org.eclipse.fragment.host").toString(), "org.eclipse.fragment.fragment.old"));


    // create a project level  library which contains a fake jar for bundle-classpath resolution.
    // since this doesn't necessarily need to be an OSGI-fied library it's not added to the bundle manager explicitly
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        LibraryTable.ModifiableModel projectLibraryModel = ProjectLibraryTable.getInstance(getProject()).getModifiableModel();
        myFakeJarLibrary = projectLibraryModel.createLibrary("fakeJar");
        Library.ModifiableModel libraryModel = myFakeJarLibrary.getModifiableModel();
        libraryModel.addRoot("file:///foo/bar/" + FAKE_JAR_NAME, OrderRootType.CLASSES);
        libraryModel.commit();
        projectLibraryModel.commit();
      }
    });
  }


  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    // make sure we don't have duplicate project level libraries.
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        LibraryTable.ModifiableModel projectLibraryModel = ProjectLibraryTable.getInstance(getProject()).getModifiableModel();
        projectLibraryModel.removeLibrary(myFakeJarLibrary);
        projectLibraryModel.commit();
      }
    });
  }

  /**
   * Tests resolving a require-bundle dependency which has visibility:=reexport set.
   */
  public void testResolveRequireBundleWithReexport() {
    myBundleManager.addManifestHolder(
      makeHolder("MyManifest.MF", bundleSymbolicName("foo.bar").bundleVersion("1.0.0").requireBundle("org.eclipse.ui").toString(),
                 getModule()));

    Set<Object> objects = myBundleManager.resolveDependenciesOf(getModule());
    assertThat(objects, CoreMatchers.notNullValue());
    assertThat(objects.contains("org.eclipse.ui"), is(true));
    assertThat(objects.contains("org.eclipse.ui.workbench"), is(true));
    assertThat(objects.contains("org.eclipse.core.expressions"), is(true));
    assertThat(objects.size(), is(3)); // no more no less
  }


  /**
   * Tests resolving a circular import.
   */
  public void testResolveCircularDependency() throws ExecutionException, InterruptedException {
    myBundleManager.addManifestHolder(
      makeHolder("MyManifest.MF", bundleSymbolicName("foo.bar").bundleVersion("1.0.0").requireBundle("org.eclipse.circular.one").toString(),
                 getModule()));

    // may run into infinite loop, so run it in a future task
    FutureTask<Set<Object>> bt = new FutureTask<Set<Object>>(new Callable<Set<Object>>() {
      @Override
      public Set<Object> call() throws Exception {
        return myBundleManager.resolveDependenciesOf(getModule());
      }
    });
    Executors.newFixedThreadPool(1).execute(bt);

    long elapsed = 0;
    long last = System.currentTimeMillis();
    while (!bt.isDone()) {
      elapsed += System.currentTimeMillis() - last;
      last = System.currentTimeMillis();
      if (elapsed > 1000) { // should be easily doable within 1 second
        bt.cancel(true);
        fail("Infinite loop detected in MyBundleManager while resolving circular dependencies.");
      }
      try {
        //noinspection BusyWait
        Thread.sleep(100);
      }
      catch (InterruptedException ignore) {
        // ok
      }
    }

    if (bt.isDone()) {
      Set<Object> objects = bt.get();
      assertThat(objects, CoreMatchers.notNullValue());
      assertThat(objects.contains("org.eclipse.circular.one"), is(true));
      assertThat(objects.contains("org.eclipse.circular.two"), is(true));
      assertThat(objects.size(), is(2)); //  no more no less
    }
  }

  /**
   * Tests resolving the bundle classpath.
   */
  public void testResolveBundleClassPath() {
    myBundleManager.addManifestHolder(
      makeHolder("MyManifest.MF",
                 bundleSymbolicName("foo.bar").bundleVersion("1.0.0").bundleClassPath("/narf/troz/" + FAKE_JAR_NAME).toString(),
                 getModule()));

    Set<Object> objects = myBundleManager.resolveDependenciesOf(getModule());
    assertThat(objects, notNullValue());
    assertThat(objects.contains(myFakeJarLibrary), is(true));
    // should be exactly one entry.
    assertThat(objects.size(), is(1));
  }


  /**
   * Tests resolving recursive requires with fragments and circular dependencies, bundle-classpath, etc. (the whole enchilada)
   */
  public void testResolveTheWholeEnchilada() throws ExecutionException, InterruptedException {
    myBundleManager.addManifestHolder(
      makeHolder("MyManifest.MF",
                 bundleSymbolicName("foo.bar").bundleVersion("1.0.0").requireBundle("org.eclipse.fragment.host")
                   .bundleClassPath(FAKE_JAR_NAME).toString(), getModule()));

    // may run into infinite loop, so run it in a future task
    FutureTask<Set<Object>> bt = new FutureTask<Set<Object>>(new Callable<Set<Object>>() {
      @Override
      public Set<Object> call() throws Exception {
        return myBundleManager.resolveDependenciesOf(getModule());
      }
    });
    Executors.newFixedThreadPool(1).execute(bt);

    long elapsed = 0;
    long last = System.currentTimeMillis();
    while (!bt.isDone()) {
      elapsed += System.currentTimeMillis() - last;
      last = System.currentTimeMillis();
      if (elapsed > 2000) { // should be easily doable within 2 second
        bt.cancel(true);
        fail("Infinite loop detected in MyBundleManager while resolving circular dependencies.");
      }
      try {
        //noinspection BusyWait
        Thread.sleep(100);
      }
      catch (InterruptedException ignore) {
        // ok
      }
    }

    if (bt.isDone()) {
      Set<Object> objects = bt.get();
      assertThat(objects, CoreMatchers.notNullValue());
      assertThat(objects.contains("org.eclipse.fragment.host"), is(true));
      assertThat(objects.contains("org.eclipse.circular.one"), is(true));
      assertThat(objects.contains("org.eclipse.circular.two"), is(true));
      assertThat(objects.contains("org.eclipse.ui.workbench"), is(true));
      assertThat(objects.contains("org.eclipse.core.expressions"), is(true));
      assertThat(objects.contains(myFakeJarLibrary), is(true));
      assertThat(objects.size(), is(6)); // no more, no less
    }
  }


  private static ManifestHolder makeHolder(@NotNull String fileName, @NotNull String contents, @Nullable Object boundObject) {
    TestManifestHolder result = new TestManifestHolder((ManifestFile)createLightFile(fileName, contents));
    result.setBoundObject(boundObject);
    return result;
  }
}
