package com.intellij.lang.actionscript.psi.stubs.impl;

import com.intellij.lang.actionscript.ActionScriptElementTypes;
import com.intellij.lang.actionscript.psi.impl.ActionScriptVariableImpl;
import com.intellij.lang.actionscript.psi.stubs.ActionScriptVariableStub;
import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.stubs.impl.JSVariableStubBaseImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSTypeParser;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.IOUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptVariableStubImpl
  extends JSVariableStubBaseImpl<JSVariable>
  implements ActionScriptVariableStub {

  private static final BooleanStructureElement HAS_QUALIFIED_NAME_FLAG = new BooleanStructureElement();
  private static final BooleanStructureElement IS_FROM_STUB_DUMPER = new BooleanStructureElement();
  private static final BooleanStructureElement HAS_TYPE_FLAG = new BooleanStructureElement();
  private static final BooleanStructureElement HAS_INITIALIZER_TEXT_FLAG = new BooleanStructureElement();
  private static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    JSVariableStubBaseImpl.FLAGS_STRUCTURE,
    HAS_QUALIFIED_NAME_FLAG,
    HAS_TYPE_FLAG,
    HAS_INITIALIZER_TEXT_FLAG,
    IS_FROM_STUB_DUMPER
  );

  private final @Nullable String myTypeFromStubDumper;
  private final @Nullable String myInitializerFromStubDumper;

  public ActionScriptVariableStubImpl(ActionScriptVariableImpl var, StubElement parent) {
    super(var, parent, ActionScriptElementTypes.ACTIONSCRIPT_VARIABLE,
          writeFlag(0, FLAGS_STRUCTURE, HAS_QUALIFIED_NAME_FLAG, var.hasQualifiedName()));
    myTypeFromStubDumper = null;
    myInitializerFromStubDumper = null;
  }

  public ActionScriptVariableStubImpl(StubInputStream dataStream, StubElement parentStub) throws IOException {
    super(dataStream, parentStub, ActionScriptElementTypes.ACTIONSCRIPT_VARIABLE);
    myTypeFromStubDumper = readFlag(HAS_TYPE_FLAG) ? IOUtil.readUTF(dataStream) : null;
    myInitializerFromStubDumper = readFlag(HAS_INITIALIZER_TEXT_FLAG) ? IOUtil.readUTF(dataStream) : null;
  }

  /**
   * Used only by AS3InterfaceStubDumper, don't use in new code
   */
  @ApiStatus.Obsolete
  public ActionScriptVariableStubImpl(final String name,
                                      boolean isConst,
                                      @Nullable String serializedType,
                                      @Nullable String initial,
                                      String qName,
                                      @NotNull JSContext jsContext,
                                      @NotNull JSAttributeList.AccessType accessType,
                                      final StubElement parentStub) {
    super(name, isConst, initial, qName, parentStub, jsContext, accessType, ActionScriptElementTypes.ACTIONSCRIPT_VARIABLE,
          writeFlag(0, FLAGS_STRUCTURE, IS_FROM_STUB_DUMPER, true) |
          writeFlag(0, FLAGS_STRUCTURE, HAS_QUALIFIED_NAME_FLAG, qName != null && qName.contains(".")) |
          writeFlag(0, FLAGS_STRUCTURE, HAS_TYPE_FLAG, serializedType != null) |
          writeFlag(0, FLAGS_STRUCTURE, HAS_INITIALIZER_TEXT_FLAG, initial != null));
    myTypeFromStubDumper = serializedType;
    myInitializerFromStubDumper = initial;
  }

  @Override
  public void serialize(StubOutputStream dataStream) throws IOException {
    super.serialize(dataStream);
    if (myTypeFromStubDumper != null) IOUtil.writeUTF(dataStream, myTypeFromStubDumper);
    if (myInitializerFromStubDumper != null) IOUtil.writeUTF(dataStream, myInitializerFromStubDumper);
  }

  @Override
  public ActionScriptVariableImpl createPsi() {
    return new ActionScriptVariableImpl(this);
  }

  @Override
  public boolean hasQualifiedName() {
    return readFlag(HAS_QUALIFIED_NAME_FLAG);
  }

  @Override
  protected @NotNull FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    String name = getName();
    if (name != null && doIndexName()) {
      sink.occurrence(JSIndexKeys.JS_NAME_INDEX_KEY, name);
    }
  }

  private boolean doIndexName() {
    return isUnderPackageOrUnderFile(getParentStub());
  }

  @Override
  protected boolean doIndexQualifiedName() {
    return doIndexName();
  }

  @Override
  public boolean isFromStubDumper() {
    return readFlag(IS_FROM_STUB_DUMPER);
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
}
