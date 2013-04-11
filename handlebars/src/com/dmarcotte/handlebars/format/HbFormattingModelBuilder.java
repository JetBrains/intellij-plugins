package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbPsiUtil;
import com.intellij.formatting.*;
import com.intellij.formatting.templateLanguages.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.intellij.psi.formatter.xml.SyntheticBlock;
import com.intellij.psi.templateLanguages.SimpleTemplateLanguageFormattingModelBuilder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Template aware formatter which provides formatting for Handlebars/Mustache syntax and delegates formatting
 * for the templated language to that languages formatter
 */
public class HbFormattingModelBuilder extends TemplateLanguageFormattingModelBuilder {
  @Override
  public TemplateLanguageBlock createTemplateLanguageBlock(@NotNull ASTNode node,
                                                           @Nullable Wrap wrap,
                                                           @Nullable Alignment alignment,
                                                           @Nullable List<DataLanguageBlockWrapper> foreignChildren,
                                                           @NotNull CodeStyleSettings codeStyleSettings) {
    return new HandlebarsBlock(this, codeStyleSettings, node, foreignChildren);
  }

  /**
   * We have to override {@link com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder#createModel}
   * since after we delegate to some templated languages, those languages (xml/html for sure, potentially others)
   * delegate right back to us to format the HbTokenTypes.OUTER_ELEMENT_TYPE token we tell them to ignore,
   * causing an stack-overflowing loop of polite format-delegation.
   */
  @NotNull
  public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {

    if (!HbConfig.isFormattingEnabled()) {
      // formatting is disabled, return the no-op formatter (note that this still delegates formatting
      // to the templated language, which lets the users manage that separately)
      return new SimpleTemplateLanguageFormattingModelBuilder().createModel(element, settings);
    }

    final PsiFile file = element.getContainingFile();
    Block rootBlock;

    ASTNode node = element.getNode();

    if (node.getElementType() == HbTokenTypes.OUTER_ELEMENT_TYPE) {
      // If we're looking at a HbTokenTypes.OUTER_ELEMENT_TYPE element, then we've been invoked by our templated
      // language.  Make a dummy block to allow that formatter to continue
      return new SimpleTemplateLanguageFormattingModelBuilder().createModel(element, settings);
    }
    else {
      rootBlock = getRootBlock(file, file.getViewProvider(), settings);
    }
    return new DocumentBasedFormattingModel(rootBlock, element.getProject(), settings, file.getFileType(), file);
  }

  /**
   * Do format my model!
   *
   * @return false all the time to tell the {@link com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder}
   *         to not-not format our model (i.e. yes please!  Format away!)
   */
  @Override
  public boolean dontFormatMyModel() {
    return false;
  }

  private static class HandlebarsBlock extends TemplateLanguageBlock {

    HandlebarsBlock(@NotNull TemplateLanguageBlockFactory blockFactory, @NotNull CodeStyleSettings settings,
                    @NotNull ASTNode node, @Nullable List<DataLanguageBlockWrapper> foreignChildren) {
      super(blockFactory, settings, node, foreignChildren);
    }

