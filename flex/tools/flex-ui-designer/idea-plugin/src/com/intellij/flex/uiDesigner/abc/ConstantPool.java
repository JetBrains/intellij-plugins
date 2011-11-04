package com.intellij.flex.uiDesigner.abc;

import gnu.trove.TIntObjectHashMap;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

class ConstantPool {
  final DataBuffer in;
  private int size;

  final int[][] positions = new int[7][];
  final int[] ends = new int[7];
  TIntObjectHashMap<byte[]> modifiedMethodBodies;

  int totalSize;

  public ConstantPool(DataBuffer in) {
    this.in = in;

    for (int i = 0; i < 7; i++) {
      switch (i) {
        case IndexHistory.INT:
        case IndexHistory.UINT:
          positions[i] = Scanner.scanIntConstants(in);
          break;

        case IndexHistory.DOUBLE:
          positions[i] = Scanner.scanDoubleConstants(in);
          break;

        case IndexHistory.STRING:
          positions[i] = Scanner.scanStrConstants(in);
          break;

        case IndexHistory.NS:
          positions[i] = Scanner.scanNsConstants(in);
          break;

        case IndexHistory.NS_SET:
          positions[i] = Scanner.scanNsSetConstants(in);
          break;

        case IndexHistory.MULTINAME:
          positions[i] = Scanner.scanMultinameConstants(in);
          break;
      }

      ends[i] = in.position();
      size += positions[i].length == 0 ? 0 : positions[i].length - 1;
    }
  }

  public int size() {
    return size;
  }
}

final class NSPool extends PoolPart {
  NSPool(int poolPartLength) {
    super(poolPartLength);
  }

  ByteArray createByteArray() {
    return new NS();
  }
}

final class NS extends ByteArray {
  int nsKind = 0;
  int index = 0;

  void init() {
    int originalPos = data.position();
    data.seek(start);
    nsKind = data.readU8();
    switch (nsKind) {
      case CONSTANT_PrivateNamespace:
      case CONSTANT_Namespace:
      case CONSTANT_PackageNamespace:
      case CONSTANT_PackageInternalNs:
      case CONSTANT_ProtectedNamespace:
      case CONSTANT_ExplicitNamespace:
      case CONSTANT_StaticProtectedNs:
        index = data.readU32();
        break;
      default:
        assert false; // can't possibly happen...
    }
    data.seek(originalPos);

    long num = 1234 ^ nsKind ^ index;
    hash = (int)((num >> 32) ^ num);
  }

  void clear() {
    super.clear();
    nsKind = 0;
    index = 0;
  }

  public boolean equals(Object obj) {
    if (obj instanceof NS) {
      NS ns = (NS)obj;
      if (nsKind == CONSTANT_PrivateNamespace) {
        // Private namespaces are only equal if they are literally the same namespace,
        // the name is not important.
        return data == ns.data && start == ns.start && end == ns.end;
      }
      else {
        return ns.nsKind == nsKind && ns.index == index;
      }
    }
    return false;
  }
}


final class NSSPool extends PoolPart {
  NSSPool(int poolPartLength) {
    super(poolPartLength);
  }

  ByteArray createByteArray() {
    return new NSS();
  }

  private static final class NSS extends ByteArray {
    private int[] set;
    private int size;

    void init() {
      int originalPos = data.position();
      data.seek(start);
      int count = data.readU32();

      if (set == null || count > set.length) {
        set = new int[count];
      }
      size = count;

      for (int k = 0; k < count; k++) {
        set[k] = data.readU32();
      }
      data.seek(originalPos);

      long num = 1234;
      for (int k = 0; k < count; k++) {
        num ^= set[k];
      }
      hash = (int)((num >> 32) ^ num);
    }

    void clear() {
      super.clear();
      size = 0;
    }

    public boolean equals(Object obj) {
      if (obj instanceof NSS) {
        NSS nss = (NSS)obj;
        if (size == nss.size) {
          for (int i = 0; i < size; i++) {
            if (set[i] != nss.set[i]) {
              return false;
            }
          }
          return true;
        }
        else {
          return false;
        }
      }
      else {
        return false;
      }
    }
  }
}

final class MultiNamePool extends PoolPart {
  public MultiNamePool(int poolPartLength) {
    super(poolPartLength);
  }

  ByteArray createByteArray() {
    return new MultinameByteArray();
  }
}

final class MultinameByteArray extends ByteArray {
  int constKind = 0, index1 = 1, index2 = 1;

  void init() {
    data.seek(start);
    constKind = data.readU8();

    switch (constKind) {
      case CONSTANT_Qname:
      case CONSTANT_QnameA: {
        index1 = data.readU32();
        index2 = data.readU32();
        long num = 1234 ^ constKind ^ index1 ^ index2;
        hash = (int)((num >> 32) ^ num);
        break;
      }
      case CONSTANT_Multiname:
      case CONSTANT_MultinameA: {
        index1 = data.readU32();
        index2 = data.readU32();
        long num = 1234 ^ constKind ^ index1 ^ index2;
        hash = (int)((num >> 32) ^ num);
        break;
      }
      case CONSTANT_RTQname:
      case CONSTANT_RTQnameA: {
        index1 = data.readU32();
        long num = 1234 ^ constKind ^ index1;
        hash = (int)((num >> 32) ^ num);
        break;
      }
      case CONSTANT_RTQnameL:
      case CONSTANT_RTQnameLA: {
        long num = 1234 ^ constKind;
        hash = (int)((num >> 32) ^ num);
        break;
      }
      case CONSTANT_MultinameL:
      case CONSTANT_MultinameLA: {
        index1 = data.readU32();
        long num = 1234 ^ constKind ^ index1;
        hash = (int)((num >> 32) ^ num);
        break;
      }
      case CONSTANT_TypeName: {
        index1 = data.readU32();
        data.readU32();
        // Only 1 typeparam for now.
        index2 = data.readU32();
        long num = 1234 ^ constKind ^ index1 ^ index2;
        hash = (int)((num >> 32) ^ num);
        break;
      }
      default:
        assert false; // can't possibly happen...
    }
  }

  void clear() {
    super.clear();
    constKind = 0;
    index1 = 0;
    index2 = 0;
  }

  public boolean equals(Object obj) {
    if (obj instanceof MultinameByteArray) {
      MultinameByteArray mn = (MultinameByteArray)obj;

      switch (constKind) {
        case CONSTANT_Qname:
        case CONSTANT_QnameA:
        case CONSTANT_Multiname:
        case CONSTANT_MultinameA: {
          return (constKind == mn.constKind) && (index1 == mn.index1) && (index2 == mn.index2);
        }
        case CONSTANT_RTQname:
        case CONSTANT_RTQnameA: {
          return (constKind == mn.constKind) && (index1 == mn.index1);
        }
        case CONSTANT_RTQnameL:
        case CONSTANT_RTQnameLA:
          return (constKind == mn.constKind);
        case CONSTANT_MultinameL:
        case CONSTANT_MultinameLA: {
          return (constKind == mn.constKind) && (index1 == mn.index1);
        }
        case CONSTANT_TypeName: {
          return (constKind == mn.constKind && index1 == mn.index1 && index2 == mn.index2);
        }
        default:
          return false;
      }
    }
    else {
      return false;
    }
  }
}
