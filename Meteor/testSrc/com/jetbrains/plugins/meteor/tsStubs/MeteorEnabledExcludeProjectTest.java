package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.builders.ModuleFixtureBuilder;

import java.util.Collection;


public class MeteorEnabledExcludeProjectTest extends MeteorProjectTestBase {

  /**
   * test {@link MeteorProjectComponent}
   */
  public void testExcludeLocalForMeteorProjectWithMeteorFolderAsSubDir() {
    ReadAction.run(() -> {
      Project project = myFixture.getProject();

      Collection<VirtualFile> meteor = FilenameIndex.getVirtualFilesByName(".meteor", GlobalSearchScope.allScope(project));
      assertEquals(1, meteor.size());
      Collection<VirtualFile> emptyFile = FilenameIndex.getVirtualFilesByName("empty.txt", GlobalSearchScope.allScope(project));
      assertEmpty(emptyFile);
      Collection<VirtualFile> local = FilenameIndex.getVirtualFilesByName("local", GlobalSearchScope.allScope(project));
      assertEmpty(local);
    });
  }

  @Override
  protected void tuneFixture(ModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addContentRoot(getTestDataPath() + "/testProjectWithH");
  }
}
