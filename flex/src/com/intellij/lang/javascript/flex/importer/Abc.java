// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.importer;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import gnu.trove.THashSet;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Maxim.Mossienko
 */
class Abc {
  static final @NonNls String TAB = "  ";
  @NonNls static final String $CINIT = "$cinit";
  @NonNls static final String $ = "$";

  private static final int ATTR_final = 0x01; // 1=final, 0=virtual
  private static final int ATTR_override = 0x02; // 1=override, 0=new
  private static final int ATTR_metadata = 0x04; // 1=has metadata, 0=no metadata
  private static final int ATTR_public = 0x08; // 1=add public namespace

  static final int CLASS_FLAG_sealed = 0x01;
  static final int CLASS_FLAG_final = 0x02;
  static final int CLASS_FLAG_interface = 0x04;
  static final int CLASS_FLAG_protected = 0x08;


  // method flags
  private static final int NEED_ARGUMENTS = 0x01;
  static final int NEED_ACTIVATION = 0x02;
  private static final int NEED_REST = 0x04;
  private static final int HAS_OPTIONAL = 0x08;
  private static final int IGNORE_REST = 0x10;
  static final int NATIVE = 0x20;
  private static final int HAS_ParamNames = 0x80;

  private static final int CONSTANT_Utf8 = 0x01;
  private static final int CONSTANT_Int = 0x03;
  private static final int CONSTANT_UInt = 0x04;
  private static final int CONSTANT_PrivateNs = 0x05; // non-shared namespace
  private static final int CONSTANT_Double = 0x06;
  private static final int CONSTANT_Qname = 0x07; // o.ns::name, ct ns, ct name
  private static final int CONSTANT_Namespace = 0x08;
  private static final int CONSTANT_Multiname = 0x09; // o.name, ct nsset, ct name
  private static final int CONSTANT_False = 0x0A;
  private static final int CONSTANT_True = 0x0B;
  private static final int CONSTANT_Null = 0x0C;
  private static final int CONSTANT_QnameA = 0x0D; // o.@ns::name, ct ns, ct attr-name
  private static final int CONSTANT_MultinameA = 0x0E; // o.@name, ct attr-name
  private static final int CONSTANT_RTQname = 0x0F; // o.ns::name, rt ns, ct name
  private static final int CONSTANT_RTQnameA = 0x10; // o.@ns::name, rt ns, ct attr-name
  private static final int CONSTANT_RTQnameL = 0x11; // o.ns::[name], rt ns, rt name
  private static final int CONSTANT_RTQnameLA = 0x12; // o.@ns::[name], rt ns, rt attr-name
  private static final int CONSTANT_NameL = 0x13;    // o.[], ns=public implied, rt name
  private static final int CONSTANT_NameLA = 0x14; // o.@[], ns=public implied, rt attr-name
  private static final int CONSTANT_NamespaceSet = 0x15;
  private static final int CONSTANT_PackageNs = 0x16;
  private static final int CONSTANT_PackageInternalNs = 0x17;
  private static final int CONSTANT_ProtectedNs = 0x18;
  private static final int CONSTANT_StaticProtectedNs = 0x19;
  private static final int CONSTANT_StaticProtectedNs2 = 0x1a;
  private static final int CONSTANT_MultinameL = 0x1B;
  private static final int CONSTANT_MultinameLA = 0x1C;
  private static final int CONSTANT_TypeName = 0x1D;

  static final @NonNls String[] constantKinds =
    {"0", "utf8", "2", "int", "uint", "private", "double", "qname", "namespace", "multiname", "false", "true", "null", "@qname",
      "@multiname", "rtqname", "@rtqname", "[qname]", "@[qname]", "[name]", "@[name]", "nsset"};
  protected static final String SCRIPT_PREFIX = "script";

  enum TraitType {
    Slot, Method, Getter, Setter, Class, Function, Const
  }

  static final @NonNls String[] traitKinds = {"var", "function", "function get", "function set", "class", "function", "const"};

  final static @NonNls String PUBLIC_NS = "";
  static final String PRIVATE_NS = "private";
  final static @NonNls String anyNs = "*";
  static final String[] PUBLIC_NS_SET = new String[]{PUBLIC_NS};
  static final String[] PRIVATE_NS_SET = new String[]{PRIVATE_NS};

