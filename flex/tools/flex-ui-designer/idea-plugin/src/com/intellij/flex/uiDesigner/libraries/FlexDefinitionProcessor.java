package com.intellij.flex.uiDesigner.libraries;

import com.google.common.base.Charsets;
import com.intellij.flex.uiDesigner.abc.AbcModifierBase;
import com.intellij.flex.uiDesigner.abc.AbcUtil;
import com.intellij.flex.uiDesigner.abc.DataBuffer;
import com.intellij.flex.uiDesigner.abc.Encoder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.flex.uiDesigner.abc.Encoder.SkipMethodKey;

class FlexDefinitionProcessor implements DefinitionProcessor {
  private static final String MX_CORE = "mx.core:";
  private static final String SPARK_COMPONENTS = "spark.components:";
  
  static final String[] OVERLOADED = new String[]{
    "mx.styles:StyleProtoChain",
    "spark.components.supportClasses:SkinnableComponent",
    "mx.effects:Effect",
    "spark.modules:ModuleLoader",
    "mx.controls:SWFLoader",
  };

  static final char OVERLOADED_AND_BACKED_CLASS_MARK = 'F';

  private final boolean vGreaterOrEquals4_5;

  public FlexDefinitionProcessor(String version) {
    vGreaterOrEquals4_5 = StringUtil.compareVersionNumbers(version, "4.5") >= 0;
  }

  @Override
  public void process(CharSequence name, ByteBuffer buffer, Definition definition, Map<CharSequence, Definition> definitionMap) throws IOException {
    for (String overloadedClassName : OVERLOADED) {
      if (StringUtil.equals(name, overloadedClassName)) {
        changeAbcName(overloadedClassName, buffer);
        flipDefinition(definition, definitionMap, overloadedClassName);
      }
    }

    if (StringUtil.equals(name, "mx.containers:Panel")) {
      final List<SkipMethodKey> skippedMethods;
      if (vGreaterOrEquals4_5) {
        skippedMethods = new ArrayList<SkipMethodKey>(1);
        skippedMethods.add(new SkipMethodKey("addChildAt", true));
      }
      else {
        skippedMethods = null;
      }

      definition.doAbcData.abcModifier = new MethodAccessModifier("setControlBar", skippedMethods);
    }
    else if (StringUtil.equals(name, "mx.controls:SWFLoader")) {
      definition.doAbcData.abcModifier = new MethodAccessModifier("loadContent", null);
    }
    else if (StringUtil.equals(name, "mx.styles:StyleProtoChain")) {
      List<String> list = new ArrayList<String>(2);
      list.add("matchStyleDeclarations");
      list.add("sortOnSpecificity");
      definition.doAbcData.abcModifier = new MethodAccessModifier(list);
    }
    else {
      final boolean mxCore = StringUtil.startsWith(name, MX_CORE);
      if (mxCore) {
        if (equals(name, MX_CORE.length(), "UIComponent")) {
          List<SkipMethodKey> list = new ArrayList<SkipMethodKey>(1);
          list.add(new SkipMethodKey("removedFromStageHandler", false, true));
          definition.doAbcData.abcModifier = new MethodAccessModifier("UIComponent", list, new VarAccessModifier("deferredSetStyles"));
        }
        else if (vGreaterOrEquals4_5 && equals(name, MX_CORE.length(), "FTETextField")) {
          definition.doAbcData.abcModifier = new VarAccessModifier("staticHandlersAdded");
        }
      }

      if (definition.doAbcData.abcModifier != null) {
        return;
      }

      boolean skipInitialize = false;
      // Application without explicit size hangs on Stage and listen to resize - but we must set size via setLayoutBoundsSize
      boolean skipCommitProperties = false;
      boolean modifyConstructor = false;
      boolean skipColorCorrection = false;
      if (mxCore || StringUtil.startsWith(name, SPARK_COMPONENTS)) {
        final int localNameOffset = mxCore ? MX_CORE.length() : SPARK_COMPONENTS.length();
        skipInitialize = equals(name, localNameOffset, "Application");
        skipCommitProperties = skipInitialize;

        if (mxCore) {
          if (skipInitialize) {
            modifyConstructor = true;
          }
        }
        else {
          skipColorCorrection = skipInitialize && vGreaterOrEquals4_5;
          modifyConstructor = skipInitialize ||
                              equals(name, localNameOffset, "ViewNavigatorApplicationBase") ||
                              equals(name, localNameOffset, "TabbedViewNavigatorApplication");
          // AS-66
          if (!skipInitialize && vGreaterOrEquals4_5) {
            skipInitialize = equals(name, localNameOffset, "View");
          }
        }
      }
      else if (vGreaterOrEquals4_5 && StringUtil.equals(name, "spark.components.supportClasses:ViewNavigatorApplicationBase")) {
        skipInitialize = true;
      }

      final List<SkipMethodKey> skippedMethods;
      if (skipInitialize || skipCommitProperties) {
        skippedMethods = new ArrayList<SkipMethodKey>(2);
        skippedMethods.add(new SkipMethodKey("initialize", true));
        if (skipCommitProperties) {
          skippedMethods.add(new SkipMethodKey("commitProperties", false));
        }
      }
      else {
        skippedMethods = null;
      }

      if (skippedMethods != null || modifyConstructor) {
        definition.doAbcData.abcModifier = new MethodAccessModifier(skippedMethods, skipColorCorrection ? "colorCorrection" : null, modifyConstructor, null, null);
      }
    }
  }

