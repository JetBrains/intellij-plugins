package com.jetbrains.plugins.jade;

import com.intellij.psi.PsiDirectory;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.builders.EmptyModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;

public class JadeRenameTest extends UsefulTestCase {
  private CodeInsightTestFixture myFixture;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder(getName());
    myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());
    final EmptyModuleFixtureBuilder moduleFixtureBuilder = projectBuilder.addModule(EmptyModuleFixtureBuilder.class);
    moduleFixtureBuilder.addSourceContentRoot(myFixture.getTempDirPath());

    myFixture.setUp();
    myFixture.setTestDataPath(JadeHighlightingTest.TEST_DATA_PATH + "/rename");
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      myFixture.tearDown();
    }
    catch (Throwable e) {
      addSuppressedException(e);
    }
    finally {
      myFixture = null;
      super.tearDown();
    }
  }

  public void _testVar1() {
    myFixture.configureByFile(getTestName(true) + ".jade");
    myFixture.testRename(getTestName(true) + "_after.jade", "zzi");
  }

  public void testExtends1() {
    myFixture.configureByFiles(getTestName(true) + ".jade", getTestName(true) + "_2.jade");
    myFixture.testRename(getTestName(true) + "_after.jade", "foobar.jade");
    PsiDirectory dir = myFixture.getFile().getParent();
    assertNull(dir.findFile(getTestName(true) + "_2.jade"));
    assertNotNull(dir.findFile("foobar.jade"));
  }

  public void testInclude1() {
    myFixture.configureByFiles(getTestName(true) + ".jade", getTestName(true) + "_2.js");
    myFixture.testRename(getTestName(true) + "_after.jade", "foobar.js");
    PsiDirectory dir = myFixture.getFile().getParent();
    assertNull(dir.findFile(getTestName(true) + "_2.jade"));
    assertNotNull(dir.findFile("foobar.js"));
  }

  public void testClassSelector() {
    myFixture.configureByFile(getTestName(true) + ".jade");
    myFixture.testRename(getTestName(true) + "_after.jade", "z220");
  }

  public void testIdSelector() {
    myFixture.configureByFile(getTestName(true) + ".jade");
    myFixture.testRename(getTestName(true) + "_after.jade", "z220");
  }

}
