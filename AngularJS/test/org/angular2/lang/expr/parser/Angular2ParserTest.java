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
package org.angular2.lang.expr.parser;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.FileBasedTestCaseHelperEx;
import com.intellij.testFramework.LightPlatformCodeInsightTestCase;
import com.intellij.testFramework.UsefulTestCase;
import org.angular2.lang.expr.Angular2Language;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author Dennis.Ushakov
 */
@RunWith(com.intellij.testFramework.Parameterized.class)
public class Angular2ParserTest extends LightPlatformCodeInsightTestCase implements FileBasedTestCaseHelperEx {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(Angular2ParserTest.class);
  }

  @Nullable
  @Override
  public String getFileSuffix(String fileName) {
    return fileName.endsWith("js") ? fileName : null;
  }

  @Test
  public void runSingle() throws Throwable {
    doSingleTest(myFileSuffix, myTestDataPath);
  }

  private void doSingleTest(String suffix, String path) throws Throwable {
    final String text = FileUtil.loadFile(new File(path, suffix), true);
    final StringBuilder result = new StringBuilder();

    int firstDot = suffix.indexOf('.');
    String extension = suffix.substring(0, firstDot);
    int secondDot = suffix.indexOf('.', firstDot + 1);
    String name = secondDot > 0 ? suffix.substring(firstDot + 1, secondDot) : "";

    for (String line : StringUtil.splitByLines(text)) {
      if (result.length() > 0) result.append("------\n");

      PsiFile psiFile = PsiFileFactory.getInstance(getProject())
        .createFileFromText("test." + name + "." + extension, Angular2Language.INSTANCE, line);

      result.append(DebugUtil.psiToString(psiFile, false, false));
    }
    UsefulTestCase.assertSameLinesWithFile(new File(path, suffix.replace("js", "txt")).toString(), result.toString());
  }

  @Override
  public String getRelativeBasePath() {
    return "";
  }
}