  private static boolean equals(CharSequence s1, int s1Offset, CharSequence s2) {
    if ((s1.length() - s1Offset) != s2.length()) {
      return false;
    }

    for (int i = 0; i < s2.length(); i++) {
      if (s1.charAt(i + s1Offset) != s2.charAt(i)) {
        return false;
      }
    }

    return true;
  }

  private static void flipDefinition(Definition definition, Map<CharSequence, Definition> definitionMap, String name) {
    // don't remove old entry from map, it may be requred before we inject
    int i = name.indexOf(':');
    String newName = name.substring(0, i + 1) + OVERLOADED_AND_BACKED_CLASS_MARK + name.substring(i + 2);
    definitionMap.put(newName, definition);
    //definition.name = newName;
  }

  private static void changeAbcName(final String name, ByteBuffer buffer) throws IOException {
    final int oldPosition = buffer.position();
    buffer.position(buffer.position() + 4 + name.length() + 1 /* null-terminated string */);
    parseCPoolAndRename(name.substring(name.indexOf(':') + 1), buffer);

    // modify abcname
    buffer.position(oldPosition + 4 + 10);
    buffer.put((byte)OVERLOADED_AND_BACKED_CLASS_MARK);
    buffer.position(oldPosition);
  }

  private static void parseCPoolAndRename(String from, ByteBuffer buffer) throws IOException {
    buffer.position(buffer.position() + 4);

    int n = AbcUtil.readU32(buffer);
    while (n-- > 1) {
      AbcUtil.readU32(buffer);
    }

    n = AbcUtil.readU32(buffer);
    while (n-- > 1) {
      AbcUtil.readU32(buffer);
    }

    n = AbcUtil.readU32(buffer);
    if (n != 0) {
      buffer.position(buffer.position() + ((n - 1) * 8));
    }

    n = AbcUtil.readU32(buffer);
    final CharsetEncoder charsetEncoder = Charsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(
      CodingErrorAction.REPLACE);
    o:
    while (n-- > 1) {
      int l = AbcUtil.readU32(buffer);
      buffer.limit(buffer.position() + l);
      buffer.mark();
      final CharBuffer charBuffer = Charsets.UTF_8.decode(buffer);
      buffer.limit(buffer.capacity());
      int index = 0;
      do {
        index = CharArrayUtil.indexOf(charBuffer, from, index);
        if (index == -1 || (index > 0 && !isSpecialSymbol(charBuffer.get(index - 1)))) {
          continue o;
        }

        charBuffer.put(index, OVERLOADED_AND_BACKED_CLASS_MARK);

        index += from.length();
      }
      while (index < charBuffer.length());
      buffer.reset();
      charsetEncoder.encode(charBuffer, buffer, true);
      charsetEncoder.reset();
    }
  }

  // E:\dev\4.5.1\frameworks\projects\spark\src;spark\components\supportClasses;SkinnableComponent.as
  // spark.components.supportClasses:SkinnableComponent/SkinnableComponent
  private static boolean isSpecialSymbol(char c) {
    return c == ';' || c == '/' || c == ':';
  }

  private static class MethodAccessModifier extends AbcModifierBase {
    private List<String> changeAccessModifier;
    private List<SkipMethodKey> skippedMethods;
    private String skipSetter;
    private boolean modifyConstructor;

