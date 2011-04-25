package com.intellij.flex.uiDesigner.abc;

import gnu.trove.TIntHashSet;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

final class Decoder {
  public final ConstantPool constantPool;
  public final MethodInfo methodInfo;
  public final MetaDataInfo metadataInfo;
  public final ClassInfo classInfo;
  public final ScriptInfo scriptInfo;
  public final MethodBodies methodBodies;
  public final Opcodes opcodes;

  private final DataBuffer in;

  public Decoder(DataBuffer in) throws DecoderException {
    in.skip(4);
    constantPool = new ConstantPool(in);

    methodInfo = new MethodInfo(in);
    metadataInfo = new MetaDataInfo(in);
    classInfo = new ClassInfo(in);
    scriptInfo = new ScriptInfo(in);
    methodBodies = new MethodBodies(in);
    opcodes = new Opcodes(in);

    this.in = in;
  }

  public int position() {
    return in.position();
  }

  private abstract static class Info {
    final DataBuffer in;
    final int estimatedSize;
    protected final int[] positions;

    Info(DataBuffer in) {
      int pos = in.position();
      this.in = in;
      this.positions = scan();
      estimatedSize = in.position() - pos;
    }

    abstract protected int[] scan();

    public int size() {
      return positions.length;
    }
  }

  static final class MethodInfo extends Info {
    MethodInfo(DataBuffer in) {
      super(in);
    }

    @Override
    protected int[] scan() {
      return Scanner.scanMethods(in);
    }

    public void decode(int index, Encoder visitor) throws DecoderException {
      int originalPos = in.position();
      in.seek(positions[index]);
      visitor.methodInfo(in);
      in.seek(originalPos);
    }
  }

  static final class MetaDataInfo extends Info {
    MetaDataInfo(DataBuffer in) {
      super(in);
    }

    @Override
    protected int[] scan() {
      return Scanner.scanMetadata(in);
    }

    public void decode(int index, Encoder visitor) throws DecoderException {
      int pos = positions[index];
      int originalPos = in.position();
      in.seek(pos);
      visitor.metadataInfo(index, in.readU32(), in.readU32(), in);
      in.seek(originalPos);
    }
  }

  public static final class ClassInfo {
    final DataBuffer in;
    final int estimatedSize;
    private final int[] cPositions;
    private final int[] iPositions;
    private final Traits cTraits;
    private final Traits iTraits;

    ClassInfo(DataBuffer in) {
      this.in = in;
      int pos = in.position();
      int size = in.readU32();
      iPositions = Scanner.scanInstances(in, size);
      iTraits = new Traits(in);
      cPositions = Scanner.scanClasses(in, size);
      cTraits = new Traits(in);
      estimatedSize = in.position() - pos;
    }

    public int size() {
      return cPositions.length;
    }

    public void decodeInstance(int index, Encoder visitor) throws DecoderException {
      int pos = iPositions[index];
      int originalPos = in.position();
      in.seek(pos);
      visitor.startInstance(in);
      iTraits.decode(visitor);
      visitor.endInstance();

      in.seek(originalPos);
    }

    public void decodeClass(int index, Encoder visitor) throws DecoderException {
      int pos = cPositions[index];
      int originalPos = in.position();
      in.seek(pos);

      int cinit = in.readU32();
      visitor.startClass(cinit);
      cTraits.decode(visitor);
      visitor.endClass();

      in.seek(originalPos);
    }
  }

  public static final class ScriptInfo extends Info {
    private final Traits traits;

    ScriptInfo(DataBuffer in) {
      super(in);
      traits = new Traits(in);
    }

    @Override
    protected int[] scan() {
      return Scanner.scanScripts(in);
    }

    public void decode(int index, Encoder visitor) throws DecoderException {
      int pos = positions[index];
      int originalPos = in.position();
      in.seek(pos);

      visitor.startScript(in.readU32());
      traits.decode(visitor);
      visitor.endScript();

      in.seek(originalPos);
    }
  }

  public final class MethodBodies extends Info {
    private final Traits traits;