  private static final int OP_bkpt = 0x01;
  private static final int OP_nop = 0x02;
  private static final int OP_throw = 0x03;
  static final int OP_getsuper = 0x04;
  static final int OP_setsuper = 0x05;
  private static final int OP_dxns = 0x06;
  private static final int OP_dxnslate = 0x07;
  static final int OP_kill = 0x08;
  static final int OP_label = 0x09;
  static final int OP_ifnlt = 0x0C;
  static final int OP_ifnle = 0x0D;
  static final int OP_ifngt = 0x0E;
  static final int OP_ifnge = 0x0F;
  static final int OP_jump = 0x10;
  static final int OP_iftrue = 0x11;
  static final int OP_iffalse = 0x12;
  static final int OP_ifeq = 0x13;
  static final int OP_ifne = 0x14;
  static final int OP_iflt = 0x15;
  static final int OP_ifle = 0x16;
  static final int OP_ifgt = 0x17;
  static final int OP_ifge = 0x18;
  static final int OP_ifstricteq = 0x19;
  static final int OP_ifstrictne = 0x1A;
  static final int OP_lookupswitch = 0x1B;
  private static final int OP_pushwith = 0x1C;
  private static final int OP_popscope = 0x1D;
  private static final int OP_nextname = 0x1E;
  private static final int OP_hasnext = 0x1F;
  private static final int OP_pushnull = 0x20;
  private static final int OP_pushundefined = 0x21;
  private static final int OP_pushconstant = 0x22;
  private static final int OP_nextvalue = 0x23;
  static final int OP_pushbyte = 0x24;
  static final int OP_pushshort = 0x25;
  private static final int OP_pushtrue = 0x26;
  private static final int OP_pushfalse = 0x27;
  private static final int OP_pushnan = 0x28;
  private static final int OP_pop = 0x29;
  private static final int OP_dup = 0x2A;
  private static final int OP_swap = 0x2B;
  static final int OP_pushstring = 0x2C;
  static final int OP_pushint = 0x2D;
  static final int OP_pushuint = 0x2E;
  static final int OP_pushdouble = 0x2F;
  private static final int OP_pushscope = 0x30;
  static final int OP_pushnamespace = 0x31;
  static final int OP_hasnext2 = 0x32;
  static final int OP_newfunction = 0x40;
  static final int OP_call = 0x41;
  static final int OP_construct = 0x42;
  private static final int OP_callmethod = 0x43;
  static final int OP_callstatic = 0x44;
  static final int OP_callsuper = 0x45;
  static final int OP_callproperty = 0x46;
  private static final int OP_returnvoid = 0x47;
  private static final int OP_returnvalue = 0x48;
  static final int OP_constructsuper = 0x49;
  static final int OP_constructprop = 0x4A;
  private static final int OP_callsuperid = 0x4B;
  static final int OP_callproplex = 0x4C;
  private static final int OP_callinterface = 0x4D;
  static final int OP_callsupervoid = 0x4E;
  static final int OP_callpropvoid = 0x4F;
  private static final int OP_applytype = 0x53;
  static final int OP_newobject = 0x55;
  static final int OP_newarray = 0x56;
  private static final int OP_newactivation = 0x57;
  static final int OP_newclass = 0x58;
  static final int OP_getdescendants = 0x59;
  static final int OP_newcatch = 0x5A;
  static final int OP_findpropstrict = 0x5D;
  static final int OP_findproperty = 0x5E;
  static final int OP_finddef = 0x5F;
  static final int OP_getlex = 0x60;
  static final int OP_setproperty = 0x61;
  static final int OP_getlocal = 0x62;
  static final int OP_setlocal = 0x63;
  private static final int OP_getglobalscope = 0x64;
  static final int OP_getscopeobject = 0x65;
  static final int OP_getproperty = 0x66;
  private static final int OP_getouterscope = 0x67;
  static final int OP_initproperty = 0x68;
  private static final int OP_setpropertylate = 0x69;
  static final int OP_deleteproperty = 0x6A;
  private static final int OP_deletepropertylate = 0x6B;
  static final int OP_getslot = 0x6C;
  static final int OP_setslot = 0x6D;
  static final int OP_getglobalslot = 0x6E;
  static final int OP_setglobalslot = 0x6F;
  private static final int OP_convert_s = 0x70;
  private static final int OP_esc_xelem = 0x71;
  private static final int OP_esc_xattr = 0x72;
  private static final int OP_convert_i = 0x73;
  private static final int OP_convert_u = 0x74;
  private static final int OP_convert_d = 0x75;
  private static final int OP_convert_b = 0x76;
  private static final int OP_convert_o = 0x77;
  static final int OP_coerce = 0x80;
  private static final int OP_coerce_b = 0x81;
  private static final int OP_coerce_a = 0x82;
  private static final int OP_coerce_i = 0x83;
  private static final int OP_coerce_d = 0x84;
  private static final int OP_coerce_s = 0x85;
  static final int OP_astype = 0x86;
  private static final int OP_astypelate = 0x87;
  private static final int OP_coerce_u = 0x88;
  private static final int OP_coerce_o = 0x89;
  private static final int OP_negate = 0x90;
  private static final int OP_increment = 0x91;
  static final int OP_inclocal = 0x92;
  private static final int OP_decrement = 0x93;
  static final int OP_declocal = 0x94;
  private static final int OP_typeof = 0x95;
  private static final int OP_not = 0x96;
  private static final int OP_bitnot = 0x97;
  private static final int OP_concat = 0x9A;
  private static final int OP_add_d = 0x9B;
  private static final int OP_add = 0xA0;
  private static final int OP_subtract = 0xA1;
  private static final int OP_multiply = 0xA2;
  private static final int OP_divide = 0xA3;
  private static final int OP_modulo = 0xA4;
  private static final int OP_lshift = 0xA5;
  private static final int OP_rshift = 0xA6;
  private static final int OP_urshift = 0xA7;
  private static final int OP_bitand = 0xA8;
  private static final int OP_bitor = 0xA9;
  private static final int OP_bitxor = 0xAA;
  private static final int OP_equals = 0xAB;
  private static final int OP_strictequals = 0xAC;
  private static final int OP_lessthan = 0xAD;
  private static final int OP_lessequals = 0xAE;
  private static final int OP_greaterthan = 0xAF;
  private static final int OP_greaterequals = 0xB0;
  private static final int OP_instanceof = 0xB1;
  static final int OP_istype = 0xB2;
  private static final int OP_istypelate = 0xB3;
  private static final int OP_in = 0xB4;
  private static final int OP_increment_i = 0xC0;
  private static final int OP_decrement_i = 0xC1;
  static final int OP_inclocal_i = 0xC2;
  static final int OP_declocal_i = 0xC3;
  private static final int OP_negate_i = 0xC4;
  private static final int OP_add_i = 0xC5;
  private static final int OP_subtract_i = 0xC6;
  private static final int OP_multiply_i = 0xC7;
  private static final int OP_getlocal0 = 0xD0;
  private static final int OP_getlocal1 = 0xD1;
  private static final int OP_getlocal2 = 0xD2;
  private static final int OP_getlocal3 = 0xD3;
  private static final int OP_setlocal0 = 0xD4;
  private static final int OP_setlocal1 = 0xD5;
  private static final int OP_setlocal2 = 0xD6;
  private static final int OP_setlocal3 = 0xD7;
  static final int OP_debug = 0xEF;
  static final int OP_debugline = 0xF0;
  static final int OP_debugfile = 0xF1;
  private static final int OP_bkptline = 0xF2;