    /**
     * We indented the code in the following manner, playing nice with the formatting from the language
     * we're templating:
     * <pre>
     *   * Block expressions:
     *      {{#foo}}
     *          INDENTED_CONTENT
     *      {{/foo}}
     *   * Inverse block expressions:
     *      {{^bar}}
     *          INDENTED_CONTENT
     *      {{/bar}}
     *   * Conditional expressions using the "else" syntax:
     *      {{#if test}}
     *          INDENTED_CONTENT
     *      {{else}}
     *          INDENTED_CONTENT
     *      {{/if}}
     *   * Conditional expressions using the "^" syntax:
     *      {{#if test}}
     *          INDENTED_CONTENT
     *      {{^}}
     *          INDENTED_CONTENT
     *      {{/if}}
     * </pre>
     * <p/>
     * This naturally maps to any "statements" expression in the grammar which is not a child of the
     * root "program" element.  See {@link com.dmarcotte.handlebars.parsing.HbParsing#parseProgram} and
     * {@link com.dmarcotte.handlebars.parsing.HbParsing#parseStatement(com.intellij.lang.PsiBuilder)} for the
     * relevant parts of the parser.
     * <p/>
     * To understand the approach in this method, consider the following:
     * <pre>
     * {{#foo}}
     * BEGIN_STATEMENTS
     * TEMPLATE_STUFF
     * END_STATEMENTS
     * {{/foo}}
     * </pre>
     * <p/>
     * then formatting looks easy. Simply apply an indent (represented here by "[hb_indent]") to the STATEMENTS and call it a day:
     * <pre>
     * {{#foo}}
     * [hb_indent]BEGIN_STATEMENTS
     * [hb_indent]TEMPLATE_STUFF
     * [hb_indent]END_STATEMENTS
     * {{/foo}}
     * </pre>
     * <p/>
     * However, if we're contained in templated language block, it's going to provide some indents of its own
     * (call them "[tl_indent]") which quickly leads to undesirable double-indenting:
     * <p/>
     * <pre>
     * &lt;div>
     * {{#foo}}
     * [hb_indent]BEGIN_STATEMENTS
     * [hb_indent][tl_indent]TEMPLATE_STUFF
     * [hb_indent]END_STATEMENTS
     * {{/foo}}
     * &lt;/div>
     * </pre>
     * So to behave correctly in both situations, we indent STATEMENTS from the "outside" anytime we're not wrapped
     * in a templated language block, and we indent STATEMENTS from the "inside" (i.e. apply an indent to each non-template
     * language STATEMENT inside the STATEMENTS) to interleave nicely with templated-language provided indents.
     */
    @Override
    public Indent getIndent() {
      // ignore whitespace
      if (myNode.getText().trim().length() == 0) {
        return Indent.getNoneIndent();
      }

      if (HbPsiUtil.isNonRootStatementsElement(myNode.getPsi())) {
        // we're computing the indent for a non-root STATEMENTS:
        //      if it's not contained in a foreign block, indent!
        if (hasOnlyHbLanguageParents()) {
          return Indent.getNormalIndent();
        }
      }

      if (myNode.getTreeParent() != null
          && HbPsiUtil.isNonRootStatementsElement(myNode.getTreeParent().getPsi())) {
        // we're computing the indent for a direct descendant of a non-root STATEMENTS:
        //      if its Block parent (i.e. not HB AST Tree parent) is a Handlebars block
        //      which has NOT been indented, then have the element provide the indent itself
        if (getParent() instanceof HandlebarsBlock
            && ((HandlebarsBlock)getParent()).getIndent() == Indent.getNoneIndent()) {
          return Indent.getNormalIndent();
        }
      }

      // any element that is the direct descendant of a foreign block gets an indent
      if (getRealBlockParent() instanceof DataLanguageBlockWrapper) {
        return Indent.getNormalIndent();
      }

      return Indent.getNoneIndent();
    }

    /**
     * TODO implement alignment for "stacked" mustache content.  i.e.:
     * {{foo bar="baz"
     * bat="bam"}} <- note the alignment here
     */
    @Override
    public Alignment getAlignment() {
      return null;
    }

    @Override
    protected IElementType getTemplateTextElementType() {
      // we ignore CONTENT tokens since they get formatted by the templated language
      return HbTokenTypes.CONTENT;
    }

    @Override
    public boolean isRequiredRange(TextRange range) {
      // seems our approach doesn't require us to insert any custom DataLanguageBlockFragmentWrapper blocks
      return false;
    }

    /**
     * TODO if/when we implement alignment, update this method to do alignment properly
     * <p/>
     * This method handles indent and alignment on Enter.
     */
    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
      /**
       * We indent if we're in a BLOCK_WRAPPER (note that this works nicely since Enter can only be invoked
       * INSIDE a block (i.e. after the open block 'stache).
       *
       * Also indent if we are wrapped in a block created by the templated language
       */
      if (myNode.getElementType() == HbTokenTypes.BLOCK_WRAPPER
          || (getParent() instanceof DataLanguageBlockWrapper
              // hack alert: the following check opportunistically fixes com.dmarcotte.handlebars.format.HbFormatOnEnterTest#testSimpleBlockInDiv8
              //      and com.dmarcotte.handlebars.format.HbFormatOnEnterTest#testSimpleBlockInDiv8
              //      but isn't really based on solid logic (why do these checks work?), so when there's inevitably a
              //      format-on-enter bug, this is the first bit of code to be suspicious of
              &&
              (myNode.getElementType() != HbTokenTypes.STATEMENTS || myNode.getTreeNext() instanceof PsiErrorElement))) {
        return new ChildAttributes(Indent.getNormalIndent(), null);
      }
      else {
        return new ChildAttributes(Indent.getNoneIndent(), null);
      }
    }

    private boolean hasOnlyHbLanguageParents() {
      BlockWithParent parent = getParent();
      boolean hasOnlyHbLanguageParents = true;

      while (parent != null) {
        if (parent instanceof DataLanguageBlockWrapper) {
          hasOnlyHbLanguageParents = false;
          break;
        }
        parent = parent.getParent();
      }

      return hasOnlyHbLanguageParents;
    }

    /**
     * The template formatting system inserts a lot of block wrappers of type
     * "Synthetic Block".  To decide when to indent, we need to get our hands on
     * the "Real" parent.
     *
     * @return The first non-synthetic parent block
     */
    private BlockWithParent getRealBlockParent() {
      // if we can follow the chain of synthetic parent blocks, and if we end up
      // at a real DataLanguage block (i.e. the synthetic blocks didn't lead to an HbBlock),
      // we're a child of a templated language node and need an indent
      BlockWithParent parent = getParent();
      while (parent instanceof DataLanguageBlockWrapper
             && ((DataLanguageBlockWrapper)parent).getOriginal() instanceof SyntheticBlock) {
        parent = parent.getParent();
      }


      return parent;
    }
  }
}
