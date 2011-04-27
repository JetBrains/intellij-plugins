package com.intellij.flex.uiDesigner.abc;

interface ActionBlockConstants {
  /*
  * Constant pool tags
  */
  byte CONSTANT_Utf8 = 0x01;
  byte CONSTANT_Integer = 0x03;
  byte CONSTANT_UInteger = 0x04;
  byte CONSTANT_PrivateNamespace = 0x05;
  byte CONSTANT_Double = 0x06;
  byte CONSTANT_Qname = 0x07;  // ns::name, const ns, const name
  byte CONSTANT_Namespace = 0x08;
  byte CONSTANT_Multiname = 0x09;    //[ns...]::name, const [ns...], const name
  byte CONSTANT_False = 0x0A;
  byte CONSTANT_True = 0x0B;
  byte CONSTANT_Null = 0x0C;
  byte CONSTANT_QnameA = 0x0D;    // @ns::name, const ns, const name
  byte CONSTANT_MultinameA = 0x0E;// @[ns...]::name, const [ns...], const name
  byte CONSTANT_RTQname = 0x0F;    // ns::name, var ns, const name
  byte CONSTANT_RTQnameA = 0x10;    // @ns::name, var ns, const name
  byte CONSTANT_RTQnameL = 0x11;    // ns::[name], var ns, var name
  byte CONSTANT_RTQnameLA = 0x12; // @ns::[name], var ns, var name
  byte CONSTANT_Namespace_Set = 0x15; // a set of namespaces - used by multiname
  byte CONSTANT_PackageNamespace = 0x16; // a namespace that was derived from a package
  byte CONSTANT_PackageInternalNs = 0x17; // a namespace that had no uri
  byte CONSTANT_ProtectedNamespace = 0x18;
  byte CONSTANT_ExplicitNamespace = 0x19;
  byte CONSTANT_StaticProtectedNs = 0x1A;
  byte CONSTANT_MultinameL = 0x1B;
  byte CONSTANT_MultinameLA = 0x1C;
  byte CONSTANT_TypeName = 0x1D;

  /*
  * Trait tags
  */

  int TRAIT_Var = 0x00;
  int TRAIT_Method = 0x01;
  int TRAIT_Getter = 0x02;
  int TRAIT_Setter = 0x03;
  int TRAIT_Class = 0x04;
  int TRAIT_Function = 0x05;
  int TRAIT_Const = 0x06;

  int TRAIT_FLAG_metadata = 0x04;

  // Class flags
  int CLASS_FLAG_protected = 0x08;

  // Method flags
  int METHOD_HasOptional = 0x8;
  int METHOD_HasParamNames = 0x80;

  /*
  * Opcodes
  */

  int OP_bkpt = 0x01;
  int OP_nop = 0x02;
  int OP_throw = 0x03;
  int OP_getsuper = 0x04;      // <mname>(obj) : val
  int OP_setsuper = 0x05;      // <mname>(obj,val) : void
  int OP_dxns = 0x06;
  int OP_dxnslate = 0x07;
  int OP_kill = 0x08;
  int OP_label = 0x09;

  int OP_ifnlt = 0x0c;
  int OP_ifnle = 0x0d;
  int OP_ifngt = 0x0e;
  int OP_ifnge = 0x0f;
  int OP_jump = 0x10;
  int OP_iftrue = 0x11;
  int OP_iffalse = 0x12;
  int OP_ifeq = 0x13;
  int OP_ifne = 0x14;
  int OP_iflt = 0x15;
  int OP_ifle = 0x16;
  int OP_ifgt = 0x17;
  int OP_ifge = 0x18;
  int OP_ifstricteq = 0x19;
  int OP_ifstrictne = 0x1a;
  int OP_lookupswitch = 0x1b;
  int OP_pushwith = 0x1c;
  int OP_popscope = 0x1d;
  int OP_nextname = 0x1e;
  int OP_hasnext = 0x1f;

  int OP_pushnull = 0x20;
  int OP_pushundefined = 0x21;
  int OP_pushuninitialized = 0x22;
  int OP_nextvalue = 0x23;
  int OP_pushbyte = 0x24;
  int OP_pushshort = 0x25;
  int OP_pushtrue = 0x26;
  int OP_pushfalse = 0x27;
  int OP_pushnan = 0x28;
  int OP_pop = 0x29;
  int OP_dup = 0x2a;
  int OP_swap = 0x2b;
  int OP_pushstring = 0x2c;
  int OP_pushint = 0x2d;
  int OP_pushuint = 0x2e;
  int OP_pushdouble = 0x2f;
  int OP_pushscope = 0x30;
  int OP_pushnamespace = 0x31;
  int OP_hasnext2 = 0x32;
  int OP_pushdnan = 0x34;

  int OP_li8 = 0x35;
  int OP_li16 = 0x36;
  int OP_li32 = 0x37;
  int OP_lf32 = 0x38;
  int OP_lf64 = 0x39;
  int OP_si8 = 0x3A;
  int OP_si16 = 0x3B;
  int OP_si32 = 0x3C;
  int OP_sf32 = 0x3D;
  int OP_sf64 = 0x3E;

