package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.abc.Decoder.Opcodes;
import com.intellij.flex.uiDesigner.io.AbstractByteArrayOutputStream;
import com.intellij.flex.uiDesigner.io.IntIntHashMap;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;
import static com.intellij.flex.uiDesigner.abc.Decoder.MethodCodeDecoding;

@SuppressWarnings({"deprecation"})
public class Encoder {
  private final TIntArrayList tempMetadataList = new TIntArrayList(8);

  final Opcodes opcodeDecoder = new Opcodes();

  protected IndexHistory history;

  private int majorVersion, minorVersion;
  protected int decoderIndex;
  protected int opcodePass;
  private int exPass;
  private boolean disableDebugging, peepHole;

  protected DataBuffer2 methodInfo;
  private MetadataInfoByteArray metadataInfo;
  private DataBuffer2 instanceInfo;
  private DataBuffer2 classInfo;
  private DataBuffer2 scriptInfo;
  protected DataBuffer2 methodBodies;
  protected DataBuffer3 opcodes;
  private WritableDataBuffer exceptions;

  protected WritableDataBuffer currentBuffer;

  private AbcModifier abcModifier;

  public Encoder() {
    this(46, 16);
  }

  private Encoder(int majorVersion, int minorVersion) {
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;

    peepHole = false;
    disableDebugging = false;
  }

