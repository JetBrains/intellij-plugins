// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.diagrams;

import com.intellij.lang.javascript.modules.diagram.BaseJSModuleDiagramDataTestCase;
import com.intellij.psi.search.GlobalSearchScope;
import org.angularjs.AngularTestUtil;
import org.angularjs.diagram.AngularModulesProvider;

public class DiagramsTest extends BaseJSModuleDiagramDataTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }

  public void testAngularJSDiagrams() {
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureFromTempProjectFile("components/components.module.js");

    final GlobalSearchScope scope = GlobalSearchScope.fileScope(myFixture.getProject(), myFixture.getFile().getVirtualFile());
    final Checker checker = new Checker(myFixture.getProject(), scope);

    checker.assertExisting("components/components.module.js")
      .assertImported("components", "auth/auth.module.js", "components.auth");
  }

  public void testProviderSingleFileResults() {
    myFixture.copyDirectoryToProject(".", ".");
    myFixture.configureFromTempProjectFile("components/components.module.js");

    AngularModulesProvider provider = new AngularModulesProvider();
    assertSize(1, provider.getDependencies(myFixture.getFile()));
  }

}