  int OP_newfunction = 0x40;
  int OP_call = 0x41;
  int OP_construct = 0x42;
  int OP_callmethod = 0x43;
  int OP_callstatic = 0x44;
  int OP_callsuper = 0x45;
  int OP_callproperty = 0x46;
  int OP_returnvoid = 0x47;
  int OP_returnvalue = 0x48;
  int OP_constructsuper = 0x49;
  int OP_constructprop = 0x4A;
  int OP_callproplex = 0x4C;
  int OP_callsupervoid = 0x4E;
  int OP_callpropvoid = 0x4F;
  int OP_sxi1 = 0x50;
  int OP_sxi8 = 0x51;
  int OP_sxi16 = 0x52;
  int OP_applytype = 0x53;

  int OP_newobject = 0x55;
  int OP_newarray = 0x56;
  int OP_newactivation = 0x57;

  int OP_newclass = 0x58;
  int OP_getdescendants = 0x59;
  int OP_newcatch = 0x5a;
  int OP_deldescendants = 0x5b;

  int OP_findpropstrict = 0x5d;
  int OP_findproperty = 0x5e;
  int OP_finddef = 0x5f;
  int OP_getlex = 0x60;

  int OP_setproperty = 0x61;
  int OP_getlocal = 0x62;
  int OP_setlocal = 0x63;

  int OP_getglobalscope = 0x64;
  int OP_getscopeobject = 0x65;
  int OP_getproperty = 0x66;
  int OP_initproperty = 0x68;
  int OP_deleteproperty = 0x6a;
  int OP_getslot = 0x6c;
  int OP_setslot = 0x6d;

  /**
   * @deprecated use getglobalscope+getslot
   */
  int OP_getglobalslot = 0x6e;

  /**
   * @deprecated use getglobalscope+setslot
   */
  int OP_setglobalslot = 0x6f;


  int OP_convert_s = 0x70;
  int OP_esc_xelem = 0x71;
  int OP_esc_xattr = 0x72;
  int OP_convert_i = 0x73;
  int OP_convert_u = 0x74;
  int OP_convert_d = 0x75;
  int OP_convert_b = 0x76;
  int OP_convert_o = 0x77;
  int OP_checkfilter = 0x78;
  int OP_convert_m = 0x79;
  int OP_convert_m_p = 0x7a;

  int OP_coerce = 0x80;

  /**
   * @deprecated use OP_convert_b
   */
  int OP_coerce_b = 0x81;
  int OP_coerce_a = 0x82;
  /**
   * @deprecated use OP_convert_i
   */
  int OP_coerce_i = 0x83;
  /**
   * @deprecated use OP_convert_d
   */
  int OP_coerce_d = 0x84;
  int OP_coerce_s = 0x85;
  int OP_astype = 0x86;
  int OP_astypelate = 0x87;
  /**
   * @deprecated use OP_convert_u
   */
  int OP_coerce_u = 0x88;
  int OP_coerce_o = 0x89;

  int OP_negate_p = 0x8f;
  int OP_negate = 0x90;
  int OP_increment = 0x91;
  int OP_inclocal = 0x92;
  int OP_decrement = 0x93;
  int OP_declocal = 0x94;
  int OP_typeof = 0x95;
  int OP_not = 0x96;
  int OP_bitnot = 0x97;

  int OP_increment_p = 0x9c;
  int OP_inclocal_p = 0x9d;
  int OP_decrement_p = 0x9e;
  int OP_declocal_p = 0x9f;

  int OP_add = 0xa0;
  int OP_subtract = 0xa1;
  int OP_multiply = 0xa2;
  int OP_divide = 0xa3;
  int OP_modulo = 0xa4;
  int OP_lshift = 0xa5;
  int OP_rshift = 0xa6;
  int OP_urshift = 0xa7;
  int OP_bitand = 0xa8;
  int OP_bitor = 0xa9;
  int OP_bitxor = 0xaa;
  int OP_equals = 0xab;
  int OP_strictequals = 0xac;
  int OP_lessthan = 0xad;
  int OP_lessequals = 0xae;
  int OP_greaterthan = 0xaf;

  int OP_greaterequals = 0xb0;
  int OP_instanceof = 0xb1;
  int OP_istype = 0xb2;
  int OP_istypelate = 0xb3;
  int OP_in = 0xb4;
  // arithmetic with decimal parameters
  int OP_add_p = 0xb5;
  int OP_subtract_p = 0xb6;
  int OP_multiply_p = 0xb7;
  int OP_divide_p = 0xb8;
  int OP_modulo_p = 0xb9;

  int OP_increment_i = 0xc0;
  int OP_decrement_i = 0xc1;
  int OP_inclocal_i = 0xc2;
  int OP_declocal_i = 0xc3;
  int OP_negate_i = 0xc4;
  int OP_add_i = 0xc5;
  int OP_subtract_i = 0xc6;
  int OP_multiply_i = 0xc7;

  int OP_getlocal0 = 0xd0;
  int OP_getlocal1 = 0xd1;
  int OP_getlocal2 = 0xd2;
  int OP_getlocal3 = 0xd3;
  int OP_setlocal0 = 0xd4;
  int OP_setlocal1 = 0xd5;
  int OP_setlocal2 = 0xd6;
  int OP_setlocal3 = 0xd7;

  int OP_debug = 0xef;

  int OP_debugline = 0xf0;
  int OP_debugfile = 0xf1;
  int OP_bkptline = 0xf2;
  int OP_timestamp = 0xf3;
}
