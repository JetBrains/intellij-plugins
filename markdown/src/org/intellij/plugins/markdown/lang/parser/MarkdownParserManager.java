package org.intellij.plugins.markdown.lang.parser;

import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.ast.ASTNode;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor;
import org.jetbrains.annotations.NotNull;

public class MarkdownParserManager {

  private static final ThreadLocal<ParsingInfo> ourLastParsingResult = new ThreadLocal<ParsingInfo>();

  public static ASTNode parseContent(@NotNull CharSequence buffer) {
    final ParsingInfo info = ourLastParsingResult.get();
    if (info != null && info.myBufferHash == buffer.hashCode() && info.myBuffer.equals(buffer)) {
      return info.myParseResult;
    }

    final ASTNode parseResult = new MarkdownParser(CommonMarkMarkerProcessor.Factory.INSTANCE$)
      .parse(MarkdownElementTypes.MARKDOWN_FILE, buffer.toString(), false);
    ourLastParsingResult.set(new ParsingInfo(buffer, parseResult));
    return parseResult;
  }

  private static class ParsingInfo {
    @NotNull
    final CharSequence myBuffer;
    final int myBufferHash;
    @NotNull
    final ASTNode myParseResult;

    public ParsingInfo(@NotNull CharSequence buffer, @NotNull ASTNode parseResult) {
      myBuffer = buffer;
      myBufferHash = myBuffer.hashCode();
      myParseResult = parseResult;
    }
  }
}