    MethodBodies(DataBuffer in) {
      super(in);
      traits = new Traits(in);
    }

    @Override
    protected int[] scan() {
      return Scanner.scanMethodBodies(in);
    }

    public void decode(int index, int opcodePass, Encoder visitor) throws DecoderException {
      int pos = positions[index];
      int originalPos = in.position();
      in.seek(pos);

      int methodInfo = in.readU32();
      int maxStack = in.readU32();
      int maxRegs = in.readU32();
      int scopeDepth = in.readU32();
      int maxScope = in.readU32();

      int codeLength = in.readU32();
      int codeStart = in.position();
      in.skip(codeLength);

      visitor.startMethodBody(methodInfo, maxStack, maxRegs, scopeDepth, maxScope);

      int exPos = in.position();
      for (int i = 0; i < opcodePass; i++) {
        opcodes.reset();
        in.seek(exPos);
        int exCount = in.readU32();
        visitor.startExceptions(exCount);

        decodeExceptions(in, codeStart, visitor, exCount);
        opcodes.decode(codeStart, codeLength, visitor);

        visitor.endOpcodes();
        visitor.endExceptions();
      }
      traits.decode(visitor);
      visitor.endMethodBody();

      in.seek(originalPos);
    }

    private void decodeExceptions(DataBuffer in, int codeStart, Encoder visitor, int exCount) {
      boolean hasNames = (in.minorVersion() != 15);

      for (int i = 0; i < exCount; i++) {
        int start = codeStart + in.readU32();
        int end = codeStart + in.readU32();
        int target = codeStart + in.readU32();

        int type = in.readU32(); // multiname

        int nameIndex = hasNames ? in.readU32() : 0;

        opcodes.addTarget(start);
        opcodes.addTarget(end);
        opcodes.addTarget(target);

        visitor.exception(start, end, target, type, nameIndex);
      }
    }
  }

  private static class Traits {
    Traits(DataBuffer in) {
      this.in = in;
    }

    final DataBuffer in;

    void decode(Encoder visitor) throws DecoderException {
      int count = in.readU32();
      visitor.traitCount(count);

      for (int i = 0; i < count; i++) {
        int name = in.readU32();
        int kind = in.readU8();
        int slotID, typeID, valueID;
        int value_kind = 0;

        switch (kind & 0x0f) {
          case TRAIT_Var:
          case TRAIT_Const:
            slotID = in.readU32();
            typeID = in.readU32();
            valueID = in.readU32();
            if (valueID != 0) {
              value_kind = in.readU8();
            }
            visitor.slotTrait(kind, name, slotID, typeID, valueID, value_kind, decodeMetaData(kind));
            break;
          case TRAIT_Method:
          case TRAIT_Getter:
          case TRAIT_Setter:
            visitor.methodTrait(kind, name, in.readU32(), in.readU32(), decodeMetaData(kind));
            break;
          case TRAIT_Class:
            visitor.classTrait(kind, name, in.readU32(), in.readU32(), decodeMetaData(kind));
            break;
          case TRAIT_Function:
            visitor.functionTrait(kind, name, in.readU32(), in.readU32(), decodeMetaData(kind));
            break;
        }
      }
    }

    private int[] decodeMetaData(int kind) {
      int[] md = null;
      if (((kind >> 4) & TRAIT_FLAG_metadata) != 0) {
        int length = in.readU32();
        if (length > 0) {
          md = new int[length];
          for (int i = 0; i < length; i++) {
            md[i] = in.readU32();
          }
        }
      }

      return md;
    }
  }

  @SuppressWarnings({"deprecation"})
  private static class Opcodes {
    public Opcodes(DataBuffer in) {
      this.in = in;
    }

    final DataBuffer in;

    private TIntHashSet targetSet;

    public void addTarget(int pos) {
      if (targetSet == null) {
        targetSet = new TIntHashSet();
      }
      targetSet.add(pos);
    }

    public void reset() {
      targetSet = null;
    }

