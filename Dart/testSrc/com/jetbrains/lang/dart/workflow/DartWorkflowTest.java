package com.jetbrains.lang.dart.workflow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.util.DartTestUtils;
import com.jetbrains.lang.dart.util.DartUrlResolver;

public class DartWorkflowTest extends DartCodeInsightFixtureTestCase {

  public void testPackagesFolderExclusion() throws Exception {
    try {
      final String rootUrl = ModuleRootManager.getInstance(myModule).getContentEntries()[0].getUrl();

      myFixture.addFileToProject("dir1/pubspec.yaml", "name: project1");
      myFixture.addFileToProject("dir1/lib/foo.txt", "");
      myFixture.addFileToProject("dir1/someFolder/lib/foo.dart", "");

      final VirtualFile pubspec2 = myFixture.addFileToProject("dir2/pubspec.yaml", "name: project2\n" +
                                                                                   "dependencies:\n" +
                                                                                   "  project1:\n" +
                                                                                   "    path: ../dir1").getVirtualFile();
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

      ApplicationManager.getApplication().runWriteAction(() -> {
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
      });

      DartProjectComponent.excludeBuildAndPackagesFolders(myModule, pubspec2);

      assertSameElements(ModuleRootManager.getInstance(myModule).getContentEntries()[0].getExcludeFolderUrls(),
                         rootUrl + "/dir1/someFolder",
                         rootUrl + "/dir1/packages/project1",
                         rootUrl + "/dir1/web/packages",
                         rootUrl + "/dir2/.pub",
                         rootUrl + "/dir2/build",
                         rootUrl + "/dir2/packages",
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
                         rootUrl + "/dir2/example/packages",
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
                         rootUrl + "/dir2/packages",
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
                         rootUrl + "/dir2/example/.pub",
                         rootUrl + "/dir2/example/build",
                         rootUrl + "/dir2/example/packages",
                         rootUrl + "/dir2/example/lib/packages",
                         rootUrl + "/dir2/example/lib/sub/packages",
                         rootUrl + "/dir2/example/web/packages",
                         rootUrl + "/dir2/example/web/sub/packages"
      );
    }
    finally {
      DartTestUtils.resetModuleRoots(myModule);
    }
  }

  public void testDartUrlResolver() throws Exception {
    final String rootPath = ModuleRootManager.getInstance(myModule).getContentRoots()[0].getPath();
    final String rootUrl = "file:///" + StringUtil.trimLeading(rootPath, '/');

    myFixture.addFileToProject("pubspec.yaml", "name: RootProject");
    myFixture.addFileToProject("lib/rootlib.dart", "");
    final VirtualFile nestedPubspec = myFixture.addFileToProject("example/pubspec.yaml", "name: NestedProject\n" +
                                                                                         "dependencies:\n" +
                                                                                         "  RootProject:\n" +
                                                                                         "    path: ../").getVirtualFile();
    myFixture.addFileToProject("example/lib/src/nestedlib.dart", "");
    myFixture.addFileToProject("example/packages/NestedProject/nestedlib.dart", "");
    myFixture.addFileToProject("example/packages/RootProject/rootlib.dart", "");
    myFixture.addFileToProject("pub/global/cache/SomePackage/lib/somepack.dart", "");
    myFixture.saveText(myFixture.addFileToProject("example/.packages", "").getVirtualFile(),
                       "RootProject:../lib/\n" +
                       "NestedProject:lib/\n" +
                       "SomePackage:" + rootUrl + "/pub/global/cache/SomePackage/lib/");

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
    assertEquals(rootPath + "/pub/global/cache/SomePackage/lib/somepack.dart", file.getPath());
    assertEquals("package:SomePackage/somepack.dart", resolver.getDartUrlForFile(file));
  }
}
