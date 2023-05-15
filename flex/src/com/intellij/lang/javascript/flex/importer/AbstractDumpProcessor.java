package com.intellij.lang.javascript.flex.importer;

import com.intellij.lang.javascript.refactoring.ECMAL4NamesValidator;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

abstract class AbstractDumpProcessor implements FlexByteCodeInformationProcessor {
  protected @NonNls StringBuilder sb = new StringBuilder();
  private boolean firstMetaDataMember;

  final Set<String> classNameTable = new HashSet<>();

  String getResult() { return sb.toString(); }

  @Override
  public void append(@NotNull @NonNls String str) {
    sb.append(str);
  }

  @Override
  public String getParentName(final MemberInfo member) {
    String parentName = null;

    if (member.parentTraits != null) {
      if(member.parentTraits.name instanceof Multiname multiname) {

        if(multiname.hasNamespace()) {
          parentName = multiname.name;
        }
      }
      if (parentName == null) {
        parentName = StringUtil.replace(member.parentTraits.name.toString(), "::", ".");
      }
    }
    return parentName;
  }

  public void addMetaDataValue(String s, String s1) {
    append(firstMetaDataMember ? "(":",");
    firstMetaDataMember = false;
    if (s != null) {
      append(s);
      append("=");
    }
    append(s1);
  }

  @Override
  public void processVariable(SlotInfo info, String indent, String attr) {
    appendFieldSeparator();

    attr = appendModifiers(info, attr);
    processModifierList(info, attr, indent);
    append(indent + attr);
    processMemberKindAndName(info);

    append(":");
    dumpTypeRef(info.type, info.parentTraits.getClassName(), true);
    if (info.value != null) processValue(info.type, info.value);
  }

  @Override
  public void processFunction(MethodInfo methodInfo, boolean referenceNameRequested, Abc abc, String indent, String attr) {
    if (!referenceNameRequested) appendMethodSeparator();

    attr = appendModifiers(methodInfo, attr);
    processModifierList(methodInfo, attr, indent);

    append(indent + attr);

    processMemberKindAndName(methodInfo);
    final String parentName = methodInfo.getParentName();
    processArgumentList(methodInfo, parentName);
    append(":");
    dumpTypeRef(methodInfo.returnType, parentName, referenceNameRequested);
  }

  protected void dumpTypeRef(Multiname name, String parentName, boolean referenceNameRequested) {
    processMultinameAsPackageName(name, parentName);
  }

  protected void processMemberKindAndName(@NotNull final MemberInfo member) {
    append(Abc.traitKinds[member.kind != null ? member.kind.ordinal():0]);
    append(" ");

    if (member.name != null) {
      processMultinameAsPackageName(member.name, member.parentTraits.getClassName());
    } else {
      append("undefined");
    }
  }

  protected abstract String appendModifiers(MemberInfo methodInfo, String attr);
  protected abstract void processValue(Multiname type, Object value);

  protected void processArgumentList(MethodInfo methodInfo, String parentName) {
    append("(");

    for (int i = 0; i < methodInfo.paramTypes.length; ++i) {
      final Multiname paramType = methodInfo.paramTypes[i];
      final boolean restParameter = FlexByteCodeInformationProcessor.REST_PARAMETER_TYPE.equals(paramType.name);
      if (restParameter && !dumpRestParameter()) break; // original one do not dump
      if (i > 0) append(",");

      String name = methodInfo.paramNames != null && ECMAL4NamesValidator.isIdentifier(methodInfo.paramNames[i])
                    ? methodInfo.paramNames[i]
                    : "a" + (i > 0 ? String.valueOf(i + 1) : "");
      processParameter(
        name,
        paramType,
        parentName,
        methodInfo.optionalValues != null && i < methodInfo.optionalValues.length ? methodInfo.optionalValues[i] : null,
        restParameter
      );
    }

    append(")");
  }

  protected abstract boolean dumpRestParameter();