    public void decode(int start, int length, Encoder v) throws DecoderException {
      int originalPos = in.position();
      in.seek(start);

      for (int end = start + length; in.position() < end; ) {
        int pos = in.position();
        int opcode = in.readU8();

        if (opcode == OP_label) {
          addTarget(pos);
        }

        if (targetSet != null && targetSet.contains(pos)) {
          v.target(pos);
        }

        switch (opcode) {
          case OP_ifnlt: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifnlt(offset, in.position());
            continue;
          }
          case OP_ifnle: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifnle(offset, in.position());
            continue;
          }
          case OP_ifngt: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifngt(offset, in.position());
            continue;
          }
          case OP_ifnge: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifnge(offset, in.position());
            continue;
          }
          case OP_pushscope: {
            v.OP_pushscope();
            continue;
          }
          case OP_newactivation: {
            v.OP_newactivation();
            continue;
          }
          case OP_newcatch: {
            int index = in.readU32();
            v.OP_newcatch(index);
            continue;
          }
          case OP_deldescendants: {
            v.OP_deldescendants();
            continue;
          }
          case OP_getglobalscope: {
            v.OP_getglobalscope();
            continue;
          }
          case OP_getlocal0: {
            v.OP_getlocal0();
            continue;
          }
          case OP_getlocal1: {
            v.OP_getlocal1();
            continue;
          }
          case OP_getlocal2: {
            v.OP_getlocal2();
            continue;
          }
          case OP_getlocal3: {
            v.OP_getlocal3();
            continue;
          }
          case OP_setlocal0: {
            v.OP_setlocal0();
            continue;
          }
          case OP_setlocal1: {
            v.OP_setlocal1();
            continue;
          }
          case OP_setlocal2: {
            v.OP_setlocal2();
            continue;
          }
          case OP_setlocal3: {
            v.OP_setlocal3();
            continue;
          }
          case OP_returnvoid: {
            v.OP_returnvoid();
            continue;
          }
          case OP_returnvalue: {
            v.OP_returnvalue();
            continue;
          }
          case OP_nop: {
            v.OP_nop();
            continue;
          }
          case OP_bkpt: {
            v.OP_bkpt();
            continue;
          }
          case OP_timestamp: {
            v.OP_timestamp();
            continue;
          }
          case OP_debugline: {
            int linenum = in.readU32();
            v.OP_debugline(linenum);
            continue;
          }
          case OP_bkptline: {
            in.readU32();
            v.OP_bkptline();
            continue;
          }
          case OP_debug: {
            int di_local = in.readU8(); // DI_LOCAL
            int index = in.readU32(); // constant pool index...
            int slot = in.readU8();
            int linenum = in.readU32();
            v.OP_debug(di_local, index, slot, linenum);
            continue;
          }
          case OP_debugfile: {
            v.OP_debugfile(in);
            continue;
          }
          case OP_jump: {
            int jump = in.readS24(); // readjust jump...
            addTarget(jump + in.position());
            v.OP_jump(jump, in.position());
            continue;
          }
          case OP_pushnull: {
            v.OP_pushnull();
            continue;
          }
          case OP_pushundefined: {
            v.OP_pushundefined();
            continue;
          }
          case OP_pushstring: {
            int index = in.readU32(); // constant pool index...
            v.OP_pushstring(index);
            continue;
          }
          case OP_pushnamespace: {
            int index = in.readU32();
            v.OP_pushnamespace(index);
            continue;
          }
          case OP_pushint: {
            int index = in.readU32(); // constant pool index...
            v.OP_pushint(index);
            continue;
          }
          case OP_pushuint: {
            int index = in.readU32(); // constant pool index...
            v.OP_pushuint(index);
            continue;
          }
          case OP_pushdouble: {
            int index = in.readU32(); // constant pool index...
            v.OP_pushdouble(index);
            continue;
          }