  static final @NonNls String[] opNames =
    {"OP_0x00       ", "bkpt          ", "nop           ", "throw         ", "getsuper      ", "setsuper      ", "dxns          ",
      "dxnslate      ", "kill          ", "label         ", "OP_0x0A       ", "OP_0x0B       ", "ifnlt         ", "ifnle         ",
      "ifngt         ", "ifnge         ", "jump          ", "iftrue        ", "iffalse       ", "ifeq          ", "ifne          ",
      "iflt          ", "ifle          ", "ifgt          ", "ifge          ", "ifstricteq    ", "ifstrictne    ", "lookupswitch  ",
      "pushwith      ", "popscope      ", "nextname      ", "hasnext       ", "pushnull      ", "pushundefined ", "pushconstant  ",
      "nextvalue     ", "pushbyte      ", "pushshort     ", "pushtrue      ", "pushfalse     ", "pushnan       ", "pop           ",
      "dup           ", "swap          ", "pushstring    ", "pushint       ", "pushuint      ", "pushdouble    ", "pushscope     ",
      "pushnamespace ", "hasnext2      ", "OP_0x33       ", "OP_0x34       ", "OP_0x35       ", "OP_0x36       ", "OP_0x37       ",
      "OP_0x38       ", "OP_0x39       ", "OP_0x3A       ", "OP_0x3B       ", "OP_0x3C       ", "OP_0x3D       ", "OP_0x3E       ",
      "OP_0x3F       ", "newfunction   ", "call          ", "construct     ", "callmethod    ", "callstatic    ", "callsuper     ",
      "callproperty  ", "returnvoid    ", "returnvalue   ", "constructsuper", "constructprop ", "callsuperid   ", "callproplex   ",
      "callinterface ", "callsupervoid ", "callpropvoid  ", "OP_0x50       ", "OP_0x51       ", "OP_0x52       ", "applytype     ",
      "OP_0x54       ", "newobject     ", "newarray      ", "newactivation ", "newclass      ", "getdescendants", "newcatch      ",
      "OP_0x5B       ", "OP_0x5C       ", "findpropstrict", "findproperty  ", "finddef       ", "getlex        ", "setproperty   ",
      "getlocal      ", "setlocal      ", "getglobalscope", "getscopeobject", "getproperty   ", "getouterscope ", "initproperty  ",
      "OP_0x69       ", "deleteproperty", "OP_0x6A       ", "getslot       ", "setslot       ", "getglobalslot ", "setglobalslot ",
      "convert_s     ", "esc_xelem     ", "esc_xattr     ", "convert_i     ", "convert_u     ", "convert_d     ", "convert_b     ",
      "convert_o     ", "checkfilter   ", "OP_0x79       ", "OP_0x7A       ", "OP_0x7B       ", "OP_0x7C       ", "OP_0x7D       ",
      "OP_0x7E       ", "OP_0x7F       ", "coerce        ", "coerce_b      ", "coerce_a      ", "coerce_i      ", "coerce_d      ",
      "coerce_s      ", "astype        ", "astypelate    ", "coerce_u      ", "coerce_o      ", "OP_0x8A       ", "OP_0x8B       ",
      "OP_0x8C       ", "OP_0x8D       ", "OP_0x8E       ", "OP_0x8F       ", "negate        ", "increment     ", "inclocal      ",
      "decrement     ", "declocal      ", "typeof        ", "not           ", "bitnot        ", "OP_0x98       ", "OP_0x99       ",
      "concat        ", "add_d         ", "OP_0x9C       ", "OP_0x9D       ", "OP_0x9E       ", "OP_0x9F       ", "add           ",
      "subtract      ", "multiply      ", "divide        ", "modulo        ", "lshift        ", "rshift        ", "urshift       ",
      "bitand        ", "bitor         ", "bitxor        ", "equals        ", "strictequals  ", "lessthan      ", "lessequals    ",
      "greaterthan   ", "greaterequals ", "instanceof    ", "istype        ", "istypelate    ", "in            ", "OP_0xB5       ",
      "OP_0xB6       ", "OP_0xB7       ", "OP_0xB8       ", "OP_0xB9       ", "OP_0xBA       ", "OP_0xBB       ", "OP_0xBC       ",
      "OP_0xBD       ", "OP_0xBE       ", "OP_0xBF       ", "increment_i   ", "decrement_i   ", "inclocal_i    ", "declocal_i    ",
      "negate_i      ", "add_i         ", "subtract_i    ", "multiply_i    ", "OP_0xC8       ", "OP_0xC9       ", "OP_0xCA       ",
      "OP_0xCB       ", "OP_0xCC       ", "OP_0xCD       ", "OP_0xCE       ", "OP_0xCF       ", "getlocal0     ", "getlocal1     ",
      "getlocal2     ", "getlocal3     ", "setlocal0     ", "setlocal1     ", "setlocal2     ", "setlocal3     ", "OP_0xD8       ",
      "OP_0xD9       ", "OP_0xDA       ", "OP_0xDB       ", "OP_0xDC       ", "OP_0xDD       ", "OP_0xDE       ", "OP_0xDF       ",
      "OP_0xE0       ", "OP_0xE1       ", "OP_0xE2       ", "OP_0xE3       ", "OP_0xE4       ", "OP_0xE5       ", "OP_0xE6       ",
      "OP_0xE7       ", "OP_0xE8       ", "OP_0xE9       ", "OP_0xEA       ", "OP_0xEB       ", "OP_0xEC       ", "OP_0xED       ",
      "OP_0xEE       ", "debug         ", "debugline     ", "debugfile     ", "bkptline      ", "timestamp     ", "OP_0xF4       ",
      "verifypass    ", "alloc         ", "mark          ", "wb            ", "prologue      ", "sendenter     ", "doubletoatom  ",
      "sweep         ", "codegenop     ", "verifyop      ", "decode        "};
  static final Multiname OpaqueAssetsType = new Multiname(null, "Class");

