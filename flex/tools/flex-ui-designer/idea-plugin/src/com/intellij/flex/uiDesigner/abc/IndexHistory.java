package com.intellij.flex.uiDesigner.abc;

import gnu.trove.TIntObjectHashMap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
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

  private final List<Decoder> decoders;
  private final int[] map;

  private final PoolPart[] poolParts = new PoolPart[7];

  private final WritableDataBuffer in_ns, in_ns_set, in_multiname;

  // Needed so we can strip out the index for all CONSTANT_PrivateNamespace entries
  // since the name for private namespaces is not important
  private boolean disableDebuggingInfo = false;

  IndexHistory(List<Decoder> decoders) {
    this.decoders = decoders;
    int size = 0;
    int preferredSize = 0;
    int[] poolPartLengths = new int[7];
    for (int i = 0, length = decoders.size(); i < length; i++) {
      Decoder decoder = decoders.get(i);
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

  public int[] getRawPartPoolPositions(int poolIndex, int kind) {
    return decoders.get(poolIndex).constantPool.positions[kind];
  }

  public TIntObjectHashMap<byte[]> getModifiedMethodBodies(int poolIndex) {
    final ConstantPool pool = decoders.get(poolIndex).constantPool;
    if (pool.modifiedMethodBodies == null) {
      pool.modifiedMethodBodies = new TIntObjectHashMap<byte[]>();
    }

    return pool.modifiedMethodBodies;
  }

  public int getNewIndex(int insertionIndex) {
    return map[insertionIndex];
  }

  public int getIndex(int poolIndex, int kind, int index, int insertionIndex, int actualStart) {
    return map[insertionIndex] = decodeOnDemand(poolIndex, kind, index, actualStart);
  }

  public int getIndex(int poolIndex, int kind, int index) {
    if (index == 0) {
      return 0;
    }
    else {
      int mapIndex = getMapIndex(poolIndex, kind, index);
      int newIndex = getNewIndex(mapIndex);
      if (newIndex == 0) {
        return map[mapIndex] = decodeOnDemand(poolIndex, kind, index, -1);
      }
      else {
        return newIndex;
      }
    }
  }

  public ByteBuffer createBuffer(PoolPart metadataInfo) {
    int bufferSize = 0;
    for (PoolPart poolPart : poolParts) {
      if (poolPart != null && poolPart.totalBytes > bufferSize) {
        bufferSize = poolPart.totalBytes;
      }
    }

    if (metadataInfo.totalBytes > bufferSize) {
      bufferSize = metadataInfo.totalBytes;
    }

    final ByteBuffer buffer = ByteBuffer.wrap(new byte[bufferSize + 5 /*u32*/]);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    return buffer;
  }

  public void writeTo(FileChannel channel, ByteBuffer buffer) throws IOException {
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

  public int getMapIndex(final int poolIndex, final int kind, final int oldIndex) {
    ConstantPool pool = decoders.get(poolIndex).constantPool;
    int index = pool.totalSize;
    for (int i = kind + 1; i < 7; i++) {
      int length = pool.positions[i].length;
      index += length == 0 ? 0 : (length - 1);
    }

    return index + oldIndex - 1;
  }

  private int decodeOnDemand(final int poolIndex, final int kind, final int j, int actualStart) {
    final ConstantPool pool = decoders.get(poolIndex).constantPool;
    final PoolPart poolPart = poolParts[kind];
    final int[] positions = pool.positions[kind];
    final int endPos = pool.ends[kind];
    DataBuffer dataIn = pool.in;
    int start = actualStart == -1 ? positions[j] : actualStart;
    int end = (j != positions.length - 1) ? positions[j + 1] : endPos;
    if (kind == NS) {
      final int pos = positions[j];
      final int originalPos = dataIn.position();
      dataIn.seek(pos);
      start = in_ns.size();
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
          in_ns.writeU32(getIndex(poolIndex, STRING, dataIn.readU32()));
          break;
        default:
          assert false;
      }
      dataIn.seek(originalPos);
      end = in_ns.size();
      dataIn = in_ns;
    }
    else if (kind == NS_SET) {
      int pos = positions[j];
      int originalPos = dataIn.position();
      dataIn.seek(pos);
      start = in_ns_set.size();

      int count = dataIn.readU32();
      in_ns_set.writeU32(count);
      for (int k = 0; k < count; k++) {
        in_ns_set.writeU32(getIndex(poolIndex, NS, dataIn.readU32()));
      }

      dataIn.seek(originalPos);
      end = in_ns_set.size();
      dataIn = in_ns_set;
    }
    else if (kind == MULTINAME) {
      int pos = positions[j];
      int originalPos = dataIn.position();
      dataIn.seek(pos);
      start = in_multiname.size();
      int constKind = dataIn.readU8();
      if (!(constKind == CONSTANT_TypeName)) {
        in_multiname.writeU8(constKind);
      }

      switch (constKind) {
        case CONSTANT_Qname:
        case CONSTANT_QnameA: {
          in_multiname.writeU32(getIndex(poolIndex, NS, dataIn.readU32()));
          in_multiname.writeU32(getIndex(poolIndex, STRING, dataIn.readU32()));
          break;
        }
        case CONSTANT_Multiname:
        case CONSTANT_MultinameA: {
          in_multiname.writeU32(getIndex(poolIndex, STRING, dataIn.readU32()));
          in_multiname.writeU32(getIndex(poolIndex, NS_SET, dataIn.readU32()));
          break;
        }
        case CONSTANT_RTQname:
        case CONSTANT_RTQnameA: {
          in_multiname.writeU32(getIndex(poolIndex, STRING, dataIn.readU32()));
          break;
        }
        case CONSTANT_RTQnameL:
        case CONSTANT_RTQnameLA:
          break;
        case CONSTANT_MultinameL:
        case CONSTANT_MultinameLA: {
          in_multiname.writeU32(getIndex(poolIndex, NS_SET, dataIn.readU32()));
          break;
        }
        case CONSTANT_TypeName: {
          int newNameIndex = getIndex(poolIndex, MULTINAME, dataIn.readU32());
          final int count = dataIn.readU32();
          final int[] newParams = new int[count];
          for (int i = 0; i < count; i++) {
            newParams[i] = getIndex(poolIndex, MULTINAME, dataIn.readU32());
          }
          start = in_multiname.size();
          in_multiname.writeU8(constKind);
          in_multiname.writeU32(newNameIndex);
          in_multiname.writeU32(count);
          for (int i = 0; i < count; i++) {
            in_multiname.writeU32(newParams[i]);
          }
          break;
        }

        default:
          assert false; // can't possibly happen...
      }

      dataIn.seek(originalPos);
      end = in_multiname.size();
      dataIn = in_multiname;
    }

    int newIndex = poolPart.contains(dataIn, start, end);
    if (newIndex == -1) {
      newIndex = poolPart.store(dataIn, start, end);
    }

    return newIndex;
  }
}