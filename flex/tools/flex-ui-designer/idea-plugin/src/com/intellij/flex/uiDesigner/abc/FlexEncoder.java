package com.intellij.flex.uiDesigner.abc;

import gnu.trove.TIntObjectHashMap;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

class FlexEncoder extends Encoder {
  private static final String SPARK_COMPONENTS = "spark.components";
  private static final String APPLICATION = "Application";
  private static final String VIEW_NAVIGATOR_APPLICATION = "ViewNavigatorApplication";
  private static final String TABBED_VIEW_NAVIGATOR_APPLICATION = "TabbedViewNavigatorApplication";
  private static final String VIEW_NAVIGATOR_APPLICATION_BASE = "ViewNavigatorApplicationBase";

  private final static byte[] EMPTY_I_INIT_METHOD_BODY = {0x01, 0x01, 0x04, 0x05, 0x06, (byte)0xd0, 0x30, (byte)0xd0, 0x049, 0x00, 0x47, 0x00, 0x00};
  private final static byte[] EMPTY_METHOD_BODY = {0x01, 0x02, 0x04, 0x05, 0x03, (byte)0xd0, 0x30, 0x47, 0x00, 0x00};

  private boolean skipColorCorrection;
  private boolean skipInitialize;

  public void methodTrait(int trait_kind, int name, int dispId, int methodInfo, int[] metadata, DataBuffer in) {
    final int kind = trait_kind & 0x0f;
    if (skipInitialize && ((kind == TRAIT_Method && ((trait_kind >> 4) & TRAIT_FLAG_Override) != 0) || kind == TRAIT_Setter)) {
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
        if (compare(in, stringLength, "initialize")) {
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
    if (!compare(in, SPARK_COMPONENTS)) {
      return;
    }

    in.seek(history.getRawPartPoolPositions(poolIndex, IndexHistory.STRING)[localName]);
    int stringLength = in.readU32();
    skipColorCorrection = compare(in, stringLength, APPLICATION);
    skipInitialize = skipColorCorrection ||
                     compare(in, stringLength, VIEW_NAVIGATOR_APPLICATION) ||
                     compare(in, stringLength, TABBED_VIEW_NAVIGATOR_APPLICATION) ||
                     compare(in, stringLength, VIEW_NAVIGATOR_APPLICATION_BASE);
  }

  private static boolean compare(final DataBuffer in, final String s) {
    return compare(in, in.readU32(), s);
  }

  @Override
  protected void instanceEnding(int oldIInit) {
    if (skipInitialize) {
      history.getModifiedMethodBodies(poolIndex).put(oldIInit, EMPTY_I_INIT_METHOD_BODY);
    }
  }

  private static boolean compare(final DataBuffer in, final int stringLength, final String s) {
    if (stringLength != s.length()) {
      return false;
    }

    final int offset = in.position + in.offset;
    for (int j = stringLength - 1; j > -1; j--) {
      if ((char)in.data[offset + j] != s.charAt(j)) {
        //System.out.print('"' + in.readString(stringLength) + "\"\n");
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean startMethodBody(int methodInfo, int maxStack, int maxRegs, int scopeDepth, int maxScope) {
    TIntObjectHashMap<byte[]> modifiedMethodBodies = history.getModifiedMethodBodies(poolIndex);
    byte[] bytes = modifiedMethodBodies == null ? null : modifiedMethodBodies.get(methodInfo);
    if (bytes == null) {
      return super.startMethodBody(methodInfo, maxStack, maxRegs, scopeDepth, maxScope);
    }
    else {
       methodBodies.writeU32(this.methodInfo.getIndex(poolIndex, methodInfo));
      return
    }
  }

  protected boolean methodBodyStarting(int methodInfo) {
    TIntObjectHashMap<byte[]> modifiedMethodBodies = history.getModifiedMethodBodies(poolIndex);
    byte[] bytes = modifiedMethodBodies == null ? null : modifiedMethodBodies.get(methodInfo);
    if (bytes != null) {
      methodBodies.write(bytes);
      return false;
    }

    return true;
  }

  @Override
  public void traitCount(int traitCount) {
    currentBuffer.writeU32(skipInitialize ? (traitCount - 1) : traitCount);
  }

  @Override
  public void endInstance() {
    super.endInstance();
    skipInitialize = false;
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
