package com.intellij.flex.uiDesigner.abc;

import gnu.trove.TIntObjectHashMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

final class IndexHistory {
  public static final int INT = 0;
  public static final int UINT = 1;
  public static final int DOUBLE = 2;
  public static final int STRING = 3;
  public static final int NS = 4;
  public static final int NS_SET = 5;
  public static final int MULTINAME = 6;

  private final int[] map;

  private final PoolPart[] poolParts = new PoolPart[7];

  private final WritableDataBuffer in_ns, in_ns_set, in_multiname;

  // Needed so we can strip out the index for all CONSTANT_PrivateNamespace entries
  // since the name for private namespaces is not important
  private boolean disableDebuggingInfo = false;

  ConstantPool constantPool;

  IndexHistory(List<Decoder> decoders) {
    int size = 0;
    int preferredSize = 0;
    int[] poolPartLengths = new int[7];
    for (Decoder decoder : decoders) {
      if (decoder == null) {
        continue;
      }

      final ConstantPool pool = decoder.constantPool;
      pool.totalSize = size;
      size += pool.size();
      preferredSize += pool.ends[MULTINAME] - pool.ends[STRING];

      for (int j = 0; j < 7; j++) {
        poolPartLengths[j] += pool.positions[j].length;
      }
    }

    map = new int[size];
    in_ns = new WritableDataBuffer(preferredSize);
    in_ns_set = new WritableDataBuffer(preferredSize);
    in_multiname = new WritableDataBuffer(preferredSize);

    for (int i = 0; i < 7; i++) {
      final int poolPartLength = poolPartLengths[i];
      if (poolPartLength == 0) {
        continue;
      }

      if (i < NS) {
        poolParts[i] = new PoolPart(poolPartLength);
      }
      else {
        switch (i) {
          case NS:
            poolParts[i] = new NSPool(poolPartLength);
            break;

          case NS_SET:
            poolParts[i] = new NSSPool(poolPartLength);
            break;

          case MULTINAME:
            poolParts[i] = new MultiNamePool(poolPartLength);
            break;
        }
      }
    }
  }

  void disableDebugging() {
    disableDebuggingInfo = true;
  }

  public int[] getRawPartPoolPositions(int kind) {
    return constantPool.positions[kind];
  }

  TIntObjectHashMap<byte[]> getModifiedMethodBodies() {
    return constantPool.modifiedMethodBodies == null
           ? (constantPool.modifiedMethodBodies = new TIntObjectHashMap<byte[]>())
           : constantPool.modifiedMethodBodies;
  }

  public int getNewIndex(int insertionIndex) {
    return map[insertionIndex];
  }

  public int getIndex(int kind, int index, int insertionIndex, int actualStart) {
    return map[insertionIndex] = decodeOnDemand(kind, index, actualStart, -1);
  }

  public int getIndexWithSpecifiedNsRaw(int index, int actuaNsRaw) {
    return map[getMapIndex(MULTINAME, index)] = decodeOnDemand(MULTINAME, index, -1, actuaNsRaw);
  }

  public int getIndex(int kind, int index) {
    if (index == 0) {
      return 0;
    }
    else {
      int mapIndex = getMapIndex(kind, index);
      int newIndex = getNewIndex(mapIndex);
      if (newIndex == 0) {
        return map[mapIndex] = decodeOnDemand(kind, index, -1, -1);
      }
      else {
        return newIndex;
      }
    }
  }

  public ByteBuffer createBuffer(PoolPart metadataInfo) {
    int bufferSize = metadataInfo.totalBytes;
    for (PoolPart poolPart : poolParts) {
      if (poolPart != null && poolPart.totalBytes > bufferSize) {
        bufferSize = poolPart.totalBytes;
      }
    }

    return ByteBuffer.wrap(new byte[bufferSize + 5 /*u32*/]).order(ByteOrder.LITTLE_ENDIAN);
  }

  public void writeTo(WritableByteChannel channel, ByteBuffer buffer) throws IOException {
    for (PoolPart poolPart : poolParts) {
      if (poolPart == null) {
        PoolPart.writeU32(buffer, 0);
      }
      else {
        poolPart.writeTo(buffer);
      }

      buffer.flip();
      channel.write(buffer);
      buffer.clear();
    }
  }

  public int getMapIndex(final int kind, final int oldIndex) {
    int index = constantPool.totalSize;
    for (int i = kind + 1; i < 7; i++) {
      int length = constantPool.positions[i].length;
      if (length != 0) {
        index += length - 1;
      }
    }

    return index + oldIndex - 1;
  }

