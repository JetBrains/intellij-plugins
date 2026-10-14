package com.intellij.protobuf.ide.highlighter;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.TestUtils;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.TestDisposable;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

@TestApplication
public class PbHighlightingAnnotatorWhileDumbTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp(@TestDisposable Disposable disposable) throws Exception {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    myFixture.addFileToProject(
      TestUtils.OPENSOURCE_DESCRIPTOR_PATH, TestUtils.getOpensourceDescriptorText());
    TestUtils.addTestFileResolveProvider(
      myFixture.getProject(), TestUtils.OPENSOURCE_DESCRIPTOR_PATH, disposable);
    EdtTestUtil.runInEdtAndWait(TestUtils::registerTestdataFileExtension);
  }

  @Test
  public void testAnnotatorInDumbMode(@TestDisposable Disposable disposable) {
    myFixture.configureByFile("ide/highlighter/TopLevelString.proto.testdata");

    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, disposable);
    EdtTestUtil.runInEdtAndWait(() -> DumbModeTestUtils.runInDumbModeSynchronously(myFixture.getProject(), () -> {
      myFixture.testFile("ide/highlighter/Highlighted.dumb.proto.testdata").checkSymbolNames().test();
    }));
  }
}
