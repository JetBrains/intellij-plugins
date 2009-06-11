package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.psi.PsiElement;

/**
 * @author Alexey Chmutov
 *         Date: Jun 10, 2009
 *         Time: 2:45:07 PM
 */
public class TapestryAttributeDescriptor extends BasicXmlAttributeDescriptor {
  public PsiElement getDeclaration() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void init(PsiElement element) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public Object[] getDependences() {
    return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean isRequired() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean isFixed() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean hasIdType() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean hasIdRefType() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getDefaultValue() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public boolean isEnumerated() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String[] getEnumeratedValues() {
    return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
  }
}
