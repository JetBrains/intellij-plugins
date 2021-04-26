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
package com.intellij.protobuf.gencodeutils;

import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allows a developer to create test source files (e.g., java), which have goto-decl test
 * expectations marked within the source files as comments. The cursor position to perform a
 * goto-decl is defined by a caret marker.
 *
 * <p>{@link #EXPECT_MARKER} partitions a test file so that a caret marker only applies to the
 * region bounded by the current {@link #EXPECT_MARKER} and the next boundary. Any caret marker
 * outside of an EXPECT partition are not checked, and only the first caret in an EXPECT partition
 * is checked. If there is no caret within an EXPECT partition, that is an error.
 *
 * <p>This class tracks the partition range, and the expected goto target as a pair of filename and
 * qualified symbol name.
 */
public final class GotoExpectationMarker {
  // Comment that specifies the expected goto-decl target from generated code (e.g., java)
  // to proto (or to generated code if not a proto symbol). The first component of the regex
  // is the filename, and the second is the "name" of the element (in the case of proto targets,
  // the qualified name relative to the file's package).
  public static final String EXPECT_MARKER = "EXPECT-NEXT:";

  private static final Pattern EXPECT_PATTERN =
      Pattern.compile("^" + EXPECT_MARKER + " (.*) / (.*)\\s");

  public final int startIndex;
  public final int endIndex;
  public final String expectedFile;
  public final String expectedElementName;

  private GotoExpectationMarker(
      int startIndex, int endIndex, String expectedFile, String expectedElementName) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.expectedFile = expectedFile;
    this.expectedElementName = expectedElementName;
  }

  public String rangeString() {
    return String.format("[%d, %d)", startIndex, endIndex);
  }

  public static List<GotoExpectationMarker> parseExpectations(PsiFile file) {
    String text = file.getText();
    int nextIndex = text.indexOf(EXPECT_MARKER);
    List<GotoExpectationMarker> expectations = new ArrayList<>();

    while (nextIndex != -1) {
      int startIndex = nextIndex;
      int endIndex;
      nextIndex = text.indexOf(EXPECT_MARKER, nextIndex + 1);
      if (nextIndex == -1) {
        endIndex = text.length();
      } else {
        endIndex = nextIndex;
      }
      GotoExpectationMarker expectation = parseExpectation(startIndex, endIndex, text);
      expectations.add(expectation);
    }
    return expectations;
  }

  private static GotoExpectationMarker parseExpectation(int startIndex, int endIndex, String text) {
    String substring = text.substring(startIndex, endIndex);
    Matcher matcher = EXPECT_PATTERN.matcher(substring);
    String expectedFile;
    String expectedElementName;
    if (matcher.find()) {
      expectedFile = matcher.group(1).trim();
      expectedElementName = matcher.group(2).trim();
    } else {
      throw new IllegalStateException(
          String.format(
              "Expected a match for %s within %s at range [%s, %s)",
              EXPECT_PATTERN.pattern(), substring, startIndex, endIndex));
    }
    return new GotoExpectationMarker(startIndex, endIndex, expectedFile, expectedElementName);
  }
}
