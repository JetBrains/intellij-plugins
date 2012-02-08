package com.intellij.flex.uiDesigner.abc;

import com.intellij.util.ArrayUtil;
import gnu.trove.TIntHashSet;
import org.jetbrains.annotations.Nullable;

import static com.intellij.flex.uiDesigner.abc.ActionBlockConstants.*;

public final class Decoder {
  public final ConstantPool constantPool;
  public final MethodInfo methodInfo;
  public final MetaDataInfo metadataInfo;
  public final ClassInfo classInfo;
  public final ScriptInfo scriptInfo;
  public final MethodBodies methodBodies;

  final DataBuffer in;

  public final char[] name;
  @Nullable
  public final AbcModifier abcModifier;

  public Decoder(DataBuffer in) {
    this(in, null, null);
  }

  public Decoder(DataBuffer in, AbcModifier abcModifier) {
    this(in, null, abcModifier);
  }

  public Decoder(DataBuffer in, @Nullable char[] name) {
    this(in, name, null);
  }

  public Decoder(DataBuffer in, @Nullable char[] name, @Nullable AbcModifier abcModifier) {
    this.abcModifier = abcModifier;
    assert in.position() == 0;

    in.skip(4);
    this.name = name;

    constantPool = new ConstantPool(in);

    methodInfo = new MethodInfo(in);
    metadataInfo = new MetaDataInfo(in);
    classInfo = new ClassInfo(in);
    scriptInfo = new ScriptInfo(in);
    methodBodies = new MethodBodies(in);

    this.in = in;
  }

  public int position() {
    return in.position();
  }

  private abstract static class Info {
    final int estimatedSize;
    protected final int[] positions;

    Info(DataBuffer in) throws DecoderException {
      int pos = in.position();
      positions = scan(in);
      estimatedSize = in.position() - pos;
    }

    abstract protected int[] scan(DataBuffer in) throws DecoderException;

    public int size() {
      return positions.length;
    }
  }

  static final class MethodInfo extends Info {
    MethodInfo(DataBuffer in) throws DecoderException {
      super(in);
    }

    @Override
    protected int[] scan(DataBuffer in) {
      return Scanner.scanMethods(in);
    }

    public void decodeAll(Encoder visitor, DataBuffer in) throws DecoderException {
      for (int position : positions) {
        in.seek(position);
        visitor.methodInfo(in);
      }
    }
  }

  static final class MetaDataInfo extends Info {
    MetaDataInfo(DataBuffer in) throws DecoderException {
      super(in);
    }

    @Override
    protected int[] scan(DataBuffer in) {
      return Scanner.scanMetadata(in);
    }

    public void decodeAll(Encoder visitor, DataBuffer in) throws DecoderException {
      for (int index = 0, n = positions.length; index < n; index++) {
        in.seek(positions[index]);
        visitor.metadataInfo(index, in.readU32(), in.readU32(), in);
      }
    }
  }

  public static final class ClassInfo {
    final int instanceEstimatedSize;
    final int classEstimatedSize;
    // first half — instance, second half — class
    private final int[] positions;

    ClassInfo(final DataBuffer in) throws DecoderException {
      int position = in.position();
      final int size = in.readU32();
      if (size == 0) {
        positions = ArrayUtil.EMPTY_INT_ARRAY;
        instanceEstimatedSize = in.position() - position;
        classEstimatedSize = 0;
      }
      else {
        positions = new int[size * 2];

        scanInstances(in, size, positions);
        instanceEstimatedSize = in.position() - position;

        position = in.position();
        scanClasses(in, size, positions);
        classEstimatedSize = in.position() - position;
      }
    }

    private static void scanInstances(DataBuffer in, int size, int[] positions) throws DecoderException {
      for (int i = 0; i < size; i++) {
        positions[i] = in.position();

        in.skipEntries(2); //name & super index
        int flags = in.readU8();

        if ((flags & CLASS_FLAG_protected) != 0) {
          in.readU32();//protected namespace
        }

        in.skipEntries(in.readU32());
        in.readU32(); //init index

        Scanner.scanTraits(in);
      }
    }

