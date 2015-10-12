/*
 * Copyright (c) 2012, the Dart project authors.
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

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * The class {@code StringUtilities} defines utility methods for strings.
 * 
 * @coverage dart.server.utilities
 */
public final class StringUtilities {

  /**
   * The empty String {@code ""}.
   */
  public static final String EMPTY = "";

  /**
   * An empty array of {@link String}s.
   */
  public static final String[] EMPTY_ARRAY = new String[0];

  /**
   * An empty list of {@link String}s.
   */
  public static final List<String> EMPTY_LIST = Lists.newArrayList();

  /**
   * The {@link Interner} instance to use for {@link #intern(String)}.
   */
  private static final Interner<String> INTERNER = Interners.newWeakInterner();

  /**
   * Abbreviates a String using ellipses inserted at left.
   */
  public static String abbreviateLeft(String s, int width) {
    int length = s.length();
    if (length > width) {
      if (width < 4) {
        throw new IllegalArgumentException("Minimal width is 4");
      }
      return "..." + s.substring(length - (width - 3));
    }
    return s;
  }

  /**
   * Return {@code true} if the three-character substring occurs at the end of the given string.
   * 
   * @param string the string being searched
   * @param char1 the first character in the substring
   * @param char2 the second character in the substring
   * @param char3 the third character in the substring
   * @return {@code true} if the substring occurs at the end of the string
   */
  public static boolean endsWith3(String string, int char1, int char2, int char3) {
    int length = string.length();
    return length >= 3 && string.charAt(length - 3) == char1 && string.charAt(length - 2) == char2
        && string.charAt(length - 1) == char3;
  }

  /**
   * Return {@code true} if the given string ends with the given character.
   * 
   * @param string the string being searched
   * @param character the character being tested for
   * @return {@code true} if the string ends with the character
   */
  public static boolean endsWithChar(String string, int character) {
    int length = string.length();
    return length > 0 && string.charAt(length - 1) == character;
  }

  /**
   * Return the index of the first occurrence of the given character in the given string that is at
   * or after the given starting index. Return {@code -1} if the substring does not occur.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @param char1 the first character in the substring
   * @return the index of the first occurrence of the substring
   */
  public static int indexOf1(String string, int startIndex, int char1) {
    int index = startIndex;
    int last = string.length();
    while (index < last) {
      if (string.charAt(index) == char1) {
        return index;
      }
      index++;
    }
    return -1;
  }

  /**
   * Return the index of the first occurrence of the given characters as a substring of the given
   * string that is at or after the given starting index. Return {@code -1} if the substring does
   * not occur.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @param char1 the first character in the substring
   * @param char2 the second character in the substring
   * @return the index of the first occurrence of the substring
   */
  public static int indexOf2(String string, int startIndex, int char1, int char2) {
    int index = startIndex;
    int last = string.length() - 1;
    while (index < last) {
      if (string.charAt(index) == char1 && string.charAt(index + 1) == char2) {
        return index;
      }
      index++;
    }
    return -1;
  }

  /**
   * Return the index of the first occurrence of the given characters as a substring of the given
   * string that is at or after the given starting index. Return {@code -1} if the substring does
   * not occur.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @param char1 the first character in the substring
   * @param char2 the second character in the substring
   * @param char3 the third character in the substring
   * @param char4 the fourth character in the substring
   * @return the index of the first occurrence of the substring
   */
  public static int indexOf4(String string, int startIndex, int char1, int char2, int char3,
      int char4) {
    int index = startIndex;
    int last = string.length() - 3;
    while (index < last) {
      if (string.charAt(index) == char1 && string.charAt(index + 1) == char2
          && string.charAt(index + 2) == char3 && string.charAt(index + 3) == char4) {
        return index;
      }
      index++;
    }
    return -1;
  }