    private final int traitDelta;
    private final VarAccessModifier varAccessModifier;

    private final String classLocalName;

    private MethodAccessModifier(List<String> changeAccessModifier) {
      this(null, null, false, null, null);
      this.changeAccessModifier = changeAccessModifier;
    }

    private MethodAccessModifier(String classLocalName, List<SkipMethodKey> skippedMethods, VarAccessModifier varAccessModifier) {
      this(skippedMethods, null, false, varAccessModifier, classLocalName);
    }

    private MethodAccessModifier(String changeAccessModifier, List<SkipMethodKey> skippedMethods) {
      this(skippedMethods, null, false, null, null);
      this.changeAccessModifier = Collections.singletonList(changeAccessModifier);
    }

    private MethodAccessModifier(@Nullable List<SkipMethodKey> skippedMethods,
                                 @Nullable String skipSetter,
                                 boolean modifyConstructor,
                                 @Nullable VarAccessModifier varAccessModifier,
                                 @Nullable String classLocalName) {
      this.changeAccessModifier = null;
      this.skippedMethods = skippedMethods;
      this.skipSetter = skipSetter;
      this.modifyConstructor = modifyConstructor;
      traitDelta = computeTraitDelta();
      this.varAccessModifier = varAccessModifier;
      this.classLocalName = classLocalName;
    }

    private int computeTraitDelta() {
      int traitDelta = 0;
      if (skippedMethods != null) {
        for (SkipMethodKey key : skippedMethods) {
          if (!key.clear) {
            traitDelta--;
          }
        }
      }
      return traitDelta;
    }

    @Override
    public String getClassLocalName() {
      return classLocalName;
    }

    @Override
    public int instanceMethodTraitDelta() {
      assert skippedMethods == null || traitDelta == computeTraitDelta();
      return traitDelta;
    }

    @Override
    public boolean slotTraitName(int name, int traitKind, DataBuffer in, Encoder encoder) {
      return varAccessModifier != null && varAccessModifier.slotTraitName(name, traitKind, in, encoder);
    }

    @Override
    public boolean methodTraitName(int name, int traitKind, DataBuffer in, Encoder encoder) {
      if (changeAccessModifier != null && !changeAccessModifier.isEmpty() && isNotOverridenMethod(traitKind)) {
        for (int i = 0, size = changeAccessModifier.size(); i < size; i++) {
          String mname = changeAccessModifier.get(i);
          if (encoder.changeAccessModifier(mname, name, in)) {
            if (changeAccessModifier.size() == 1) {
              changeAccessModifier = null;
            }
            else {
              changeAccessModifier.remove(i);
            }

            return true;
          }
        }
      }

      return false;
    }

    @Override
    public boolean methodTrait(int traitKind, int name, DataBuffer in, int methodInfo, Encoder encoder) {
      if (skippedMethods != null && !skippedMethods.isEmpty()) {
        int index;
        if ((index = encoder.skipMethod(skippedMethods, name, in, methodInfo)) != -1) {
          if (skippedMethods.size() == 1) {
            skippedMethods = null;
          }
          else {
            skippedMethods.remove(index);
          }
          return index != -2;
        }
      }
      else if (skipSetter != null && isSetter(traitKind) && encoder.clearMethodBody(skipSetter, name, in, methodInfo)) {
        skipSetter = null;
        // false, clearMethodBody just put it to map for deferred processing
        return false;
      }

      return false;
    }

    @Override
    public boolean isModifyConstructor() {
      if (!modifyConstructor) {
        return false;
      }

      modifyConstructor = false;
      return true;
    }

    @Override
    public void assertOnInstanceEnd() {
      assert skippedMethods == null;
    }
  }

  private static class VarAccessModifier extends AbcModifierBase {
    private String[] fieldNames;

    private VarAccessModifier(String ...fieldNames) {
      this.fieldNames = fieldNames;
    }

    @Override
    public boolean slotTraitName(int name, int traitKind, DataBuffer in, Encoder encoder) {
      for (int i = 0, length = fieldNames.length; i < length; i++) {
        String fieldName = fieldNames[i];
        if (fieldName != null && isVar(traitKind)) {
          if (encoder.changeAccessModifier(fieldName, name, in)) {
            fieldNames[i] = null;
            return true;
          }
        }
      }
      
      return false;
    }

    @Override
    public void assertOnInstanceEnd() {
    }
  }
}