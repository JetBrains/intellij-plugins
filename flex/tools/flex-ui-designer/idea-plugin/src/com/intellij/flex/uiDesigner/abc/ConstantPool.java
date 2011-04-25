package com.intellij.flex.uiDesigner.abc;

import java.util.ArrayList;
import java.util.Collections;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

class ConstantPool {
  public static final Object NULL = new Object();

  DataBuffer in;
  private int size;

  final int[][] positions = new int[7][];
  final int[] ends = new int[7];

  public ConstantPool(DataBuffer in) throws DecoderException {
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

  public int getInt(int index) {
    if (index == 0) {
      return 0;
    }

    int pos = positions[IndexHistory.INT][index];
    int originalPos = in.position();
    in.seek(pos);

    int value = in.readU32();
    in.seek(originalPos);
    return value;
  }

  public long getLong(int index) {
    if (index == 0) {
      return 0;
    }

    int pos = positions[IndexHistory.UINT][index];
    int originalPos = in.position();
    in.seek(pos);
    long value = in.readU32();
    in.seek(originalPos);
    return value;
  }

  public double getDouble(int index) {
    if (index == 0) {
      return 0;
    }

    int pos = positions[IndexHistory.DOUBLE][index];
    int originalPos = in.position();
    in.seek(pos);
    double value = in.readDouble();
    in.seek(originalPos);
    return value;
  }

  public String getString(int index) throws DecoderException {
    if (index == 0) {
      return null;
    }

    int pos = positions[IndexHistory.STRING][index];
    int originalPos = in.position();
    in.seek(pos);
    String value = in.readString(in.readU32());
    in.seek(originalPos);
    if (value != null) {
      return value;
    }
    else {
      throw new DecoderException("abc Decoder Erro: problem reading UTF-8 encoded strings.");
    }
  }

  public String getNamespaceName(int index) throws DecoderException {
    if (index == 0) {
      return null;
    }
    int pos = positions[IndexHistory.NS][index];
    int originalPos = in.position();
    in.seek(pos);
    int kind = in.readU8();
    String value;
    switch (kind) {
      case CONSTANT_PrivateNamespace:
      case CONSTANT_Namespace:
      case CONSTANT_PackageNamespace:
      case CONSTANT_PackageInternalNs:
      case CONSTANT_ProtectedNamespace:
      case CONSTANT_ExplicitNamespace:
      case CONSTANT_StaticProtectedNs:
        value = getString(in.readU32());
        break;
      default:
        throw new DecoderException(
          "abc Decoder Error: constant pool index '" + index + "' is not a Namespace type. The actual type is '" + kind + "'");
    }
    in.seek(originalPos);
    return value;
  }

  public String[] getNamespaceSet(int index) throws DecoderException {
    if (index == 0) {
      return null;
    }

    int pos = positions[IndexHistory.NS_SET][index];
    int originalPos = in.position();
    in.seek(pos);
    int count = in.readU32();
    String[] value = new String[count];
    for (int j = 0; j < count; j++) {
      value[j] = getNamespaceName(in.readU32());
    }
    in.seek(originalPos);
    return value;
  }

  public QName getQName(int index) throws DecoderException {
    if (index == 0) {
      return null;
    }

    int pos = positions[IndexHistory.MULTINAME][index];
    int originalPos = in.position();
    in.seek(pos);
    int kind = in.readU8();

    switch (kind) {
      case CONSTANT_Qname:
      case CONSTANT_QnameA: {
        int namespaceIndex = in.readU32();
        int nameIndex = in.readU32();
        QName value = createQName(getNamespaceName(namespaceIndex), getString(nameIndex));
        in.seek(originalPos);
        return value;
      }
      case CONSTANT_TypeName: {
        int nameIndex = in.readU32();
        int count = in.readU32();
        String params = ".<";
        QName base = getQName(nameIndex);
        for (int i = 0; i < count; ++i) {
          params += (i > 0 ? "," : "") + getQName(in.readU32());
        }
        params += ">";
        QName value = createQName(base.getNamespace(), base.getLocalPart() + params);
        in.seek(originalPos);
        return value;
      }
      default:
        in.seek(originalPos);
        throw new DecoderException(
          "abc Decoder Error: constant pool index '" + index + "' is not a QName type. The actual type is '" + kind + "'");
    }
  }

  public MultiName getMultiName(int index) throws DecoderException {
    if (index == 0) {
      return null;
    }

    int pos = positions[IndexHistory.MULTINAME][index];
    int originalPos = in.position();
    in.seek(pos);
    int kind = in.readU8();

    switch (kind) {
      case CONSTANT_Multiname:
      case CONSTANT_MultinameA:
        String name = getString(in.readU32());
        int namespace_set = in.readU32();
        String[] namespaces = getNamespaceSet(namespace_set);
        MultiName value = createMultiName(name, namespaces);
        in.seek(originalPos);
        return value;
      default:
        in.seek(originalPos);
        throw new DecoderException(
          "abc Decoder Error: constant constantPool index '" + index + "' is not a MultiName type. The actual type is '" + kind + "'");
    }
  }

  public Object getGeneralMultiname(int index) throws DecoderException {
    if (index == 0) {
      return null;
    }

    int pos = positions[IndexHistory.MULTINAME][index];
    int originalPos = in.position();
    in.seek(pos);
    int kind = in.readU8();

    switch (kind) {
      case CONSTANT_Qname:
      case CONSTANT_QnameA: {
        int namespaceIndex = in.readU32();
        int nameIndex = in.readU32();
        QName value = createQName(getNamespaceName(namespaceIndex), getString(nameIndex));
        in.seek(originalPos);
        return value;
      }
      case CONSTANT_Multiname:
      case CONSTANT_MultinameA: {
        String name = getString(in.readU32());
        int namespace_set = in.readU32();
        String[] namespaces = getNamespaceSet(namespace_set);
        MultiName value = createMultiName(name, namespaces);
        in.seek(originalPos);
        return value;
      }
      case CONSTANT_RTQnameL:
        in.seek(originalPos);
        return "CONSTANT_RTQnameL"; // Boolean.FALSE;
      case CONSTANT_RTQnameLA:
        in.seek(originalPos);
        return "CONSTANT_RTQnameLA"; // Boolean.TRUE;
      case CONSTANT_MultinameL:
      case CONSTANT_MultinameLA: {
        int namespacesetIndex = in.readU32();
        String[] value = getNamespaceSet(namespacesetIndex);
        ArrayList<String> a = new ArrayList<String>();
        Collections.addAll(a, value);
        in.seek(originalPos);
        return a;
      }
      case CONSTANT_RTQname:
      case CONSTANT_RTQnameA: {
        int idx = in.readU32();
        String s = getString(idx);
        in.seek(originalPos);
        return s;
      }
      default:
        in.seek(originalPos);
        throw new DecoderException(
          "abc Decoder Error: constant pool index '" + index + "' is not a QName type. The actual type is '" + kind + "'");
    }
  }

  public Object get(int index, int kind) throws DecoderException {
    if (index == 0) {
      return null;
    }

    Object value;
    switch (kind) {
      case CONSTANT_Utf8:
        value = getString(index);
        return value;
      case CONSTANT_Integer:
        value = getInt(index);
        return value;
      case CONSTANT_UInteger:
        return getLong(index);
      case CONSTANT_Double:
        return getDouble(index);
      case CONSTANT_Qname:
      case CONSTANT_QnameA:
        value = getQName(index);
        return value;
      case CONSTANT_Namespace:
      case CONSTANT_PrivateNamespace:
      case CONSTANT_PackageNamespace:
      case CONSTANT_PackageInternalNs:
      case CONSTANT_ProtectedNamespace:
      case CONSTANT_ExplicitNamespace:
      case CONSTANT_StaticProtectedNs:
        value = getNamespaceName(index);
        return value;
      case CONSTANT_Multiname:
      case CONSTANT_MultinameA:
        value = getMultiName(index);
        return value;
      case CONSTANT_False:
        value = Boolean.FALSE;
        return value;
      case CONSTANT_True:
        value = Boolean.TRUE;
        return value;
      case CONSTANT_Null:
        return NULL;
      case CONSTANT_RTQname:
      case CONSTANT_RTQnameA:
        value = getGeneralMultiname(index);
        return value;
      case CONSTANT_RTQnameL:
        value = "CONSTANT_RTQnameL"; // Boolean.FALSE;
        return value;
      case CONSTANT_RTQnameLA:
        value = "CONSTANT_RTQnameLA"; // Boolean.TRUE;
        return value;
      case CONSTANT_MultinameL:
        value = getNamespaceSet(getInt(index));
        return value;
      case CONSTANT_MultinameLA:
        value = getNamespaceSet(getInt(index));
        return value;
      case CONSTANT_Namespace_Set:
        value = getNamespaceSet(index);
        return value;
      default:
        throw new DecoderException("Error: Unhandled constant type - " + kind);
    }
  }

  private QName createQName(String ns, String name) {
    return new QName(ns, name);
  }

  private MultiName createMultiName(String name, String[] ns) {
    return new MultiName(name, ns);
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
    int originalPos = data.position();
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

    data.seek(originalPos);
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
