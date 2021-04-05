package com.intellij.flex.flexunit.codeInsight;

import com.intellij.codeInsight.JavaCodeInsightTestCase;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.FlexUnitLibs;
import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRuntimeConfigurationProducer;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FlexUnitConfigurationTest extends JavaCodeInsightTestCase implements FlexUnitLibs {

  private static final String BASE_PATH = "/config/";

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "flexUnit");
    super.setUp();
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);

    FlexTestUtils.addFlexUnitLib(getClass(), getTestName(false), getModule(), getTestDataPath(), FLEX_UNIT_0_9_SWC, FLEX_UNIT_4_SWC);
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("flexUnit");
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @Override
  public Object getData(@NotNull final String dataId) {
    return LangDataKeys.MODULE.is(dataId) ? myModule : super.getData(dataId);
  }

  private void defaultTest() throws Exception {
    doTest(getTestName(false) + ".as");
  }

  private void doTest(String filename) throws Exception {
    configureByFile(BASE_PATH + filename);

    final Map<Integer, String> markers = JSTestUtils.extractPositionMarkers(getProject(), getEditor().getDocument());
    assertFalse("No caret markers found", markers.isEmpty());
    assertFalse("No 'expected' value", markers.containsValue(null));

    final DataContext dataContext = DataManager.getInstance().getDataContext(getEditor().getComponent());
    int i = 1;
    for (Map.Entry<Integer, String> marker : markers.entrySet()) {
      String place = filename + ": marker " + i++ + ": ";
      getEditor().getCaretModel().moveToOffset(marker.getKey());

      final ConfigurationFromContext configurationFromContext =
        new FlexUnitRuntimeConfigurationProducer().createConfigurationFromContext(ConfigurationContext.getFromContext(dataContext, ActionPlaces.UNKNOWN));
      final RunConfiguration configuration = configurationFromContext == null ? null : configurationFromContext.getConfiguration();

      if ("null".equals(marker.getValue())) {
        assertNull(place + "Null configuration expected", configuration);
      }
      else {
        assertNotNull(place + "Not null configuration expected", configuration);
        assertTrue(place + "Invalid configuration", configuration instanceof FlexUnitRunConfiguration);

        final String[] expected;
        expected = marker.getValue().split(" ");
        assertEquals(
          place + "Expected should be in the form: \"Class com.test.Foo\" or \"Method com.test.Foo.testBar()\" or \"Package com.test\"",
          2, expected.length);
        final FlexUnitRunnerParameters params = ((FlexUnitRunConfiguration)configuration).getRunnerParameters();
        assertEquals(place + "Invalid scope", expected[0], params.getScope().name());

        final String definition;
        switch (params.getScope()) {
          case Class:
            definition = params.getClassName();
            break;
          case Method:
            definition = params.getClassName() + "." + params.getMethodName() + "()";
            break;
          case Package:
            definition = params.getPackageName();
            break;

          default:
            fail(place + "Unknown scope: " + params.getScope());
            definition = null;
        }

        assertEquals(place + "Invalid definition", expected[1], definition);
      }
    }
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit1() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit1WithFlexUnit4() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit1Empty() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit1NonPublic() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit1EmptyWithFlex4() throws Exception {
    doTest("FlexUnit1Empty.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit1WithNonDefaultConstructor() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit1WithNonDefaultConstructorWithFlex4() throws Exception {
    doTest("FlexUnit1WithNonDefaultConstructor.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit4WithFlexUnit1() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testFlexUnit4WithFlexUnit1_2() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4WithNonDefaultConstructor() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4Empty() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlexUnit4NonPublic() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testNoFlexUnit() throws Exception {
    defaultTest();
  }

  public void testNoFlex() throws Exception {
    doTest("NoFlexUnit.as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlunit1() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testSuite1() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testNoSuiteWithFlexUnit1() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testTestMethodInSuite() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testWithCustomRunner() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testOldStyleSuite() throws Exception {
    defaultTest();
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testFlunitSuite() throws Exception {
    defaultTest();
  }
}
