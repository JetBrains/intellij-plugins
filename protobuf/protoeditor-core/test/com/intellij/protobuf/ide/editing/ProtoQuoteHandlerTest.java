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

import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;

/** Tests for {@link ProtoQuoteHandler}. */
public class ProtoQuoteHandlerTest extends PbCodeInsightFixtureTestCase {

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
