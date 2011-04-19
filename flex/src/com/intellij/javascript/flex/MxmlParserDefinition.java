package com.intellij.javascript.flex;

import com.intellij.lang.javascript.flex.MxmlFileType;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;

/**
 * Created by IntelliJ IDEA.
 * User: maxim.mossienko
 * Date: 19.04.11
 * Time: 12:06
 */
public class MxmlParserDefinition extends XMLParserDefinition{
  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new MxmlFile(viewProvider);
  }

  public static class MxmlFile extends XmlFileImpl {
    private static IFileElementType MXML_FILE = new IFileElementType("MXML_FILE", MxmlFileType.LANGUAGE);

    public MxmlFile(FileViewProvider viewProvider) {
      super(viewProvider, MXML_FILE);
    }
  }
}
