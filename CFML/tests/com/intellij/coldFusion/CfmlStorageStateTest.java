// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion;

import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.configurationStore.XmlSerializer;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

import static com.intellij.testFramework.assertions.Assertions.assertThat;

public class CfmlStorageStateTest extends CodeInsightFixtureTestCase {
  public void test10_5Compatibility() throws Throwable {
    String xml = """
      <?xml version="1.0" encoding="UTF-8"?>
          <project version="4">
            <component name="CfmlProjectConfiguration">
              <mappings>
                <CfmlMappingsConfig>
                  <server_mappings>
                    <mapping directory="cal" logical_path="C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar" />
                  </server_mappings>
                </CfmlMappingsConfig>
              </mappings>
            </component>
          </project>""";
    doTest(xml);
  }

  public void test11_1Compatibility() throws Throwable {
    String xml = """
      <?xml version="1.0" encoding="UTF-8"?>
          <project version="4">
            <component name="CfmlProjectConfiguration">
              <mappings2>
                <CfmlMappingsConfig>
                  <server_mappings>
                    <mapping directory="/cal" logical_path="C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar" />
                  </server_mappings>
                </CfmlMappingsConfig>
              </mappings2>
            </component>
          </project>""";

    doTest(xml);
  }

  public void test12_oldCompatibility() throws Throwable {
    String xml = """
      <?xml version="1.0" encoding="UTF-8"?>
      <project version="4">
        <component name="CfmlProjectConfiguration">
          <mappings>
            <mapping logical_path="/cal" directory="C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar" />
          </mappings>
        </component>
      </project>

      """;

    doTest(xml);
  }

  public void test12Compatibility() throws Throwable {
    String xml = """
      <?xml version="1.0" encoding="UTF-8"?>
      <project version="4">
        <component name="CfmlProjectConfiguration">
          <mapps>
            <mapping logical_path="/cal" directory="C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar" />
          </mapps>
        </component>
      </project>

      """;

    doTest(xml);
  }


  private void doTest(String xml) throws Throwable {
    final CfmlProjectConfiguration def = CfmlProjectConfiguration.getInstance(getProject());
    final CfmlProjectConfiguration.State defaultState = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlProjectConfiguration.State configState = XmlSerializer.deserialize(JDOMUtil.load(xml).getChild("component"), CfmlProjectConfiguration.State.class);
    try {
      def.loadState(configState);
      assertThat(XmlSerializer.serialize(def.getState())).isEqualTo("""
                                                                      <State>
                                                                        <mapps>
                                                                          <mapping logical_path="/cal" directory="C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar" />
                                                                        </mapps>
                                                                      </State>""");
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
