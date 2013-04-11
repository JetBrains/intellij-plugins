package com.dmarcotte.handlebars.psi;

/**
 * Base element for mustaches which open blocks (i.e. "{{#foo}}" and "{{^foo}}")
 */
public interface HbOpenBlockMustache extends HbBlockMustache {

  @Override
  HbCloseBlockMustache getPairedElement();
}