  @Override
  public void processMetadata(MetaData metaData) {
    append("[");
    append(metaData.name);
    firstMetaDataMember = true;

    for (String n : metaData.keySet()) {
      addMetaDataValue(!"*".equals(n) || doStarMetaAttrNameDump() ?  n:null, '"' + quote(metaData.get(n)) + '"');
    }

    if (!firstMetaDataMember) append(")");
    append("]");
  }

  @Override
  public void processClass(SlotInfo slotInfo, Abc abc, String attr, String indent) {
    append("\n");

    @NonNls String def;
    final boolean isInterface = slotInfo.isInterfaceClass();
    if (isInterface) def = "interface";
    else {
      def = "class";
    }

    if (!doStarTypeDumpInExtends()) {
      final String ns = slotInfo.name.hasNamespace() ? slotInfo.name.getNsName(slotInfo) : null;

      if (ns != null && ns.length() > 0) attr += ns;
      else attr+="public";
      attr += " ";
    }

    Traits ct = (Traits)slotInfo.value;
    Traits it = ct.itraits;
    if (!isInterface) {
      if ((it.flags & Abc.CLASS_FLAG_final) != 0) attr += "final ";
      if ((it.flags & Abc.CLASS_FLAG_sealed) == 0) attr += "dynamic ";
    }

    processModifierList(slotInfo, attr, indent);

    append(indent + attr + def + " ");
    if (slotInfo.name.hasNotEmptyNs()) {
      classNameTable.add(slotInfo.name.nsset[0] + ":" + slotInfo.name.name);
    }
    processMultinameAsPackageName(slotInfo.name, null);
    dumpExtendsList(it);

    append("\n");
    String oldindent = indent;
    indent += Abc.TAB;

    dumpInterfacesList(indent, it, isInterface);
    append(oldindent + "{\n");
    setProcessingInterface(isInterface);

    if (doDumpMember(it.init)) it.init.dump(abc, indent, "", this);
    it.dump(abc, indent, "", this);
    ct.dump(abc, indent, "static ", this);
    ct.init.dump(abc, indent, "static ", this);
    if (ct.usedNamespacesToNamesMap != null) {
      String[] strings = new String[ct.usedNamespacesToNamesMap.size()];
      ct.usedNamespacesToNamesMap.keySet().toArray(strings);
      Arrays.sort(strings);
      for(String e: strings) {
        SlotInfo info = new SlotInfo(new Multiname(Abc.PRIVATE_NS_SET, ct.usedNamespacesToNamesMap.get(e)), Abc.TraitType.Const);
        info.type = NsType;
        info.value = e;
        info.parentTraits = it;

        info.dump(abc, indent, "static ", this);
      }
    }
    append(oldindent + "}\n");
    setProcessingInterface(false);
  }

  private static final Multiname NsType = new Multiname(Abc.PUBLIC_NS_SET, "*");

  protected void processModifierList(MemberInfo memberInfo, String attr, String indent) {
    memberInfo.dumpMetaData(indent, this);
  }

  protected void dumpExtendsList(Traits it) {
    if (!it.base.isStarReference() || doStarTypeDumpInExtends()) {
      append(" extends ");
      dumpTypeRef(it.base, null, true);
    }
  }

  protected void dumpInterfacesList(String indent, Traits it, boolean anInterface) {
    if (it.interfaces.length > 0) {
      append(indent + (anInterface && this instanceof AS3InterfaceDumper ? "extends ":"implements "));
      boolean first = true;

      for (Multiname name : it.interfaces) {
        if (!first) append(",");
        first = false;
        dumpTypeRef(name,null, true);
      }
      append("\n");
    }
  }

  protected static String quote(final String s) {
    if (s.length() == 0) return s;
    final StringBuilder b = new StringBuilder(s.length());

    for(int i = 0; i < s.length(); ++i) {
      final char ch = s.charAt(i);

      if (ch == '\\' || ch == '"') {
        b.append('\\');
      } else if (ch == '\n') {
        b.append("\\n");
        continue;
      } else if (ch == '\r') {
        b.append("\\r");
        continue;
      }
      b.append(ch);
    }
    return b.toString();
  }
}
