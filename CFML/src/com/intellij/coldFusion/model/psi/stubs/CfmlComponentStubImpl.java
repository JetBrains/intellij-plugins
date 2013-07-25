/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
