package com.intellij.flex.uiDesigner.abc;

import java.util.ArrayList;
import java.util.Collections;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

class ConstantPool {
  public static final Object NULL = new Object();

  DataBuffer in;
  private int size;

  int[] intpositions;
  int[] uintpositions;
  int[] doublepositions;
  int[] strpositions;
  int[] nspositions;
  int[] nsspositions;
  int[] mnpositions;

  int intEnd;
  int uintEnd;
  int doubleEnd;
  int strEnd;
  int nsEnd;
  int nssEnd;
  int mnEnd;

  public ConstantPool(DataBuffer in) throws DecoderException {
    this.in = in;
    scan();
  }

  private void scan() throws DecoderException {
    intpositions = Scanner.scanIntConstants(in);
    intEnd = in.position();
    uintpositions = Scanner.scanUIntConstants(in);
    uintEnd = in.position();
    doublepositions = Scanner.scanDoubleConstants(in);
    doubleEnd = in.position();

    strpositions = Scanner.scanStrConstants(in);
    strEnd = in.position();
    nspositions = Scanner.scanNsConstants(in);
    nsEnd = in.position();
    nsspositions = Scanner.scanNsSetConstants(in);
    nssEnd = in.position();
    mnpositions = Scanner.scanMultinameConstants(in);
    mnEnd = in.position();

    size = ((intpositions.length == 0) ? 0 : (intpositions.length - 1)) +
           ((uintpositions.length == 0) ? 0 : (uintpositions.length - 1)) +
           ((doublepositions.length == 0) ? 0 : (doublepositions.length - 1)) +
           ((strpositions.length == 0) ? 0 : (strpositions.length - 1)) +
           ((nspositions.length == 0) ? 0 : (nspositions.length - 1)) +
           ((nsspositions.length == 0) ? 0 : (nsspositions.length - 1)) +
           ((mnpositions.length == 0) ? 0 : (mnpositions.length - 1));
  }

  public int size() {
    return size;
  }

  public int getInt(int index) {
    if (index == 0) {
      return 0;
    }

    int pos = intpositions[index];
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

    int pos = uintpositions[index];
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

    int pos = doublepositions[index];
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

    int pos = strpositions[index];
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
    int pos = nspositions[index];
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

    int pos = nsspositions[index];
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

    int pos = mnpositions[index];
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

    int pos = mnpositions[index];
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

    int pos = mnpositions[index];
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

final class NSPool extends ByteArrayPool {
  NSPool() {
    super();
  }

  ByteArray newByteArray() {
    return new NS();
  }
}

final class NS extends ByteArray {
  int nsKind = 0;
  int index = 0;

  void init() {
    super.init();

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
    boolean equal = false;
    if (obj instanceof NS) {
      NS ns = (NS)obj;
      if (this.nsKind == CONSTANT_PrivateNamespace) {
        // Private namespaces are only equal if they are literally the same namespace,
        // the name is not important.
        equal = (this.data == ns.data) && (this.start == ns.start) && (this.end == ns.end);
      }
      else {
        equal = (ns.nsKind == this.nsKind) && (ns.index == this.index);
      }
    }
    return equal;
  }
}


final class NSSPool extends ByteArrayPool {
  NSSPool() {
    super();
  }

  ByteArray newByteArray() {
    return new NSS();
  }
}

final class NSS extends ByteArray {
  int[] set = null;
  int size = 0;

  void init() {
    super.init();

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

final class MultiNamePool extends ByteArrayPool {
  MultiNamePool() {
    super();
  }

  ByteArray newByteArray() {
    return new MN();
  }
}

final class MN extends ByteArray {
  int constKind = 0, index1 = 1, index2 = 1;

  void init() {
    super.init();

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
    if (obj instanceof MN) {
      MN mn = (MN)obj;

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
