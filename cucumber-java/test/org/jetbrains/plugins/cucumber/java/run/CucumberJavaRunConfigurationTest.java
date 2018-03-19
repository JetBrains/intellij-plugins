package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.ide.DataManager;
import com.intellij.idea.IdeaTestApplication;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.TestDataProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaCodeInsightTestCase;
import org.jetbrains.plugins.cucumber.java.CucumberJavaTestUtil;

public class CucumberJavaRunConfigurationTest extends CucumberJavaCodeInsightTestCase {
  public void testScenarioOutlineNameFilter() {
    doTest("^a few cukes$");
  }

  public void testScenarioOutlineWithPatternNameFilter() {
    doTest("^a .* few cukes$");
  }

  public void testProgramArguments() {
    myFixture.configureByText("test.feature", "Fea<caret>ture: test");

    ConfigurationFactory configurationFactory = CucumberJavaRunConfigurationType.getInstance().getConfigurationFactories()[0];
    CucumberJavaRunConfiguration runConfiguration = new CucumberJavaRunConfiguration("", myFixture.getProject(), configurationFactory);
    runConfiguration.setProgramParameters("--plugin pretty");
    CucumberJavaFeatureRunConfigurationProducer producer = new CucumberJavaFeatureRunConfigurationProducer();
    final DataContext dataContext = DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent());
    ConfigurationContext configurationContext = ConfigurationContext.getFromContext(dataContext);

    PsiElement elementAtCaret = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
    producer.setupConfigurationFromContext(runConfiguration, configurationContext, new Ref<>(elementAtCaret));

    assertTrue(runConfiguration.getProgramParameters().contains("--plugin pretty"));
  }

  @Override
  protected String getBasePath() {
    return CucumberJavaTestUtil.RELATED_TEST_DATA_PATH + "run";
  }

  private void doTest(@NotNull String expectedFilter) {
    myFixture.copyDirectoryToProject(getTestName(true), "");
    myFixture.configureByFile("test.feature");

    ConfigurationFactory configurationFactory = CucumberJavaRunConfigurationType.getInstance().getConfigurationFactories()[0];
    CucumberJavaRunConfiguration runConfiguration = new CucumberJavaRunConfiguration("", myFixture.getProject(), configurationFactory);

    final DataContext dataContext = DataManager.getInstance().getDataContext(myFixture.getEditor().getComponent());
    ConfigurationContext configurationContext = ConfigurationContext.getFromContext(dataContext);

    CucumberJavaScenarioRunConfigurationProducer producer = new CucumberJavaScenarioRunConfigurationProducer();
    PsiElement elementAtCaret = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
    producer.setupConfigurationFromContext(runConfiguration, configurationContext, new Ref<>(elementAtCaret));

    assertEquals(expectedFilter, runConfiguration.getNameFilter());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    IdeaTestApplication.getInstance().setDataProvider(new TestDataProvider(getProject()) {
      @Override
      public Object getData(@NonNls String dataId) {
        if (LangDataKeys.MODULE.is(dataId)) {
          return myFixture.getModule();
        }
        return super.getData(dataId);
      }
    });
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return CucumberJavaTestUtil.createCucumberProjectDescriptor();
  }
}
