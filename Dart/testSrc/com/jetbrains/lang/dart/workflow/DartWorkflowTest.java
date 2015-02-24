package com.jetbrains.lang.dart.workflow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.sdk.listPackageDirs.PubListPackageDirsAction2;
import com.jetbrains.lang.dart.util.DartTestUtils;
import com.jetbrains.lang.dart.util.DartUrlResolver;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DartWorkflowTest extends DartCodeInsightFixtureTestCase {

  public void testPackagesFolderExclusion() throws Exception {
    try {
      final String rootUrl = ModuleRootManager.getInstance(myModule).getContentEntries()[0].getUrl();

      myFixture.addFileToProject("dir1/pubspec.yaml", "name: project1");
      myFixture.addFileToProject("dir1/someFolder/lib/foo.dart", "");
      final VirtualFile pubspec2 = myFixture.addFileToProject("dir2/pubspec.yaml", "name: project2").getVirtualFile();
      myFixture.addFileToProject("dir2/.pub/foo.txt", "");
      myFixture.addFileToProject("dir2/bin/foo.dart", "");
      myFixture.addFileToProject("dir2/bin/sub/foo.dart", "");
      myFixture.addFileToProject("dir2/lib/foo.dart", "");
      myFixture.addFileToProject("dir2/lib/sub/foo.dart", "");
      myFixture.addFileToProject("dir2/benchmark/foo.dart", "");
      myFixture.addFileToProject("dir2/benchmark/sub/foo.dart", "");
      myFixture.addFileToProject("dir2/test/foo.dart", "");
      myFixture.addFileToProject("dir2/test/sub/foo.dart", "");
      myFixture.addFileToProject("dir2/tool/foo.dart", "");
      myFixture.addFileToProject("dir2/tool/sub/foo.dart", "");
      myFixture.addFileToProject("dir2/web/foo.dart", "");
      myFixture.addFileToProject("dir2/web/sub/foo.dart", "");
      myFixture.addFileToProject("dir2/build/foo.dart", "");
      myFixture.addFileToProject("dir2/build/sub/foo.dart", "");
      final VirtualFile pubspec3 = myFixture.addFileToProject("dir2/example/pubspec.yaml",
                                                              "name: project3\n" +
                                                              "dependencies:\n" +
                                                              "  project2:\n" +
                                                              "    path: ..\n" +
                                                              "  outside_project:\n" +
                                                              "    path: ../../dir1/someFolder").getVirtualFile();
      myFixture.addFileToProject("dir2/example/lib/foo.dart", "");
      myFixture.addFileToProject("dir2/example/lib/sub/foo.dart", "");
      myFixture.addFileToProject("dir2/example/web/foo.dart", "");
      myFixture.addFileToProject("dir2/example/web/sub/foo.dart", "");

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          final ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
          final ContentEntry contentEntry = model.getContentEntries()[0];
          contentEntry.addExcludeFolder(rootUrl + "/dir1/someFolder");
          contentEntry.addExcludeFolder(rootUrl + "/dir1/packages/project1");
          contentEntry.addExcludeFolder(rootUrl + "/dir1/web/packages");
          contentEntry.addExcludeFolder(rootUrl + "/dir2/packages/oldProject2Name");
          contentEntry.addExcludeFolder(rootUrl + "/dir2/someFolder");
          contentEntry.addExcludeFolder(rootUrl + "/dir2/lib/someFolder");
          contentEntry.addExcludeFolder(rootUrl + "/dir2/example/nonexistent/packages");
          contentEntry.addExcludeFolder(rootUrl + "/dir2/example/packages/oldProject3Name");
          model.commit();
        }
      });

      DartProjectComponent.excludeBuildAndPackagesFolders(myModule, pubspec2);

      assertSameElements(ModuleRootManager.getInstance(myModule).getContentEntries()[0].getExcludeFolderUrls(),
                         rootUrl + "/dir1/someFolder",
                         rootUrl + "/dir1/packages/project1",
                         rootUrl + "/dir1/web/packages",
                         rootUrl + "/dir2/.pub",
                         rootUrl + "/dir2/build",
                         rootUrl + "/dir2/packages/project2",
                         rootUrl + "/dir2/someFolder",
                         rootUrl + "/dir2/lib/someFolder",
                         rootUrl + "/dir2/bin/packages",
                         rootUrl + "/dir2/benchmark/packages",
                         rootUrl + "/dir2/benchmark/sub/packages",
                         rootUrl + "/dir2/test/packages",
                         rootUrl + "/dir2/test/sub/packages",
                         rootUrl + "/dir2/tool/packages",
                         rootUrl + "/dir2/tool/sub/packages",
                         rootUrl + "/dir2/web/packages",
                         rootUrl + "/dir2/web/sub/packages",
                         rootUrl + "/dir2/example/lib/packages",
                         rootUrl + "/dir2/example/lib/sub/packages",
                         rootUrl + "/dir2/example/web/packages",
                         rootUrl + "/dir2/example/web/sub/packages",
                         rootUrl + "/dir2/example/packages/oldProject3Name"
      );

      DartProjectComponent.excludeBuildAndPackagesFolders(myModule, pubspec3);

      assertSameElements(ModuleRootManager.getInstance(myModule).getContentEntries()[0].getExcludeFolderUrls(),
                         rootUrl + "/dir1/someFolder",
                         rootUrl + "/dir1/packages/project1",
                         rootUrl + "/dir1/web/packages",
                         rootUrl + "/dir2/.pub",
                         rootUrl + "/dir2/build",
                         rootUrl + "/dir2/packages/project2",
                         rootUrl + "/dir2/someFolder",
                         rootUrl + "/dir2/lib/someFolder",
                         rootUrl + "/dir2/bin/packages",
                         rootUrl + "/dir2/benchmark/packages",
                         rootUrl + "/dir2/benchmark/sub/packages",
                         rootUrl + "/dir2/test/packages",
                         rootUrl + "/dir2/test/sub/packages",
                         rootUrl + "/dir2/tool/packages",
                         rootUrl + "/dir2/tool/sub/packages",
                         rootUrl + "/dir2/web/packages",
                         rootUrl + "/dir2/web/sub/packages",
                         rootUrl + "/dir2/example/lib/packages",
                         rootUrl + "/dir2/example/lib/sub/packages",
                         rootUrl + "/dir2/example/web/packages",
                         rootUrl + "/dir2/example/web/sub/packages",
                         rootUrl + "/dir2/example/packages/project3",
                         rootUrl + "/dir2/example/packages/project2"
      );
    }
    finally {
      DartTestUtils.resetModuleRoots(myModule);
    }
  }

  public void testDartUrlResolver() throws Exception {
    final String rootPath = ModuleRootManager.getInstance(myModule).getContentRoots()[0].getPath();

    myFixture.addFileToProject("pubspec.yaml", "name: RootProject");
    myFixture.addFileToProject("lib/rootlib.dart", "");
    final VirtualFile nestedPubspec = myFixture.addFileToProject("example/pubspec.yaml", "name: NestedProject\n" +
                                                                                         "dependencies:\n" +
                                                                                         "  RootProject:\n" +
                                                                                         "    path: ../").getVirtualFile();
    myFixture.addFileToProject("example/lib/src/nestedlib.dart", "");
    myFixture.addFileToProject("example/packages/NestedProject/nestedlib.dart", "");
    myFixture.addFileToProject("example/packages/RootProject/rootlib.dart", "");
    myFixture.addFileToProject("example/packages/SomePackage/somepack.dart", "");
    final VirtualFile customPack1 =
      myFixture.addFileToProject("custom_pack1/RootProject/rootlib.dart", "").getVirtualFile().getParent().getParent();
    final VirtualFile customPack2 =
      myFixture.addFileToProject("custom_pack2/SomePackage/somepack.dart", "").getVirtualFile().getParent().getParent();

    DartUrlResolver resolver = DartUrlResolver.getInstance(getProject(), nestedPubspec);
    VirtualFile file;

    file = resolver.findFileByDartUrl("dart:collection");
    assertNotNull(file);
    assertEquals(DartTestUtils.SDK_HOME_PATH + "/lib/collection/collection.dart", file.getPath());
    assertEquals("dart:collection", resolver.getDartUrlForFile(file));

    file = resolver.findFileByDartUrl("dart:collection/hash_map.dart");
    assertNotNull(file);
    assertEquals(DartTestUtils.SDK_HOME_PATH + "/lib/collection/hash_map.dart", file.getPath());
    assertEquals("dart:collection/hash_map.dart", resolver.getDartUrlForFile(file));

    file = resolver.findFileByDartUrl("package:NestedProject/src/nestedlib.dart");
    assertNotNull(file);
    assertEquals(rootPath + "/example/lib/src/nestedlib.dart", file.getPath());
    assertEquals("package:NestedProject/src/nestedlib.dart", resolver.getDartUrlForFile(file));

    file = resolver.findFileByDartUrl("package:RootProject/rootlib.dart");
    assertNotNull(file);
    assertEquals(rootPath + "/lib/rootlib.dart", file.getPath());
    assertEquals("package:RootProject/rootlib.dart", resolver.getDartUrlForFile(file));

    file = resolver.findFileByDartUrl("package:SomePackage/somepack.dart");
    assertNotNull(file);
    assertEquals(rootPath + "/example/packages/SomePackage/somepack.dart", file.getPath());
    assertEquals("package:SomePackage/somepack.dart", resolver.getDartUrlForFile(file));

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(myModule).getModifiableModel();
        try {
          final Library library = modifiableModel.getModuleLibraryTable().createLibrary("Dart custom package roots");
          final Library.ModifiableModel libModel = library.getModifiableModel();
          libModel.addRoot(customPack1.getUrl(), OrderRootType.CLASSES);
          libModel.addRoot(customPack2.getUrl(), OrderRootType.CLASSES);
          libModel.commit();
          modifiableModel.commit();
        }
        catch (Exception e) {
          if (!modifiableModel.isDisposed()) modifiableModel.dispose();
        }
      }
    });

    resolver = DartUrlResolver.getInstance(getProject(), nestedPubspec);

    file = resolver.findFileByDartUrl("package:NestedProject/src/nestedlib.dart");
    assertNull(file);

    file = resolver.findFileByDartUrl("package:RootProject/rootlib.dart");
    assertNotNull(file);
    assertEquals(rootPath + "/custom_pack1/RootProject/rootlib.dart", file.getPath());
    assertEquals("package:RootProject/rootlib.dart", resolver.getDartUrlForFile(file));

    file = resolver.findFileByDartUrl("package:SomePackage/somepack.dart");
    assertNotNull(file);
    assertEquals(rootPath + "/custom_pack2/SomePackage/somepack.dart", file.getPath());
    assertEquals("package:SomePackage/somepack.dart", resolver.getDartUrlForFile(file));
  }

  public void testTwoPackageDirsForOnePackage() throws Exception {
    final VirtualFile packageDir1 = myFixture.addFileToProject("PackageDir1/foo/bar1.dart", "").getVirtualFile().getParent().getParent();
    final VirtualFile packageDir2 = myFixture.addFileToProject("PackageDir2/foo/bar2.dart", "").getVirtualFile().getParent().getParent();
    final List<File> packageDirList = Arrays.asList(new File(packageDir1.getPath()), new File(packageDir2.getPath()), new File("nofile"));
    try {
      PubListPackageDirsAction2.configurePubListPackageDirsLibrary(getProject(),
                                                                   Collections.singleton(myModule),
                                                                   Arrays.asList(packageDir1.getPath(), packageDir2.getPath()),
                                                                   Collections.singletonMap("PackageName", packageDirList));
      final DartUrlResolver resolver = DartUrlResolver.getInstance(getProject(),
                                                                   ModuleRootManager.getInstance(myModule).getContentRoots()[0]);
      assertEquals(packageDir1, resolver.getPackageDirIfLivePackageOrFromPubListPackageDirs("PackageName", "foo/bar1.dart"));
      assertEquals(packageDir2, resolver.getPackageDirIfLivePackageOrFromPubListPackageDirs("PackageName", "foo/bar2.dart"));
      assertEquals(packageDir1, resolver.getPackageDirIfLivePackageOrFromPubListPackageDirs("PackageName", "incorrect"));
      assertEquals(packageDir1, resolver.getPackageDirIfLivePackageOrFromPubListPackageDirs("PackageName", ""));
      assertEquals(packageDir1, resolver.getPackageDirIfLivePackageOrFromPubListPackageDirs("PackageName", null));
    }
    finally {
      PubListPackageDirsAction2.configurePubListPackageDirsLibrary(getProject(),
                                                                   Collections.<Module>emptySet(),
                                                                   Collections.<String>emptyList(),
                                                                   Collections.<String, List<File>>emptyMap());
    }
  }
}
