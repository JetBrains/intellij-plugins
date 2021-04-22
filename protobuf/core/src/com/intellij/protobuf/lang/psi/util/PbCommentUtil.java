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
package com.intellij.protobuf.lang.psi.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

/**
 * Utilities for collecting documentation comments surrounding proto elements. Collection methods
 * follow the rules outlined in the proto tokenizer:
 *
 * <pre>
 *   optional int32 foo = 1;  // Comment attached to foo.
 *   // Comment attached to bar.
 *   optional int32 bar = 2;
 *
 *   optional string baz = 3;
 *   // Comment attached to baz.
 *   // Another line attached to baz.
 *
 *   // Comment attached to qux.
 *   //
 *   // Another line attached to qux.
 *   optional double qux = 4;
 *
 *   // These comments are not connected
 *   // to anything and are ignored.
 *
 *   optional string corge = 5;
 *   /* Block comment attached
 *   to corge. *&#47
 *   /* Block comment attached to
 *   * grault. *&#47
 *   optional int32 grault = 6;
 *
 *   // ignored detached comments.
 * </pre>
 */
public class PbCommentUtil {

  /** Collect comments that begin directly before <code>element</code>. */
  public static List<PsiComment> collectLeadingComments(PsiElement element) {
    // First, backup to the previous non-comment, non-whitespace element and collect from there.
    PsiElement start = backup(element);
    CommentCollector collector = new CommentCollector();
    collectComments(element.getContainingFile(), start, collector);
    return collector.getLeadingComments();
  }

  /** Collect comments that begin directly after <code>element</code>. */
  public static List<PsiComment> collectTrailingComments(PsiElement element) {
    CommentCollector collector = new CommentCollector();
    collectComments(element.getContainingFile(), element, collector);
    return collector.getTrailingComments();
  }

  /**
   * Extracts actual text from the given comments and returns a list of single-line strings. The
   * following steps are performed.
   *
   * <ul>
   *   <li>For line comments, strip off the beginning "//" or "#"
   *   <li>For block comments, split into lines, strip off the /* and *&#47 from the first and last
   *       lines, and strip off leading whitespace and * on each intermediate line
   *   <li>Remove common leading spaces from all comments
   * </ul>
   *
   * @param comments the list of comments to extract text from
   * @return the list of extracted text lines
   */
  public static List<String> extractText(List<PsiComment> comments) {
    List<String> result = new ArrayList<>(comments.size());
    for (PsiComment comment : comments) {
      if (PbPsiUtil.isLineComment(comment)) {
        result.add(stripLineCommentStart(comment.getText()));
      } else if (PbPsiUtil.isBlockComment(comment)) {
        result.addAll(splitAndStripBlockCommentLines(comment.getText()));
      }
    }

    int commonSpaces = Integer.MAX_VALUE;
    for (String string : result) {
      int spaces = countLeadingSpaces(string);
      if (spaces >= 0 && spaces < commonSpaces) {
        commonSpaces = spaces;
      }
    }

    ListIterator<String> it = result.listIterator();
    while (it.hasNext()) {
      String string = it.next();
      if (StringUtil.isEmptyOrSpaces(string)) {
        string = "";
      } else if (commonSpaces > 0 && string.length() > commonSpaces) {
        string = string.substring(commonSpaces);
      }
      it.set(StringUtil.trimTrailing(string));
    }

    return result;
  }

  private static final Pattern LINE_COMMENT_START = Pattern.compile("^((//)|#)");
  private static final Pattern BLOCK_COMMENT_START_END = Pattern.compile("(^/\\*)|(\\*/$)");
  private static final Pattern BLOCK_COMMENT_INTERIOR = Pattern.compile("^[ \\t\\r\\f\\x0b]*\\*");

  private static String stripLineCommentStart(String lineComment) {
    return LINE_COMMENT_START.matcher(lineComment).replaceAll("");
  }

  private static List<String> splitAndStripBlockCommentLines(String blockComment) {
    String withoutStartAndEnd = BLOCK_COMMENT_START_END.matcher(blockComment).replaceAll("");
    String[] lines = withoutStartAndEnd.split("\n");
    List<String> result = new ArrayList<>(lines.length);
    if (!StringUtil.isEmptyOrSpaces(lines[0])) {
      result.add(lines[0]);
    }
    for (int i = 1; i < lines.length; i++) {
      String line = BLOCK_COMMENT_INTERIOR.matcher(lines[i]).replaceAll("");
      // Don't add the last line if it's empty.
      if (!(i == lines.length - 1 && StringUtil.isEmptyOrSpaces(line))) {
        result.add(line);
      }
    }
    return result;
  }

