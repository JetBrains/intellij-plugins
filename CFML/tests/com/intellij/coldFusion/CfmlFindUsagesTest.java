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

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.usageView.UsageInfo;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */
public class CfmlFindUsagesTest extends CfmlCodeInsightFixtureTestCase {
  private Collection<UsageInfo> getUsages() {
    return myFixture.testFindUsages(Util.getInputDataFileName(getTestName(true)));
  }

  public void testFunctionUsagesInScript() {
    assertEquals(3, getUsages().size());
  }

  public void testFunctionUsages() {
    assertEquals(1, getUsages().size());
  }

  public void testFunctionArgumentUsages() {
    assertEquals(5, getUsages().size());
  }

  public void testFunctionArgumentUsagesInScript() {
    assertEquals(2, getUsages().size());
  }


  @Override
  protected String getBasePath() {
    return "/findUsages";
  }
}
