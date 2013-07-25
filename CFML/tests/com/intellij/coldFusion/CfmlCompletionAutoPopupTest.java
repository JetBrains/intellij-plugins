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

/**
 * @author Nadya Zabrodina
 */
class CfmlCompletionAutoPopupTest extends CfmlCodeInsightFixtureTestCase {

  public void testAutopopupBasics() throws Throwable {
    myFixture.configureByText("a.cfml", "<cfinclude template=\"folder<caret>\">");

    myFixture.addFileToProject("folder/subfolder/b.cfml", "");
    myFixture.addFileToProject("folder/subfolder2/b.cfml", "");
    myFixture.type('/');
    assertSameElements(myFixture.getLookupElementStrings(), "subfolder", "subfolder2");
  }
}