  /**
   * Return the index of the first occurrence of the given characters as a substring of the given
   * string that is at or after the given starting index. Return {@code -1} if the substring does
   * not occur.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @param char1 the first character in the substring
   * @param char2 the second character in the substring
   * @param char3 the third character in the substring
   * @param char4 the fourth character in the substring
   * @param char5 the fifth character in the substring
   * @return the index of the first occurrence of the substring
   */
  public static int indexOf5(String string, int startIndex, int char1, int char2, int char3,
      int char4, int char5) {
    int index = startIndex;
    int last = string.length() - 4;
    while (index < last) {
      if (string.charAt(index) == char1 && string.charAt(index + 1) == char2
          && string.charAt(index + 2) == char3 && string.charAt(index + 3) == char4
          && string.charAt(index + 4) == char5) {
        return index;
      }
      index++;
    }
    return -1;
  }

  /**
   * Return the index of the first not letter/digit character in the given string that is at or
   * after the given starting index. Return the length of the given string if the all characters to
   * the end are letters/digits.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @return the index of the first not letter/digit character
   */
  public static int indexOfFirstNotLetterDigit(String string, int startIndex) {
    int index = startIndex;
    int last = string.length();
    while (index < last) {
      char c = string.charAt(index);
      if (!Character.isLetterOrDigit(c)) {
        return index;
      }
      index++;
    }
    return last;
  }

  /**
   * Returns a canonical representation for the given {@link String}.
   * 
   * @return the given {@link String} or its canonical representation.
   */
  public static String intern(String str) {
    if (str == null) {
      return null;
    }
    str = new String(str);
    return INTERNER.intern(str);
  }