  private final FlexByteCodeInformationProcessor processor;
  int totalSize;
  final int opSizes[] = new int[256];

  Abc(final @NotNull ByteBuffer _data, @NotNull FlexByteCodeInformationProcessor _processor) {
    data = _data;
    processor = _processor;

    data.setPosition(0);
    magic = data.readInt();

    _processor.dumpStat("magic " + Integer.toString(magic, 16) + "\n");

    if (magic != (46 << 16 | 14) && magic != (46 << 16 | 15) && magic != (46 << 16 | 16)) {
      throw new Error("not an abc file.  magic=" + Integer.toString(magic, 16));
    }

    parseCpool();

    defaults[CONSTANT_Utf8] = strings;
    defaults[CONSTANT_Int] = ints;
    defaults[CONSTANT_UInt] = uints;
    defaults[CONSTANT_Double] = doubles;
    defaults[CONSTANT_False] = buildSparseArray(10, "false");
    defaults[CONSTANT_True] = buildSparseArray(11, "true");
    defaults[CONSTANT_Namespace] = namespaces;
    defaults[CONSTANT_PrivateNs] = namespaces;
    defaults[CONSTANT_PackageNs] = namespaces;
    defaults[CONSTANT_PackageInternalNs] = namespaces;
    defaults[CONSTANT_ProtectedNs] = namespaces;
    defaults[CONSTANT_StaticProtectedNs] = namespaces;
    defaults[CONSTANT_StaticProtectedNs2] = namespaces;
    defaults[CONSTANT_Null] = buildSparseArray(12, "null");

    parseMethodInfos();
    parseMetadataInfos();
    parseInstanceInfos();
    parseClassInfos();
    parseScriptInfos();
    parseMethodBodies();
  }