    private static void scanClasses(DataBuffer in, int size, int[] positions) throws DecoderException {
      for (int i = 0; i < size; i++) {
        positions[i + size] = in.position();
        in.readU32();
        Scanner.scanTraits(in);
      }
    }

    public int size() {
      return positions.length / 2;
    }

    public void decodeInstances(Encoder visitor, DataBuffer in) throws DecoderException {
      for (int i = 0, n = size(); i < n; i++) {
        in.seek(positions[i]);
        visitor.startInstance(in);
        decodeTraits(visitor, in);
        visitor.endInstance();
      }
    }

    public void decodeClasses(Encoder visitor, DataBuffer in) throws DecoderException {
      for (int i = size(), n = positions.length; i < n; i++) {
        in.seek(positions[i]);

        visitor.startClass(in.readU32());
        decodeTraits(visitor, in);
        visitor.endClass();
      }
    }
  }

  public static final class ScriptInfo extends Info {
    ScriptInfo(DataBuffer in) throws DecoderException {
      super(in);
    }

    @Override
    protected int[] scan(DataBuffer in) throws DecoderException {
      return Scanner.scanScripts(in);
    }

    public void decodeAll(Encoder visitor, DataBuffer in) throws DecoderException {
      for (int position : positions) {
        in.seek(position);

        visitor.startScript(in.readU32());
        decodeTraits(visitor, in);
        visitor.endScript();
      }
    }
  }

  public static final class MethodBodies extends Info {
    MethodBodies(DataBuffer in) throws DecoderException {
      super(in);
    }

    @Override
    protected int[] scan(DataBuffer in) throws DecoderException {
      return Scanner.scanMethodBodies(in);
    }

    public void decodeAll(Encoder visitor, DataBuffer in) throws DecoderException {
      final Opcodes opcodes = visitor.opcodeDecoder;
      for (int position : positions) {
        in.seek(position);

        int methodInfo = in.readU32();
        int maxStack = in.readU32();
        int maxRegs = in.readU32();
        int scopeDepth = in.readU32();
        int maxScope = in.readU32();

        int codeLength = in.readU32();
        int codeStart = in.position();
        in.skip(codeLength);

        final int behaviour = visitor.startMethodBody(methodInfo, maxStack, maxRegs, scopeDepth, maxScope);
        if (behaviour == MethodCodeDecoding.STOP) {
          skipExceptions(in.readU32(), in);
          int traitCount = in.readU32();
          assert traitCount == 0;
          continue;
        }

        int exPos = in.position();
        for (int i = 0; i < 2; i++) {
          opcodes.reset();
          in.seek(exPos);
          int exCount = in.readU32();
          visitor.startExceptions(exCount);

          decodeExceptions(in, codeStart, visitor, exCount);
          opcodes.decode(codeStart, codeLength, visitor, behaviour == MethodCodeDecoding.STOP_AFTER_CONSTRUCT_SUPER, in);

          visitor.endOpcodes();
          visitor.endExceptions();
        }
        decodeTraits(visitor, in);
        visitor.endMethodBody();
      }
    }

    private static void skipExceptions(int exCount, DataBuffer in) {
      for (int i = 0; i < exCount; i++) {
        in.readU32();
        in.readU32();
        in.readU32();
        in.readU32();
        in.readU32();
      }
    }

    private static void decodeExceptions(DataBuffer in, int codeStart, Encoder visitor, int exCount) {
      final Opcodes opcodes = visitor.opcodeDecoder;
      for (int i = 0; i < exCount; i++) {
        int start = codeStart + in.readU32();
        int end = codeStart + in.readU32();
        int target = codeStart + in.readU32();

        int type = in.readU32(); // multiname
        int nameIndex = in.readU32();

        opcodes.addTarget(start);
        opcodes.addTarget(end);
        opcodes.addTarget(target);

        visitor.exception(start, end, target, type, nameIndex);
      }
    }
  }

