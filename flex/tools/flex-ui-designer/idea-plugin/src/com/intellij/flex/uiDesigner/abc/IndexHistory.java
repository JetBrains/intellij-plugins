package com.intellij.flex.uiDesigner.abc;

import gnu.trove.TIntArrayList;

import java.nio.ByteBuffer;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

final class IndexHistory {
  public static final int cp_int = 0;
  public static final int cp_uint = 1;
  public static final int cp_double = 2;
  public static final int cp_decimal = 3;
  public static final int cp_string = 4;
  public static final int cp_ns = 5;
  public static final int cp_nsset = 6;
  public static final int cp_mn = 7;

    public int total, duplicate, totalBytes, duplicateBytes;

  private ConstantPool[] pools;
  private int[] poolSizes;
  private int[] map;

  private ByteArrayPool intP, uintP, doubleP, stringP, nsP, nssP, mnP;
  private WritableDataBuffer in_ns, in_nsset, in_mn;

  // Needed so we can strip out the index for all CONSTANT_PrivateNamespace entries
  // since the name for private namespaces is not important
  private boolean disableDebuggingInfo = false;

  IndexHistory(ConstantPool[] pools) {
    this.pools = pools;
    poolSizes = new int[pools.length];

    int size = 0, preferredSize = 0;
    for (int i = 0, length = pools.length; i < length; i++) {
      poolSizes[i] = (i == 0) ? 0 : size;
      size += pools[i].size();
      preferredSize += (pools[i].mnEnd - pools[i].strEnd);
    }

    map = new int[size];
    in_ns = new WritableDataBuffer(preferredSize);
    in_nsset = new WritableDataBuffer(preferredSize);
    in_mn = new WritableDataBuffer(preferredSize);

    intP = new ByteArrayPool();
    uintP = new ByteArrayPool();
    doubleP = new ByteArrayPool();

    stringP = new ByteArrayPool();
    nsP = new NSPool();
    nssP = new NSSPool();
    mnP = new MultiNamePool();

    total = 0;
    duplicate = 0;
    totalBytes = 0;
    duplicateBytes = 0;
  }

  void disableDebugging() {
    disableDebuggingInfo = true;
  }

  public int getIndex(int poolIndex, int kind, int index) {
    if (index == 0) {
      return 0;
    }
    else {
      int newIndex = calculateIndex(poolIndex, kind, index);
      if (map[newIndex] == 0) {
        decodeOnDemand(poolIndex, kind, index, newIndex);
      }

      return map[newIndex];
    }
  }

  public void writeTo(ByteBuffer buffer) {
    intP.writeTo(buffer);
    uintP.writeTo(buffer);
    doubleP.writeTo(buffer);

    stringP.writeTo(buffer);
    nsP.writeTo(buffer);
    nssP.writeTo(buffer);
    mnP.writeTo(buffer);
  }

  /**
   * @param poolIndex 0-based
   * @param kind      0-based
   * @param oldIndex  1-based
   */
  private int calculateIndex(final int poolIndex, final int kind, final int oldIndex) {
    int index = poolSizes[poolIndex];

    if (kind > cp_int) {
      index += (pools[poolIndex].intpositions.length == 0) ? 0 : (pools[poolIndex].intpositions.length - 1);
    }

    if (kind > cp_uint) {
      index += (pools[poolIndex].uintpositions.length == 0) ? 0 : (pools[poolIndex].uintpositions.length - 1);
    }

    if (kind > cp_double) {
      index += (pools[poolIndex].doublepositions.length == 0) ? 0 : (pools[poolIndex].doublepositions.length - 1);
    }

    if (kind > cp_string) {
      index += (pools[poolIndex].strpositions.length == 0) ? 0 : (pools[poolIndex].strpositions.length - 1);
    }

    if (kind > cp_ns) {
      index += (pools[poolIndex].nspositions.length == 0) ? 0 : (pools[poolIndex].nspositions.length - 1);
    }

    if (kind > cp_nsset) {
      index += (pools[poolIndex].nsspositions.length == 0) ? 0 : (pools[poolIndex].nsspositions.length - 1);
    }

    if (kind > cp_mn) {
      index += (pools[poolIndex].mnpositions.length == 0) ? 0 : (pools[poolIndex].mnpositions.length - 1);
    }

    index += (oldIndex - 1);
    return index;
  }

