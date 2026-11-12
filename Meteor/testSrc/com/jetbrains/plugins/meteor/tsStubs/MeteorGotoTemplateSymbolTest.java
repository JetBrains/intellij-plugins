package com.jetbrains.plugins.meteor.tsStubs;

import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.ide.util.gotoByName.GotoSymbolModel2;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.ArrayUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/Meteor/testData")
public class MeteorGotoTemplateSymbolTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  @BeforeEach
  void setUp() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    MeteorTestUtil.enableMeteor();
    VirtualFile moduleRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(myFixture.getTestDataPath() + "/testGotoSymbol/module");
    assertNotNull(moduleRoot);
    PsiTestUtil.addSourceContentToRoots(myFixture.getModule(), moduleRoot);
    MeteorTestUtil.initMeteorDirs(myFixture.getProject());
  }

  @AfterEach
  void tearDown() {
    MeteorTestUtil.disableMeteor();
  }

  @Test
  public void testGoto() {
    runGotoTest("myTemplateForHelper1");
  }

  private void runGotoTest(final String... expectedSymbols) {
    CodeInsightTestFixture myFixture = codeInsight.get();
    WriteAction.runAndWait(() -> {
      ChooseByNameModel model2 = new GotoSymbolModel2(myFixture.getProject(), myFixture.getTestRootDisposable());
      String[] names = model2.getNames(false);

      for (String expectedSymbol : expectedSymbols) {
        assertTrue(ArrayUtil.contains(expectedSymbol, names), expectedSymbol+": "+Arrays.asList(names));
      }
    });
  }
}