  private static Object[] buildSparseArray(int index, @NonNls String s1) {
    final Object[] result = new Object[index + 1];
    result[index] = s1;
    return result;
  }

  public void dump(String indent) {
    for (Traits t : scripts) {
      processor.dumpTopLevelTraits(this, t, indent);
    }

    for (MethodInfo m : methods) {
      if (m.anon) {
        processor.dumpToplevelAnonymousMethod(this, m);
      }
    }

    processor.dumpStat("OPCODE\tSIZE\t% OF " + totalSize + "\n");
    final Set<Integer> done = new HashSet<>();
    while (true) {
      int max = -1;
      int maxsize = 0;
      for (int i = 0; i < 256; i++) {
        if (opSizes[i] > maxsize && !done.contains(i)) {
          max = i;
          maxsize = opSizes[i];
        }
      }
      if (max == -1) break;
      done.add(max);
      processor.dumpStat(opNames[max] + "\t" + opSizes[max] + "\t" + (int)(100f * opSizes[max] / totalSize) + "%\n");
    }
  }

  private final ByteBuffer data;

  Integer[] ints;
  Integer[] uints;
  Double[] doubles;
  @NonNls String[] strings;
  @NonNls String[] namespaces;
  @NonNls String[][] nssets;
  Multiname[] names;

  final Object[][] defaults = new Object[Math.max(constantKinds.length, CONSTANT_MultinameLA + 1)][];

  MethodInfo methods[];
  Traits instances[];
  Traits classes[];
  Traits[] scripts;
  MetaData metadata[];

  final int magic;

  int readU32() {
    return data.readU32();
  }

