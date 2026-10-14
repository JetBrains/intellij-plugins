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
package com.intellij.protobuf.ide.editing;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.project.Project;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;

/** Tests for {@link ProtoQuoteHandler}. */
@TestApplication
public class ProtoQuoteHandlerTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  @BeforeEach
  void setUp() {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
  }

  @Test
  public void testSingleQuoteHandling() {
    myFixture.configureByText("test.proto", "syntax = <caret>");
    myFixture.checkResult("syntax = ");
    // Closing quote should be auto-inserted.
    myFixture.type("'proto2");
    myFixture.checkResult("syntax = 'proto2'");
    // Erase "proto2", leaving only two quotes.
    myFixture.type("\b\b\b\b\b\b");
    myFixture.checkResult("syntax = ''");
    // One more backspace should delete both quotes.
    myFixture.type('\b');
    myFixture.checkResult("syntax = ");
  }

  @Test
  public void testDoubleQuoteHandling() {
    myFixture.configureByText("test.proto", "syntax = <caret>");
    myFixture.checkResult("syntax = ");
    // Closing quote should be auto-inserted.
    myFixture.type("\"proto2");
    myFixture.checkResult("syntax = \"proto2\"");
    // Erase "proto2", leaving only two quotes.
    myFixture.type("\b\b\b\b\b\b");
    myFixture.checkResult("syntax = \"\"");
    // One more backspace should delete both quotes.
    myFixture.type('\b');
    myFixture.checkResult("syntax = ");
  }

  @Test
  public void testTextSingleQuoteHandling() {
    myFixture.configureByText("test.pb", "foo: <caret>");
    myFixture.checkResult("foo: ");
    // Closing quote should be auto-inserted.
    myFixture.type("'bar");
    myFixture.checkResult("foo: 'bar'");
    // Erase "bar", leaving only two quotes.
    myFixture.type("\b\b\b");
    myFixture.checkResult("foo: ''");
    // One more backspace should delete both quotes.
    myFixture.type('\b');
    myFixture.checkResult("foo: ");
  }

  @Test
  public void testTextDoubleQuoteHandling() {
    myFixture.configureByText("test.pb", "foo: <caret>");
    myFixture.checkResult("foo: ");
    // Closing quote should be auto-inserted.
    myFixture.type("\"bar");
    myFixture.checkResult("foo: \"bar\"");
    // Erase "bar", leaving only two quotes.
    myFixture.type("\b\b\b");
    myFixture.checkResult("foo: \"\"");
    // One more backspace should delete both quotes.
    myFixture.type('\b');
    myFixture.checkResult("foo: ");
  }
}