  private int decodeOnDemand(final int kind, final int index, final int actualStart, final int actuaNsRaw) {
    final PoolPart poolPart = poolParts[kind];
    final int[] positions = constantPool.positions[kind];
    final int endPos = constantPool.ends[kind];
    DataBuffer dataIn = constantPool.in;
    int start = actualStart == -1 ? positions[index] : actualStart;
    int end = (index != positions.length - 1) ? positions[index + 1] : endPos;
    if (kind == NS) {
      final int originalPos = dataIn.position();
      dataIn.seek(positions[index]);
      start = in_ns.getSize();
      switch (in_ns.copyU8(dataIn)) {
        case CONSTANT_PrivateNamespace:
          if (disableDebuggingInfo) {
            in_ns.writeU32(0); // name not important for private namespace
            break;
          }
          // else fall through and treat like a normal namespace
        case CONSTANT_Namespace:
        case CONSTANT_PackageNamespace:
        case CONSTANT_PackageInternalNs:
        case CONSTANT_ProtectedNamespace:
        case CONSTANT_ExplicitNamespace:
        case CONSTANT_StaticProtectedNs:
          in_ns.writeU32(getIndex(STRING, dataIn.readU32()));
          break;
        default:
          assert false;
      }
      dataIn.seek(originalPos);
      end = in_ns.getSize();
      dataIn = in_ns;
    }
    else if (kind == NS_SET) {
      int originalPos = dataIn.position();
      dataIn.seek(positions[index]);
      start = in_ns_set.getSize();

      int count = dataIn.readU32();
      in_ns_set.writeU32(count);
      for (int k = 0; k < count; k++) {
        in_ns_set.writeU32(getIndex(NS, dataIn.readU32()));
      }

      dataIn.seek(originalPos);
      end = in_ns_set.getSize();
      dataIn = in_ns_set;
    }
    else if (kind == MULTINAME) {
      final int originalPos = dataIn.position();
      dataIn.seek(positions[index]);
      start = in_multiname.getSize();
      int constKind = dataIn.readU8();
      if (!(constKind == CONSTANT_TypeName)) {
        in_multiname.writeU8(constKind);
      }

      switch (constKind) {
        case CONSTANT_Qname:
        case CONSTANT_QnameA: {
          final int ns;
          if (actuaNsRaw != -1) {
            dataIn.readU32();
            ns = actuaNsRaw;
          }
          else {
            ns = dataIn.readU32();
          }
          in_multiname.writeU32(getIndex(NS, ns));
          in_multiname.writeU32(getIndex(STRING, dataIn.readU32()));
          break;
        }
        case CONSTANT_Multiname:
        case CONSTANT_MultinameA: {
          in_multiname.writeU32(getIndex(STRING, dataIn.readU32()));
          in_multiname.writeU32(getIndex(NS_SET, dataIn.readU32()));
          break;
        }
        case CONSTANT_RTQname:
        case CONSTANT_RTQnameA: {
          in_multiname.writeU32(getIndex(STRING, dataIn.readU32()));
          break;
        }
        case CONSTANT_RTQnameL:
        case CONSTANT_RTQnameLA:
          break;
        case CONSTANT_MultinameL:
        case CONSTANT_MultinameLA: {
          in_multiname.writeU32(getIndex(NS_SET, dataIn.readU32()));
          break;
        }
        case CONSTANT_TypeName: {
          int newNameIndex = getIndex(MULTINAME, dataIn.readU32());
          final int count = dataIn.readU32();
          final int[] newParams = new int[count];
          for (int i = 0; i < count; i++) {
            newParams[i] = getIndex(MULTINAME, dataIn.readU32());
          }
          start = in_multiname.getSize();
          in_multiname.writeU8(constKind);
          in_multiname.writeU32(newNameIndex);
          in_multiname.writeU32(count);
          for (int i = 0; i < count; i++) {
            in_multiname.writeU32(newParams[i]);
          }
          break;
        }

        default:
          assert false;
      }

      dataIn.seek(originalPos);
      end = in_multiname.getSize();
      dataIn = in_multiname;
    }

    int newIndex = poolPart.contains(dataIn, start, end);
    if (newIndex == -1) {
      newIndex = poolPart.store(dataIn, start, end);
    }

    return newIndex;
  }
}