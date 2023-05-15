// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.format;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.parsing.HbTokenTypes;
import com.dmarcotte.handlebars.psi.HbPsiUtil;
import com.intellij.formatting.*;
import com.intellij.formatting.templateLanguages.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.formatter.xml.HtmlPolicy;
import com.intellij.psi.formatter.xml.SyntheticBlock;
import com.intellij.psi.templateLanguages.SimpleTemplateLanguageFormattingModelBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.psi.formatter.WrappingUtil.getWrapType;

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
    final FormattingDocumentModelImpl documentModel = FormattingDocumentModelImpl.createOn(node.getPsi().getContainingFile());
    HtmlPolicy policy = new HtmlPolicy(codeStyleSettings, documentModel);
    return HbTokenTypes.TAGS.contains(node.getElementType()) ?
           new HandlebarsTagBlock(node, wrap, alignment, this, codeStyleSettings, foreignChildren, policy) :
           new HandlebarsBlock(node, wrap, alignment, this, codeStyleSettings, foreignChildren, policy);
  }

  /**
   * We have to override {@link TemplateLanguageFormattingModelBuilder#createModel}
   * since after we delegate to some templated languages, those languages (xml/html for sure, potentially others)
   * delegate right back to us to format the HbTokenTypes.OUTER_ELEMENT_TYPE token we tell them to ignore,
   * causing a stack-overflowing loop of polite format-delegation.
   */
  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    if (!HbConfig.isFormattingEnabled()) {
      // formatting is disabled, return the no-op formatter (note that this still delegates formatting
      // to the templated language, which lets the users manage that separately)
      return new SimpleTemplateLanguageFormattingModelBuilder().createModel(formattingContext);
    }

    final PsiFile file = formattingContext.getContainingFile();
    Block rootBlock;

    ASTNode node = formattingContext.getNode();

    if (node.getElementType() == HbTokenTypes.OUTER_ELEMENT_TYPE) {
      // If we're looking at a HbTokenTypes.OUTER_ELEMENT_TYPE element, then we've been invoked by our templated
      // language.  Make a dummy block to allow that formatter to continue
      return new SimpleTemplateLanguageFormattingModelBuilder().createModel(formattingContext);
    }
    else {
      rootBlock = getRootBlock(file, file.getViewProvider(), formattingContext.getCodeStyleSettings());
    }
    return new DocumentBasedFormattingModel(
      rootBlock, formattingContext.getProject(), formattingContext.getCodeStyleSettings(), file.getFileType(), file);
  }

  /**
   * Do format my model!
   *
   * @return false all the time to tell the {@link TemplateLanguageFormattingModelBuilder}
   * to not-not format our model (i.e. yes please!  Format away!)
   */
  @Override
  public boolean dontFormatMyModel() {
    return false;
  }

  private static class HandlebarsTagBlock extends HandlebarsBlock {
    @NotNull
    private final Alignment myChildAttributeAlignment;


    HandlebarsTagBlock(@NotNull ASTNode node,
                       Wrap wrap,
                       Alignment alignment,
                       @NotNull TemplateLanguageBlockFactory blockFactory,
                       @NotNull CodeStyleSettings settings,
                       @Nullable List<DataLanguageBlockWrapper> foreignChildren,
                       HtmlPolicy htmlPolicy) {
      super(node, wrap, alignment, blockFactory, settings, foreignChildren, htmlPolicy);

      myChildAttributeAlignment = Alignment.createAlignment();
    }

    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
      if (newChildIndex > 0) {
        List<Block> blocks = getSubBlocks();
        if (blocks.size() > newChildIndex - 1) {
          Block prevBlock = blocks.get(newChildIndex - 1);
          if (prevBlock instanceof AbstractBlock) {
            ASTNode node = ((AbstractBlock)prevBlock).getNode();
            if (isAttribute(node) ||
                node.getElementType() == HbTokenTypes.MUSTACHE_NAME) {
              return new ChildAttributes(null, prevBlock.getAlignment());
            }
          }
        }
      }

      return super.getChildAttributes(newChildIndex);
    }

    @Override
    protected Alignment createChildAlignment(ASTNode child) {
      if (isAttribute(child)) {
        return myChildAttributeAlignment;
      }
      return super.createChildAlignment(child);
    }

    @Override
    protected Wrap createChildWrap(ASTNode child) {
      if (isAttribute(child)) {
        return Wrap.createWrap(getWrapType(myHtmlPolicy.getAttributesWrap()), false);
      }
      return null;
    }
  }

  private static boolean isAttribute(ASTNode child) {
    IElementType type = child.getElementType();
    return type == HbTokenTypes.PARAM || type == HbTokenTypes.HASH;
  }

  private static class HandlebarsBlock extends TemplateLanguageBlock {

    @NotNull
    protected final HtmlPolicy myHtmlPolicy;


    HandlebarsBlock(@NotNull ASTNode node,
                    Wrap wrap,
                    Alignment alignment,
                    @NotNull TemplateLanguageBlockFactory blockFactory,
                    @NotNull CodeStyleSettings settings,
                    @Nullable List<DataLanguageBlockWrapper> foreignChildren,
                    @NotNull HtmlPolicy htmlPolicy) {
      super(node, wrap, alignment, blockFactory, settings, foreignChildren);
      myHtmlPolicy = htmlPolicy;
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
     * [tl_indent]{{#foo}}
     *            [hb_indent]BEGIN_STATEMENTS
     *            [tl_indent][hb_indent]TEMPLATE_STUFF
     *            [hb_indent]END_STATEMENTS
     * [tl_indent]{{/foo}}
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

      if (isAttribute(myNode)) {
        return null;
      }

      if (HbPsiUtil.isNonRootStatementsElement(myNode.getPsi())) {
        // we're computing the indent for a non-root STATEMENTS:
        //      if it's not contained in a foreign block, indent!
        DataLanguageBlockWrapper foreignBlockParent = getForeignBlockParent(false);
        if (foreignBlockParent == null) {
          return Indent.getNormalIndent();
        }

        // otherwise, only indent if our foreign parent isn't indenting us
        if (foreignBlockParent.getNode() instanceof XmlTag xmlTag) {
          if (!myHtmlPolicy.indentChildrenOf(xmlTag)) {
            // no indent from xml parent, add our own
            return Indent.getNormalIndent();
          }
        }

        return Indent.getNoneIndent();
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
      // (unless that foreign element has been configured to not indent its children)
      DataLanguageBlockWrapper foreignParent = getForeignBlockParent(true);
      if (foreignParent != null) {
        if (foreignParent.getNode() instanceof XmlTag
            && !myHtmlPolicy.indentChildrenOf((XmlTag)foreignParent.getNode())) {
          return Indent.getNoneIndent();
        }
        return Indent.getNormalIndent();
      }

      return Indent.getNoneIndent();
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
     * <p/>
     * This method handles indent and alignment on Enter.
     */
    @NotNull
    @Override
    public ChildAttributes getChildAttributes(int newChildIndex) {
      /*
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
              (myNode.getElementType() != HbTokenTypes.STATEMENTS
               || newChildIndex != 0
               || myNode.getTreeNext() instanceof PsiErrorElement))) {
        return new ChildAttributes(Indent.getNormalIndent(), null);
      }

      return new ChildAttributes(Indent.getNoneIndent(), null);
    }


    /**
     * Returns this block's first "real" foreign block parent if it exists, and null otherwise.  (By "real" here, we mean that this method
     * skips SyntheticBlock blocks inserted by the template formatter)
     *
     * @param immediate Pass true to only check for an immediate foreign parent, false to look up the hierarchy.
     */
    private DataLanguageBlockWrapper getForeignBlockParent(boolean immediate) {
      DataLanguageBlockWrapper foreignBlockParent = null;
      BlockWithParent parent = getParent();

      while (parent != null) {
        if (parent instanceof DataLanguageBlockWrapper && !(((DataLanguageBlockWrapper)parent).getOriginal() instanceof SyntheticBlock)) {
          foreignBlockParent = (DataLanguageBlockWrapper)parent;
          break;
        }
        else if (immediate && parent instanceof HandlebarsBlock) {
          break;
        }
        parent = parent.getParent();
      }

      return foreignBlockParent;
    }
  }
}
