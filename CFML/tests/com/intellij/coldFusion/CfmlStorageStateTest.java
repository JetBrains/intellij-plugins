// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion;

import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.configurationStore.XmlSerializer;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nadya Zabrodina
 */
public class CfmlStorageStateTest extends CodeInsightFixtureTestCase {
  public void test10_5Compatibility() throws Throwable {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                 "    <project version=\"4\">\n" +
                 "      <component name=\"CfmlProjectConfiguration\">\n" +
                 "        <mappings>\n" +
                 "          <CfmlMappingsConfig>\n" +
                 "            <server_mappings>\n" +
                 "              <mapping directory=\"cal\" logical_path=\"C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar\" />\n" +
                 "            </server_mappings>\n" +
                 "          </CfmlMappingsConfig>\n" +
                 "        </mappings>\n" +
                 "      </component>\n" +
                 "    </project>";
    doTest(xml);
  }

  public void test11_1Compatibility() throws Throwable {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                 "    <project version=\"4\">\n" +
                 "      <component name=\"CfmlProjectConfiguration\">\n" +
                 "        <mappings2>\n" +
                 "          <CfmlMappingsConfig>\n" +
                 "            <server_mappings>\n" +
                 "              <mapping directory=\"/cal\" logical_path=\"C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar\" />\n" +
                 "            </server_mappings>\n" +
                 "          </CfmlMappingsConfig>\n" +
                 "        </mappings2>\n" +
                 "      </component>\n" +
                 "    </project>";

    doTest(xml);
  }

  public void test12_oldCompatibility() throws Throwable {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                 "<project version=\"4\">\n" +
                 "  <component name=\"CfmlProjectConfiguration\">\n" +
                 "    <mappings>\n" +
                 "      <mapping logical_path=\"/cal\" directory=\"C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar\" />\n" +
                 "    </mappings>\n" +
                 "  </component>\n" +
                 "</project>\n" +
                 "\n";

    doTest(xml);
  }

  public void test12Compatibility() throws Throwable {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                 "<project version=\"4\">\n" +
                 "  <component name=\"CfmlProjectConfiguration\">\n" +
                 "    <mapps>\n" +
                 "      <mapping logical_path=\"/cal\" directory=\"C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar\" />\n" +
                 "    </mapps>\n" +
                 "  </component>\n" +
                 "</project>\n" +
                 "\n";

    doTest(xml);
  }


  private void doTest(String xml) throws Throwable {
    final CfmlProjectConfiguration def = CfmlProjectConfiguration.getInstance(getProject());
    final CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State configState = XmlSerializer.deserialize(JDOMUtil.load(xml).getChild("component"), CfmlProjectConfiguration.State.class);
    try {
      def.loadState(configState);
      assertThat(XmlSerializer.serialize(def.getState())).isEqualTo("<State>\n" +
                                                                      "  <mapps>\n" +
                                                                      "    <mapping logical_path=\"/cal\" directory=\"C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar\" />\n" +
                                                                      "  </mapps>\n" +
                                                                      "</State>");
    }
    finally {
      CfmlProjectConfiguration.getInstance(getProject()).loadState(defaultState);
    }
    //XmlSerializer.serialize(state);

    //assertEquals(1, defaultConfig.getState().getMapps().getServerMappings().size());
  }

  @Override
  protected String getBasePath() {
    return "/plugins/CFML/tests/testData/completion";
  }
}
