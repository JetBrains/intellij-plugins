// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.ognl;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.SkipSlowTestLocally;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.testFramework.propertyBased.MadTestingAction;
import com.intellij.testFramework.propertyBased.MadTestingUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jetCheck.Generator;
import org.jetbrains.jetCheck.PropertyChecker;

import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;

@SkipSlowTestLocally
public class OgnlCodeInsightSanityTest extends LightCodeInsightFixtureTestCase {

  @NonNls
  private static final String EXTENSION = OgnlFileType.INSTANCE.getDefaultExtension();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    WriteAction.run(() -> FileTypeManager.getInstance().associateExtension(OgnlFileType.INSTANCE, EXTENSION));
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      WriteAction.run(() -> FileTypeManager.getInstance().removeAssociatedExtension(OgnlFileType.INSTANCE, EXTENSION));
    }
    finally {
      super.tearDown();
    }
  }

  public void testReparse() {
    PropertyChecker.checkScenarios(actionsOnOgnlFiles(MadTestingUtil::randomEditsWithReparseChecks));
  }

  @NotNull
  private Supplier<MadTestingAction> actionsOnOgnlFiles(Function<PsiFile, Generator<? extends MadTestingAction>> fileActions) {
    return MadTestingUtil.actionsOnFileContents(myFixture,
                                                PathManager.getHomePath().replace(File.separatorChar, '/') +
                                                "/contrib/struts2/ognl/testData/",
                                                f -> f.getName().endsWith("." + EXTENSION), fileActions);
  }
}