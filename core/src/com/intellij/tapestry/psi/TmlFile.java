package com.intellij.tapestry.psi;

import com.intellij.psi.FileViewProvider;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.xml.XmlFile;

/**
 * @author Alexey Chmutov
 *         Date: Jun 18, 2009
 *         Time: 9:46:22 PM
 */
public class TmlFile extends XmlFileImpl implements XmlFile {
  public TmlFile(FileViewProvider viewProvider) {
    super(viewProvider, TmlElementType.TML_FILE);
  }
}
