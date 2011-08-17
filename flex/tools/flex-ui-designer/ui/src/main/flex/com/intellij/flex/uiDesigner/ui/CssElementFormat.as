package com.intellij.flex.uiDesigner.ui {
import flash.text.engine.BreakOpportunity;
import flash.text.engine.ElementFormat;
import flash.text.engine.FontDescription;
import flash.text.engine.FontWeight;

// todo grab from server
public final class CssElementFormat {
  public static const MONOSPACED_FONT_DESCRIPTION:FontDescription = new FontDescription("Menlo, Monaco, Consolas, Inconsolata, Bitstream Vera Sans Mono, Courier, monospace");

  private static const LUCIDA_FONT_BOLD_ESCRIPTION:FontDescription = new FontDescription("Lucida Grande, Segoe UI, Sans", FontWeight.BOLD);

  public static const fileLinkHover:ElementFormat = new ElementFormat(LUCIDA_FONT_BOLD_ESCRIPTION, 11, 0x0000ff);
  
  public static const linkHover:ElementFormat = new ElementFormat(MONOSPACED_FONT_DESCRIPTION, 12, 0x0000ff);
  linkHover.breakOpportunity = BreakOpportunity.ANY;

  public static const defaultText:ElementFormat = new ElementFormat(MONOSPACED_FONT_DESCRIPTION, 12);
  public static const identifier:ElementFormat = new ElementFormat(MONOSPACED_FONT_DESCRIPTION, 12, 0x000080);
  public static const func:ElementFormat = identifier;
  public static const string:ElementFormat = new ElementFormat(MONOSPACED_FONT_DESCRIPTION, 12, 0x008000);
  string.breakOpportunity = BreakOpportunity.ANY;

  public static const number:ElementFormat = new ElementFormat(MONOSPACED_FONT_DESCRIPTION, 12, 0x0000ff);
  public static const propertyName:ElementFormat = number;
}
}
