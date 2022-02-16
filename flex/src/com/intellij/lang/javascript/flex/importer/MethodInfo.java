package com.intellij.lang.javascript.flex.importer;

import com.intellij.openapi.util.text.StringUtil;

import static com.intellij.lang.javascript.flex.importer.Abc.*;

/**
 * @author Maxim.Mossienko
 */
class MethodInfo extends MemberInfo {
  int flags;
  String debugName;
  Multiname[] paramTypes;
  String[] paramNames;
  Multiname[] optionalValues;
  Multiname returnType;
  int local_count;
  int max_scope;
  int max_stack;
  ByteBuffer code;
  Traits activation;
  boolean anon;

  @Override
  void dump(Abc abc, String indent, String attr, final FlexByteCodeInformationProcessor processor) {
    if (!processor.doDumpMember(this)) return;

    processor.processFunction(this, false, abc, indent, attr);
  }

  protected void dumpCode(Abc abc, String indent, FlexByteCodeInformationProcessor processor) {
    processor.append(indent + "{\n");
    String oldindent = indent;
    indent += TAB;

    if ((flags & NEED_ACTIVATION) != 0) {
      processor.append(indent + "activation {\n");
      activation.dump(abc, indent + TAB, "", processor);
      processor.append(indent + "}\n");
    }
    processor.append(indent +
                     "// local_count=" +
                     local_count +
                     " max_scope=" +
                     max_scope +
                     " max_stack=" +
                     max_stack +
                     " code_len=" +
                     code.bytesSize() +
                     "\n");
    code.setPosition(0);
    LabelInfo labels = new LabelInfo();

    while (!code.eof()) {
      int start = code.getPosition();
      int opcode = code.readUnsignedByte();

      if (opcode == OP_label || (labels.containsKey(start))) {
        processor.append(indent + "\n");
        processor.append(indent + labels.labelFor(start) + ": \n");
      }

      String str = indent + start;
      processor.append(str);
      for(int i = str.length(); i < 12; ++i) processor.append(" ");

      processor.append(opNames[opcode]);
      processor.append(opNames[opcode].length() < 8 ? "\t\t" : "\t");

      switch (opcode) {
        case OP_debugfile:
        case OP_pushstring:
          processor.append('"' + StringUtil.replace(StringUtil.replace(abc.strings[readU32()], "\n", "\\n"), "\t", "\\t") + '"');
          break;
        case OP_pushnamespace:
          processor.append(abc.namespaces[readU32()]);
          break;
        case OP_pushint: {
          int i = abc.ints[readU32()];
          processor.append(i + "\t// 0x" + Integer.toString(i, 16));
          break;
        }
        case OP_pushuint:
          int u = abc.uints[readU32()];
          processor.append(u + "\t// 0x" + Integer.toString(u, 16));
          break;
        case OP_pushdouble:
          processor.append(abc.doubles[readU32()].toString());
          break;
        case OP_getsuper:
        case OP_setsuper:
        case OP_getproperty:
        case OP_initproperty:
        case OP_setproperty:
        case OP_getlex:
        case OP_findpropstrict:
        case OP_findproperty:
        case OP_finddef:
        case OP_deleteproperty:
        case OP_istype:
        case OP_coerce:
        case OP_astype:
        case OP_getdescendants:
          processor.append(abc.names[readU32()].toString());
          break;
        case OP_constructprop:
        case OP_callproperty:
        case OP_callproplex:
        case OP_callsuper:
        case OP_callsupervoid:
        case OP_callpropvoid:
          processor.append(abc.names[readU32()].toString());
          processor.append(" (" + readU32() + ")");
          break;
        case OP_newfunction: {
          int method_id = readU32();
          processor.processFunction(abc.methods[method_id], true, abc, "", "");
          abc.methods[method_id].anon = true;
          break;
        }
        case OP_callstatic:
          processor.processFunction(abc.methods[readU32()], true, abc, "", "");
          processor.append(" (" + readU32() + ")");
          break;
        case OP_newclass:
          processor.append(abc.instances[readU32()].toString());
          break;
        case OP_lookupswitch: {
          int target = start + readS24();
          int maxindex = readU32();
          processor.append("default:" + labels.labelFor(target)); // target + "("+(target-pos)+")"
          processor.append(" maxcase:" + maxindex);
          for (int i = 0; i <= maxindex; i++) {
            target = start + readS24();
            processor.append(" " + labels.labelFor(target)); // target + "("+(target-pos)+")"
          }
          break;
        }
        case OP_jump:
        case OP_iftrue:
        case OP_iffalse:
        case OP_ifeq:
        case OP_ifne:
        case OP_ifge:
        case OP_ifnge:
        case OP_ifgt:
        case OP_ifngt:
        case OP_ifle:
        case OP_ifnle:
        case OP_iflt:
        case OP_ifnlt:
        case OP_ifstricteq:
        case OP_ifstrictne: {
          int offset = readS24();
          int target = code.getPosition() + offset;
          //s += target + " ("+offset+")"
          processor.append(labels.labelFor(target));
          if (!(labels.containsKey(code.getPosition()))) processor.append("\n");
          break;
        }
        case OP_inclocal:
        case OP_declocal:
        case OP_inclocal_i:
        case OP_declocal_i:
        case OP_getlocal:
        case OP_kill:
        case OP_setlocal:
        case OP_debugline:
        case OP_getglobalslot:
        case OP_getslot:
        case OP_setglobalslot:
        case OP_setslot:
        case OP_pushshort:
        case OP_newcatch:
          processor.append(String.valueOf(readU32()));
          break;
        case OP_debug:
          processor.append(String.valueOf(code.readUnsignedByte()));
          processor.append(" " + readU32());
          processor.append(" " + code.readUnsignedByte());
          processor.append(" " + readU32());
          break;
        case OP_newobject:
          processor.append("{" + readU32() + "}");
          break;
        case OP_newarray:
          processor.append("[" + readU32() + "]");
          break;
        case OP_call:
        case OP_construct:
        case OP_constructsuper:
          processor.append("(" + readU32() + ")");
          break;
        case OP_pushbyte:
        case OP_getscopeobject:
          processor.append(String.valueOf(code.readByte()));
          break;
        case OP_hasnext2:
          processor.append(readU32() + " " + readU32());
        default:
          /*if (opNames[opcode] == ("0x"+opcode.toString(16).toUpperCase()))
      s += " UNKNOWN OPCODE"*/
          break;
      }

      int size = code.getPosition() - start;
      abc.totalSize += size;
      abc.opSizes[opcode] += size;
      processor.append("\n");
    }
    processor.append(oldindent + "}\n");
  }

  int readU32() {
    return code.readU32();
  }

  int readS24() {
    int b = code.readUnsignedByte();
    b |= code.readUnsignedByte() << 8;
    b |= code.readByte() << 16;
    return b;
  }

  boolean isGetMethod() {
    return kind == TraitType.Getter;
  }

  boolean isSetMethod() {
    return kind == TraitType.Setter;
  }
}
