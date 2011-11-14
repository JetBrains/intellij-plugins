package com.intellij.flex.uiDesigner.abc;

import gnu.trove.TIntObjectHashMap;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;
import static com.intellij.flex.uiDesigner.abc.Decoder.MethodCodeDecoding;

public class FlexEncoder extends Encoder {
  private static final String MX_CORE = "mx.core";
  private static final String SPARK_COMPONENTS = "spark.components";
  private static final String SPARK_COMPONENTS_SUPPORT_CLASSES = "spark.components.supportClasses";
  private static final String APPLICATION = "Application";
  private static final String UI_COMPONENT = "UIComponent";
  private static final String VIEW_NAVIGATOR_APPLICATION = "ViewNavigatorApplication";
  private static final String TABBED_VIEW_NAVIGATOR_APPLICATION = "TabbedViewNavigatorApplication";
  private static final String VIEW_NAVIGATOR_APPLICATION_BASE = "ViewNavigatorApplicationBase";

  private final static byte[] MODIFY_INIT_METHOD_BODY_MARKER = {};
  private final static byte[] EMPTY_METHOD_BODY = {0x01, 0x02, 0x04, 0x05, 0x03, (byte)0xd0, 0x30, 0x47, 0x00, 0x00};

  private boolean skipColorCorrection;
  private boolean skipInitialize;
  // IDEA-72935
  private boolean skipPanelAddChild;
  private boolean modifyConstructor;

  private String modifyAccessModifier;

  private final byte[] debugBasepath;
  private final boolean isFlex45;

  public FlexEncoder(String inputFilename, String flexSdkVersion) {
    isFlex45 = flexSdkVersion.startsWith("4.5");
    debugBasepath = new byte[inputFilename.length() + 1];
    debugBasepath[0] = '$';
    //noinspection deprecation
    inputFilename.getBytes(0, inputFilename.length(), debugBasepath, 1);
  }

  public void methodTrait(int trait_kind, int name, int dispId, int methodInfo, int[] metadata, DataBuffer in) {
    final int kind = trait_kind & 0x0f;
    if (((skipInitialize || skipPanelAddChild) && kind == TRAIT_Method && ((trait_kind >> 4) & TRAIT_FLAG_Override) != 0) ||
        (skipColorCorrection && kind == TRAIT_Setter)) {
      final int originalPosition = in.position();
      in.seek(history.getRawPartPoolPositions(IndexHistory.MULTINAME)[name]);

      int constKind = in.readU8();
      assert constKind == CONSTANT_Qname || constKind == CONSTANT_QnameA;
      int ns = in.readU32();
      int localName = in.readU32();

      in.seek(history.getRawPartPoolPositions(IndexHistory.NS)[ns]);
      int nsKind = in.readU8();
      if (nsKind == CONSTANT_PackageNamespace) {
        in.seek(history.getRawPartPoolPositions(IndexHistory.STRING)[localName]);
        int stringLength = in.readU32();
        if (skipInitialize && compare(in, stringLength, "initialize")) {
          in.seek(originalPosition);
          return;
        }
        else if (skipColorCorrection && compare(in, stringLength, "colorCorrection")) {
          history.getModifiedMethodBodies(decoderIndex).put(methodInfo, EMPTY_METHOD_BODY);
        }
        else if (skipPanelAddChild && compare(in, stringLength, "addChildAt")) {
          in.seek(originalPosition);
          skipPanelAddChild = false;
          return;
        }
      }

      in.seek(originalPosition);
    }

    super.methodTrait(trait_kind, name, dispId, methodInfo, metadata, in);
  }

  @Override
  protected void instanceStarting(final int name, final DataBuffer in) {
    in.seek(history.getRawPartPoolPositions(IndexHistory.MULTINAME)[name]);

    final int constKind = in.readU8();
    assert constKind == CONSTANT_Qname || constKind == CONSTANT_QnameA;
    final int ns = in.readU32();
    final int localName = in.readU32();

    in.seek(history.getRawPartPoolPositions(IndexHistory.NS)[ns]);
    int nsKind = in.readU8();
    if (nsKind != CONSTANT_PackageNamespace) {
      return;
    }

    in.seek(history.getRawPartPoolPositions(IndexHistory.STRING)[in.readU32()]);
    final int packageLength = in.readU32();
    boolean mxCore = false;
    if (compare(in, packageLength, SPARK_COMPONENTS) || (mxCore = compare(in, packageLength, MX_CORE))) {
      in.seek(history.getRawPartPoolPositions(IndexHistory.STRING)[localName]);
      int stringLength = in.readU32();
      skipInitialize = compare(in, stringLength, APPLICATION);
      if (mxCore) {
        if (skipInitialize) {
          modifyConstructor = skipInitialize;
        }
        else if (compare(in, stringLength, UI_COMPONENT)) {
          modifyAccessModifier = "deferredSetStyles";
        }
      }
      else {
        skipColorCorrection = skipInitialize;
        modifyConstructor = skipInitialize ||
                            compare(in, stringLength, VIEW_NAVIGATOR_APPLICATION) ||
                            compare(in, stringLength, TABBED_VIEW_NAVIGATOR_APPLICATION);
        // AS-66
        if (!skipInitialize && isFlex45) {
          skipInitialize = compare(in, stringLength, "View");
        }
      }
    }
    else if (isFlex45) {
      if (compare(in, packageLength, SPARK_COMPONENTS_SUPPORT_CLASSES)) {
        in.seek(history.getRawPartPoolPositions(IndexHistory.STRING)[localName]);
        skipInitialize = compare(in, VIEW_NAVIGATOR_APPLICATION_BASE);
      }
    }
  }