  public void enablePeepHole() {
    peepHole = true;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public void disableDebugging() {
    disableDebugging = true;
    history.disableDebugging();
  }

  public void configure(List<Decoder> decoders, @Nullable AbcTranscoder.TransientString excludedName) {
    int estimatedSize = 0, total = 0;

    final int n = decoders.size();
    for (int i = 0; i < n; i++) {
      Decoder decoder = decoders.get(i);
      if (excludedName != null && decoder.name != null && excludedName.same(decoder.name)) {
        decoders.set(i, null);
        continue;
      }

      estimatedSize += decoder.methodInfo.estimatedSize;
      total += decoder.methodInfo.size();
    }
    methodInfo = new DataBuffer2(estimatedSize);
    methodInfo.writeU32(total);

    total = 0;
    for (Decoder decoder : decoders) {
      if (decoder == null) {
        continue;
      }

      total += decoder.metadataInfo.size();
    }
    metadataInfo = new MetadataInfoByteArray(total);

    estimatedSize = 0;
    int classEstimatedSize = 0;
    total = 0;
    for (Decoder decoder : decoders) {
      if (decoder == null) {
        continue;
      }

      estimatedSize += decoder.classInfo.instanceEstimatedSize;
      classEstimatedSize += decoder.classInfo.classEstimatedSize;
      total += decoder.classInfo.size();
    }
    instanceInfo = new DataBuffer2(estimatedSize);
    classInfo = new DataBuffer2(classEstimatedSize);
    instanceInfo.writeU32(total);

    estimatedSize = 0;
    total = 0;
    for (Decoder decoder : decoders) {
      if (decoder == null) {
        continue;
      }

      estimatedSize += decoder.scriptInfo.estimatedSize;
      total += decoder.scriptInfo.size();
    }
    scriptInfo = new DataBuffer2(estimatedSize);
    scriptInfo.writeU32(total);

    estimatedSize = 0;
    total = 0;
    for (Decoder decoder : decoders) {
      if (decoder == null) {
        continue;
      }

      estimatedSize += decoder.methodBodies.estimatedSize;
      total += decoder.methodBodies.size();
    }
    methodBodies = new DataBuffer2(estimatedSize);
    methodBodies.writeU32(total);

    opcodes = new DataBuffer3(decoders, 4096);
    exceptions = new WritableDataBuffer(4096);

    history = new IndexHistory(decoders);
    if (disableDebugging) {
      history.disableDebugging();
    }
  }

  public void useDecoder(int index, Decoder decoder) {
    decoderIndex = index;
    history.constantPool = decoder.constantPool;
    abcModifier = decoder.abcModifier;
  }

  public void endDecoder(Decoder decoder) {
    methodInfo.processedSize += decoder.methodInfo.size();
    metadataInfo.processedSize += decoder.metadataInfo.size();
    instanceInfo.processedSize += decoder.classInfo.size();
    classInfo.processedSize += decoder.classInfo.size();
    scriptInfo.processedSize += decoder.scriptInfo.size();
    methodBodies.processedSize += decoder.methodBodies.size();
  }

  public ByteBuffer writeDoAbc(final AbstractByteArrayOutputStream channel) throws IOException {
    final ByteBuffer buffer = history.createBuffer(metadataInfo);
    final int filePositionBefore = channel.size();
    buffer.putShort((short)((TagTypes.DoABC2 << 6) | 63));
    buffer.position(6);
    buffer.putInt(1);
    buffer.put((byte)'_');
    buffer.put((byte)0);

    buffer.putShort((short)minorVersion);
    buffer.putShort((short)majorVersion);

    buffer.flip();
    channel.write(buffer);
    buffer.clear();

    history.writeTo(channel, buffer);
    methodInfo.writeTo(channel);

    metadataInfo.writeTo(buffer);
    buffer.flip();
    channel.write(buffer);
    buffer.clear();

    instanceInfo.writeTo(channel);
    classInfo.writeTo(channel);
    scriptInfo.writeTo(channel);
    methodBodies.writeTo(channel);

    final int size = channel.size() - filePositionBefore - 6;
    buffer.putInt(size);
    buffer.flip();
    channel.write(buffer, filePositionBefore + 2);

    return buffer;
  }

  public void writeDoAbc(final FileChannel channel, final boolean asTag) throws IOException {
    final ByteBuffer buffer = history.createBuffer(metadataInfo);
    final int filePositionBefore;
    if (asTag) {
      filePositionBefore = (int)channel.position();
      buffer.putShort((short)((TagTypes.DoABC2 << 6) | 63));
      buffer.position(6);
      buffer.putInt(1);
      buffer.put((byte)'_');
      buffer.put((byte)0);
    }
    else {
      filePositionBefore = -1;
    }

    buffer.putShort((short)minorVersion);
    buffer.putShort((short)majorVersion);

    buffer.flip();
    channel.write(buffer);
    buffer.clear();

    history.writeTo(channel, buffer);
    methodInfo.writeTo(channel);

    metadataInfo.writeTo(buffer);
    buffer.flip();
    channel.write(buffer);
    buffer.clear();

    instanceInfo.writeTo(channel);
    classInfo.writeTo(channel);
    scriptInfo.writeTo(channel);
    methodBodies.writeTo(channel);

    if (asTag) {
      final int size = (int)channel.position() - filePositionBefore - 6;
      buffer.putInt(size);
      buffer.flip();
      channel.write(buffer, filePositionBefore + 2);
    }
  }

  public void methodInfo(DataBuffer in) throws DecoderException {
    int paramCount = in.readU32();
    methodInfo.writeU32(paramCount);
    int returnType = in.readU32();
    methodInfo.writeU32(history.getIndex(IndexHistory.MULTINAME, returnType));

    for (int i = 0; i < paramCount; i++) {
      methodInfo.writeU32(history.getIndex(IndexHistory.MULTINAME, in.readU32()));
    }

    int nativeName = in.readU32();
    methodInfo.writeU32((disableDebugging) ? 0 : history.getIndex(IndexHistory.STRING, nativeName));

    int flags = in.readU8();
    if (disableDebugging) {
      // Nuke the param names if we're getting rid of debugging info, don't want them showing
      // up in release code
      flags &= ~METHOD_HasParamNames;
    }
    methodInfo.writeU8(flags);

    if ((flags & METHOD_HasOptional) != 0) {
      int optionalCount = in.readU32();
      methodInfo.writeU32(optionalCount);
      for (int i = 0; i < optionalCount; i++) {
        int value = in.readU32();
        int kind = -1;
        int rawKind = in.readU8();
        switch (rawKind) {
          case CONSTANT_Utf8:
            kind = IndexHistory.STRING;
            break;
          case CONSTANT_Integer:
            kind = IndexHistory.INT;
            break;
          case CONSTANT_UInteger:
            kind = IndexHistory.UINT;
            break;
          case CONSTANT_Double:
            kind = IndexHistory.DOUBLE;
            break;
          case CONSTANT_Namespace:
          case CONSTANT_PrivateNamespace:
          case CONSTANT_PackageNamespace:
          case CONSTANT_PackageInternalNs:
          case CONSTANT_ProtectedNamespace:
          case CONSTANT_ExplicitNamespace:
          case CONSTANT_StaticProtectedNs:
            kind = IndexHistory.NS;
            break;
          case CONSTANT_Qname:
          case CONSTANT_QnameA:
          case CONSTANT_Multiname:
          case CONSTANT_MultinameA:
          case CONSTANT_TypeName:
            kind = IndexHistory.MULTINAME;
            break;
          case CONSTANT_Namespace_Set:
            kind = IndexHistory.NS_SET;
            break;
        }

        int newIndex;
        switch (rawKind) {
          case 0:
          case CONSTANT_False:
          case CONSTANT_True:
          case CONSTANT_Null:
            // The index doesn't matter, as long as its non 0
            // there are no boolean values in any cpool, instead the value will be determined by the kind byte
            newIndex = value;
            break;
          default: {
            if (kind == -1) {
              throw new DecoderException("Unknown constant type " + rawKind + " for " + value);
            }
            newIndex = history.getIndex(kind, value);
          }
        }

        methodInfo.writeU32(newIndex);
        methodInfo.writeU8(rawKind);
      }
    }

    if ((flags & METHOD_HasParamNames) != 0 && paramCount != 0) {
      for (int j = 0; j < paramCount; ++j) {
        methodInfo.writeU32(history.getIndex(IndexHistory.STRING, in.readU32()));
      }
    }
  }

  public void metadataInfo(int index, int name, int itemCount, DataBuffer data) throws DecoderException {
    WritableDataBuffer buffer = new WritableDataBuffer(6);
    buffer.writeU32(history.getIndex(IndexHistory.STRING, name));
    buffer.writeU32(itemCount);
    if (itemCount != 0) {
      for (int j = 0; j < itemCount; j++) {
        buffer.writeU32(history.getIndex(IndexHistory.STRING, data.readU32()));
      }
      for (int j = 0; j < itemCount; j++) {
        buffer.writeU32(history.getIndex(IndexHistory.STRING, data.readU32()));
      }
    }

    metadataInfo.addData(index, buffer);
  }

  public void startInstance(DataBuffer in) {
    instanceInfo.writeU32(history.getIndex(IndexHistory.MULTINAME, in.readU32()));
    instanceInfo.writeU32(history.getIndex(IndexHistory.MULTINAME, in.readU32()));

    int flags = in.readU8();
    instanceInfo.writeU8(flags);
    if ((flags & CLASS_FLAG_protected) != 0) {
      instanceInfo.writeU32(history.getIndex(IndexHistory.NS, in.readU32()));
    }

    final int interfaceCount = in.readU32();
    instanceInfo.writeU32(interfaceCount);
    if (interfaceCount > 0) {
      for (int j = 0; j < interfaceCount; j++) {
        instanceInfo.writeU32(history.getIndex(IndexHistory.MULTINAME, in.readU32()));
      }
    }

    final int oldIInit = in.readU32();
    instanceInfo.writeU32(methodInfo.getIndex(oldIInit));
    instanceEnding(oldIInit);

    currentBuffer = instanceInfo;
  }

  protected void instanceEnding(int oldIInit) {
  }

  public void endInstance() {
    currentBuffer = null;
  }

  public void startClass(int cinit) {
    classInfo.writeU32(methodInfo.getIndex(cinit));

    currentBuffer = classInfo;
  }

  public void endClass() {
    currentBuffer = null;
  }

  public void startScript(int initID) {
    scriptInfo.writeU32(methodInfo.getIndex(initID));

    currentBuffer = scriptInfo;
  }

  public void endScript() {
    currentBuffer = null;
  }

  public int startMethodBody(int methodInfoIndex, int maxStack, int maxRegs, int scopeDepth, int maxScope) {
    TIntObjectHashMap<byte[]> modifiedMethodBodies = history.constantPool.modifiedMethodBodies == null ? null : history.getModifiedMethodBodies();
    byte[] bytes = modifiedMethodBodies == null ? null : modifiedMethodBodies.get(methodInfoIndex);
    final int result;
    if (bytes != null) {
      if (bytes == EMPTY_METHOD_BODY) {
        methodBodies.writeU32(methodInfo.getIndex(methodInfoIndex));
        methodBodies.writeTo(bytes);
        return MethodCodeDecoding.STOP;
      }
      else {
        // we cannot change iinit to empty body, because iinit may contains some default value for complex instance properties,
        // so, we allow all code before constructsuper opcode.
        result = MethodCodeDecoding.STOP_AFTER_CONSTRUCT_SUPER;
      }
    }
    else {
      result = MethodCodeDecoding.CONTINUE;
    }

    methodBodies.writeU32(methodInfo.getIndex(methodInfoIndex));

    methodBodies.writeU32(maxStack);
    methodBodies.writeU32(maxRegs);
    methodBodies.writeU32(scopeDepth);
    methodBodies.writeU32(maxScope);

    currentBuffer = methodBodies;
    opcodePass = 1;
    exPass = 1;

    return result;
  }

  public void endMethodBody() {
    currentBuffer = null;
    opcodes.clear();
    exceptions.clear();
  }

  public void endOpcodes() {
    if (opcodePass == 1) {
      opcodePass = 2;
    }
    else if (opcodePass == 2) {
      methodBodies.writeU32(opcodes.getSize());
      methodBodies.writeTo(opcodes);
    }
  }

  public void exception(int start, int end, int target, int type, int name) {
    if (exPass == 2) {
      exceptions.writeU32(opcodes.getOffset(start));
      exceptions.writeU32(opcodes.getOffset(end));
      exceptions.writeU32(opcodes.getOffset(target));
      exceptions.writeU32(history.getIndex(IndexHistory.MULTINAME, type));
      exceptions.writeU32(history.getIndex(IndexHistory.MULTINAME, name));
    }
  }

  public void startExceptions(int exceptionCount) {
    if (exPass == 2) {
      exceptions.writeU32(exceptionCount);
    }
  }

  public void endExceptions() {
    if (exPass == 1) {
      exPass++;
    }
    else if (exPass == 2) {
      methodBodies.writeTo(exceptions);
    }
  }

  public void traitCount(int traitCount) {
    if (abcModifier != null && currentBuffer == instanceInfo) {
      traitCount += abcModifier.methodTraitDelta();
      assert traitCount >= 0;
    }

    currentBuffer.writeU32(traitCount);
  }

  private void encodeMetaData(TIntArrayList metadata) {
    if (metadata != null) {
      currentBuffer.writeU32(metadata.size());
      for (int i = 0, n = metadata.size(); i < n; i++) {
        currentBuffer.writeU32(metadata.getQuick(i));
      }
    }
  }

  private TIntArrayList trimMetadata(int[] metadata) {
    if (metadata == null) {
      return null;
    }

    tempMetadataList.resetQuick();

    for (int aMetadata : metadata) {
      int newIndex = metadataInfo.getIndex(aMetadata);
      if (newIndex != -1) {
        tempMetadataList.add(newIndex);
      }
    }
    return tempMetadataList.isEmpty() ? null : tempMetadataList;
  }

  public void slotTrait(int traitKind, int name, int slotId, int type, int value, int valueKind, int[] metadata, DataBuffer in) throws DecoderException {
    if (abcModifier == null || !abcModifier.slotTraitName(name, traitKind, in, this)) {
      currentBuffer.writeU32(history.getIndex(IndexHistory.MULTINAME, name));
    }

    TIntArrayList newMetadata = trimMetadata(metadata);
    if (((traitKind >> 4) & TRAIT_FLAG_metadata) != 0 && newMetadata == null) {
      traitKind = traitKind & ~(TRAIT_FLAG_metadata << 4);
    }
    currentBuffer.writeU8(traitKind);

    currentBuffer.writeU32(slotId);
    currentBuffer.writeU32(history.getIndex(IndexHistory.MULTINAME, type));

    int kind = -1;

    switch (valueKind) {
      case CONSTANT_Utf8:
        kind = IndexHistory.STRING;
        break;
      case CONSTANT_Integer:
        kind = IndexHistory.INT;
        break;
      case CONSTANT_UInteger:
        kind = IndexHistory.UINT;
        break;
      case CONSTANT_Double:
        kind = IndexHistory.DOUBLE;
        break;
      case CONSTANT_Namespace:
      case CONSTANT_PrivateNamespace:
      case CONSTANT_PackageNamespace:
      case CONSTANT_PackageInternalNs:
      case CONSTANT_ProtectedNamespace:
      case CONSTANT_ExplicitNamespace:
      case CONSTANT_StaticProtectedNs:
        kind = IndexHistory.NS;
        break;
      case CONSTANT_Qname:
      case CONSTANT_QnameA:
      case CONSTANT_Multiname:
      case CONSTANT_MultinameA:
      case CONSTANT_TypeName:
        kind = IndexHistory.MULTINAME;
        break;
      case CONSTANT_Namespace_Set:
        kind = IndexHistory.NS_SET;
        break;
    }

    int newIndex;
    switch (valueKind) {
      case 0:
      case CONSTANT_False:
      case CONSTANT_True:
      case CONSTANT_Null:
        newIndex = value;
        break;
      default: {
        if (kind == -1) {
          throw new DecoderException("writing slotTrait: don't know what constant type it is... " + valueKind + "," + value);
        }
        newIndex = history.getIndex(kind, value);
      }
    }

    currentBuffer.writeU32(newIndex);
    if (value != 0) {
      currentBuffer.writeU8(valueKind);
    }

    encodeMetaData(newMetadata);
  }

  public boolean changeAccessModifier(String fieldName, int name, DataBuffer in) {
    final int originalPosition = in.position();
    in.seek(history.getRawPartPoolPositions(IndexHistory.MULTINAME)[name]);
    int constKind = in.readU8();
    assert constKind == CONSTANT_Qname || constKind == CONSTANT_QnameA;
    int ns = in.readU32();
    int localName = in.readU32();
    in.seek(history.getRawPartPoolPositions(IndexHistory.NS)[ns]);
    int nsKind = in.readU8();
    if (nsKind == CONSTANT_PrivateNamespace) {
      in.seek(history.getRawPartPoolPositions(IndexHistory.STRING)[localName]);
      if (compare(in, fieldName)) {
        currentBuffer.writeU32(history.getIndexWithSpecifiedNsRaw(name, findPublicNamespace(in)));
        in.seek(originalPosition);
        return true;
      }
    }

    in.seek(originalPosition);
    return false;
  }

  public boolean skipMethod(String name, int nameIndex, DataBuffer in) {
    final int originalPosition = in.position();
    in.seek(history.getRawPartPoolPositions(IndexHistory.MULTINAME)[nameIndex]);

    int constKind = in.readU8();
    assert constKind == CONSTANT_Qname || constKind == CONSTANT_QnameA;
    int ns = in.readU32();
    int localName = in.readU32();

    in.seek(history.getRawPartPoolPositions(IndexHistory.NS)[ns]);
    int nsKind = in.readU8();
    if (nsKind == CONSTANT_PackageNamespace) {
      in.seek(history.getRawPartPoolPositions(IndexHistory.STRING)[localName]);
      if (compare(in, name)) {
        in.seek(originalPosition);
        return true;
      }
    }

    in.seek(originalPosition);
    return false;
  }

  private final static byte[] EMPTY_METHOD_BODY = {0x01, 0x02, 0x04, 0x05, 0x03, (byte)0xd0, 0x30, 0x47, 0x00, 0x00};

  public boolean clearMethodBody(String name, int nameIndex, DataBuffer in, int methodInfo) {
    final int originalPosition = in.position();
    in.seek(history.getRawPartPoolPositions(IndexHistory.MULTINAME)[nameIndex]);

    int constKind = in.readU8();
    assert constKind == CONSTANT_Qname || constKind == CONSTANT_QnameA;
    int ns = in.readU32();
    int localName = in.readU32();

    in.seek(history.getRawPartPoolPositions(IndexHistory.NS)[ns]);
    int nsKind = in.readU8();
    if (nsKind == CONSTANT_PackageNamespace) {
      in.seek(history.getRawPartPoolPositions(IndexHistory.STRING)[localName]);
      if (compare(in, name)) {
        history.getModifiedMethodBodies().put(methodInfo, EMPTY_METHOD_BODY);
        in.seek(originalPosition);
        return true;
      }
    }

    in.seek(originalPosition);
    return false;
  }

  private int findPublicNamespace(DataBuffer in) {
    final int originalPosition = in.position();
    try {
      int[] positions = history.getRawPartPoolPositions(IndexHistory.NS);
      for (int i = 0, positionsLength = positions.length; i < positionsLength; i++) {
        in.seek(positions[i]);
        if (in.readU8() == CONSTANT_PackageNamespace) {
          in.seek(history.getRawPartPoolPositions(IndexHistory.STRING)[in.readU32()]);
          // magic, I don't know, cannot find info in AVM spec
          // but ns with kind CONSTANT_PackageNamespace is public and ns with empty name is current public in current class
          if (in.readU32() == 0) {
            return i;
          }
        }
      }

      throw new IllegalArgumentException();
    }
    finally {
      in.seek(originalPosition);
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
      if (in.data[offset + j] != s.charAt(j)) {
        return false;
      }
    }

    return true;
  }

  public void methodTrait(int traitKind, int name, int dispId, int methodInfoIndex, int[] metadata, DataBuffer in) {
    if (abcModifier != null && abcModifier.methodTrait(traitKind, name, in, methodInfoIndex, this)) {
      return;
    }

    if (abcModifier == null || !abcModifier.methodTraitName(name, traitKind, in, this)) {
      currentBuffer.writeU32(history.getIndex(IndexHistory.MULTINAME, name));
    }

    TIntArrayList newMetadata = trimMetadata(metadata);
    if (((traitKind >> 4) & TRAIT_FLAG_metadata) != 0 && newMetadata == null) {
      traitKind = traitKind & ~(TRAIT_FLAG_metadata << 4);
    }
    currentBuffer.writeU8(traitKind);

    currentBuffer.writeU32(dispId);
    currentBuffer.writeU32(methodInfo.getIndex(methodInfoIndex));

    encodeMetaData(newMetadata);
  }

  public void classTrait(int kind, int name, int slotId, int classInfoIndex, int[] metadata) {
    currentBuffer.writeU32(history.getIndex(IndexHistory.MULTINAME, name));
    TIntArrayList newMetadata = trimMetadata(metadata);
    if (((kind >> 4) & TRAIT_FLAG_metadata) != 0 && newMetadata == null) {
      kind = kind & ~(TRAIT_FLAG_metadata << 4);
    }
    currentBuffer.writeU8(kind);

    currentBuffer.writeU32(slotId);
    currentBuffer.writeU32(classInfo.getIndex(classInfoIndex));

    encodeMetaData(newMetadata);
  }

  public void functionTrait(int kind, int name, int slotId, int methodInfo, int[] metadata) {
    currentBuffer.writeU32(history.getIndex(IndexHistory.MULTINAME, name));
    TIntArrayList newMetadata = trimMetadata(metadata);
    if (((kind >> 4) & TRAIT_FLAG_metadata) != 0 && newMetadata == null) {
      kind = kind & ~(TRAIT_FLAG_metadata << 4);
    }
    currentBuffer.writeU8(kind);

    currentBuffer.writeU32(slotId);
    currentBuffer.writeU32(this.methodInfo.getIndex(methodInfo));

    encodeMetaData(newMetadata);
  }

  static final int W = 8;
  int[] window = new int[W];
  int windowSize = 0;
  int head = 0;

  void clearWindow() {
    for (int i = 0; i < W; i++) {
      window[i] = 0;
    }
    windowSize = 0;
  }

  void beginop(int opcode) {
    window[head] = opcodes.getSize();
    head = (head + 1) & (W - 1);
    if (windowSize < 8) {
      ++windowSize;
    }
    opcodes.writeU8(opcode);
  }

  int opat(int i) {
    if (peepHole && i <= windowSize) {
      int ip = window[(head - i) & (W - 1)];
      if (ip < opcodes.getSize()) {
        return opcodes.readU8(ip);
      }
    }
    return 0;
  }

  void setOpcodeAt(int i, int opcode) {
    assert peepHole;

    if (i <= windowSize) {
      int ip = window[(head - i) & (W - 1)];
      if (ip < opcodes.getSize()) {
        opcodes.writeU8(ip, opcode);
      }
    }
  }

  int readByteAt(int i) {
    return i <= windowSize ? (byte)opcodes.readU8(1 + window[(head - i) & (W - 1)]) : 0;
  }

  int readIntAt(int i) {
    return i <= windowSize ? opcodes.readU32(1 + window[(head - i) & (W - 1)]) : 0;
  }

  void rewind(int i) {
    int to = (head - i) & (W - 1);
    opcodes.delete(opcodes.getSize() - window[to]);
    head = to;
    windowSize -= i;
  }

  public void target(int oldPos) {
    if (opcodePass == 1) {
      opcodes.mapOffsets(oldPos);
      clearWindow();
    }
  }

  public void OP_returnvoid() {
    if (opcodePass == 1) {
      beginop(OP_returnvoid);
    }
  }

  public void OP_returnvalue() {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a) {
        rewind(1);
      }

      if (opat(1) == OP_pushundefined) {
        rewind(1);
        OP_returnvoid();
        return;
      }

      beginop(OP_returnvalue);
    }
  }

  public void OP_nop() {
    if (opcodePass == 1) {
      beginop(OP_nop);
    }
  }

  public void OP_bkpt() {
    if (opcodePass == 1) {
      beginop(OP_bkpt);
    }
  }

  public void OP_timestamp() {
    if (opcodePass == 1) {
      beginop(OP_timestamp);
    }
  }

  public void OP_debugline(int linenum) {
    if (opcodePass == 1) {
      if (!disableDebugging) {
        beginop(OP_debugline);
        opcodes.writeU32(linenum);
      }
    }
  }

  public void OP_bkptline() {
    if (opcodePass == 1) {
      beginop(OP_bkptline);
    }
  }

  public void OP_debug(int di_local, int index, int slot, int linenum) {
    if (opcodePass == 1 && !disableDebugging) {
      beginop(OP_debug);
      opcodes.writeU8(di_local);
      opcodes.writeU32(history.getIndex(IndexHistory.STRING, index));
      opcodes.writeU8(slot);
      opcodes.writeU32(linenum);
    }
  }

  public void OP_debugfile(DataBuffer in) {
    int index = in.readU32();
    if (opcodePass == 1 && !disableDebugging) {
      beginop(OP_debugfile);
      if (index == 0) {
        opcodes.writeU32(0);
      }
      else {
        writeDebugFile(in, index);
      }
    }
  }

  protected void writeDebugFile(DataBuffer in, int oldIndex) {
    opcodes.writeU32(history.getIndex(IndexHistory.STRING, oldIndex));
  }

  public void OP_jump(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_jump);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_pushnull() {
    if (opcodePass == 1) {
      beginop(OP_pushnull);
    }
  }

  public void OP_pushundefined() {
    if (opcodePass == 1) {
      beginop(OP_pushundefined);
    }
  }

  public void OP_pushstring(int index) {
    if (opcodePass == 1) {
      beginop(OP_pushstring);
      opcodes.writeU32(history.getIndex(IndexHistory.STRING, index));
    }
  }

  public void OP_pushnamespace(int index) {
    if (opcodePass == 1) {
      beginop(OP_pushnamespace);
      opcodes.writeU32(history.getIndex(IndexHistory.NS, index));
    }
  }

  public void OP_pushint(int index) {
    if (opcodePass == 1) {
      beginop(OP_pushint);
      opcodes.writeU32(history.getIndex(IndexHistory.INT, index));
    }
  }

  public void OP_pushuint(int index) {
    if (opcodePass == 1) {
      beginop(OP_pushuint);
      opcodes.writeU32(history.getIndex(IndexHistory.UINT, index));
    }
  }

  public void OP_pushdouble(int index) {
    if (opcodePass == 1) {
      beginop(OP_pushdouble);
      opcodes.writeU32(history.getIndex(IndexHistory.DOUBLE, index));
    }
  }

  public void OP_getlocal(int index) {
    if (opcodePass == 1) {
      if (opat(1) == OP_setlocal && readIntAt(1) == index) {
        rewind(1);
        OP_dup();
        OP_setlocal(index);
        return;
      }

      beginop(OP_getlocal);
      opcodes.writeU32(index);
    }
  }

  public void OP_pushtrue() {
    if (opcodePass == 1) {
      beginop(OP_pushtrue);
    }
  }

  public void OP_pushfalse() {
    if (opcodePass == 1) {
      beginop(OP_pushfalse);
    }
  }

  public void OP_pushnan() {
    if (opcodePass == 1) {
      beginop(OP_pushnan);
    }
  }

  public void OP_pushdnan() {
    if (opcodePass == 1) {
      beginop(OP_pushdnan);
    }
  }

  public void OP_pop() {
    if (opcodePass == 1) {
      switch (opat(1)) {
        case OP_callproperty:
          setOpcodeAt(1, OP_callpropvoid);
          return;
        case OP_callsuper:
          setOpcodeAt(1, OP_callsupervoid);
          return;
      }

      beginop(OP_pop);
    }
  }

  public void OP_dup() {
    if (opcodePass == 1) {
      beginop(OP_dup);
    }
  }

  public void OP_swap() {
    if (opcodePass == 1) {
      beginop(OP_swap);
    }
  }

  public void OP_convert_s() {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a) {
        rewind(1);
      }

      switch (opat(1)) {
        case OP_coerce_s:
        case OP_convert_s:
        case OP_pushstring:
        case OP_typeof:
          // result is already string
          return;
      }

      if (opat(2) == OP_pushstring && opat(1) == OP_add) {
        // result must be string, so dont coerce after
        return;
      }

      beginop(OP_convert_s);
    }
  }

  public void OP_esc_xelem() {
    if (opcodePass == 1) {
      beginop(OP_esc_xelem);
    }
  }

  public void OP_esc_xattr() {
    if (opcodePass == 1) {
      beginop(OP_esc_xattr);
    }
  }

  public void OP_checkfilter() {
    if (opcodePass == 1) {
      beginop(OP_checkfilter);
    }
  }

  public void OP_convert_d() {
    if (opcodePass == 1) {
      beginop(OP_convert_d);
    }
  }

  public void OP_convert_m() {
    if (opcodePass == 1) {
      beginop(OP_convert_m);
    }
  }

  public void OP_convert_m_p(int param) {
    if (opcodePass == 1) {
      beginop(OP_convert_m);
      opcodes.writeU32(param);
    }
  }

  public void OP_convert_b() {
    if (opcodePass == 1) {
      switch (opat(1)) {
        case OP_equals:
        case OP_strictequals:
        case OP_not:
        case OP_greaterthan:
        case OP_lessthan:
        case OP_greaterequals:
        case OP_lessequals:
        case OP_istype:
        case OP_istypelate:
        case OP_instanceof:
        case OP_deleteproperty:
        case OP_in:
        case OP_convert_b:
        case OP_pushtrue:
        case OP_pushfalse:
          // dont need convert
          return;
      }

      beginop(OP_convert_b);
    }
  }

  public void OP_convert_o() {
    if (opcodePass == 1) {
      beginop(OP_convert_o);
    }
  }

  public void OP_negate() {
    if (opcodePass == 1) {
      beginop(OP_negate);
    }
  }

  public void OP_negate_p(int param) {
    if (opcodePass == 1) {
      beginop(OP_negate_p);
      opcodes.writeU32(param);
    }
  }

  public void OP_negate_i() {
    if (opcodePass == 1) {
      beginop(OP_negate_i);
    }
  }

  public void OP_increment() {
    if (opcodePass == 1) {
      beginop(OP_increment);
    }
  }

  public void OP_increment_p(int param) {
    if (opcodePass == 1) {
      beginop(OP_increment_p);
      opcodes.writeU32(param);
    }
  }

  public void OP_increment_i() {
    if (opcodePass == 1) {
      beginop(OP_increment_i);
    }
  }

  public void OP_inclocal(int index) {
    if (opcodePass == 1) {
      beginop(OP_inclocal);
      opcodes.writeU32(index);
    }
  }

  public void OP_inclocal_p(int param, int index) {
    if (opcodePass == 1) {
      beginop(OP_inclocal_p);
      opcodes.writeU32(param);
      opcodes.writeU32(index);
    }
  }

  public void OP_kill(int index) {
    if (opcodePass == 1) {
      switch (opat(1)) {
        case OP_returnvalue:
        case OP_returnvoid:
          // unreachable
          return;
      }

      beginop(OP_kill);
      opcodes.writeU32(index);
    }
  }

  public void OP_inclocal_i(int index) {
    if (opcodePass == 1) {
      beginop(OP_inclocal_i);
      opcodes.writeU32(index);
    }
  }

  public void OP_decrement() {
    if (opcodePass == 1) {
      beginop(OP_decrement);
    }
  }

  public void OP_decrement_p(int param) {
    if (opcodePass == 1) {
      beginop(OP_decrement_p);
      opcodes.writeU32(param);
    }
  }

  public void OP_decrement_i() {
    if (opcodePass == 1) {
      beginop(OP_decrement_i);
    }
  }

  public void OP_declocal(int index) {
    if (opcodePass == 1) {
      beginop(OP_declocal);
      opcodes.writeU32(index);
    }
  }

  public void OP_declocal_p(int param, int index) {
    if (opcodePass == 1) {
      beginop(OP_declocal_p);
      opcodes.writeU32(param);
      opcodes.writeU32(index);
    }
  }

  public void OP_declocal_i(int index) {
    if (opcodePass == 1) {
      beginop(OP_declocal_i);
      opcodes.writeU32(index);
    }
  }

  public void OP_typeof() {
    if (opcodePass == 1) {
      beginop(OP_typeof);
    }
  }

  public void OP_not() {
    if (opcodePass == 1) {
      beginop(OP_not);
    }
  }

  public void OP_bitnot() {
    if (opcodePass == 1) {
      beginop(OP_bitnot);
    }
  }

  public void OP_setlocal(int index) {
    if (opcodePass == 1) {
      if (opat(2) == OP_getlocal && readIntAt(2) == index &&
          opat(1) == OP_increment_i) {
        rewind(2);
        OP_inclocal_i(index);
        return;
      }

      if (opat(2) == OP_getlocal && readIntAt(2) == index &&
          opat(1) == OP_increment) {
        rewind(2);
        OP_inclocal(index);
        return;
      }

      beginop(OP_setlocal);
      opcodes.writeU32(index);
    }
  }

  public void OP_add() {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a) {
        rewind(1);
      }
      beginop(OP_add);
    }
  }

  public void OP_add_p(int param) {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a) {
        rewind(1);
      }
      beginop(OP_add_p);
      opcodes.writeU32(param);
    }
  }

  public void OP_add_i() {
    if (opcodePass == 1) {
      beginop(OP_add_i);
    }
  }

  public void OP_subtract() {
    if (opcodePass == 1) {
      if (opat(1) == OP_pushbyte && readByteAt(1) == 1) {
        rewind(1);
        OP_decrement();
        return;
      }
      beginop(OP_subtract);
    }
  }

  public void OP_subtract_p(int param) {
    if (opcodePass == 1) {
      if (opat(1) == OP_pushbyte && readByteAt(1) == 1) {
        rewind(1);
        OP_decrement_p(param);
        return;
      }
      beginop(OP_subtract_p);
      opcodes.writeU32(param);
    }
  }

  public void OP_subtract_i() {
    if (opcodePass == 1) {
      if (opat(1) == OP_pushbyte && readIntAt(1) == 1) {
        rewind(1);
        OP_decrement_i();
        return;
      }
      beginop(OP_subtract_i);
    }
  }

  public void OP_multiply() {
    if (opcodePass == 1) {
      beginop(OP_multiply);
    }
  }

  public void OP_multiply_p(int param) {
    if (opcodePass == 1) {
      beginop(OP_multiply_p);
      opcodes.writeU32(param);
    }
  }

  public void OP_multiply_i() {
    if (opcodePass == 1) {
      beginop(OP_multiply_i);
    }
  }

  public void OP_divide() {
    if (opcodePass == 1) {
      beginop(OP_divide);
    }
  }

  public void OP_divide_p(int param) {
    if (opcodePass == 1) {
      beginop(OP_divide_p);
      opcodes.writeU32(param);
    }
  }

  public void OP_modulo() {
    if (opcodePass == 1) {
      beginop(OP_modulo);
    }
  }

  public void OP_modulo_p(int param) {
    if (opcodePass == 1) {
      beginop(OP_modulo_p);
      opcodes.writeU32(param);
    }
  }

  public void OP_lshift() {
    if (opcodePass == 1) {
      beginop(OP_lshift);
    }
  }

  public void OP_rshift() {
    if (opcodePass == 1) {
      beginop(OP_rshift);
    }
  }

  public void OP_urshift() {
    if (opcodePass == 1) {
      beginop(OP_urshift);
    }
  }

  public void OP_bitand() {
    if (opcodePass == 1) {
      beginop(OP_bitand);
    }
  }

  public void OP_bitor() {
    if (opcodePass == 1) {
      beginop(OP_bitor);
    }
  }

  public void OP_bitxor() {
    if (opcodePass == 1) {
      beginop(OP_bitxor);
    }
  }

  public void OP_equals() {
    if (opcodePass == 1) {
      beginop(OP_equals);
    }
  }

  public void OP_strictequals() {
    if (opcodePass == 1) {
      beginop(OP_strictequals);
    }
  }

  public void OP_lookupswitch(int defaultPos, int[] casePos, int oldPos, int oldTablePos) {
    if (opcodePass == 1) {
      opcodes.mapOffsets(oldPos);
      beginop(OP_lookupswitch);
      opcodes.mapOffsets(oldPos + 1);
      opcodes.writeS24(defaultPos);
      opcodes.writeU32(casePos.length == 0 ? 0 : casePos.length - 1);
      for (int i = 0, size = casePos.length; i < size; i++) {
        opcodes.mapOffsets(oldTablePos + 3 * i);
        opcodes.writeS24(casePos[i]);
      }
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(oldPos + 1, oldPos, oldPos + defaultPos);
      for (int i = 0, size = casePos.length; i < size; i++) {
        opcodes.updateOffset(oldTablePos + (3 * i), oldPos, oldPos + casePos[i]);
      }
    }
  }

  public void OP_iftrue(int offset, int pos) {
    if (opcodePass == 1) {
      if (opat(1) == OP_convert_b) {
        rewind(1);
      }

      if (opat(1) == OP_pushtrue) {
        rewind(1);
        OP_jump(offset, pos);
        return;
      }

      beginop(OP_iftrue);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_iffalse(int offset, int pos) {
    if (opcodePass == 1) {
      if (opat(1) == OP_convert_b) {
        rewind(1);
      }

      if (opat(2) == OP_strictequals && opat(1) == OP_not) {
        rewind(2);
        OP_ifstricteq(offset, pos);
        return;
      }

      if (opat(2) == OP_equals && opat(1) == OP_not) {
        rewind(2);
        OP_ifeq(offset, pos);
        return;
      }

      if (opat(1) == OP_not) {
        rewind(1);
        OP_iftrue(offset, pos);
        return;
      }

      if (opat(1) == OP_pushfalse) {
        rewind(1);
        OP_jump(offset, pos);
        return;
      }

      beginop(OP_iffalse);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifeq(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifeq);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifne(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifne);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifstricteq(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifstricteq);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifstrictne(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifstrictne);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_iflt(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_iflt);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifle(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifle);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifgt(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifgt);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifge(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifge);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_lessthan() {
    if (opcodePass == 1) {
      beginop(OP_lessthan);
    }
  }

  public void OP_lessequals() {
    if (opcodePass == 1) {
      beginop(OP_lessequals);
    }
  }

  public void OP_greaterthan() {
    if (opcodePass == 1) {
      beginop(OP_greaterthan);
    }
  }

  public void OP_greaterequals() {
    if (opcodePass == 1) {
      beginop(OP_greaterequals);
    }
  }

  public void OP_newobject(int size) {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a && size >= 1) {
        rewind(1);
      }

      beginop(OP_newobject);
      opcodes.writeU32(size);
    }
  }

  public void OP_newarray(int size) {
    if (opcodePass == 1) {
      beginop(OP_newarray);
      opcodes.writeU32(size);
    }
  }

  public void OP_getproperty(int index) {
    if (opcodePass == 1) {
      if (opat(1) == OP_findpropstrict && readIntAt(1) == history.getIndex(IndexHistory.MULTINAME, index)) {
        rewind(1);
        OP_getlex(index);
        return;
      }

      beginop(OP_getproperty);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_setproperty(int index) {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a) {
        rewind(1);
      }

      beginop(OP_setproperty);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_initproperty(int index) {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a) {
        rewind(1);
      }

      beginop(OP_initproperty);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_getdescendants(int index) {
    if (opcodePass == 1) {
      beginop(OP_getdescendants);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_findpropstrict(int index) {
    if (opcodePass == 1) {
      beginop(OP_findpropstrict);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_findproperty(int index) {
    if (opcodePass == 1) {
      beginop(OP_findproperty);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_finddef(int index) {
    if (opcodePass == 1) {
      beginop(OP_finddef);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_getlex(int index) {
    if (opcodePass == 1) {
      beginop(OP_getlex);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_nextname() {
    if (opcodePass == 1) {
      beginop(OP_nextname);
    }
  }

  public void OP_nextvalue() {
    if (opcodePass == 1) {
      beginop(OP_nextvalue);
    }
  }

  public void OP_hasnext() {
    if (opcodePass == 1) {
      beginop(OP_hasnext);
    }
  }

  public void OP_hasnext2(int objectRegister, int indexRegister) {
    if (opcodePass == 1) {
      beginop(OP_hasnext2);
      opcodes.writeU32(objectRegister);
      opcodes.writeU32(indexRegister);
    }
  }

  public void OP_deleteproperty(int index) {
    if (opcodePass == 1) {
      beginop(OP_deleteproperty);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_setslot(int index) {
    if (opcodePass == 1) {
      beginop(OP_setslot);
      opcodes.writeU32(index);
    }
  }

  public void OP_getslot(int index) {
    if (opcodePass == 1) {
      beginop(OP_getslot);
      opcodes.writeU32(index);
    }
  }

  @SuppressWarnings({"deprecation"})
  public void OP_setglobalslot(int index) {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a) {
        rewind(1);
      }

      beginop(OP_setglobalslot);
      opcodes.writeU32(index);
    }
  }

  @SuppressWarnings({"deprecation"})
  public void OP_getglobalslot(int index) {
    if (opcodePass == 1) {
      beginop(OP_getglobalslot);
      opcodes.writeU32(index);
    }
  }

  public void OP_call(int size) {
    if (opcodePass == 1) {
      beginop(OP_call);
      opcodes.writeU32(size);
    }
  }

  public void OP_construct(int size) {
    if (opcodePass == 1) {
      beginop(OP_construct);
      opcodes.writeU32(size);
    }
  }

  public void OP_applytype(int size) {
    if (opcodePass == 1) {
      beginop(OP_applytype);
      opcodes.writeU32(size);
    }
  }

  public void OP_newfunction(int id) {
    if (opcodePass == 1) {
      beginop(OP_newfunction);
      opcodes.writeU32(methodInfo.getIndex(id));
    }
  }

  public void OP_newclass(int id) {
    if (opcodePass == 1) {
      beginop(OP_newclass);
      opcodes.writeU32(classInfo.getIndex(id));
    }
  }

  public void OP_callstatic(int id, int argc) {
    if (opcodePass == 1) {
      beginop(OP_callstatic);
      opcodes.writeU32(methodInfo.getIndex(id));
      opcodes.writeU32(argc);
    }
  }

  public void OP_callmethod(int id, int argc) {
    if (opcodePass == 1) {
      beginop(OP_callmethod);
      opcodes.writeU32(methodInfo.getIndex(id));
      opcodes.writeU32(argc);
    }
  }

  public void OP_callproperty(int index, int argc) {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a) {
        rewind(1);
      }

      beginop(OP_callproperty);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
      opcodes.writeU32(argc);
    }
  }

  public void OP_callproplex(int index, int argc) {
    if (opcodePass == 1) {
      beginop(OP_callproplex);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
      opcodes.writeU32(argc);
    }
  }

  public void OP_constructprop(int index, int argc) {
    if (opcodePass == 1) {
      beginop(OP_constructprop);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
      opcodes.writeU32(argc);
    }
  }

  public void OP_callsuper(int index, int argc) {
    if (opcodePass == 1) {
      beginop(OP_callsuper);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
      opcodes.writeU32(argc);
    }
  }

  public void OP_getsuper(int index) {
    if (opcodePass == 1) {
      beginop(OP_getsuper);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_setsuper(int index) {
    if (opcodePass == 1) {
      beginop(OP_setsuper);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_constructsuper(int argc) {
    if (opcodePass == 1) {
      beginop(OP_constructsuper);
      opcodes.writeU32(argc);
    }
  }

  public void OP_pushshort(int n) {
    if (opcodePass == 1) {
      if (peepHole && n >= -128 && n <= 127) {
        OP_pushbyte(n);
        return;
      }
      beginop(OP_pushshort);
      opcodes.writeU32(n);
    }
  }

  public void OP_astype(int index) {
    if (opcodePass == 1) {
      beginop(OP_astype);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_astypelate() {
    if (opcodePass == 1) {
      beginop(OP_astypelate);
    }
  }

  public void OP_coerce(int index) {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce && readIntAt(1) == history.getIndex(IndexHistory.MULTINAME, index)) {
        // second coerce to same type is redundant
        return;
      }

      beginop(OP_coerce);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_coerce_b() {
    if (opcodePass == 1) {
      beginop(OP_coerce_b);
    }
  }

  public void OP_coerce_o() {
    if (opcodePass == 1) {
      beginop(OP_coerce_o);
    }
  }

  public void OP_coerce_a() {
    if (opcodePass == 1) {
      if (opat(1) == OP_coerce_a) {
        return;
      }
      beginop(OP_coerce_a);
    }
  }

  public void OP_coerce_i() {
    if (opcodePass == 1) {
      switch (opat(1)) {
        case OP_coerce_i:
        case OP_convert_i:
        case OP_increment_i:
        case OP_decrement_i:
        case OP_pushbyte:
        case OP_pushshort:
        case OP_pushint:
        case OP_bitand:
        case OP_bitor:
        case OP_bitxor:
        case OP_lshift:
        case OP_rshift:
        case OP_add_i:
        case OP_subtract_i:
        case OP_multiply_i:
        case OP_bitnot:
          // coerce is redundant
          return;
      }
      beginop(OP_coerce_i);
    }
  }

  public void OP_coerce_u() {
    if (opcodePass == 1) {
      switch (opat(1)) {
        case OP_coerce_u:
        case OP_convert_u:
        case OP_urshift:
          // redundant coerce
          return;
      }
      beginop(OP_coerce_u);
    }
  }

  public void OP_coerce_d() {
    if (opcodePass == 1) {
      switch (opat(1)) {
        case OP_subtract:
        case OP_multiply:
        case OP_divide:
        case OP_modulo:
        case OP_increment:
        case OP_decrement:
        case OP_inclocal:
        case OP_declocal:
        case OP_coerce_d:
        case OP_convert_d:
          // coerce is redundant
          return;
      }
      beginop(OP_coerce_d);
    }
  }

  public void OP_coerce_s() {
    if (opcodePass == 1) {
      switch (opat(1)) {
        case OP_coerce_s:
        case OP_convert_s:
        case OP_pushstring:
        case OP_typeof:
          // result is already string
          return;
      }

      if (opat(2) == OP_pushstring && opat(1) == OP_add) {
        // result must be string, so dont coerce after
        return;
      }

      beginop(OP_coerce_s);
    }
  }

  public void OP_istype(int index) {
    if (opcodePass == 1) {
      beginop(OP_istype);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
    }
  }

  public void OP_istypelate() {
    if (opcodePass == 1) {
      beginop(OP_istypelate);
    }
  }

  public void OP_pushbyte(int n) {
    if (opcodePass == 1) {
      if (opat(1) == OP_pushbyte && readByteAt(1) == n ||
          opat(1) == OP_dup && opat(2) == OP_pushbyte && readByteAt(2) == n) {
        OP_dup();
        return;
      }
      beginop(OP_pushbyte);
      opcodes.writeU8(n);
    }
  }

  public void OP_getscopeobject(int index) {
    if (opcodePass == 1) {
      beginop(OP_getscopeobject);
      opcodes.writeU8(index);
    }
  }

  public void OP_pushscope() {
    if (opcodePass == 1) {
      beginop(OP_pushscope);
    }
  }

  public void OP_popscope() {
    if (opcodePass == 1) {
      beginop(OP_popscope);
    }
  }

  public void OP_convert_i() {
    if (opcodePass == 1) {
      switch (opat(1)) {
        case OP_convert_i:
        case OP_coerce_i:
        case OP_bitand:
        case OP_bitor:
        case OP_bitxor:
        case OP_lshift:
        case OP_rshift:
        case OP_add_i:
        case OP_subtract_i:
        case OP_increment_i:
        case OP_decrement_i:
        case OP_multiply_i:
        case OP_pushbyte:
        case OP_pushshort:
        case OP_pushint:
          return;
      }

      beginop(OP_convert_i);
    }
  }

  public void OP_convert_u() {
    if (opcodePass == 1) {
      beginop(OP_convert_u);
    }
  }

  public void OP_throw() {
    if (opcodePass == 1) {
      beginop(OP_throw);
    }
  }

  public void OP_instanceof() {
    if (opcodePass == 1) {
      beginop(OP_instanceof);
    }
  }

  public void OP_in() {
    if (opcodePass == 1) {
      beginop(OP_in);
    }
  }

  public void OP_dxns(int index) {
    if (opcodePass == 1) {
      beginop(OP_dxns);
      opcodes.writeU32(history.getIndex(IndexHistory.STRING, index));
    }
  }

  public void OP_dxnslate() {
    if (opcodePass == 1) {
      beginop(OP_dxnslate);
    }
  }

  public void OP_ifnlt(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifnlt);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifnle(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifnle);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifngt(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifngt);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_ifnge(int offset, int pos) {
    if (opcodePass == 1) {
      beginop(OP_ifnge);
      opcodes.writeS24(offset);
      opcodes.mapOffsets(pos);
    }
    else if (opcodePass == 2) {
      opcodes.updateOffset(pos + offset);
    }
  }

  public void OP_pushwith() {
    if (opcodePass == 1) {
      beginop(OP_pushwith);
    }
  }

  public void OP_newactivation() {
    if (opcodePass == 1) {
      beginop(OP_newactivation);
    }
  }

  public void OP_newcatch(int index) {
    if (opcodePass == 1) {
      beginop(OP_newcatch);
      opcodes.writeU32(index);
    }
  }

  public void OP_deldescendants() {
    if (opcodePass == 1) {
      beginop(OP_deldescendants);
    }
  }

  public void OP_getglobalscope() {
    if (opcodePass == 1) {
      beginop(OP_getglobalscope);
    }
  }

  public void OP_getlocal0() {
    if (opcodePass == 1) {
      beginop(OP_getlocal0);
    }
  }

  public void OP_getlocal1() {
    if (opcodePass == 1) {
      beginop(OP_getlocal1);
    }
  }

  public void OP_getlocal2() {
    if (opcodePass == 1) {
      beginop(OP_getlocal2);
    }
  }

  public void OP_getlocal3() {
    if (opcodePass == 1) {
      beginop(OP_getlocal3);
    }
  }

  public void OP_setlocal0() {
    if (opcodePass == 1) {
      beginop(OP_setlocal0);
    }
  }

  public void OP_setlocal1() {
    if (opcodePass == 1) {
      if (opat(2) == OP_getlocal1 && opat(1) == OP_increment_i) {
        rewind(2);
        OP_inclocal_i(1);
        return;
      }
      if (opat(2) == OP_getlocal1 && opat(1) == OP_increment) {
        rewind(2);
        OP_inclocal(1);
        return;
      }
      beginop(OP_setlocal1);
    }
  }

  public void OP_setlocal2() {
    if (opcodePass == 1) {
      if (opat(2) == OP_getlocal2 && opat(1) == OP_increment_i) {
        rewind(2);
        OP_inclocal_i(2);
        return;
      }
      if (opat(2) == OP_getlocal2 && opat(1) == OP_increment) {
        rewind(2);
        OP_inclocal(2);
        return;
      }
      beginop(OP_setlocal2);
    }
  }

  public void OP_setlocal3() {
    if (opcodePass == 1) {
      if (opat(2) == OP_getlocal3 && opat(1) == OP_increment_i) {
        rewind(2);
        OP_inclocal_i(3);
        return;
      }
      if (opat(2) == OP_getlocal3 && opat(1) == OP_increment) {
        rewind(2);
        OP_inclocal(3);
        return;
      }
      beginop(OP_setlocal3);
    }
  }

  public void OP_label() {
    if (opcodePass == 1) {
      beginop(OP_label);
    }
  }

  public void OP_pushconstant(int id) {
    if (opcodePass == 1) {
      beginop(OP_pushuninitialized);
      opcodes.writeU32(id);
    }
  }

  public void OP_callsupervoid(int index, int argc) {
    if (opcodePass == 1) {
      beginop(OP_callsupervoid);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
      opcodes.writeU32(argc);
    }
  }

  public void OP_callpropvoid(int index, int argc) {
    if (opcodePass == 1) {
      beginop(OP_callpropvoid);
      opcodes.writeU32(history.getIndex(IndexHistory.MULTINAME, index));
      opcodes.writeU32(argc);
    }
  }

  public void OP_li8() {
    if (opcodePass == 1) {
      beginop(OP_li8);
    }
  }

  public void OP_li16() {
    if (opcodePass == 1) {
      beginop(OP_li16);
    }
  }

  public void OP_li32() {
    if (opcodePass == 1) {
      beginop(OP_li32);
    }
  }

  public void OP_lf32() {
    if (opcodePass == 1) {
      beginop(OP_lf32);
    }
  }

  public void OP_lf64() {
    if (opcodePass == 1) {
      beginop(OP_lf64);
    }
  }

  public void OP_si8() {
    if (opcodePass == 1) {
      beginop(OP_si8);
    }
  }

  public void OP_si16() {
    if (opcodePass == 1) {
      beginop(OP_si16);
    }
  }

  public void OP_si32() {
    if (opcodePass == 1) {
      beginop(OP_si32);
    }
  }

  public void OP_sf32() {
    if (opcodePass == 1) {
      beginop(OP_sf32);
    }
  }

  public void OP_sf64() {
    if (opcodePass == 1) {
      beginop(OP_sf64);
    }
  }

  public void OP_sxi1() {
    if (opcodePass == 1) {
      beginop(OP_sxi1);
    }
  }

  public void OP_sxi8() {
    if (opcodePass == 1) {
      beginop(OP_sxi8);
    }
  }

  public void OP_sxi16() {
    if (opcodePass == 1) {
      beginop(OP_sxi16);
    }
  }

  private static class MetadataInfoByteArray extends PoolPart {
    private int size = 0;
    private int processedSize;
    private final IntIntHashMap indexes;

    MetadataInfoByteArray(int total) {
      super(total + 1);
      indexes = new IntIntHashMap(total);
    }

    int addData(int oldIndex, WritableDataBuffer data) {
      int index = contains(data, 0, data.getSize());
      if (index == -1) {
        index = store(data, 0, data.getSize());
        size += data.getSize();
      }
      // ByteArrayPool is 1 based, we want zero based for metadataInfos
      indexes.put(calcIndex(oldIndex), index - 1);
      return index;
    }

    private int calcIndex(int oldIndex) {
      return processedSize + oldIndex;
    }

    int getIndex(int oldIndex) {
      return indexes.get(calcIndex(oldIndex));
    }

    int size() {
      return size;
    }

    void writeTo(ByteBuffer buffer) {
      writeTo(buffer, 0);
    }
  }

  protected static class DataBuffer2 extends WritableDataBuffer {
    private int processedSize;

    DataBuffer2(int estimatedSize) {
      super(estimatedSize);
    }

    int getIndex(int oldIndex) {
      return processedSize + oldIndex;
    }
  }

  protected class DataBuffer3 extends WritableDataBuffer {
    final IntIntHashMap offsets;
    final List<Decoder> decoders;

    DataBuffer3(List<Decoder> decoders, int estimatedSize) {
      super(estimatedSize);
      offsets = new IntIntHashMap();
      this.decoders = decoders;
    }

    void mapOffsets(int offset) {
      offsets.put(offset, getSize());
    }

    int getOffset(int offset) {
      int i = offsets.get(offset);
      if (i != -1) {
        return i;
      }
      else {
        throw new IllegalArgumentException("getOffset: can't match " + offset + " with a new offset");
      }
    }

    void updateOffset(int offset) {
      int i = offsets.get(offset);
      int p = offsets.get(decoders.get(decoderIndex).position());
      if (i != -1 && p != -1) {
        writeS24(p - 3, i - p);
      }
    }

    void updateOffset(int oldOffsetPos, int oldPos, int oldTarget) {
      int i = offsets.get(oldTarget);
      int p = offsets.get(oldPos);
      int s = offsets.get(oldOffsetPos);
      if (i != -1 && p != -1 && s != -1) {
        writeS24(s, i - p);
      }
      else {
        if (i == -1) {
          System.out.println("updateOffset2: can't match i " + oldTarget + " with a new offset");
        }
        if (p == -1) {
          System.out.println("updateOffset2: can't match p " + oldPos + " with a new offset");
        }
        if (s == -1) {
          System.out.println("updateOffset2: can't match s " + oldOffsetPos + " with a new offset");
        }
        System.out.println(offsets);
      }
    }

    public void clear() {
      super.clear();
      offsets.clear();
    }
  }
}

