package com.dmarcotte.handlebars.psi;

/**
 * Base type for all mustaches which define blocks (openBlock, openInverseBlock, closeBlock... others in the future?)
 */
public interface HbBlockMustache extends HbPsiElement {

  /**
   * Returns the {@link HbPath} which is the "main" path in this block. i.e. the "foo.bar" in
   * <pre>{{#foo.bar baz}}</pre>
   * and
   * <pre>{{/foo.bar}}</pre>
   * <p/>
   * <p/>
   * This path is important because it is used to pair open and close block mustaches
   *
   * @return the main {@link HbPath} for this block or null if none found (which should only happen if there are
   *         currently parse errors in the file)
   */
  public HbPath getBlockMainPath();

  /**
   * Get the block element paired with this one
   *
   * @return the matching {@link HbBlockMustache} element (i.e. for {{#foo}}, returns {{/foo}} and vice-versa or null
   *         if none found (which should only happen if there are currently parse errors in the file)
   */
  public HbBlockMustache getPairedElement();
}
