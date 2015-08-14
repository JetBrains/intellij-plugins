package com.jetbrains.lang.dart.ide.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartFormattingModelBuilder implements FormattingModelBuilder {
  @NotNull
  @Override
  public FormattingModel createModel(@NotNull final PsiElement element, @NotNull final CodeStyleSettings settings) {
    // element can be DartFile, DartEmbeddedContent, DartExpressionCodeFragment
    final PsiFile psiFile = element.getContainingFile();
    final ASTNode rootNode = psiFile instanceof DartFile ? psiFile.getNode() : element.getNode();
    final DartBlock rootBlock;
    Object wasFormatted = rootNode.getUserData(DartPreFormatProcessor.FORMAT_MARK);
    if (wasFormatted == DartPreFormatProcessor.FORMAT_MARKER) {
      rootNode.putUserData(DartPreFormatProcessor.FORMAT_MARK, null);
      rootBlock = new NonFormattingDartBlock(rootNode, null, null, settings);
    } else {
      rootBlock = new DartBlock(rootNode, null, null, settings);
    }
    return new DartFormattingModel(rootBlock, element.getProject(), settings, psiFile.getFileType(), psiFile);
  }

  @Nullable
  @Override
  public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
    return null;
  }

  private static class DartFormattingModel extends DocumentBasedFormattingModel {
    private DartFormattingModel(DartBlock rootBlock, Project project, CodeStyleSettings settings, FileType fileType, PsiFile psiFile) {
      super(rootBlock, project, settings, fileType, psiFile);
    }
  }

  private static class NonFormattingDartBlock extends DartBlock {
    NonFormattingDartBlock(ASTNode node, Wrap wrap, Alignment alignment, CodeStyleSettings settings) {
      super(node, wrap, alignment, settings);
    }

    @Override
    public Indent getIndent() {
      return null;
    }

    @Override
    public Spacing getSpacing(Block child1, @NotNull Block child2) {
      return null;
    }

    @Override
    protected List<Block> buildChildren() {
      return new ArrayList<Block>();
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(final int newIndex) {
      return new ChildAttributes(Indent.getNoneIndent(), null);
    }

    @Override
    public boolean isLeaf() {
      return true;
    }
  }
}
