package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jun 10, 2009
 *         Time: 2:45:07 PM
 */
public class TapestryIdOrTypeAttributeDescriptor extends BasicTapestryAttributeDescriptor {
  private final String myName;
  private final XmlTag myContext;

  public TapestryIdOrTypeAttributeDescriptor(@NotNull String name, @NotNull XmlTag context) {
    myName = name;
    myContext = context;
  }

  public PsiElement getDeclaration() {
    return myContext;
  }

  public String getName() {
    return myName;
  }
}