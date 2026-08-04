package com.intellij.lang.actionscript.psi.stubs.impl;

import com.intellij.lang.actionscript.ActionScriptElementTypes;
import com.intellij.lang.actionscript.psi.impl.ActionScriptParameterImpl;
import com.intellij.lang.actionscript.psi.stubs.ActionScriptParameterStub;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.stubs.impl.JSParameterStubImpl;
import com.intellij.lang.javascript.psi.types.JSTypeParser;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.IOUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class ActionScriptParameterStubImpl extends JSParameterStubImpl implements ActionScriptParameterStub {

  private static final BooleanStructureElement IS_FROM_STUB_DUMPER = new BooleanStructureElement();
  private static final BooleanStructureElement IS_OPTIONAL_FLAG = new BooleanStructureElement();
  private static final BooleanStructureElement IS_TYPE_REST_FLAG = new BooleanStructureElement();
  private static final BooleanStructureElement HAS_TYPE_FLAG = new BooleanStructureElement();
  private static final BooleanStructureElement HAS_INITIALIZER_TEXT_FLAG = new BooleanStructureElement();
  private static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    JSParameterStubImpl.FLAGS_STRUCTURE,
    IS_FROM_STUB_DUMPER,
    IS_OPTIONAL_FLAG,
    IS_TYPE_REST_FLAG,
    HAS_TYPE_FLAG,
    HAS_INITIALIZER_TEXT_FLAG
  );

  private final @Nullable String myTypeFromStubDumper;
  private final @Nullable String myInitializerFromStubDumper;

  public ActionScriptParameterStubImpl(ActionScriptParameterImpl clazz, StubElement parent) {
    super(clazz, parent, ActionScriptElementTypes.ACTIONSCRIPT_PARAMETER, 0);
    myTypeFromStubDumper = null;
    myInitializerFromStubDumper = null;
  }

  public ActionScriptParameterStubImpl(StubInputStream dataStream, StubElement parentStub) throws IOException {
    super(dataStream, parentStub, ActionScriptElementTypes.ACTIONSCRIPT_PARAMETER);
    myTypeFromStubDumper = readFlag(HAS_TYPE_FLAG) ? IOUtil.readUTF(dataStream) : null;
    myInitializerFromStubDumper = readFlag(HAS_INITIALIZER_TEXT_FLAG) ? IOUtil.readUTF(dataStream) : null;
  }

  /**
   * Used only by AS3InterfaceStubDumper, don't use in new code
   */
  @ApiStatus.Obsolete
  public ActionScriptParameterStubImpl(String name,
                                       boolean isRest,
                                       @Nullable String type,
                                       @Nullable String initial,
                                       StubElement parentStub) {
    super(name, isRest, initial, parentStub,
          writeFlag(0, FLAGS_STRUCTURE, IS_FROM_STUB_DUMPER, true) |
          writeFlag(0, FLAGS_STRUCTURE, IS_OPTIONAL_FLAG, initial != null) |
          writeFlag(0, FLAGS_STRUCTURE, IS_TYPE_REST_FLAG, isRest) |
          writeFlag(0, FLAGS_STRUCTURE, HAS_TYPE_FLAG, type != null) |
          writeFlag(0, FLAGS_STRUCTURE, HAS_INITIALIZER_TEXT_FLAG, initial != null));
    myTypeFromStubDumper = type;
    myInitializerFromStubDumper = initial;
  }

  @Override
  public void serialize(StubOutputStream dataStream) throws IOException {
    super.serialize(dataStream);
    if (myTypeFromStubDumper != null) IOUtil.writeUTF(dataStream, myTypeFromStubDumper);
    if (myInitializerFromStubDumper != null) IOUtil.writeUTF(dataStream, myInitializerFromStubDumper);
  }

  @Override
  public JSParameter createPsi() {
    return new ActionScriptParameterImpl(this);
  }

  @Override
  public boolean isFromStubDumper() {
    return readFlag(IS_FROM_STUB_DUMPER);
  }

  @Override
  public boolean isOptionalFromStubDumper() {
    return readFlag(IS_OPTIONAL_FLAG);
  }

  @Override
  public boolean isTypeRestFromStubDumper() {
    return readFlag(IS_TYPE_REST_FLAG);
  }

  @Override
  public @Nullable JSType getTypeFromStubDumper(@NotNull PsiElement sourceElement) {
    JSTypeSource source = JSTypeSourceFactory.createTypeSource(sourceElement, false);
    return JSTypeParser.parseSerializedType(getProject(), myTypeFromStubDumper, source);
  }

  @Override
  public @Nullable String getInitializerFromStubDumper() {
    return myInitializerFromStubDumper;
  }

  @Override
  protected @NotNull FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }
}
