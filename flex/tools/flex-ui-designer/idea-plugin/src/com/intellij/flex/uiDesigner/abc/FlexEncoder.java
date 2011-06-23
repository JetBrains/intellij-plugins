package com.intellij.flex.uiDesigner.abc;

import gnu.trove.TIntObjectHashMap;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;
import static com.intellij.flex.uiDesigner.abc.Decoder.MethodCodeDecoding;

class FlexEncoder extends Encoder {
  private static final String MX_CORE = "mx.core";
  private static final String SPARK_COMPONENTS = "spark.components";
  private static final String SPARK_COMPONENTS_SUPPORT_CLASSES = "spark.components.supportClasses";
  private static final String APPLICATION = "Application";
  private static final String VIEW_NAVIGATOR_APPLICATION = "ViewNavigatorApplication";
  private static final String TABBED_VIEW_NAVIGATOR_APPLICATION = "TabbedViewNavigatorApplication";
  private static final String VIEW_NAVIGATOR_APPLICATION_BASE = "ViewNavigatorApplicationBase";

  private final static byte[] MODIFY_INIT_METHOD_BODY_MARKER = {};
  private final static byte[] EMPTY_METHOD_BODY = {0x01, 0x02, 0x04, 0x05, 0x03, (byte)0xd0, 0x30, 0x47, 0x00, 0x00};

  private boolean skipColorCorrection;
  private boolean skipInitialize;
  private boolean modifyConstructor;

  public void methodTrait(int trait_kind, int name, int dispId, int methodInfo, int[] metadata, DataBuffer in) {
    final int kind = trait_kind & 0x0f;
    if ((skipInitialize && kind == TRAIT_Method && ((trait_kind >> 4) & TRAIT_FLAG_Override) != 0) || (skipColorCorrection && kind == TRAIT_Setter)) {
      final int originalPosition = in.position();
      in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.MULTINAME)[name]);

      int constKind = in.readU8();
      assert constKind == CONSTANT_Qname || constKind == CONSTANT_QnameA;
      int ns = in.readU32();
      int localName = in.readU32();

      in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.NS)[ns]);
      if (in.readU8() == CONSTANT_PackageNamespace) {
        in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.STRING)[in.readU32()]);
        skipString(in);

        in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.STRING)[localName]);
        int stringLength = in.readU32();
        if (skipInitialize && compare(in, stringLength, "initialize")) {
          in.seek(originalPosition);
          return;
        }
        else if (skipColorCorrection && compare(in, stringLength, "colorCorrection")) {
          history.getModifiedMethodBodies(poolIndex).put(methodInfo, EMPTY_METHOD_BODY);
        }
      }

      in.seek(originalPosition);
    }

    super.methodTrait(trait_kind, name, dispId, methodInfo, metadata, in);
  }

  @Override
  protected void instanceStarting(int name, DataBuffer in) {
    in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.MULTINAME)[name]);

    int constKind = in.readU8();
    assert constKind == CONSTANT_Qname || constKind == CONSTANT_QnameA;
    int ns = in.readU32();
    int localName = in.readU32();

    in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.NS)[ns]);
    if (in.readU8() != CONSTANT_PackageNamespace) {
      return;
    }

    in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.STRING)[in.readU32()]);
    int packageLength = in.readU32();
    boolean mxCore = false;
    if (compare(in, packageLength, SPARK_COMPONENTS) || (mxCore = compare(in, packageLength, MX_CORE))) {
      in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.STRING)[localName]);
      int stringLength = in.readU32();
      skipInitialize = compare(in, stringLength, APPLICATION);
      if (mxCore) {
        modifyConstructor = skipInitialize;
      }
      else {
        skipColorCorrection = skipInitialize;
        modifyConstructor = skipInitialize ||
                            compare(in, stringLength, VIEW_NAVIGATOR_APPLICATION) ||
                            compare(in, stringLength, TABBED_VIEW_NAVIGATOR_APPLICATION);
      }
    }
    else if (compare(in, packageLength, SPARK_COMPONENTS_SUPPORT_CLASSES)) {
      in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.STRING)[localName]);
      skipInitialize = compare(in, VIEW_NAVIGATOR_APPLICATION_BASE);
    }
  }

  private static boolean compare(final DataBuffer in, final String s) {
    return compare(in, in.readU32(), s);
  }

  @Override
  protected void instanceEnding(int oldIInit) {
    if (modifyConstructor) {
      history.getModifiedMethodBodies(poolIndex).put(oldIInit, MODIFY_INIT_METHOD_BODY_MARKER);
    }
  }

  private static boolean compare(final DataBuffer in, final int stringLength, final String s) {
    if (stringLength != s.length()) {
      return false;
    }

    final int offset = in.position + in.offset;
    for (int j = stringLength - 1; j > -1; j--) {
      if ((char)in.data[offset + j] != s.charAt(j)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int startMethodBody(int methodInfo, int maxStack, int maxRegs, int scopeDepth, int maxScope) {
    TIntObjectHashMap<byte[]> modifiedMethodBodies = history.getModifiedMethodBodies(poolIndex);
    byte[] bytes = modifiedMethodBodies == null ? null : modifiedMethodBodies.get(methodInfo);
    if (bytes == null) {
      return super.startMethodBody(methodInfo, maxStack, maxRegs, scopeDepth, maxScope);
    }
    else {
      if (bytes == EMPTY_METHOD_BODY) {
        methodBodies.writeU32(this.methodInfo.getIndex(poolIndex, methodInfo));
        methodBodies.write(bytes);
        return MethodCodeDecoding.STOP;
      }
      else {
        super.startMethodBody(methodInfo, maxStack, maxRegs, scopeDepth, maxScope);
        // we cannot change iinit to empty body, because iinit may contains some default value for complex instance properties,
        // so, we allow all code before constructsuper opcode.
        return MethodCodeDecoding.STOP_AFTER_CONSTRUCT_SUPER;
      }
    }
  }
  
  @Override
  public void traitCount(int traitCount) {
    currentBuffer.writeU32(skipInitialize ? (traitCount - 1) : traitCount);
  }

  @Override
  public void endInstance() {
    super.endInstance();
    skipInitialize = false;
    modifyConstructor = false;
    skipColorCorrection = false;
  }

  @SuppressWarnings("UnusedDeclaration")
  private char[] readChars(DataBuffer in) {
    final int stringLength = in.readU32();
    char[] chars = new char[stringLength];
    final int offset = in.position + in.offset;
    for (int i = 0; i < stringLength; i++) {
      chars[i] = (char)in.data[offset + i];
    }

    return chars;
  }

  private void skipString(DataBuffer in) {
    int stringLength = in.readU32();
    in.seek(in.position() + stringLength);
  }

  @SuppressWarnings("UnusedDeclaration")
  private String dd(DataBuffer in) {
    int stringLength = in.readU32();
    char[] s = new char[stringLength];
    for (int j = 0; j < stringLength; j++) {
      s[j] = (char)in.data[in.position + in.offset + j];
    }
    return new String(s);
  }
}