  void parseCpool() {
    int i, j;
    int n;
    int start = data.getPosition();

    // ints
    n = readU32();
    ints = new Integer[n > 0 ? n : 1];
    ints[0] = 0;
    for (i = 1; i < n; i++) {
      ints[i] = readU32();
    }

    // uints
    n = readU32();
    uints = new Integer[n > 0 ? n : 1];
    uints[0] = 0;
    for (i = 1; i < n; i++) {
      uints[i] = readU32();
    }

    // doubles
    n = readU32();
    doubles = new Double[n > 0 ? n : 1];
    doubles[0] = Double.NaN;
    for (i = 1; i < n; i++) {
      doubles[i] = data.readDouble();
    }

    reportAboutPercentage("Cpool numbers size ", data, start, processor);
    start = data.getPosition();

    // strings
    n = readU32();
    strings = new String[n];
    strings[0] = "";
    for (i = 1; i < n; i++) {
      strings[i] = data.readUTFBytes(readU32());
    }

    reportAboutPercentage("Cpool strings count " + n + " size ", data, start, processor);
    start = data.getPosition();

    // namespaces
    n = readU32();
    namespaces = new String[n];
    namespaces[0] = PUBLIC_NS;
    for (i = 1; i < n; i++) {
      switch (data.readByte()) {
        case CONSTANT_Namespace:
        case CONSTANT_PackageNs:
        case CONSTANT_PackageInternalNs:
        case CONSTANT_ProtectedNs:
        case CONSTANT_StaticProtectedNs:
        case CONSTANT_StaticProtectedNs2: {
          namespaces[i] = strings[readU32()];
          // todo mark kind of namespace.
          break;
        }
        case CONSTANT_PrivateNs:
          readU32();
          namespaces[i] = "private";
          break;
      }
    }

    reportAboutPercentage("Cpool namespaces count " + n + " size ", data, start, processor);
    start = data.getPosition();

    // namespace sets
    n = readU32();
    nssets = new String[n][];
    for (i = 1; i < n; i++) {
      int count = readU32();
      String[] nsset = nssets[i] = new String[count];
      for (j = 0; j < count; j++) {
        nsset[j] = namespaces[readU32()];
      }
    }

    reportAboutPercentage("Cpool nssets count " + n + " size ", data, start, processor);
    start = data.getPosition();

    // multinames
    n = readU32();
    names = new Multiname[n];
    namespaces[0] = anyNs;
    strings[0] = "*"; // any name

    class TypeNameInfo {
      int index;
      int base;
      TIntArrayList genericIndices;
    }

    final List<TypeNameInfo> typeNameInfos = new ArrayList<>();

    for (i = 1; i < n; i++) {
      switch (data.readByte()) {
        case CONSTANT_Qname:
        case CONSTANT_QnameA:
          names[i] = new Multiname(new String[]{namespaces[readU32()]}, strings[readU32()]);
          break;

        case CONSTANT_RTQname:
        case CONSTANT_RTQnameA:
          names[i] = new Multiname(new String[]{strings[readU32()]}, null);
          break;

        case CONSTANT_RTQnameL:
        case CONSTANT_RTQnameLA:
          names[i] = null;
          break;

        case CONSTANT_NameL:
        case CONSTANT_NameLA:
          names[i] = new Multiname(PUBLIC_NS_SET, null);
          break;

        case CONSTANT_Multiname:
        case CONSTANT_MultinameA:
          String name = strings[readU32()];
          names[i] = new Multiname(nssets[readU32()], name);
          break;

        case CONSTANT_MultinameL:
        case CONSTANT_MultinameLA:
          names[i] = new Multiname(nssets[readU32()], null);
          break;

        case CONSTANT_TypeName:
          // TODO:
          int nameId = readU32();
          final TypeNameInfo e = new TypeNameInfo();
          typeNameInfos.add(e);
          e.index = i;
          e.base = nameId;

          int count = readU32();
          if (count > 0) e.genericIndices = new TIntArrayList();

          if (count > 0) {
            for (int k = 0; k < count; k++) {
              nameId = readU32();
              e.genericIndices.add(nameId);
            }
          }
          break;

        default:
          throw new Error("invalid kind " + data.getByte(data.getPosition() - 1));
      }
    }

    boolean hasSomething = false;
    boolean doneSomething = false;

    do {
      NextType:
      for (TypeNameInfo tni : typeNameInfos) {
        if (names[tni.index] != null) continue;
        if (names[tni.base] == null) {
          hasSomething = true;
          continue;
        }

        String nsName = names[tni.base].toString();

        if (tni.genericIndices != null) {
          nsName += ".<";
          for (int k = 0; k < tni.genericIndices.size(); k++) {
            if (k != 0) nsName += ",";

            final Multiname typeArgName = names[tni.genericIndices.get(k)];
            if (typeArgName == null) continue NextType;
            String typeArgNameString;

            if (processor instanceof AS3InterfaceDumper) {
              boolean hasNotEmptyNs = typeArgName.hasNotEmptyNs();
              final boolean vector = hasNotEmptyNs && typeArgName.nsset[0].equals("__AS3__.vec");
              final boolean isPrivate = hasNotEmptyNs && typeArgName.nsset[0].equals("private");
              typeArgNameString = vector || isPrivate ? typeArgName.name : typeArgName.toString();
              typeArgNameString = StringUtil.replace(typeArgNameString, "::", ".") + (vector ? " " : "");
            }
            else {
              typeArgNameString = typeArgName.toString();
            }
            nsName += typeArgNameString;
          }

          nsName += ">";
        }

        final int index = nsName.indexOf("::");

        names[tni.index] =
          new Multiname(index != -1 ? new String[]{nsName.substring(0, index)}:PUBLIC_NS_SET, index != -1 ? nsName.substring(index + 2) : nsName);
        doneSomething = true;
      }
    } while (hasSomething && doneSomething);

    reportAboutPercentage("Cpool names count " + n + " size ", data, start, processor);
    start = data.getPosition();

    namespaces[0] = PUBLIC_NS;
    strings[0] = "*";
  }