  private static void decodeTraits(Encoder visitor, DataBuffer in) throws DecoderException {
    int count = in.readU32();
    visitor.traitCount(count);

    for (int i = 0; i < count; i++) {
      int name = in.readU32();
      int kind = in.readU8();
      int valueId;
      switch (kind & 0x0f) {
        case TRAIT_Var:
        case TRAIT_Const:
          visitor.slotTrait(kind, name, in.readU32(), in.readU32(), (valueId = in.readU32()), valueId != 0 ? in.readU8() : 0,
                            decodeTraitsMetadata(kind, in), in);
          break;
        case TRAIT_Method:
        case TRAIT_Getter:
        case TRAIT_Setter:
          visitor.methodTrait(kind, name, in.readU32(), in.readU32(), decodeTraitsMetadata(kind, in), in);
          break;
        case TRAIT_Class:
          visitor.classTrait(kind, name, in.readU32(), in.readU32(), decodeTraitsMetadata(kind, in));
          break;
        case TRAIT_Function:
          visitor.functionTrait(kind, name, in.readU32(), in.readU32(), decodeTraitsMetadata(kind, in));
          break;

        default:
          throw new DecoderException("Unknown trait kind " + kind);
      }
    }
  }

  @Nullable
  private static int[] decodeTraitsMetadata(int kind, DataBuffer in) {
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

  @SuppressWarnings({"deprecation"})
  static class Opcodes {
    private TIntHashSet targetSet;
    private boolean targetSetExists;

    public void addTarget(int pos) {
      if (targetSet == null) {
        targetSet = new TIntHashSet();
      }
      targetSetExists = true;
      targetSet.add(pos);
    }

    public void reset() {
      if (targetSet != null) {
        targetSet.clear();
        targetSetExists = false;
      }
    }

    @SuppressWarnings("ConstantConditions")
    public void decode(int start, int length, Encoder v, boolean stopAfterConstructSuper, DataBuffer in) throws DecoderException {
      int originalPos = in.position();
      in.seek(start);

      int end = start + length;
      w:
      while (in.position() < end) {
        int pos = in.position();
        int opcode = in.readU8();

        if (opcode == OP_label) {
          addTarget(pos);
        }

        if (targetSetExists && targetSet.contains(pos)) {
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

          case OP_newcatch: {
            v.OP_newcatch(in.readU32());
            continue;
          }

          case OP_setlocal1:
            v.OP_setlocal1();
            continue;
          case OP_setlocal2:
            v.OP_setlocal2();
            continue;
          case OP_setlocal3:
            v.OP_setlocal3();
            continue;
          case OP_returnvoid:
            if (v.opcodePass == 1) {
              v.beginop(OP_returnvoid);
            }
            continue;
          case OP_returnvalue:
            v.OP_returnvalue();
            continue;
          case OP_nop:
            if (v.opcodePass == 1) {
              v.beginop(OP_nop);
            }
            continue;
          case OP_bkpt:
            if (v.opcodePass == 1) {
              v.beginop(OP_bkpt);
            }
            continue;
          case OP_timestamp:
            if (v.opcodePass == 1) {
              v.beginop(OP_timestamp);
            }
            continue;
          case OP_debugline:
            v.OP_debugline(in.readU32());
            continue;
          case OP_bkptline:
            in.readU32();
            if (v.opcodePass == 1) {
              v.beginop(OP_bkptline);
            }
            continue;
          case OP_debug: {
            v.OP_debug(in.readU8(), in.readU32(), in.readU8(), in.readU32());
            continue;
          }
          case OP_debugfile:
            v.OP_debugfile(in);
            continue;
          case OP_jump: {
            int jump = in.readS24(); // readjust jump...
            addTarget(jump + in.position());
            v.OP_jump(jump, in.position());
            continue;
          }
          case OP_pushstring:
            v.OP_pushstring(in.readU32());
            continue;
          case OP_pushnamespace:
            v.OP_pushnamespace(in.readU32());
            continue;
          case OP_pushint:
            v.OP_pushint(in.readU32());
            continue;
          case OP_pushuint:
            v.OP_pushuint(in.readU32());
            continue;
          case OP_pushdouble:
            v.OP_pushdouble(in.readU32());
            continue;
          case OP_getlocal:
            v.OP_getlocal(in.readU32());
            continue;

          case OP_pop: {
            v.OP_pop();
            continue;
          }

          case OP_convert_s: {
            v.OP_convert_s();
            continue;
          }

          case OP_convert_b: {
            v.OP_convert_b();
            continue;
          }

          case OP_pushnull:
          case OP_pushundefined:
          case OP_pushtrue:
          case OP_pushfalse:
          case OP_pushnan:
          case OP_pushdnan:
          case OP_dup:
          case OP_swap:
          case OP_checkfilter:
          case OP_convert_d:
          case OP_newactivation:
          case OP_deldescendants:
          case OP_getglobalscope:
          case OP_getlocal0:
          case OP_getlocal1:
          case OP_getlocal2:
          case OP_getlocal3:
          case OP_setlocal0:
          case OP_pushscope:
          case OP_popscope:
          case OP_coerce_b:
          case OP_esc_xelem:
          case OP_esc_xattr:
          case OP_negate:
          case OP_convert_o:
          case OP_convert_m:
          case OP_nextname:
          case OP_nextvalue:
          case OP_astypelate:
          case OP_coerce_o:
          case OP_hasnext:
          case OP_increment:
          case OP_increment_i:
          case OP_typeof:
          case OP_not:
          case OP_bitnot:
          case OP_lshift:
          case OP_rshift:
          case OP_urshift:
          case OP_bitand:
          case OP_bitor:
          case OP_bitxor:
          case OP_equals:
          case OP_strictequals:
          case OP_negate_i:
          case OP_decrement_i:
          case OP_add_i:
          case OP_multiply_i:
          case OP_divide:
          case OP_multiply:
          case OP_lessthan:
          case OP_lessequals:
          case OP_greaterthan:
          case OP_greaterequals:
          case OP_pushwith:
          case OP_instanceof:
          case OP_in:
          case OP_dxnslate:
          case OP_li8:
          case OP_li16:
          case OP_li32:
          case OP_lf32:
          case OP_lf64:
          case OP_si8:
          case OP_si16:
          case OP_si32:
          case OP_sf32:
          case OP_sf64:
          case OP_sxi1:
          case OP_sxi8:
          case OP_sxi16:
          case OP_convert_u:
          case OP_throw:
            if (v.opcodePass == 1) {
              v.beginop(opcode);
            }
            continue;

          case OP_convert_m_p: {
            v.OP_convert_m_p(in.readU32());
            continue;
          }

          case OP_negate_p: {
            v.OP_negate_p(in.readU32());
            continue;
          }
          case OP_increment_p: {
            v.OP_increment_p(in.readU32());
            continue;
          }
          case OP_inclocal: {
            v.OP_inclocal(in.readU32());
            continue;
          }
          case OP_inclocal_p: {
            v.OP_inclocal_p(in.readU32(), in.readU32());
            continue;
          }
          case OP_kill: {
            v.OP_kill(in.readU32());
            continue;
          }
          case OP_label: {
            v.OP_label();
            continue;
          }
          case OP_inclocal_i: {
            v.OP_inclocal_i(in.readU32());
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
          case OP_declocal: {
            int index = in.readU32();
            v.OP_declocal(index);
            continue;
          }
          case OP_declocal_p: {
            v.OP_declocal_p(in.readU32(), in.readU32());
            continue;
          }
          case OP_declocal_i: {
            v.OP_declocal_i(in.readU32());
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
          case OP_subtract: {
            v.OP_subtract();
            continue;
          }
          case OP_subtract_p: {
            v.OP_subtract_p(in.readU32());
            continue;
          }
          case OP_subtract_i: {
            v.OP_subtract_i();
            continue;
          }
          case OP_multiply_p: {
            v.OP_multiply_p(in.readU32());
            continue;
          }
          case OP_divide_p: {
            v.OP_divide_p(in.readU32());
            continue;
          }
          case OP_modulo: {
            v.OP_modulo();
            continue;
          }
          case OP_modulo_p: {
            v.OP_modulo_p(in.readU32());
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
          case OP_newobject: {
            v.OP_newobject(in.readU32());
            continue;
          }
          case OP_newarray: {
            v.OP_newarray(in.readU32());
            continue;
          }
          // get a property using a multiname ref
          case OP_getproperty: {
            v.OP_getproperty(in.readU32());
            continue;
          }
          // set a property using a multiname ref
          case OP_setproperty: {
            v.OP_setproperty(in.readU32());
            continue;
          }
          // set a property using a multiname ref
          case OP_initproperty: {
            v.OP_initproperty(in.readU32());
            continue;
          }
          case OP_getdescendants: {
            v.OP_getdescendants(in.readU32());
            continue;
          }
          // search the scope chain for a given property and return the object
          // that contains it.  the next instruction will usually be getpropname
          // or setpropname.
          case OP_findpropstrict: {
            v.OP_findpropstrict(in.readU32());
            continue;
          }
          case OP_getlex: {
            v.OP_getlex(in.readU32());
            continue;
          }
          case OP_findproperty: {
            v.OP_findproperty(in.readU32());
            continue;
          }
          case OP_finddef: {
            v.OP_finddef(in.readU32());
            continue;
          }
          case OP_hasnext2: {
            v.OP_hasnext2(in.readU32(), in.readU32());
            continue;
          }
          // delete property using multiname
          case OP_deleteproperty: {
            v.OP_deleteproperty(in.readU32());
            continue;
          }
          case OP_setslot: {
            v.OP_setslot(in.readU32());
            continue;
          }
          case OP_getslot: {
            v.OP_getslot(in.readU32());
            continue;
          }
          case OP_setglobalslot: {
            v.OP_setglobalslot(in.readU32());
            continue;
          }
          case OP_getglobalslot: {
            v.OP_getglobalslot(in.readU32());
            continue;
          }
          case OP_call: {
            v.OP_call(in.readU32());
            continue;
          }
          case OP_construct: {
            v.OP_construct(in.readU32());
            continue;
          }
          case OP_applytype: {
            v.OP_applytype(in.readU32());
            continue;
          }
          case OP_newfunction: {
            v.OP_newfunction(in.readU32());
            continue;
          }
          case OP_newclass: {
            v.OP_newclass(in.readU32());
            continue;
          }
          case OP_callstatic: {
            v.OP_callstatic(in.readU32(), in.readU32());
            continue;
          }
          case OP_callmethod: {
            v.OP_callmethod(in.readU32(), in.readU32());
            continue;
          }
          case OP_callproperty: {
            v.OP_callproperty(in.readU32(), in.readU32());
            continue;
          }
          case OP_callproplex: {
            v.OP_callproplex(in.readU32(), in.readU32());
            continue;
          }
          case OP_constructprop: {
            v.OP_constructprop(in.readU32(), in.readU32());
            continue;
          }
          case OP_callsuper: {
            v.OP_callsuper(in.readU32(), in.readU32());
            continue;
          }
          case OP_getsuper: {
            v.OP_getsuper(in.readU32());
            continue;
          }
          case OP_setsuper: {
            v.OP_setsuper(in.readU32());
            continue;
          }
          case OP_constructsuper:
            v.OP_constructsuper(in.readU32());
            if (stopAfterConstructSuper) {
              if (v.opcodePass == 1) {
                v.beginop(OP_returnvoid);
              }
              break w;
            }
            continue;
          case OP_pushshort: {
            // fixme this just pushes an integer since we dont have short atoms yet
            int n = in.readU32();
            v.OP_pushshort(n);
            continue;
          }
          case OP_astype: {
            v.OP_astype(in.readU32());
            continue;
          }
          case OP_coerce: {
            v.OP_coerce(in.readU32());
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
            v.OP_istype(in.readU32());
            continue;
          }
          case OP_istypelate: {
            v.OP_istypelate();
            continue;
          }
          case OP_pushbyte: {
            v.OP_pushbyte(in.readU8());
            continue;
          }
          case OP_getscopeobject: {
            v.OP_getscopeobject(in.readU8());
            continue;
          }
          case OP_convert_i: {
            v.OP_convert_i();
            continue;
          }
          case OP_dxns: {
            v.OP_dxns(in.readU32());
            continue;
          }
          case OP_pushuninitialized: {
            v.OP_pushconstant(in.readU32());
            continue;
          }
          case OP_callsupervoid: {
            v.OP_callsupervoid(in.readU32(), in.readU32());
            continue;
          }
          case OP_callpropvoid: {
            v.OP_callpropvoid(in.readU32(), in.readU32());
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

  interface MethodCodeDecoding {
    int CONTINUE = 0;
    int STOP = 1;
    int STOP_AFTER_CONSTRUCT_SUPER = 2;
  }
}