  /**
   * <p>
   * Checks if the CharSequence contains only Unicode letters.
   * </p>
   * <p>
   * {@code null} will return {@code false}. An empty CharSequence (length()=0) will return
   * {@code false}.
   * </p>
   * 
   * <pre>
   * StringUtils.isAlpha(null)   = false
   * StringUtils.isAlpha("")     = false
   * StringUtils.isAlpha("  ")   = false
   * StringUtils.isAlpha("abc")  = true
   * StringUtils.isAlpha("ab2c") = false
   * StringUtils.isAlpha("ab-c") = false
   * </pre>
   * 
   * @param cs the CharSequence to check, may be null
   * @return {@code true} if only contains letters, and is non-null
   */
  public static boolean isAlpha(CharSequence cs) {
    if (cs == null || cs.length() == 0) {
      return false;
    }
    int sz = cs.length();
    for (int i = 0; i < sz; i++) {
      if (Character.isLetter(cs.charAt(i)) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return {@code true} if the given CharSequence is empty ("") or null.
   * 
   * <pre>
   * StringUtils.isEmpty(null)      = true
   * StringUtils.isEmpty("")        = true
   * StringUtils.isEmpty(" ")       = false
   * StringUtils.isEmpty("bob")     = false
   * StringUtils.isEmpty("  bob  ") = false
   * </pre>
   * 
   * @param cs the CharSequence to check, may be null
   * @return {@code true} if the CharSequence is empty or null
   */
  public static boolean isEmpty(CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  /**
   * <p>
   * Checks if the String can be used as a tag name.
   * </p>
   * <p>
   * {@code null} will return {@code false}. An empty String (length()=0) will return {@code false}.
   * </p>
   * 
   * <pre>
   * StringUtils.isAlpha(null)   = false
   * StringUtils.isAlpha("")     = false
   * StringUtils.isAlpha("  ")   = false
   * StringUtils.isAlpha("ab c") = false
   * StringUtils.isAlpha("abc")  = true
   * StringUtils.isAlpha("ab2c") = true
   * StringUtils.isAlpha("ab-c") = true
   * </pre>
   * 
   * @param s the String to check, may be null
   * @return {@code true} if can be used as a tag name, and is non-null
   */
  public static boolean isTagName(String s) {
    if (s == null || s.length() == 0) {
      return false;
    }
    int sz = s.length();
    for (int i = 0; i < sz; i++) {
      char c = s.charAt(i);
      if (!Character.isLetter(c)) {
        if (i == 0) {
          return false;
        }
        if (!Character.isDigit(c) && c != '-') {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Produce a string containing all of the names in the given array, surrounded by single quotes,
   * and separated by commas. The list must contain at least two elements.
   * 
   * @param names the names to be printed
   * @return the result of printing the names
   */
  public static String printListOfQuotedNames(String[] names) {
    if (names == null) {
      throw new IllegalArgumentException("The list must not be null");
    }
    int count = names.length;
    if (count < 2) {
      throw new IllegalArgumentException("The list must contain at least two names");
    }
    StringBuilder builder = new StringBuilder();
    builder.append("'");
    builder.append(names[0]);
    builder.append("'");
    for (int i = 1; i < count - 1; i++) {
      builder.append(", '");
      builder.append(names[i]);
      builder.append("'");
    }
    builder.append(" and '");
    builder.append(names[count - 1]);
    builder.append("'");
    return builder.toString();
  }

  /**
   * Return {@code true} if the two-character substring occurs at the given index in the given
   * string.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @param char1 the first character in the substring
   * @param char2 the second character in the substring
   * @return {@code true} if the substring occurs at the given index in the string
   */
  public static boolean startsWith2(String string, int startIndex, int char1, int char2) {
    return string.length() - startIndex >= 2 && string.charAt(startIndex) == char1
        && string.charAt(startIndex + 1) == char2;
  }

  /**
   * Return {@code true} if the three-character substring occurs at the given index in the given
   * string.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @param char1 the first character in the substring
   * @param char2 the second character in the substring
   * @param char3 the third character in the substring
   * @return {@code true} if the substring occurs at the given index in the string
   */
  public static boolean startsWith3(String string, int startIndex, int char1, int char2, int char3) {
    return string.length() - startIndex >= 3 && string.charAt(startIndex) == char1
        && string.charAt(startIndex + 1) == char2 && string.charAt(startIndex + 2) == char3;
  }

  /**
   * Return {@code true} if the four-character substring occurs at the given index in the given
   * string.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @param char1 the first character in the substring
   * @param char2 the second character in the substring
   * @param char3 the third character in the substring
   * @param char4 the fourth character in the substring
   * @return {@code true} if the substring occurs at the given index in the string
   */
  public static boolean startsWith4(String string, int startIndex, int char1, int char2, int char3,
      int char4) {
    return string.length() - startIndex >= 4 && string.charAt(startIndex) == char1
        && string.charAt(startIndex + 1) == char2 && string.charAt(startIndex + 2) == char3
        && string.charAt(startIndex + 3) == char4;
  }

  /**
   * Return {@code true} if the five-character substring occurs at the given index in the given
   * string.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @param char1 the first character in the substring
   * @param char2 the second character in the substring
   * @param char3 the third character in the substring
   * @param char4 the fourth character in the substring
   * @param char5 the fifth character in the substring
   * @return {@code true} if the substring occurs at the given index in the string
   */
  public static boolean startsWith5(String string, int startIndex, int char1, int char2, int char3,
      int char4, int char5) {
    return string.length() - startIndex >= 5 && string.charAt(startIndex) == char1
        && string.charAt(startIndex + 1) == char2 && string.charAt(startIndex + 2) == char3
        && string.charAt(startIndex + 3) == char4 && string.charAt(startIndex + 4) == char5;
  }

  /**
   * Return {@code true} if the six-character substring occurs at the given index in the given
   * string.
   * 
   * @param string the string being searched
   * @param startIndex the index at which the search should begin
   * @param char1 the first character in the substring
   * @param char2 the second character in the substring
   * @param char3 the third character in the substring
   * @param char4 the fourth character in the substring
   * @param char5 the fifth character in the substring
   * @param char6 the sixth character in the substring
   * @return {@code true} if the substring occurs at the given index in the string
   */
  public static boolean startsWith6(String string, int startIndex, int char1, int char2, int char3,
      int char4, int char5, int char6) {
    return string.length() - startIndex >= 6 && string.charAt(startIndex) == char1
        && string.charAt(startIndex + 1) == char2 && string.charAt(startIndex + 2) == char3
        && string.charAt(startIndex + 3) == char4 && string.charAt(startIndex + 4) == char5
        && string.charAt(startIndex + 5) == char6;
  }

  /**
   * Return {@code true} if the given string starts with the given character.
   * 
   * @param string the string being searched
   * @param character the character being tested for
   * @return {@code true} if the string starts with the character
   */
  public static boolean startsWithChar(String string, int character) {
    return string.length() > 0 && string.charAt(0) == character;
  }

  /**
   * <p>
   * Gets the substring after the first occurrence of a separator. The separator is not returned.
   * </p>
   * <p>
   * A {@code null} string input will return {@code null}. An empty ("") string input will return
   * the empty string. A {@code null} separator will return the empty string if the input string is
   * not {@code null}.
   * </p>
   * <p>
   * If nothing is found, the empty string is returned.
   * </p>
   * 
   * <pre>
   * StringUtils.substringAfter(null, *)      = null
   * StringUtils.substringAfter("", *)        = ""
   * StringUtils.substringAfter(*, null)      = ""
   * StringUtils.substringAfter("abc", "a")   = "bc"
   * StringUtils.substringAfter("abcba", "b") = "cba"
   * StringUtils.substringAfter("abc", "c")   = ""
   * StringUtils.substringAfter("abc", "d")   = ""
   * StringUtils.substringAfter("abc", "")    = "abc"
   * </pre>
   * 
   * @param str the String to get a substring from, may be null
   * @param separator the String to search for, may be null
   * @return the substring after the first occurrence of the separator, {@code null} if null String
   *         input
   */
  public static String substringAfter(String str, String separator) {
    if (isEmpty(str)) {
      return str;
    }
    if (separator == null) {
      return EMPTY;
    }
    int pos = str.indexOf(separator);
    if (pos == -1) {
      return EMPTY;
    }
    return str.substring(pos + separator.length());
  }

  /**
   * Return the substring before the first occurrence of a separator. The separator is not returned.
   * <p>
   * A {@code null} string input will return {@code null}. An empty ("") string input will return
   * the empty string. A {@code null} separator will return the input string.
   * <p>
   * If nothing is found, the string input is returned.
   * 
   * <pre>
   * StringUtils.substringBefore(null, *)      = null
   * StringUtils.substringBefore("", *)        = ""
   * StringUtils.substringBefore("abc", "a")   = ""
   * StringUtils.substringBefore("abcba", "b") = "a"
   * StringUtils.substringBefore("abc", "c")   = "ab"
   * StringUtils.substringBefore("abc", "d")   = "abc"
   * StringUtils.substringBefore("abc", "")    = ""
   * StringUtils.substringBefore("abc", null)  = "abc"
   * </pre>
   * 
   * @param str the string to get a substring from, may be null
   * @param separator the string to search for, may be null
   * @return the substring before the first occurrence of the separator
   */
  public static String substringBefore(String str, String separator) {
    if (isEmpty(str) || separator == null) {
      return str;
    }
    if (separator.length() == 0) {
      return EMPTY;
    }
    int pos = str.indexOf(separator);
    if (pos == -1) {
      return str;
    }
    return str.substring(0, pos);
  }

  /**
   * Return the substring before the first occurrence of a separator. The separator is not included
   * in the returned value.
   * <p>
   * A {@code null} string input will return {@code null}. An empty ("") string input will return
   * the empty string.
   * <p>
   * If nothing is found, the string input is returned.
   * 
   * <pre>
   * StringUtils.substringBefore(null, *)      = null
   * StringUtils.substringBefore("", *)        = ""
   * StringUtils.substringBefore("abc", 'a')   = ""
   * StringUtils.substringBefore("abcba", 'b') = "a"
   * StringUtils.substringBefore("abc", 'c')   = "ab"
   * StringUtils.substringBefore("abc", 'd')   = "abc"
   * </pre>
   * 
   * @param str the string to get a substring from, may be null
   * @param separator the character to search for
   * @return the substring before the first occurrence of the separator
   */
  public static String substringBeforeChar(String str, int separator) {
    if (isEmpty(str)) {
      return str;
    }
    int pos = str.indexOf(separator);
    if (pos < 0) {
      return str;
    }
    return str.substring(0, pos);
  }

  /**
   * Prevent the creation of instances of this class.
   */
  private StringUtilities() {
  }
}
