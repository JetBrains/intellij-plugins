package com.intellij.lang.javascript.flex.importer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Mossienko
*/
class AbcDumper extends AbstractDumpProcessor {
  private final boolean dumpCode;

  public AbcDumper(final boolean _dumpCode) {
    dumpCode = _dumpCode;
  }

  public void dumpStat(@NotNull final String stat) {
    sb.append(stat);
  }

  public void hasError(@NotNull final String error) {
    sb.append(error);
  }

  @Override
  protected String appendModifiers(MemberInfo member, String attr) {
    attr += (member instanceof MethodInfo && (((MethodInfo)member).flags & Abc.NATIVE) != 0 ? "native ":"");
    return attr;
  }

  public void processMultinameAsPackageName(@NotNull final Multiname name,
                                            @Nullable final String parentName) {
    append(name.hasNotEmptyNs() ? (name.getValidNsName(classNameTable) + "::" + name.name) : name.name);
  }

  public void dumpToplevelAnonymousMethod(final @NotNull Abc abc, final @NotNull MethodInfo m) {
    m.dump(abc, "", "", this);
  }

  public void dumpTopLevelTraits(final Abc abc, final @NotNull Traits t, final String indent) {
    sb.append(indent + t.name + "\n");
    t.dump(abc, indent, "", this);
    t.init.dump(abc, indent, "", this);
  }

  public boolean doDumpMember(final @NotNull MemberInfo memberInfo) {
    return true;
  }

  public void appendMethodSeparator() {
    append("\n");
  }

  public void appendFieldSeparator() {
    append("");
  }

  public String getAbcInSwfIndent() {
    return "  ";
  }

  public void processValue(final Multiname type, final Object value) {
    append(" = "+String.valueOf(value instanceof String ? ('"' + value.toString() + '"') : value));
  }

  public boolean doDumpMetaData(final @NotNull MetaData md) {
    return true;
  }

  public void processParameter(@NotNull String name, @Nullable Multiname type, String parentName, @Nullable Multiname value, boolean rest) {
    processMultinameAsPackageName(type, parentName);
  }

  public boolean doStarTypeDumpInExtends() {
    return true;
  }

  public boolean doStarMetaAttrNameDump() {
    return true;
  }

  public void setProcessingInterface(final boolean anInterface) {
  }

  protected boolean dumpRestParameter() {
    return false;
  }

  @Override
  public void processFunction(MethodInfo methodInfo, boolean referenceNameRequested, Abc abc, String indent, String attr) {
    super.processFunction(methodInfo, referenceNameRequested, abc, indent, attr);
    append("\t/* disp_id " + methodInfo.id + "*/");

    if (!referenceNameRequested) { // !verbose -> anonymouse
      append("\n");
      if (dumpCode && methodInfo.code != null) {
        methodInfo.dumpCode(abc, indent, this);
      }
    }
  }

  @Override
  public void processVariable(SlotInfo info, String indent, String attr) {
    super.processVariable(info, indent, attr);
    append("\t/* slot_id " + info.id + " */\n");
  }
}
