package com.intellij.tapestry.psi;

import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.tapestry.lang.TmlLanguage;

/**
 * @author Alexey Chmutov
 */
public interface TmlElementType extends XmlElementType {
  IFileElementType TML_FILE = new IFileElementType("TML_FILE", TmlLanguage.INSTANCE);
}
