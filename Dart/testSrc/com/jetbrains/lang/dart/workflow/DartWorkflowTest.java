package com.jetbrains.lang.dart.workflow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartCodeInsightFixtureTestCase;
import com.jetbrains.lang.dart.DartProjectComponent;

public class DartWorkflowTest extends DartCodeInsightFixtureTestCase {

  public void testPackagesFolderExclusion() throws Exception {
    final String rootUrl = ModuleRootManager.getInstance(myModule).getContentEntries()[0].getUrl();

    myFixture.addFileToProject("dir1/pubspec.yaml", "name: project1");
    final VirtualFile pubspec2 = myFixture.addFileToProject("dir2/pubspec.yaml", "name: project2").getVirtualFile();
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
    final VirtualFile pubspec3 = myFixture.addFileToProject("dir2/example/pubspec.yaml", "name: project3\n" +
                                                                                         "dependencies:\n" +
                                                                                         "  project2:\n" +
                                                                                         "    path: ..").getVirtualFile();
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

    DartProjectComponent.excludePackagesFolders(myModule, pubspec2);

    assertSameElements(ModuleRootManager.getInstance(myModule).getContentEntries()[0].getExcludeFolderUrls(),
                       rootUrl + "/dir1/someFolder",
                       rootUrl + "/dir1/packages/project1",
                       rootUrl + "/dir1/web/packages",
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

    DartProjectComponent.excludePackagesFolders(myModule, pubspec3);

    assertSameElements(ModuleRootManager.getInstance(myModule).getContentEntries()[0].getExcludeFolderUrls(),
                       rootUrl + "/dir1/someFolder",
                       rootUrl + "/dir1/packages/project1",
                       rootUrl + "/dir1/web/packages",
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
}
