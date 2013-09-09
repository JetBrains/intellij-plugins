package org.osmorc.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.util.TimeoutUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.TestManifestHolder;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.impl.ManifestHolderRegistryImpl;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Test case for the {@link org.osmorc.BundleManager}.
 */
public class BundleManagerTest extends LightIdeaTestCase {
  private static final String FAKE_JAR_NAME = "fakeJar-1.0.0.jar";

  private MyBundleManager myBundleManager;
  private Library myFakeJarLibrary;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    myBundleManager = new MyBundleManager(new ManifestHolderRegistryImpl(getProject()), getProject());
    myBundleManager.addManifestHolder(makeHolder(
      "Manifest.MF",
      "Bundle-SymbolicName: org.eclipse.ui\n" +
      "Bundle-Version: 3.4.1\n" +
      "Require-Bundle: org.eclipse.ui.workbench;bundle-version=\"[3.3.0,4.0.0)\";visibility:=reexport",
      "org.eclipse.ui"));
    myBundleManager.addManifestHolder(makeHolder(
      "Manifest2.MF",
      "Bundle-SymbolicName: org.eclipse.ui.workbench\n" +
      "Bundle-Version: 3.4.1\n" +
      "Require-Bundle: org.eclipse.core.expressions;bundle-version=\"[3.2.0,4.0.0)\";visibility:=reexport",
      "org.eclipse.ui.workbench"));
    myBundleManager.addManifestHolder(makeHolder(
      "Manifest3.MF",
      "Bundle-SymbolicName: org.eclipse.core.expressions\n" +
      "Bundle-Version: 3.4.1",
      "org.eclipse.core.expressions"));
    myBundleManager.addManifestHolder(makeHolder(
      "Manifest4.MF",
      "Bundle-SymbolicName: org.eclipse.circular.one\n" +
      "Bundle-Version: 3.4.1\n" +
      "Require-Bundle: org.eclipse.circular.two;bundle-version=\"[3.2.0, 4.0.0)\";visibility:=reexport",
      "org.eclipse.circular.one"));
    myBundleManager.addManifestHolder(makeHolder(
      "Manifest5.MF",
      "Bundle-SymbolicName: org.eclipse.circular.two\n" +
      "Bundle-Version: 3.4.1\n" +
      "Require-Bundle: org.eclipse.circular.one;bundle-version=\"[3.2.0, 4.0.0)\";visibility:=reexport",
      "org.eclipse.circular.two"));
    myBundleManager.addManifestHolder(makeHolder(
      "Manifest6.MF",
      "Bundle-SymbolicName: org.eclipse.fragment.host\n" +
      "Bundle-Version: 2.0.0\n" +
      "Require-Bundle: org.eclipse.ui.workbench;bundle-version=\"[3.3.0,4.0.0)\";visibility:=reexport",
      "org.eclipse.fragment.host"));
    myBundleManager.addManifestHolder(makeHolder(
      "Manifest7.MF",
      "Bundle-SymbolicName: org.eclipse.fragment.fragment\n" +
      "Bundle-Version: 2.0.0\n" +
      "Require-Bundle: org.eclipse.circular.one;bundle-version=\"[3.3.0,4.0.0)\";visibility:=reexport\n" +
      "Fragment-Host: org.eclipse.fragment.host",
      "org.eclipse.fragment.fragment"));

    // this is inserted to trick the bundle manager in taking an older version of the fragment,
    // which would lead to different (and wrong) dependencies
    myBundleManager.addManifestHolder(makeHolder(
      "Manifest8.MF",
      "Bundle-SymbolicName: org.eclipse.fragment.fragment\n" +
      "Bundle-Version: 1.0.0\n" +
      "Require-Bundle: org.eclipse.ui;bundle-version=\"[3.3.0,4.0.0)\";visibility:=reexport\n" +
      "Fragment-Host: org.eclipse.fragment.host",
      "org.eclipse.fragment.fragment.old"));

