package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.jetbrains.plugins.meteor.MeteorFacade;
import com.jetbrains.plugins.meteor.settings.MeteorSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/Meteor/testData")
public class MeteorProjectTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUp() {
    MeteorTestUtil.enableMeteor();
    MeteorTestUtil.initMeteorDirs(codeInsight.get().getProject());
  }

  @AfterEach
  void tearDown() {
    MeteorTestUtil.disableMeteor();
  }

  @Test
  public void testExcludeLocalForMeteorProjectWithRootMeteorFolder() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.copyDirectoryToProject("/testProject", "");
    ReadAction.run(() -> {
      Project project = myFixture.getProject();
      Collection<VirtualFile> local = FilenameIndex.getVirtualFilesByName("local", GlobalSearchScope.allScope(project));
      Collection<VirtualFile> meteor = FilenameIndex.getVirtualFilesByName(".meteor", GlobalSearchScope.allScope(project));
      Collection<VirtualFile> emptyFile = FilenameIndex.getVirtualFilesByName("empty.txt", GlobalSearchScope.allScope(project));
      assertThat(meteor).isNotEmpty();
      assertThat(local).isEmpty();
      assertThat(emptyFile).isEmpty();
    });
  }

  /**
   * test {@link MeteorDirectoryIndexExcludePolicy}
   */
  @Test
  public void testIncludeLocalMeteorProject() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    boolean oldValue = MeteorSettings.getInstance().isExcludeMeteorLocalFolder();
    MeteorSettings.getInstance().setExcludeMeteorLocalFolder(false);
    try {
      myFixture.copyDirectoryToProject("/testProject", "");

      Project project = myFixture.getProject();
      Collection<VirtualFile> local = ReadAction.compute(() -> FilenameIndex.getVirtualFilesByName("local", GlobalSearchScope.allScope(project)));
      assertEquals(1, local.size());
    }
    finally {
      MeteorSettings.getInstance().setExcludeMeteorLocalFolder(oldValue);
    }
  }

  @Test
  public void testNotMeteorProject() {
    assertFalse(MeteorFacade.getInstance().isMeteorProject(codeInsight.get().getProject()));
  }
}