  void parseMethodInfos() {
    int start = data.getPosition();
    names[0] = new Multiname(PUBLIC_NS_SET, "*");
    int method_count = readU32();
    methods = new MethodInfo[method_count];

    for (int i = 0; i < method_count; i++) {
      MethodInfo m = methods[i] = new MethodInfo();
      int param_count = readU32();
      m.returnType = names[readU32()];
      if (m.returnType == null) m.returnType = OpaqueAssetsType;

      m.paramTypes = new Multiname[param_count];
      for (int j = 0; j < param_count; j++) {
        m.paramTypes[j] = names[readU32()];
        if (m.paramTypes[j] == null) m.paramTypes[j] = OpaqueAssetsType;
      }
      m.debugName = strings[readU32()];
      m.flags = data.readByte();

      if ((m.flags & HAS_OPTIONAL) != 0) {
        // has_optional
        int optional_count = readU32();
        m.optionalValues = new Multiname[param_count];
        for (int k = param_count - optional_count; k < param_count; ++k) {
          int index = readU32();    // optional value index
          int kind = data.readByte(); // kind byte for each default value

          if (index == 0) {
            // kind is ignored, default value is based on type
            @NonNls String value;
            final @NonNls String type = m.paramTypes[k].toString();

            if ("Number".equals(type) || "decimal".equals(type)) {
              value = "0";
            }
            else if ("*".equals(type)) {
              value = "null";
            }
            else if ("String".equals(type)) {
              value = "";
            }
            else {
              value = "null";
            }
            m.optionalValues[k] = new Multiname(null, value);
          }
          else {
            if (defaults[kind] == null) {
              processor.hasError("ERROR kind=" + kind + " method_id " + i + "\n");
            }
            else {
              m.optionalValues[k] = new Multiname(null, defaults[kind][index].toString());
            }
          }
        }
      }
      if ((m.flags & HAS_ParamNames) != 0) {
        if (param_count > 0) {
          m.paramNames = new String[param_count];
          Set<String> usedNames = new THashSet<>(m.paramNames.length);
          for (int k = 0; k < param_count; ++k) {
            final int index = readU32();
            final String name = strings[index];
            m.paramNames[k] = StringUtil.isJavaIdentifier(name) && usedNames.add(name) ? name : "_" + k;
          }
        } else {
          m.paramNames = ArrayUtilRt.EMPTY_STRING_ARRAY;
        }
      }

      if ((m.flags & NEED_REST) != 0) {
        m.paramTypes = ArrayUtil.append(m.paramTypes, new Multiname(null, "..."));

        if (m.paramNames != null) {
          boolean hasRestName = false;
          for(String s:m.paramNames) {
            if ("rest".equals(s)) {
              hasRestName = true;
              break;
            }
          }
          String element = hasRestName ? "__rest":"rest";
          m.paramNames = ArrayUtil.append(m.paramNames, element);
        }
      }
    }

    reportAboutPercentage("MethodInfo count " + method_count + " size ", data, start, processor);
  }

  void parseMetadataInfos() {
    int count = readU32();
    metadata = new MetaData[count];
    for (int i = 0; i < count; i++) {
      // MetadataInfo
      MetaData m = metadata[i] = new MetaData();
      m.name = strings[readU32()];
      int values_count = readU32();
      String names[] = new String[values_count];

      for (int q = 0; q < values_count; ++q) {
        names[q] = strings[readU32()]; // name
      }
      for (int q = 0; q < values_count; ++q) {
        m.put(names[q], strings[readU32()]); // value
      }
    }
  }

  void parseInstanceInfos() {
    int start = data.getPosition();
    int count = readU32();
    instances = new Traits[count];
    for (int i = 0; i < count; i++) {
      Traits t = instances[i] = new Traits();
      t.name = names[readU32()];
      t.base = names[readU32()];
      t.flags = data.readByte();

      if ((t.flags & CLASS_FLAG_protected) != 0) t.protectedNs = namespaces[readU32()];

      int interface_count = readU32();
      t.interfaces = new Multiname[interface_count];
      for (int j = 0; j < interface_count; j++) {
        t.interfaces[j] = names[readU32()];
      }
      MethodInfo m = t.init = methods[readU32()];
      m.name = ((Multiname)t.name);
      m.kind = TraitType.Method;
      m.id = -1;
      m.parentTraits = t;
      parseTraits(t);
    }

    reportAboutPercentage("InstanceInfo size ", data, start, processor);
  }

  static final TraitType[] traitTypes = TraitType.values();

