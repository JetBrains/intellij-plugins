/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.formatter;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

public class PbTextFormatterTest extends PbCodeInsightFixtureTestCase {

  @Override
  public String getTestDataPath() {
    return super.getTestDataPath() + "/ide/formatter/";
  }

  public void testFormatter() {
    myFixture.configureByFiles("FormatterTestBefore.pb");
    WriteCommandAction.runWriteCommandAction(getProject(), () -> {
        CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
    });
    myFixture.checkResultByFile("FormatterTestAfter.pb");
  }
}