  @Override
  public void startClass(int cinit, int index, DataBuffer in) {
    super.startClass(cinit, index, in);
    // class traits in scripts — after class_info (class_info contains traits_info), so, we cannot check class name
    modifyAccessModifier = "staticHandlersAdded";
  }

  @Override
  public void endClass() {
    super.endClass();
    modifyAccessModifier = null;
  }

  @Override
  protected void instanceEnding(int oldIInit) {
    if (modifyConstructor) {
      history.getModifiedMethodBodies(decoderIndex).put(oldIInit, MODIFY_INIT_METHOD_BODY_MARKER);
    }
  }

  private static boolean compare(final DataBuffer in, final String s) {
    return compare(in, in.readU32(), s);
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
    TIntObjectHashMap<byte[]> modifiedMethodBodies = history.getModifiedMethodBodies(decoderIndex);
    byte[] bytes = modifiedMethodBodies == null ? null : modifiedMethodBodies.get(methodInfo);
    if (bytes == null) {
      return super.startMethodBody(methodInfo, maxStack, maxRegs, scopeDepth, maxScope);
    }
    else {
      if (bytes == EMPTY_METHOD_BODY) {
        methodBodies.writeU32(this.methodInfo.getIndex(methodInfo));
        methodBodies.writeTo(bytes);
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
    currentBuffer.writeU32((skipInitialize || skipPanelAddChild) ? (traitCount - 1) : traitCount);
  }

  @Override
  public void endInstance() {
    super.endInstance();
    skipInitialize = false;
    modifyConstructor = false;
    skipColorCorrection = false;
    skipPanelAddChild = false;
    modifyAccessModifier = null;
  }

  @SuppressWarnings("UnusedDeclaration")
  private static char[] readChars(DataBuffer in) {
    final int stringLength = in.readU32();
    char[] chars = new char[stringLength];
    final int offset = in.position + in.offset;
    for (int i = 0; i < stringLength; i++) {
      chars[i] = (char)in.data[offset + i];
    }

    return chars;
  }

  @SuppressWarnings("UnusedDeclaration")
  private static String dd(DataBuffer in) {
    int stringLength = in.readU32();
    char[] s = new char[stringLength];
    for (int j = 0; j < stringLength; j++) {
      s[j] = (char)in.data[in.position + in.offset + j];
    }
    return new String(s);
  }

  @Override
  protected void writeDebugFile(DataBuffer in, int oldIndex) {
    int insertionIndex = history.getMapIndex(IndexHistory.STRING, oldIndex);
    int newIndex = history.getNewIndex(insertionIndex);
    if (newIndex == 0) {
      // E:\dev\hero_private\frameworks\projects\framework\src => _
      // but for included file (include "someFile.as") another format — just 'debugfile "C:\Vellum\branches\v2\2.0\dev\output\openSource\textLayout\src\flashx\textLayout\formats\TextLayoutFormatInc.as' — we don't support it yet
      int originalPosition = in.position();
      int start = history.getRawPartPoolPositions(IndexHistory.STRING)[oldIndex];
      in.seek(start);
      int stringLength = in.readU32();
      //char[] s = new char[n];
      //for (int j = 0; j < n; j++) {
      //  s[j] = (char)in.data[in.position + in.offset + j];
      //}
      //String file = new String(s);

      byte[] data = in.data;
      int c;
      int actualStart = -1;
      for (int i = 0; i < stringLength; i++) {
        c = data[in.position + in.offset + i];
        if (c > 127) {
          break; // supports only ASCII
        }

        if (c == ';') {
          if (i < debugBasepath.length) {
            // may be, our injected classes
            break;
          }
          actualStart = in.position + i - debugBasepath.length;
          final int p = in.offset + actualStart;

          System.arraycopy(debugBasepath, 0, data, p, debugBasepath.length);

          stringLength = stringLength - i + debugBasepath.length;
          if (stringLength < 128) {
            actualStart--;
            data[p - 1] = (byte)stringLength;
          }
          else {
            actualStart -= 2;
            data[p - 2] = (byte)((stringLength & 0x7F) | 0x80);
            data[p - 1] = (byte)((stringLength >> 7) & 0x7F);
          }
          break;
        }
      }
      in.seek(originalPosition);

      newIndex = history.getIndex(IndexHistory.STRING, oldIndex, insertionIndex, actualStart);
    }

    opcodes.writeU32(newIndex);
  }
}
