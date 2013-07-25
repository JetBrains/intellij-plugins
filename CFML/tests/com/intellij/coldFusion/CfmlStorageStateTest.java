/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion;

import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;

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
    String result = "<State>\n" +
                    "  <language_level>cf10_tags.xml</language_level>\n" +
                    "  <mapps>\n" +
                    "    <mapping logical_path=\"/cal\" directory=\"C:\\ColdFusion9\\wwwroot\\Sandbox\\calendar\" />\n" +
                    "  </mapps>\n" +
                    "</State>";
    CfmlProjectConfiguration.State configState =
      XmlSerializer.deserialize(JDOMUtil.loadDocument(xml).getRootElement().getChild("component"), CfmlProjectConfiguration.State.class);
    try {
      def.loadState(configState);
      Element resSer = XmlSerializer.serialize(def.getState());
      String resxml = JDOMUtil.writeElement(resSer, "\n");
      assertEquals(result, resxml);
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
