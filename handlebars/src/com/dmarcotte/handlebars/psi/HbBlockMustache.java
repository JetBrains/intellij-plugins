package com.dmarcotte.handlebars.psi;

/**
 * Base type for all mustaches which define blocks (openBlock, openInverseBlock, closeBlock... others in the future?)
 */
public interface HbBlockMustache extends HbPsiElement {

  /**
   * Returns the {@link HbMustacheName} element for this block. i.e. the element wrapping "foo.bar" in
   * <pre>{{#foo.bar baz}}</pre>
   * and
   * <pre>{{/foo.bar}}</pre>
   *
   * @return the {@link HbMustacheName} for this block or null if none found (which should only happen if there are
   *         currently parse errors in the file)
   */
  public HbMustacheName getBlockMustacheName();

  /**
   * Get the block element paired with this one
   *
   * @return the matching {@link HbBlockMustache} element (i.e. for {{#foo}}, returns {{/foo}} and vice-versa or null
   *         if none found (which should only happen if there are currently parse errors in the file)
   */
  public HbBlockMustache getPairedElement();
}