  private void decodeOnDemand(final int poolIndex, final int kind, final int j, final int j2) {
    final ConstantPool pool = pools[poolIndex];
    final ByteArrayPool byteArrayPool;
    final int[] positions;
    int length, endPos;

    switch (kind) {
      case cp_int:
        positions = pool.intpositions;
        length = positions.length;
        endPos = pool.intEnd;
        byteArrayPool = intP;
        break;

      case cp_uint:
        positions = pool.uintpositions;
        length = positions.length;
        endPos = pool.uintEnd;
        byteArrayPool = uintP;
        break;

      case cp_double:
        positions = pool.doublepositions;
        length = positions.length;
        endPos = pool.doubleEnd;
        byteArrayPool = doubleP;
        break;

      case cp_string:
        positions = pool.strpositions;
        length = positions.length;
        endPos = pool.strEnd;
        byteArrayPool = stringP;
        break;

      case cp_ns:
        positions = pool.nspositions;
		    length = positions.length;
		    endPos = pool.nsEnd;
		    byteArrayPool = nsP;
        break;

      case cp_nsset:
       positions = pool.nsspositions;
		    length = positions.length;
		    endPos = pool.nssEnd;
		    byteArrayPool = nssP;
        break;

      case cp_mn:
        positions = pool.mnpositions;
		    length = positions.length;
		    endPos = pool.mnEnd;
		    byteArrayPool = mnP;
        break;

      default:
        throw new IllegalArgumentException("unknown" + kind);
    }

    DataBuffer dataIn = pool.in;
    int start = positions[j];
    int end = (j != length - 1) ? positions[j + 1] : endPos;
    if (kind == cp_ns) {
      int pos = positions[j];
      int originalPos = dataIn.position();
      dataIn.seek(pos);
      start = in_ns.size();
      int nsKind = dataIn.readU8();
      in_ns.writeU8(nsKind);
      switch (nsKind) {
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
          int index = dataIn.readU32();
          int newIndex = getIndex(poolIndex, cp_string, index);
          in_ns.writeU32(newIndex);
          break;
        default:
          assert false;
      }
      dataIn.seek(originalPos);
      end = in_ns.size();
      dataIn = in_ns;
    }
    else if (kind == cp_nsset) {
      int pos = positions[j];
      int originalPos = dataIn.position();
      dataIn.seek(pos);
      start = in_nsset.size();

      int count = dataIn.readU32();
      in_nsset.writeU32(count);
      for (int k = 0; k < count; k++) {
        in_nsset.writeU32(getIndex(poolIndex, cp_ns, dataIn.readU32()));
      }

      dataIn.seek(originalPos);
      end = in_nsset.size();
      dataIn = in_nsset;
    }
    else if (kind == cp_mn) {
      int pos = positions[j];
      int originalPos = dataIn.position();
      dataIn.seek(pos);
      start = in_mn.size();
      int constKind = dataIn.readU8();
      if (!(constKind == CONSTANT_TypeName)) {
        in_mn.writeU8(constKind);
      }

      switch (constKind) {
        case CONSTANT_Qname:
        case CONSTANT_QnameA: {
          int namespaceIndex = dataIn.readU32();
          int newNamespaceIndex = getIndex(poolIndex, cp_ns, namespaceIndex);
          in_mn.writeU32(newNamespaceIndex);
          in_mn.writeU32(getIndex(poolIndex, cp_string, dataIn.readU32()));
          break;
        }
        case CONSTANT_Multiname:
        case CONSTANT_MultinameA: {
          int nameIndex = dataIn.readU32();
          int newNameIndex = getIndex(poolIndex, cp_string, nameIndex);
          in_mn.writeU32(newNameIndex);
          in_mn.writeU32(getIndex(poolIndex, cp_nsset, dataIn.readU32()));
          break;
        }
        case CONSTANT_RTQname:
        case CONSTANT_RTQnameA: {
          in_mn.writeU32(getIndex(poolIndex, cp_string, dataIn.readU32()));
          break;
        }
        case CONSTANT_RTQnameL:
        case CONSTANT_RTQnameLA:
          break;
        case CONSTANT_MultinameL:
        case CONSTANT_MultinameLA: {
          in_mn.writeU32(getIndex(poolIndex, cp_nsset, dataIn.readU32()));
          break;
        }
        case CONSTANT_TypeName: {
          int newNameIndex = getIndex(poolIndex, cp_mn, dataIn.readU32());
          int count = dataIn.readU32();
          TIntArrayList newParams = new TIntArrayList(count);
          for (int i = 0; i < count; ++i) {
            newParams.add(getIndex(poolIndex, cp_mn, dataIn.readU32()));
          }
          start = in_mn.size();
          in_mn.writeU8(constKind);
          in_mn.writeU32(newNameIndex);
          in_mn.writeU32(count);
          for (int i = 0; i < count; ++i) {
            in_mn.writeU32(newParams.get(i));
          }
          break;
        }

        default:
          assert false; // can't possibly happen...
      }

      dataIn.seek(originalPos);
      end = in_mn.size();
      dataIn = in_mn;
    }

    int newIndex = byteArrayPool.contains(dataIn, start, end);
    if (newIndex == -1) {
      newIndex = byteArrayPool.store(dataIn, start, end);
    }
    else {
      duplicate++;
      duplicateBytes += (end - start);
    }

    total++;
    totalBytes += (end - start);

    if (j != 0) {
      map[j2] = newIndex;
    }
  }
}
