package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;

import java.util.Collection;

public class MeteorProjectTest extends MeteorProjectTestBase {
  
  public void testExcludeLocalForMeteorProjectWithRootMeteorFolder() {
    myFixture.copyDirectoryToProject("/testProject", "");

    Project project = myFixture.getProject();
    Collection<VirtualFile> local = FilenameIndex.getVirtualFilesByName("local", GlobalSearchScope.allScope(project));
    Collection<VirtualFile> meteor = FilenameIndex.getVirtualFilesByName(".meteor", GlobalSearchScope.allScope(project));
    Collection<VirtualFile> emptyFile = FilenameIndex.getVirtualFilesByName("empty.txt", GlobalSearchScope.allScope(project));
    assertNotEmpty(meteor);
    assertEmpty(local);
    assertEmpty(emptyFile);
  }

  /**
   * test {@link MeteorDirectoryIndexExcludePolicy}
   */
  public void testIncludeLocalMeteorProject() {
    boolean oldValue = MeteorSettings.getInstance().isExcludeMeteorLocalFolder();
    MeteorSettings.getInstance().setExcludeMeteorLocalFolder(false);
    try {
      myFixture.copyDirectoryToProject("/testProject", "");

      Project project = myFixture.getProject();
      Collection<VirtualFile> local = FilenameIndex.getVirtualFilesByName("local", GlobalSearchScope.allScope(project));
      assertEquals(1, local.size());
    }
    finally {
      MeteorSettings.getInstance().setExcludeMeteorLocalFolder(oldValue);
    }
  }

  public void testNotMeteorProject() {
    assertFalse(MeteorFacade.getInstance().isMeteorProject(myFixture.getProject()));
  }
}
