package com.intellij.tapestry.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.tapestry.lang.TmlLanguage;

/**
 * @author Alexey Chmutov
 *         Date: Jun 18, 2009
 *         Time: 9:23:58 PM
 */
public interface TmlElementType extends XmlElementType {
  IFileElementType TML_FILE = new IFileElementType("TML_FILE", TmlLanguage.INSTANCE);
  IElementType TML_PROLOG = new IElementType("TML_PROLOG", TmlLanguage.INSTANCE);
}
