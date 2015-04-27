/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.osmorc.inspection;

import org.osmorc.LightOsgiFixtureTestCase;

public class WrongImportPackageInspectionTest extends LightOsgiFixtureTestCase {
  public void test() {
    doTest(
      "Import-Package: org.osgi.framework,\n" +
      " org.osgi.resource.*,\n" +
      " org.osgi.framework.launch;version=1.1,\n" +
      " javax.swing,\n" +
      " <error descr=\"The package is not exported by the bundle dependencies\">org.apache.felix.framework</error>\n");
  }

  private void doTest(String text) {
    myFixture.enableInspections(new WrongImportPackageInspection());
    myFixture.configureByText("MANIFEST.MF", text);
    myFixture.checkHighlighting(true, false, false);
  }
}
