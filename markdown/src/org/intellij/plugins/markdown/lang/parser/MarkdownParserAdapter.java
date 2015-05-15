package org.intellij.plugins.markdown.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.intellij.markdown.MarkdownElementTypes;
import org.intellij.markdown.ast.LeafASTNode;
import org.intellij.markdown.ast.visitors.RecursiveVisitor;
import org.intellij.markdown.parser.MarkdownParser;
import org.intellij.markdown.parser.dialects.commonmark.CommonMarkMarkerProcessor;
import org.intellij.plugins.markdown.lang.MarkdownElementType;
import org.jetbrains.annotations.NotNull;

public class MarkdownParserAdapter implements PsiParser {

  @NotNull
  public ASTNode parse(IElementType root, PsiBuilder builder) {

    PsiBuilder.Marker rootMarker = builder.mark();

    final PsiBuilderTokensCache tokensCache = new PsiBuilderTokensCache(builder);
    final org.intellij.markdown.ast.ASTNode parsedTree = new MarkdownParser(CommonMarkMarkerProcessor.Factory.INSTANCE$)
            .parse(MarkdownElementTypes.MARKDOWN_FILE, tokensCache);

    assert builder.getCurrentOffset() == 0;
    new PsiBuilderFillingVisitor(builder).visitNode(parsedTree);

    rootMarker.done(root);

    return builder.getTreeBuilt();
  }

  private static class PsiBuilderFillingVisitor extends RecursiveVisitor {
    @NotNull
    private final PsiBuilder builder;

    public PsiBuilderFillingVisitor(@NotNull PsiBuilder builder) {
      this.builder = builder;
    }

    @Override
    public void visitNode(@NotNull org.intellij.markdown.ast.ASTNode node) {
      if (node instanceof LeafASTNode) {
        return;
      }

      ensureBuilderInPosition(node.getStartOffset());
      final PsiBuilder.Marker marker = builder.mark();

      super.visitNode(node);

      ensureBuilderInPosition(node.getEndOffset());
      marker.done(MarkdownElementType.platformType(node.getType()));
    }

    private void ensureBuilderInPosition(int position) {
      while (builder.getCurrentOffset() < position) {
        builder.advanceLexer();
      }

      if (builder.getCurrentOffset() != position) {
        throw new AssertionError("parsed tree and lexer are unsynchronized");
      }
    }
  }
}
