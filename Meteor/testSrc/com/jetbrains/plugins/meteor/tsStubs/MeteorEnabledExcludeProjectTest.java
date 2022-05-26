package com.jetbrains.plugins.meteor.tsStubs;

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
    Project project = myFixture.getProject();

    Collection<VirtualFile> meteor = FilenameIndex.getVirtualFilesByName(".meteor", GlobalSearchScope.allScope(project));
    Collection<VirtualFile> emptyFile = FilenameIndex.getVirtualFilesByName("empty.txt", GlobalSearchScope.allScope(project));
    Collection<VirtualFile> local = FilenameIndex.getVirtualFilesByName("local", GlobalSearchScope.allScope(project));
    assertEquals(1, meteor.size());
    assertEmpty(emptyFile);
    assertEmpty(local);
  }

  @Override
  protected void tuneFixture(ModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addContentRoot(getTestDataPath() + "/testProjectWithH");
  }
}