  private static int countLeadingSpaces(String string) {
    int count = 0;
    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) == ' ') {
        count++;
      } else {
        // Found a non-space character.
        return count;
      }
    }
    // This was a blank line.
    return -1;
  }

  private static class CommentCollector {
    // Is the comment in the comment buffer a line comment?
    private boolean hasLineComment = false;

    // Is it still possible that we could be reading a comment attached to the
    // previous token?
    private boolean canAttachToPrevious = true;

    // The current comment buffer.
    private List<PsiComment> buffer = null;

    // Comments stored as trailing the previous element.
    private List<PsiComment> trailingComments = null;

    void flush() {
      if (buffer != null) {
        if (canAttachToPrevious) {
          trailingComments = buffer;
          canAttachToPrevious = false;
        }
        clear();
      }
    }

    void addLineComment(PsiComment comment) {
      if (buffer != null && !hasLineComment) {
        flush();
      }
      hasLineComment = true;
      initAndAdd(comment);
    }

    void addBlockComment(PsiComment comment) {
      if (buffer != null) {
        flush();
      }
      hasLineComment = false;
      initAndAdd(comment);
    }

    void clear() {
      buffer = null;
    }

    void detach() {
      canAttachToPrevious = false;
    }

    List<PsiComment> getLeadingComments() {
      if (buffer == null) {
        return Collections.emptyList();
      }
      return buffer;
    }

    List<PsiComment> getTrailingComments() {
      if (trailingComments == null) {
        return Collections.emptyList();
      }
      return trailingComments;
    }

    private void initAndAdd(PsiComment comment) {
      if (buffer == null) {
        buffer = new ArrayList<>();
      }
      buffer.add(comment);
    }
  }

  /**
   * Starting at the given element, back up until the first non-whitespace, non-comment token is
   * found. Returns null if the start of the file is reached.
   */
  private static PsiElement backup(PsiElement leaf) {
    do {
      leaf = PsiTreeUtil.prevLeaf(leaf);
    } while (leaf != null && (PbPsiUtil.isComment(leaf) || PbPsiUtil.isWhitespace(leaf)));
    return leaf;
  }

  // This method, and the associated CommentCollector, mimic the NextWithComment function's behavior
  // in tokenizer.cc.
  // https://github.com/google/protobuf/blob/master/src/google/protobuf/io/tokenizer.cc
  private static void collectComments(PsiFile file, PsiElement start, CommentCollector collector) {
    // A comment appearing on the same line must be attached to the previous
    // declaration.
    PsiElement leaf;
    if (start == null) {
      // Start of file.
      leaf = PsiTreeUtil.getDeepestFirst(file);
      collector.detach();
    } else {
      leaf = nextLeaf(start);
      if (PbPsiUtil.isLineComment(leaf)) {
        collector.addLineComment((PsiComment) leaf);
        leaf = nextLeaf(leaf);

        // Don't allow comments on subsequent lines to be attached to a trailing
        // comment.
        collector.flush();
      } else if (PbPsiUtil.isBlockComment(leaf)) {
        collector.addBlockComment((PsiComment) leaf);
        leaf = nextLeaf(leaf);

        if (!isWhitespaceWithNewline(leaf)) {
          // Oops, the next token is on the same line.  If we recorded a comment
          // we really have no idea which token it should be attached to.
          collector.clear();
          return;
        }

        // Don't allow comments on subsequent lines to be attached to a trailing
        // comment.
        collector.flush();
      } else if (!isWhitespaceWithNewline(leaf)) {
        // The next token is on the same line.  There are no comments.
        return;
      }
    }

    leaf = skipWhitespaceWithoutBlankLine(leaf);

    // OK, we are now on the line *after* the previous token.
    while (true) {
      leaf = skipWhitespaceWithoutNewline(leaf);
      if (PbPsiUtil.isLineComment(leaf)) {
        collector.addLineComment((PsiComment) leaf);
      } else if (PbPsiUtil.isBlockComment(leaf)) {
        collector.addBlockComment((PsiComment) leaf);
      } else if (PbPsiUtil.isWhitespace(leaf)) {
        if (isWhitespaceWithBlankLine(leaf)) {
          // Completely blank line.
          collector.flush();
          collector.detach();
        }
        // Else, skip the whitespace.
      } else {
        if (leaf == null || isEndOfBlock(leaf)) {
          // It looks like we're at the end of a scope.  In this case it
          // makes no sense to attach a comment to the following token.
          collector.flush();
        }
        return;
      }

      leaf = nextLeaf(leaf);
    }
  }

  private static PsiElement nextLeaf(PsiElement leaf) {
    leaf = PsiTreeUtil.nextLeaf(leaf);
    return skipWhitespaceWithoutNewline(leaf);
  }

  private static int countNewlines(CharSequence whitespace, int max) {
    int count = 0;
    for (int i = 0; i < whitespace.length() && count < max; i++) {
      if (whitespace.charAt(i) == '\n') {
        count++;
      }
    }
    return count;
  }

  private static boolean isWhitespaceWithNewline(PsiElement element) {
    return PbPsiUtil.isWhitespace(element) && countNewlines(element.getText(), 1) > 0;
  }

  private static boolean isWhitespaceWithBlankLine(PsiElement element) {
    return PbPsiUtil.isWhitespace(element) && countNewlines(element.getText(), 2) > 1;
  }

  private static boolean isEndOfBlock(PsiElement element) {
    return PbPsiUtil.isElementType(element, ProtoTokenTypes.RBRACE)
        || PbPsiUtil.isElementType(element, ProtoTokenTypes.RBRACK)
        || PbPsiUtil.isElementType(element, ProtoTokenTypes.RPAREN);
  }

  private static PsiElement skipWhitespaceWithoutNewline(PsiElement leaf) {
    while (PbPsiUtil.isWhitespace(leaf) && !isWhitespaceWithNewline(leaf)) {
      leaf = PsiTreeUtil.nextLeaf(leaf);
    }
    return leaf;
  }

  private static PsiElement skipWhitespaceWithoutBlankLine(PsiElement leaf) {
    while (PbPsiUtil.isWhitespace(leaf) && !isWhitespaceWithBlankLine(leaf)) {
      leaf = PsiTreeUtil.nextLeaf(leaf);
    }
    return leaf;
  }
}
