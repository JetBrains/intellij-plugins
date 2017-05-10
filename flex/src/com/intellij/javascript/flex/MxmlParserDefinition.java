package com.intellij.javascript.flex;

import com.intellij.lang.javascript.flex.MxmlLanguage;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.tree.IFileElementType;

public class MxmlParserDefinition extends XMLParserDefinition{
  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new MxmlFile(viewProvider);
  }

  public static class MxmlFile extends XmlFileImpl {
    private static final IFileElementType MXML_FILE = new IFileElementType("MXML_FILE", MxmlLanguage.INSTANCE);

    public MxmlFile(FileViewProvider viewProvider) {
      super(viewProvider, MXML_FILE);
    }
  }
}