package com.intellij.lang.javascript.psi.stubs.impl;

import com.intellij.lang.actionscript.ActionScriptElementTypes;
import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.lang.javascript.psi.JSElementType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.impl.ActionScriptClassImpl;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSClassStub;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Maxim.Mossienko
 */
public class ActionScriptClassStubImpl extends JSClassStubBase<JSClass> implements JSClassStub<JSClass> {

  private static final BooleanStructureElement IS_INTERFACE_FLAG = new BooleanStructureElement();
  private static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    JSQualifiedObjectStubBase.FLAGS_STRUCTURE,
    IS_INTERFACE_FLAG
  );

  public ActionScriptClassStubImpl(JSClass clazz, final StubElement parent, @NotNull JSElementType<JSClass> elementType) {
    super(clazz, parent, elementType, writeFlag(0, FLAGS_STRUCTURE, IS_INTERFACE_FLAG, clazz.isInterface()));
  }

  /**
   * Used only by AS3InterfaceStubDumper, don't use in new code
   */
  @ApiStatus.Obsolete
  public ActionScriptClassStubImpl(String name, boolean isInterface, String qName, @NotNull JSAttributeList.AccessType accessType,
                                   final StubElement parentStub)  {
    super(ActionScriptResolveUtil.replaceInternalName(name), ActionScriptResolveUtil.replaceInternalName(qName), parentStub, JSContext.STATIC,
          accessType, ActionScriptElementTypes.ACTIONSCRIPT_CLASS, writeFlag(0, FLAGS_STRUCTURE, IS_INTERFACE_FLAG, isInterface)
    );
  }

  public ActionScriptClassStubImpl(final StubInputStream dataStream, final StubElement parentStub, @NotNull JSElementType elementType) throws IOException {
    super(dataStream, parentStub, elementType);
  }

  @Override
  public boolean isInterface() {
    return readFlag(IS_INTERFACE_FLAG);
  }

  @Override
  public JSClass createPsi() {
    return new ActionScriptClassImpl(this);
  }

  @Override
  protected @NotNull FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    String name = getName();
    if (name != null) {
      sink.occurrence(JSIndexKeys.JS_NAME_INDEX_KEY, name);
    }
  }

  @Override
  protected boolean doIndexQualifiedName() {
    return true;
  }
}
