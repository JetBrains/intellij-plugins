package com.intellij.lang.actionscript.psi.stubs.impl;

import com.intellij.lang.actionscript.ActionScriptElementTypes;
import com.intellij.lang.actionscript.psi.impl.ActionScriptFunctionImpl;
import com.intellij.lang.actionscript.psi.stubs.ActionScriptFunctionStub;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.stubs.impl.JSFunctionStubBaseImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSTypeParser;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.io.IOUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptFunctionStubImpl
  extends JSFunctionStubBaseImpl<ActionScriptFunctionImpl>
  implements ActionScriptFunctionStub {

  private static final BooleanStructureElement HAS_QUALIFIED_NAME_FLAG = new BooleanStructureElement();
  private static final BooleanStructureElement IS_FROM_STUB_DUMPER = new BooleanStructureElement();
  private static final BooleanStructureElement HAS_RETURN_TYPE_FLAG = new BooleanStructureElement();
  private static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    JSFunctionStubBaseImpl.FLAGS_STRUCTURE,
    HAS_QUALIFIED_NAME_FLAG,
    IS_FROM_STUB_DUMPER,
    HAS_RETURN_TYPE_FLAG
  );

  private final @Nullable String myStubDumperReturnType;

  public ActionScriptFunctionStubImpl(ActionScriptFunctionImpl function, final StubElement parent) {
    super(function, parent, ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION,
          writeFlag(0, FLAGS_STRUCTURE, HAS_QUALIFIED_NAME_FLAG, function.hasQualifiedName()));
    myStubDumperReturnType = null;
  }

  public ActionScriptFunctionStubImpl(final StubInputStream dataStream, final StubElement parentStub) throws IOException {
    super(dataStream, parentStub, ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION);
    myStubDumperReturnType = readFlag(HAS_RETURN_TYPE_FLAG) ? IOUtil.readUTF(dataStream) : null;
  }

  /**
   * Used only by AS3InterfaceStubDumper, don't use in new code
   */
  @ApiStatus.Obsolete
  public ActionScriptFunctionStubImpl(final String name, @NotNull JSFunction.FunctionKind kind, String qName,
                                      @Nullable String serializedReturnType,
                                      @NotNull JSContext jsContext, @NotNull JSAttributeList.AccessType accessType,
                                      final StubElement parentStub) {
    super(name, kind, qName, parentStub, jsContext, accessType, ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION,
          writeFlag(0, FLAGS_STRUCTURE, IS_FROM_STUB_DUMPER, true) |
          writeFlag(0, FLAGS_STRUCTURE, HAS_QUALIFIED_NAME_FLAG, qName != null && qName.contains(".")) |
          writeFlag(0, FLAGS_STRUCTURE, HAS_RETURN_TYPE_FLAG, serializedReturnType != null));
    myStubDumperReturnType = serializedReturnType;
  }

  @Override
  public void serialize(StubOutputStream dataStream) throws IOException {
    super.serialize(dataStream);
    if (myStubDumperReturnType != null) IOUtil.writeUTF(dataStream, myStubDumperReturnType);
  }

  @Override
  public ActionScriptFunctionImpl createPsi() {
    return new ActionScriptFunctionImpl(this);
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
    IElementType type = getElementType();
    if (!JSElementTypes.FUNCTION_DECLARATIONS.contains(type)) return false;
    return isUnderPackageOrUnderFile(this);
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
  public @Nullable JSType getStubDumperReturnType(@NotNull PsiElement sourceElement) {
    JSTypeSource source = JSTypeSourceFactory.createTypeSource(sourceElement, false);
    return JSTypeParser.parseSerializedType(getProject(), myStubDumperReturnType, source);
  }
}