  void parseTraits(Traits t) {
    int namecount = readU32();
    for (int i = 0; i < namecount; i++) {
      Multiname name = names[readU32()];
      int tag = data.readByte();
      int traitTypeTag = tag & 0xf;
      TraitType kind;

      if (traitTypeTag >= traitTypes.length) {
        processor.hasError("error trait kind " + traitTypeTag + "\n");
        kind = null;
      }
      else {
        kind = traitTypes[traitTypeTag];
      }

      MemberInfo member = null;

      switch (kind) {
        case Slot:
        case Const:
        case Class:
          SlotInfo slot = new SlotInfo(name, kind);
          member = slot;
          slot.id = readU32();
          t.slots.put(slot.id, slot);
          if (kind == TraitType.Slot || kind == TraitType.Const) {
            int typeIndex = readU32();
            slot.type = names[typeIndex];
            if (slot.type == null) {
              slot.type = OpaqueAssetsType;
            }
            int index = readU32();
            if (index > 0) slot.value = defaults[data.readByte()][index];
          }
          else // (kind == Class)
          {
            slot.value = classes[readU32()];
          }
          break;
        case Method:
        case Getter:
        case Setter:
          int disp_id = readU32();
          MethodInfo method = methods[readU32()];
          member = method;
          t.methods.put(disp_id, method);
          method.id = disp_id;
          member.kind = kind;
          member.name = name;
          break;
      }

      t.members.put(i, member);
      t.names.put(name.toString(), member);
      member.parentTraits = t;

      final int val = tag >> 4;
      if ((val & ATTR_metadata) != 0) {
        int mdCount = readU32();
        member.metadata = new MetaData[mdCount];
        for (int j = 0; j < mdCount; ++j) {
          member.metadata[j] = metadata[readU32()];
        }
      }

      if ((val & ATTR_final) != 0) {
        member.isFinal = true;
      }

      if ((val & ATTR_public) != 0) {
        member.isPublic = true;
      }

      if ((val & ATTR_override) != 0) {
        member.isOverride = true;
      }
    }
  }

  void parseClassInfos() {
    int start = data.getPosition();
    int count = instances.length;
    classes = new Traits[count];
    for (int i = 0; i < count; i++) {
      Traits t = classes[i] = new Traits();
      t.init = methods[readU32()];
      t.base = new Multiname(null, "Class");
      t.itraits = instances[i];
      instances[i].staticTrait = t;
      t.name = t.itraits.name + $;
      t.init.parentTraits = t;
      t.init.name = new Multiname(null, t.itraits.name + $CINIT);
      t.init.kind = TraitType.Method;
      parseTraits(t);
    }
    reportAboutPercentage("ClassInfo size ", data, start, processor);
  }

  void parseScriptInfos() {
    int start = data.getPosition();
    int count = readU32();
    scripts = new Traits[count];

    for (int i = 0; i < count; i++) {
      Traits t = new Traits();
      scripts[i] = t;
      t.name = SCRIPT_PREFIX + i;
      t.base = names[0]; // Object
      t.init = methods[readU32()];
      t.init.name = new Multiname(null, t.name + "$init");
      t.init.kind = TraitType.Method;
      t.init.parentTraits = t;
      parseTraits(t);
    }

    reportAboutPercentage("ScriptInfo size ", data, start, processor);
  }

  void parseMethodBodies() {
    int start = data.getPosition();
    int count = readU32();

    for (int i = 0; i < count; i++) {
      MethodInfo m = methods[readU32()];
      m.max_stack = readU32();
      m.local_count = readU32();
      int initScopeDepth = readU32();
      int maxScopeDepth = readU32();
      m.max_scope = maxScopeDepth - initScopeDepth;
      int code_length = readU32();
      m.code = new ByteBuffer();
      m.code.setLittleEndian();

      if (code_length > 0) data.readBytes(m.code, code_length);

      int ex_count = readU32();
      for (int j = 0; j < ex_count; j++) {
        int from = readU32();
        int to = readU32();
        int target = readU32();
        Multiname type = names[readU32()];
        //sb.append("magic " + magic.toString(16))
        //if (magic >= (46<<16|16))
        Multiname name = names[readU32()];
      }
      parseTraits(m.activation = new Traits());
    }

    reportAboutPercentage("MethodBodies size ", data, start, processor);
  }

  private static void reportAboutPercentage(String s, ByteBuffer data, int start, @NotNull FlexByteCodeInformationProcessor processor) {
    processor.dumpStat(s + (data.getPosition() - start) + " " + (int)100f * (data.getPosition() - start) / data.bytesSize() + " %\n");
  }

}
