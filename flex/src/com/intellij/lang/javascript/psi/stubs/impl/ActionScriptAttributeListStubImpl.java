package com.intellij.lang.javascript.psi.stubs.impl;


import com.intellij.lang.actionscript.ActionScriptElementTypes;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptAttributeListImpl;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class ActionScriptAttributeListStubImpl extends JSAttributeListStubImpl {

  public ActionScriptAttributeListStubImpl(JSAttributeList clazz,
                                           StubElement parent,
                                           @NotNull IElementType elementType) {
    super(clazz, parent, elementType);
  }

  public ActionScriptAttributeListStubImpl(StubInputStream dataStream,
                                           StubElement parentStub,
                                           @NotNull IElementType elementType) throws IOException {
    super(dataStream, parentStub, elementType);
  }

  /**
   * Used only by AS3InterfaceStubDumper, don't use in new code
   */
  @ApiStatus.Obsolete
  public ActionScriptAttributeListStubImpl(StubElement parentStub,
                                           String namespace,
                                           String resolvedNamespace,
                                           JSAttributeList.AccessType accessType,
                                           JSAttributeList.ModifierType... modifiers) {
    super(parentStub, namespace, resolvedNamespace, accessType, ActionScriptElementTypes.ACTIONSCRIPT_ATTRIBUTE_LIST, modifiers);
  }

  @Override
  public JSAttributeList createPsi() {
    return new ActionScriptAttributeListImpl(this, ActionScriptElementTypes.ACTIONSCRIPT_ATTRIBUTE_LIST);
  }
}
