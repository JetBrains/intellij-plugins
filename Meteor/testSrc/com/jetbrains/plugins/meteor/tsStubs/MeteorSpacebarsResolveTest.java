package com.jetbrains.plugins.meteor.tsStubs;

import com.dmarcotte.handlebars.util.HbTestUtils;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestApplication
@TestDataPath("$PROJECT_ROOT/contrib/Meteor/testData/testResolveHelpers")
public class MeteorSpacebarsResolveTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);
  private String testName;

  @BeforeEach
  void setUp(TestInfo testInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.getTestMethod().orElseThrow().getName(), true);
    MeteorTestUtil.enableMeteor();
    CodeInsightTestFixture myFixture = codeInsight.get();
    HbTestUtils.setOpenHtmlAsHandlebars(true, myFixture.getProject(), myFixture.getTestRootDisposable());
  }

  @AfterEach
  void tearDown() {
    MeteorTestUtil.disableMeteor();
  }

  @Test
  public void testSingleTag() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("Template.myTemplateForHelper.testProperty", ((JSQualifiedNamedElement)resolve).getQualifiedName());
  }

  @Test
  public void testSingleUnescapedTag() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("Template.myTemplateForHelper.testProperty", ((JSQualifiedNamedElement)resolve).getQualifiedName());
  }

  @Test
  public void testThreeTags() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("Template.myTemplateForHelper2.testProperty", ((JSQualifiedNamedElement)resolve).getQualifiedName());
  }

  @Test
  public void testNotResolve() {
    PsiElement resolve = resolveElement();
    assertNull(resolve);
  }

  @Test
  public void testResolvedObjectLiteralHelper() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    String qualifiedName = ((JSQualifiedNamedElement)resolve).getQualifiedName();
    assertEquals("MeteorTemplateHelpersNamespace.myTemplateForLiteral." + "testPropertyLiteral", qualifiedName);
  }

  @Test
  public void testResolveGlobalHelper() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("MeteorGlobalHelpersNamespace.globalHelper1", ((JSPsiElementBase)resolve).getQualifiedName());
  }

  @Test
  public void testResolveTemplate() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    String text = ReadAction.compute(() -> resolve.getText());
    assertEquals("\"templateForResolving\"", text);
  }

  @Test
  public void testResolveForBodyTag() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("MeteorTemplateHelpersNamespace.body.testPropertyForBody", ((JSPsiElementBase)resolve).getQualifiedName());
  }

  @Test
  public void testResolveBlockTag() {
    PsiElement resolve = resolveElement();
    assertNotNull(resolve);
    assertEquals("MeteorGlobalHelpersNamespace.globalHelperForBlock", ((JSPsiElementBase)resolve).getQualifiedName());
  }

  private PsiElement resolveElement() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    VirtualFile directory = myFixture.copyDirectoryToProject(testName + "/module", "module");
    MeteorTestUtil.initMeteorDirs(myFixture.getProject());
    VirtualFile templates = directory.findFileByRelativePath("templates.html");
    assertNotNull(templates);
    myFixture.configureFromExistingVirtualFile(templates);
    return ReadAction.compute(() -> {
      int offset = myFixture.getCaretOffset();
      PsiReference reference = myFixture.getFile().findReferenceAt(offset);
      return reference.resolve();
    });
  }
}
