package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;

/**
 * @author vnikolaenko
 */
public class CfmlComponentStubImpl extends NamedStubBase<CfmlComponent> implements CfmlComponentStub {
  private boolean myIsInterface;
  private String mySuperClass;
  private String[] myInterfaces;

  protected CfmlComponentStubImpl(final StubElement parent, final IStubElementType elementType, final String name,
                                  boolean isInterface, String superclass, String[] interfaces) {
    super(parent, elementType, name);
    myIsInterface = isInterface;
    mySuperClass = superclass;
    myInterfaces = interfaces;
  }

  public String getSuperclass() {
    return mySuperClass;
  }

  public String[] getInterfaces() {
    return myInterfaces;
  }

  public boolean isInterface() {
    return myIsInterface;
  }
}