    // create a project level  library which contains a fake jar for bundle-classpath resolution.
    // since this doesn't necessarily need to be an OSGi-fied library it's not added to the bundle manager explicitly
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
    // make sure we don't have duplicate project level libraries.
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        ProjectLibraryTable.getInstance(getProject()).removeLibrary(myFakeJarLibrary);
      }
    });

    super.tearDown();
  }

  /**
   * Tests resolving a require-bundle dependency which has visibility:=reexport set.
   */
  public void testResolveRequireBundleWithReexport() {
    myBundleManager.addManifestHolder(makeHolder(
      "MyManifest.MF",
      "Bundle-SymbolicName: foo.bar\n" +
      "Bundle-Version: 1.0.0\n" +
      "Require-Bundle: org.eclipse.ui",
      getModule()));

    Set<Object> objects = myBundleManager.resolveDependenciesOf(getModule());
    assertThat(objects, notNullValue());
    assertThat(objects.contains("org.eclipse.ui"), is(true));
    assertThat(objects.contains("org.eclipse.ui.workbench"), is(true));
    assertThat(objects.contains("org.eclipse.core.expressions"), is(true));
    assertThat(objects.size(), is(3)); // no more no less
  }

  /**
   * Tests resolving a circular import.
   */
  public void testResolveCircularDependency() throws ExecutionException, InterruptedException {
    myBundleManager.addManifestHolder(makeHolder(
      "MyManifest.MF",
      "Bundle-SymbolicName: foo.bar\n" +
      "Bundle-Version: 1.0.0\n" +
      "Require-Bundle: org.eclipse.circular.one",
      getModule()));

    // may run into infinite loop, so run it in a future task
    FutureTask<Set<Object>> bt = new FutureTask<Set<Object>>(new Callable<Set<Object>>() {
      @Override
      public Set<Object> call() throws Exception {
        return myBundleManager.resolveDependenciesOf(getModule());
      }
    });
    Executors.newFixedThreadPool(1).execute(bt);

    long t = System.currentTimeMillis();
    while (!bt.isDone()) {
      long elapsed = System.currentTimeMillis() - t;
      if (elapsed > 1000) { // should be easily doable within 1 second
        bt.cancel(true);
        fail("Infinite loop detected in MyBundleManager while resolving circular dependencies.");
      }
      TimeoutUtil.sleep(100);
    }

    Set<Object> objects = bt.get();
    assertThat(objects, notNullValue());
    assertThat(objects.contains("org.eclipse.circular.one"), is(true));
    assertThat(objects.contains("org.eclipse.circular.two"), is(true));
    assertThat(objects.size(), is(2)); //  no more no less
  }

  /**
   * Tests resolving the bundle classpath.
   */
  public void testResolveBundleClassPath() {
    myBundleManager.addManifestHolder(makeHolder(
      "MyManifest.MF",
      "Bundle-SymbolicName: foo.bar\n" +
      "Bundle-Version: 1.0.0\n" +
      "Bundle-ClassPath: /naff/trot/" + FAKE_JAR_NAME,
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
    myBundleManager.addManifestHolder(makeHolder(
      "MyManifest.MF",
      "Bundle-SymbolicName: foo.bar\n" +
      "Bundle-Version: 1.0.0\n" +
      "Require-Bundle: org.eclipse.fragment.host\n" +
      "Bundle-ClassPath: " + FAKE_JAR_NAME, getModule()));

    // may run into infinite loop, so run it in a future task
    FutureTask<Set<Object>> bt = new FutureTask<Set<Object>>(new Callable<Set<Object>>() {
      @Override
      public Set<Object> call() throws Exception {
        return myBundleManager.resolveDependenciesOf(getModule());
      }
    });
    Executors.newFixedThreadPool(1).execute(bt);

    long t = System.currentTimeMillis();
    while (!bt.isDone()) {
      long elapsed = System.currentTimeMillis() - t;
      if (elapsed > 2000) { // should be easily doable within 2 second
        bt.cancel(true);
        fail("Infinite loop detected in MyBundleManager while resolving circular dependencies.");
      }
      TimeoutUtil.sleep(100);
    }

    Set<Object> objects = bt.get();
    assertThat(objects, notNullValue());
    assertThat(objects.contains("org.eclipse.fragment.host"), is(true));
    assertThat(objects.contains("org.eclipse.circular.one"), is(true));
    assertThat(objects.contains("org.eclipse.circular.two"), is(true));
    assertThat(objects.contains("org.eclipse.ui.workbench"), is(true));
    assertThat(objects.contains("org.eclipse.core.expressions"), is(true));
    assertThat(objects.contains(myFakeJarLibrary), is(true));
    assertThat(objects.size(), is(6)); // no more, no less
  }

  private static ManifestHolder makeHolder(@NotNull String fileName, @NotNull String contents, @Nullable Object boundObject) {
    TestManifestHolder result = new TestManifestHolder((ManifestFile)createLightFile(fileName, contents));
    result.setBoundObject(boundObject);
    return result;
  }
}
