/*
 * Copyright (c) 2013, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.server.utilities.general;

import junit.framework.TestCase;

public class StringUtilitiesTest extends TestCase {
  public void test_abbreviateLeft() throws Exception {
    assertEquals("123456789", StringUtilities.abbreviateLeft("123456789", 100));
    assertEquals("123456789", StringUtilities.abbreviateLeft("123456789", 9));
    assertEquals("...56789", StringUtilities.abbreviateLeft("123456789", 8));
    assertEquals("...6789", StringUtilities.abbreviateLeft("123456789", 7));
    assertEquals("...789", StringUtilities.abbreviateLeft("123456789", 6));
    assertEquals("...89", StringUtilities.abbreviateLeft("123456789", 5));
    assertEquals("...9", StringUtilities.abbreviateLeft("123456789", 4));
    try {
      assertEquals("...", StringUtilities.abbreviateLeft("123456789", 3));
      fail();
    } catch (IllegalArgumentException e) {

    }
  }

  public void test_EMPTY() {
    assertEquals("", StringUtilities.EMPTY);
    assertTrue(StringUtilities.EMPTY.isEmpty());
  }

  public void test_EMPTY_ARRAY() {
    assertEquals(0, StringUtilities.EMPTY_ARRAY.length);
  }

  public void test_EMPTY_LIST() {
    assertTrue(StringUtilities.EMPTY_LIST.isEmpty());
  }

  public void test_endsWith3() {
    assertTrue(StringUtilities.endsWith3("abc", 'a', 'b', 'c')); // all
    assertTrue(StringUtilities.endsWith3("abcdefghi", 'g', 'h', 'i')); // end
    assertFalse(StringUtilities.endsWith3("abcdefghi", 'd', 'e', 'a')); // missing
  }

  public void test_endsWithChar() {
    assertTrue(StringUtilities.endsWithChar("a", 'a'));
    assertFalse(StringUtilities.endsWithChar("b", 'a'));
    assertFalse(StringUtilities.endsWithChar("", 'a'));
  }

  public void test_indexOf1() {
    assertEquals(0, StringUtilities.indexOf1("a", 0, 'a')); // all
    assertEquals(0, StringUtilities.indexOf1("abcdef", 0, 'a')); // first
    assertEquals(2, StringUtilities.indexOf1("abcdef", 0, 'c')); // middle
    assertEquals(5, StringUtilities.indexOf1("abcdef", 0, 'f')); // last
    assertEquals(-1, StringUtilities.indexOf1("abcdef", 0, 'z')); // missing
    assertEquals(-1, StringUtilities.indexOf1("abcdef", 1, 'a')); // before start
  }

  public void test_indexOf2() {
    assertEquals(0, StringUtilities.indexOf2("ab", 0, 'a', 'b')); // all
    assertEquals(0, StringUtilities.indexOf2("abcdef", 0, 'a', 'b')); // first
    assertEquals(2, StringUtilities.indexOf2("abcdef", 0, 'c', 'd')); // middle
    assertEquals(4, StringUtilities.indexOf2("abcdef", 0, 'e', 'f')); // last
    assertEquals(-1, StringUtilities.indexOf2("abcdef", 0, 'd', 'a')); // missing
    assertEquals(-1, StringUtilities.indexOf2("abcdef", 1, 'a', 'b')); // before start
  }

  public void test_indexOf4() {
    assertEquals(0, StringUtilities.indexOf4("abcd", 0, 'a', 'b', 'c', 'd')); // all
    assertEquals(0, StringUtilities.indexOf4("abcdefghi", 0, 'a', 'b', 'c', 'd')); // first
    assertEquals(2, StringUtilities.indexOf4("abcdefghi", 0, 'c', 'd', 'e', 'f')); // middle
    assertEquals(5, StringUtilities.indexOf4("abcdefghi", 0, 'f', 'g', 'h', 'i')); // last
    assertEquals(-1, StringUtilities.indexOf4("abcdefghi", 0, 'd', 'e', 'a', 'd')); // missing
    assertEquals(-1, StringUtilities.indexOf4("abcdefghi", 1, 'a', 'b', 'c', 'd')); // before start
  }

  public void test_indexOf5() {
    assertEquals(0, StringUtilities.indexOf5("abcde", 0, 'a', 'b', 'c', 'd', 'e')); // all
    assertEquals(0, StringUtilities.indexOf5("abcdefghi", 0, 'a', 'b', 'c', 'd', 'e')); // first
    assertEquals(2, StringUtilities.indexOf5("abcdefghi", 0, 'c', 'd', 'e', 'f', 'g')); // middle
    assertEquals(4, StringUtilities.indexOf5("abcdefghi", 0, 'e', 'f', 'g', 'h', 'i')); // last
    assertEquals(-1, StringUtilities.indexOf5("abcdefghi", 0, 'd', 'e', 'f', 'i', 'n')); // missing
    assertEquals(-1, StringUtilities.indexOf5("abcdefghi", 1, 'a', 'b', 'c', 'd', 'e')); // before start
  }

  public void test_isAlpha() throws Exception {
    assertFalse(StringUtilities.isAlpha(null));
    assertFalse(StringUtilities.isAlpha(""));
    assertFalse(StringUtilities.isAlpha("-"));
    assertFalse(StringUtilities.isAlpha("0"));
    assertFalse(StringUtilities.isAlpha("0a"));
    assertFalse(StringUtilities.isAlpha("a0"));
    assertFalse(StringUtilities.isAlpha("a b"));
    assertTrue(StringUtilities.isAlpha("a"));
    assertTrue(StringUtilities.isAlpha("ab"));
  }

  public void test_isEmpty() {
    assertTrue(StringUtilities.isEmpty(""));
    assertFalse(StringUtilities.isEmpty(" "));
    assertFalse(StringUtilities.isEmpty("a"));
    assertTrue(StringUtilities.isEmpty(StringUtilities.EMPTY));
  }

  public void test_isTagName() throws Exception {
    assertFalse(StringUtilities.isTagName(null));
    assertFalse(StringUtilities.isTagName(""));
    assertFalse(StringUtilities.isTagName("-"));
    assertFalse(StringUtilities.isTagName("0"));
    assertFalse(StringUtilities.isTagName("0a"));
    assertFalse(StringUtilities.isTagName("a b"));
    assertTrue(StringUtilities.isTagName("a0"));
    assertTrue(StringUtilities.isTagName("a"));
    assertTrue(StringUtilities.isTagName("ab"));
    assertTrue(StringUtilities.isTagName("a-b"));
  }

  public void test_printListOfQuotedNames_empty() {
    try {
      StringUtilities.printListOfQuotedNames(new String[0]);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException exception) {
      // Expected
    }
  }

  public void test_printListOfQuotedNames_five() {
    assertEquals(
        "'a', 'b', 'c', 'd' and 'e'",
        StringUtilities.printListOfQuotedNames(new String[] {"a", "b", "c", "d", "e"}));
  }

  public void test_printListOfQuotedNames_null() {
    try {
      StringUtilities.printListOfQuotedNames(null);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException exception) {
      // Expected
    }
  }

  public void test_printListOfQuotedNames_one() {
    try {
      StringUtilities.printListOfQuotedNames(new String[] {"a"});
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException exception) {
      // Expected
    }
  }

  public void test_printListOfQuotedNames_three() {
    assertEquals(
        "'a', 'b' and 'c'",
        StringUtilities.printListOfQuotedNames(new String[] {"a", "b", "c"}));
  }

  public void test_printListOfQuotedNames_two() {
    assertEquals("'a' and 'b'", StringUtilities.printListOfQuotedNames(new String[] {"a", "b"}));
  }

  public void test_startsWith2() {
    assertTrue(StringUtilities.startsWith2("ab", 0, 'a', 'b')); // all
    assertTrue(StringUtilities.startsWith2("abcdefghi", 0, 'a', 'b')); // first
    assertTrue(StringUtilities.startsWith2("abcdefghi", 2, 'c', 'd')); // middle
    assertTrue(StringUtilities.startsWith2("abcdefghi", 5, 'f', 'g')); // end
    assertFalse(StringUtilities.startsWith2("abcdefghi", 0, 'd', 'd')); // missing
  }

  public void test_startsWith3() {
    assertTrue(StringUtilities.startsWith3("abc", 0, 'a', 'b', 'c')); // all
    assertTrue(StringUtilities.startsWith3("abcdefghi", 0, 'a', 'b', 'c')); // first
    assertTrue(StringUtilities.startsWith3("abcdefghi", 2, 'c', 'd', 'e')); // middle
    assertTrue(StringUtilities.startsWith3("abcdefghi", 6, 'g', 'h', 'i')); // end
    assertFalse(StringUtilities.startsWith3("abcdefghi", 0, 'd', 'e', 'a')); // missing
  }

  public void test_startsWith4() {
    assertTrue(StringUtilities.startsWith4("abcd", 0, 'a', 'b', 'c', 'd')); // all
    assertTrue(StringUtilities.startsWith4("abcdefghi", 0, 'a', 'b', 'c', 'd')); // first
    assertTrue(StringUtilities.startsWith4("abcdefghi", 2, 'c', 'd', 'e', 'f')); // middle
    assertTrue(StringUtilities.startsWith4("abcdefghi", 5, 'f', 'g', 'h', 'i')); // end
    assertFalse(StringUtilities.startsWith4("abcdefghi", 0, 'd', 'e', 'a', 'd')); // missing
  }

  public void test_startsWith5() {
    assertTrue(StringUtilities.startsWith5("abcde", 0, 'a', 'b', 'c', 'd', 'e')); // all
    assertTrue(StringUtilities.startsWith5("abcdefghi", 0, 'a', 'b', 'c', 'd', 'e')); // first
    assertTrue(StringUtilities.startsWith5("abcdefghi", 2, 'c', 'd', 'e', 'f', 'g')); // middle
    assertTrue(StringUtilities.startsWith5("abcdefghi", 4, 'e', 'f', 'g', 'h', 'i')); // end
    assertFalse(StringUtilities.startsWith5("abcdefghi", 0, 'a', 'b', 'c', 'b', 'a')); // missing
  }

  public void test_startsWith6() {
    assertTrue(StringUtilities.startsWith6("abcdef", 0, 'a', 'b', 'c', 'd', 'e', 'f')); // all
    assertTrue(StringUtilities.startsWith6("abcdefghi", 0, 'a', 'b', 'c', 'd', 'e', 'f')); // first
    assertTrue(StringUtilities.startsWith6("abcdefghi", 2, 'c', 'd', 'e', 'f', 'g', 'h')); // middle
    assertTrue(StringUtilities.startsWith6("abcdefghi", 3, 'd', 'e', 'f', 'g', 'h', 'i')); // end
    assertFalse(StringUtilities.startsWith6("abcdefghi", 0, 'a', 'b', 'c', 'd', 'e', 'g')); // missing
  }

  public void test_startsWithChar() {
    assertTrue(StringUtilities.startsWithChar("a", 'a'));
    assertFalse(StringUtilities.startsWithChar("b", 'a'));
    assertFalse(StringUtilities.startsWithChar("", 'a'));
  }

  public void test_substringBefore() {
    assertEquals(null, StringUtilities.substringBefore(null, ""));
    assertEquals(null, StringUtilities.substringBefore(null, "a"));
    assertEquals("", StringUtilities.substringBefore("", "a"));
    assertEquals("", StringUtilities.substringBefore("abc", "a"));
    assertEquals("a", StringUtilities.substringBefore("abcba", "b"));
    assertEquals("ab", StringUtilities.substringBefore("abc", "c"));
    assertEquals("abc", StringUtilities.substringBefore("abc", "d"));
    assertEquals("", StringUtilities.substringBefore("abc", ""));
    assertEquals("abc", StringUtilities.substringBefore("abc", null));
  }

  public void test_substringBeforeChar() {
    assertEquals(null, StringUtilities.substringBeforeChar(null, 'a'));
    assertEquals("", StringUtilities.substringBeforeChar("", 'a'));
    assertEquals("", StringUtilities.substringBeforeChar("abc", 'a'));
    assertEquals("a", StringUtilities.substringBeforeChar("abcba", 'b'));
    assertEquals("ab", StringUtilities.substringBeforeChar("abc", 'c'));
    assertEquals("abc", StringUtilities.substringBeforeChar("abc", 'd'));
  }
}