          case OP_getlocal: {
            int index = in.readU32();
            v.OP_getlocal(index);
            continue;
          }
          case OP_pushtrue: {
            v.OP_pushtrue();
            continue;
          }
          case OP_pushfalse: {
            v.OP_pushfalse();
            continue;
          }
          case OP_pushnan: {
            v.OP_pushnan();
            continue;
          }
          case OP_pushdnan: {
            v.OP_pushdnan();
            continue;
          }
          case OP_pop: {
            v.OP_pop();
            continue;
          }
          case OP_dup: {
            v.OP_dup();
            continue;
          }
          case OP_swap: {
            v.OP_swap();
            continue;
          }
          case OP_convert_s: {
            v.OP_convert_s();
            continue;
          }
          case OP_esc_xelem: {
            v.OP_esc_xelem();
            continue;
          }
          case OP_esc_xattr: {
            v.OP_esc_xattr();
            continue;
          }
          case OP_checkfilter: {
            v.OP_checkfilter();
            continue;
          }
          case OP_convert_d: {
            v.OP_convert_d();
            continue;
          }
          case OP_convert_b: {
            v.OP_convert_b();
            continue;
          }
          case OP_convert_o: {
            v.OP_convert_o();
            continue;
          }
          case OP_convert_m: {
            v.OP_convert_m();
            continue;
          }
          case OP_convert_m_p: {
            int param = in.readU32();
            v.OP_convert_m_p(param);
            continue;
          }
          case OP_negate: {
            v.OP_negate();
            continue;
          }
          case OP_negate_p: {
            int param = in.readU32();
            v.OP_negate_p(param);
            continue;
          }
          case OP_negate_i: {
            v.OP_negate_i();
            continue;
          }
          case OP_increment: {
            v.OP_increment();
            continue;
          }
          case OP_increment_p: {
            int param = in.readU32();
            v.OP_increment_p(param);
            continue;
          }
          case OP_increment_i: {
            v.OP_increment_i();
            continue;
          }
          case OP_inclocal: {
            int index = in.readU32();
            v.OP_inclocal(index);
            continue;
          }
          case OP_inclocal_p: {
            int param = in.readU32();
            int index = in.readU32();
            v.OP_inclocal_p(param, index);
            continue;
          }
          case OP_kill: {
            int index = in.readU32();
            v.OP_kill(index);
            continue;
          }
          case OP_label: {
            v.OP_label();
            continue;
          }
          case OP_inclocal_i: {
            int index = in.readU32();
            v.OP_inclocal_i(index);
            continue;
          }
          case OP_decrement: {
            v.OP_decrement();
            continue;
          }
          case OP_decrement_p: {
            int param = in.readU32();
            v.OP_decrement_p(param);
            continue;
          }
          case OP_decrement_i: {
            v.OP_decrement_i();
            continue;
          }
          case OP_declocal: {
            int index = in.readU32();
            v.OP_declocal(index);
            continue;
          }
          case OP_declocal_p: {
            int param = in.readU32();
            int index = in.readU32();
            v.OP_declocal_p(param, index);
            continue;
          }
          case OP_declocal_i: {
            int index = in.readU32();
            v.OP_declocal_i(index);
            continue;
          }
          case OP_typeof: {
            v.OP_typeof();
            continue;
          }
          case OP_not: {
            v.OP_not();
            continue;
          }
          case OP_bitnot: {
            v.OP_bitnot();
            continue;
          }
          case OP_setlocal: {
            int index = in.readU32();
            v.OP_setlocal(index);
            continue;
          }
          case OP_add: {
            v.OP_add();
            continue;
          }
          case OP_add_p: {
            int param = in.readU32();
            v.OP_add_p(param);
            continue;
          }
          case OP_add_i: {
            v.OP_add_i();
            continue;
          }
          case OP_subtract: {
            v.OP_subtract();
            continue;
          }
          case OP_subtract_p: {
            int param = in.readU32();
            v.OP_subtract_p(param);
            continue;
          }
          case OP_subtract_i: {
            v.OP_subtract_i();
            continue;
          }
          case OP_multiply: {
            v.OP_multiply();
            continue;
          }
          case OP_multiply_p: {
            int param = in.readU32();
            v.OP_multiply_p(param);
            continue;
          }
          case OP_multiply_i: {
            v.OP_multiply_i();
            continue;
          }
          case OP_divide: {
            v.OP_divide();
            continue;
          }
          case OP_divide_p: {
            int param = in.readU32();
            v.OP_divide_p(param);
            continue;
          }
          case OP_modulo: {
            v.OP_modulo();
            continue;
          }
          case OP_modulo_p: {
            int param = in.readU32();
            v.OP_modulo_p(param);
            continue;
          }
          case OP_lshift: {
            v.OP_lshift();
            continue;
          }
          case OP_rshift: {
            v.OP_rshift();
            continue;
          }
          case OP_urshift: {
            v.OP_urshift();
            continue;
          }
          case OP_bitand: {
            v.OP_bitand();
            continue;
          }
          case OP_bitor: {
            v.OP_bitor();
            continue;
          }
          case OP_bitxor: {
            v.OP_bitxor();
            continue;
          }
          case OP_equals: {
            v.OP_equals();
            continue;
          }
          case OP_strictequals: {
            v.OP_strictequals();
            continue;
          }
          case OP_lookupswitch: {
            int opPos = in.position() - 1; // OP_lookupswtich position...
            int defaultPos = in.readS24();
            addTarget(defaultPos + opPos);
            int size_1 = in.readU32(); // size - 1
            int[] casePos = new int[size_1 + 1];
            int caseTablePos = in.position(); // case position
            for (int i = 0, size = casePos.length; i < size; i++) {
              casePos[i] = in.readS24();
              addTarget(casePos[i] + opPos);
            }
            v.OP_lookupswitch(defaultPos, casePos, opPos, caseTablePos);
            continue;
          }
          case OP_iftrue: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_iftrue(offset, in.position());
            continue;
          }
          case OP_iffalse: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_iffalse(offset, in.position());
            continue;
          }
          case OP_ifeq: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifeq(offset, in.position());
            continue;
          }
          case OP_ifne: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifne(offset, in.position());
            continue;
          }
          case OP_ifstricteq: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifstricteq(offset, in.position());
            continue;
          }
          case OP_ifstrictne: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifstrictne(offset, in.position());
            continue;
          }
          case OP_iflt: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_iflt(offset, in.position());
            continue;
          }
          case OP_ifle: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifle(offset, in.position());
            continue;
          }
          case OP_ifgt: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifgt(offset, in.position());
            continue;
          }
          case OP_ifge: {
            int offset = in.readS24();
            addTarget(offset + in.position());
            v.OP_ifge(offset, in.position());
            continue;
          }
          case OP_lessthan: {
            v.OP_lessthan();
            continue;
          }
          case OP_lessequals: {
            v.OP_lessequals();
            continue;
          }
          case OP_greaterthan: {
            v.OP_greaterthan();
            continue;
          }
          case OP_greaterequals: {
            v.OP_greaterequals();
            continue;
          }
          case OP_newobject: {
            int size = in.readU32();
            v.OP_newobject(size);
            continue;
          }
          case OP_newarray: {
            int size = in.readU32();
            v.OP_newarray(size);
            continue;
          }
          // get a property using a multiname ref
          case OP_getproperty: {
            int index = in.readU32(); // constant pool index...
            v.OP_getproperty(index);
            continue;
          }
          // set a property using a multiname ref
          case OP_setproperty: {
            int index = in.readU32(); // constant pool index...
            v.OP_setproperty(index);
            continue;
          }
          // set a property using a multiname ref
          case OP_initproperty: {
            int index = in.readU32(); // constant pool index...
            v.OP_initproperty(index);
            continue;
          }
          case OP_getdescendants: {
            int index = in.readU32(); // constant pool index...
            v.OP_getdescendants(index);
            continue;
          }
          // search the scope chain for a given property and return the object
          // that contains it.  the next instruction will usually be getpropname
          // or setpropname.
          case OP_findpropstrict: {
            int index = in.readU32(); // constant pool index...
            v.OP_findpropstrict(index);
            continue;
          }
          case OP_getlex: {
            int index = in.readU32(); // constant pool index...
            v.OP_getlex(index);
            continue;
          }
          case OP_findproperty: {
            // stack in:  [ns [name]]
            // stack out: obj
            int index = in.readU32(); // constant pool index...
            v.OP_findproperty(index);
            continue;
          }
          case OP_finddef: {
            // stack in:
            // stack out: obj
            int index = in.readU32(); // constant pool index...
            v.OP_finddef(index);
            continue;
          }
          case OP_nextname: {
            v.OP_nextname();
            continue;
          }
          case OP_nextvalue: {
            v.OP_nextvalue();
            continue;
          }
          case OP_hasnext: {
            v.OP_hasnext();
            continue;
          }
          case OP_hasnext2: {
            int objectRegister = in.readU32();
            int indexRegister = in.readU32();
            v.OP_hasnext2(objectRegister, indexRegister);
            continue;
          }
          // delete property using multiname
          case OP_deleteproperty: {
            int index = in.readU32(); // constant pool index...
            v.OP_deleteproperty(index);
            continue;
          }
          case OP_setslot: {
            int index = in.readU32();
            v.OP_setslot(index);
            continue;
          }
          case OP_getslot: {
            int index = in.readU32();
            v.OP_getslot(index);
            continue;
          }
          case OP_setglobalslot: {
            int index = in.readU32();
            v.OP_setglobalslot(index);
            continue;
          }
          case OP_getglobalslot: {
            int index = in.readU32();
            v.OP_getglobalslot(index);
            continue;
          }
          case OP_call: {
            int size = in.readU32();
            v.OP_call(size);
            continue;
          }
          case OP_construct: {
            int size = in.readU32();
            v.OP_construct(size);
            continue;
          }
          case OP_applytype: {
            int size = in.readU32();
            v.OP_applytype(size);
            continue;
          }
          case OP_newfunction: {
            int id = in.readU32(); // method info...
            v.OP_newfunction(id);
            continue;
          }
          case OP_newclass: {
            int id = in.readU32(); // class info...
            v.OP_newclass(id);
            continue;
          }
          case OP_callstatic: {
            // stack in: receiver, arg1..N
            // stack out: result
            int id = in.readU32(); // method info...
            int argc = in.readU32();
            v.OP_callstatic(id, argc);
            continue;
          }
          case OP_callmethod: {
            // stack in: receiver, arg1..N
            // stack out: result
            int id = in.readU32(); // disp_id...
            int argc = in.readU32();
            v.OP_callmethod(id, argc);
            continue;
          }
          case OP_callproperty: {
            // stack in: obj [ns [name]] arg1..N
            // stack out: result
            int index = in.readU32(); // constant pool index...
            int argc = in.readU32();
            v.OP_callproperty(index, argc);
            continue;
          }
          case OP_callproplex: {
            // stack in: obj [ns [name]] arg1..N
            // stack out: result
            int index = in.readU32(); // constant pool index...
            int argc = in.readU32();
            v.OP_callproplex(index, argc);
            continue;
          }
          case OP_constructprop: {
            // stack in: obj [ns [name]] arg1..N
            // stack out: result
            int index = in.readU32(); // constant pool index...
            int argc = in.readU32();
            v.OP_constructprop(index, argc);
            continue;
          }
          case OP_callsuper: {
            // stack in: obj [ns [name]] arg1..N
            int index = in.readU32(); // constant pool index...
            int argc = in.readU32();
            v.OP_callsuper(index, argc);
            continue;
          }
          case OP_getsuper: {
            int index = in.readU32(); // constant pool index...
            v.OP_getsuper(index);
            continue;
          }
          case OP_setsuper: {
            int index = in.readU32(); // constant pool index...
            v.OP_setsuper(index);
            continue;
          }
          // obj arg1 arg2
          //           sp
          case OP_constructsuper: {
            // stack in:  obj arg1..N
            // stack out:
            int argc = in.readU32();
            v.OP_constructsuper(argc);
            continue;
          }
          case OP_pushshort: {
            // fixme this just pushes an integer since we dont have short atoms yet
            int n = in.readU32();
            v.OP_pushshort(n);
            continue;
          }
          case OP_astype: {
            int index = in.readU32(); // constant pool index...
            v.OP_astype(index);
            continue;
          }
          case OP_astypelate:
            v.OP_astypelate();
            continue;

          case OP_coerce: {
            // expects a CONSTANT_Multiname cpool index
            // this is the ES4 implicit coersion
            int index = in.readU32(); // constant pool index...
            v.OP_coerce(index);
            continue;
          }
          case OP_coerce_b: {
            v.OP_coerce_b();
            continue;
          }
          case OP_coerce_o: {
            v.OP_coerce_o();
            continue;
          }
          case OP_coerce_a: {
            v.OP_coerce_a();
            continue;
          }
          case OP_coerce_i: {
            v.OP_coerce_i();
            continue;
          }
          case OP_coerce_u: {
            v.OP_coerce_u();
            continue;
          }
          case OP_coerce_d: {
            v.OP_coerce_d();
            continue;
          }
          case OP_coerce_s: {
            v.OP_coerce_s();
            continue;
          }
          case OP_istype: {
            // expects a CONSTANT_Multiname cpool index
            // used when operator "is" RHS is a compile-time type constant
            int index = in.readU32(); // constant pool index...
            v.OP_istype(index);
            continue;
          }
          case OP_istypelate: {
            v.OP_istypelate();
            continue;
          }
          case OP_pushbyte: {
            int n = in.readU8();
            v.OP_pushbyte(n);
            continue;
          }
          case OP_getscopeobject: {
            int index = in.readU8();
            v.OP_getscopeobject(index);
            continue;
          }
          case OP_pushwith: {
            v.OP_pushwith();
            continue;
          }
          case OP_popscope: {
            v.OP_popscope();
            continue;
          }
          case OP_convert_i: {
            v.OP_convert_i();
            continue;
          }
          case OP_convert_u: {
            v.OP_convert_u();
            continue;
          }
          case OP_throw: {
            v.OP_throw();
            continue;
          }
          case OP_instanceof: {
            v.OP_instanceof();
            continue;
          }
          case OP_in: {
            v.OP_in();
            continue;
          }
          case OP_dxns: {
            int index = in.readU32(); // constant pool index...
            v.OP_dxns(index);
            continue;
          }
          case OP_dxnslate: {
            v.OP_dxnslate();
            continue;
          }
          case OP_pushuninitialized: {
            int id = in.readU32();
            v.OP_pushconstant(id);
            continue;
          }
          case OP_callsupervoid: {
            // stack in: obj [ns [name]] arg1..N
            int index = in.readU32(); // constant pool index...
            int argc = in.readU32();
            v.OP_callsupervoid(index, argc);
            continue;
          }
          case OP_callpropvoid: {
            // stack in: obj [ns [name]] arg1..N
            // stack out: result
            int index = in.readU32(); // constant pool index...
            int argc = in.readU32();
            v.OP_callpropvoid(index, argc);
            continue;
          }
          case OP_li8: {
            v.OP_li8();
            continue;
          }
          case OP_li16: {
            v.OP_li16();
            continue;
          }
          case OP_li32: {
            v.OP_li32();
            continue;
          }
          case OP_lf32: {
            v.OP_lf32();
            continue;
          }
          case OP_lf64: {
            v.OP_lf64();
            continue;
          }
          case OP_si8: {
            v.OP_si8();
            continue;
          }
          case OP_si16: {
            v.OP_si16();
            continue;
          }
          case OP_si32: {
            v.OP_si32();
            continue;
          }
          case OP_sf32: {
            v.OP_sf32();
            continue;
          }
          case OP_sf64: {
            v.OP_sf64();
            continue;
          }
          case OP_sxi1: {
            v.OP_sxi1();
            continue;
          }
          case OP_sxi8: {
            v.OP_sxi8();
            continue;
          }
          case OP_sxi16: {
            v.OP_sxi16();
            continue;
          }

          default: {
            throw new DecoderException("unknown opcode:" + opcode);
          }
        }
      }

      in.seek(originalPos);
    }
  }
}
