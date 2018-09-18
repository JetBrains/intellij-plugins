package com.intellij.lang.javascript.flex.importer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Mossienko
*/
class AbcDumper extends AbstractDumpProcessor {
  private final boolean dumpCode;

  AbcDumper(final boolean _dumpCode) {
    dumpCode = _dumpCode;
  }

  @Override
  public void dumpStat(@NotNull final String stat) {
    sb.append(stat);
  }

  @Override
  public void hasError(@NotNull final String error) {
    sb.append(error);
  }

  @Override
  protected String appendModifiers(MemberInfo member, String attr) {
    attr += (member instanceof MethodInfo && (((MethodInfo)member).flags & Abc.NATIVE) != 0 ? "native ":"");
    return attr;
  }

  @Override
  public void processMultinameAsPackageName(@NotNull final Multiname name,
                                            @Nullable final String parentName) {
    append(name.hasNotEmptyNs() ? (name.getValidNsName(classNameTable) + "::" + name.name) : name.name);
  }

  @Override
  public void dumpToplevelAnonymousMethod(final @NotNull Abc abc, final @NotNull MethodInfo m) {
    m.dump(abc, "", "", this);
  }

  @Override
  public void dumpTopLevelTraits(final Abc abc, final @NotNull Traits t, final String indent) {
    sb.append(indent + t.name + "\n");
    t.dump(abc, indent, "", this);
    t.init.dump(abc, indent, "", this);
  }

  @Override
  public boolean doDumpMember(final @NotNull MemberInfo memberInfo) {
    return true;
  }

  @Override
  public void appendMethodSeparator() {
    append("\n");
  }

  @Override
  public void appendFieldSeparator() {
    append("");
  }

  @Override
  public String getAbcInSwfIndent() {
    return "  ";
  }

  @Override
  public void processValue(final Multiname type, final Object value) {
    append(" = " + (value instanceof String ? ('"' + value.toString() + '"') : value));
  }

  @Override
  public boolean doDumpMetaData(final @NotNull MetaData md) {
    return true;
  }

  @Override
  public void processParameter(@NotNull String name, @Nullable Multiname type, String parentName, @Nullable Multiname value, boolean rest) {
    processMultinameAsPackageName(type, parentName);
  }

  @Override
  public boolean doStarTypeDumpInExtends() {
    return true;
  }

  @Override
  public boolean doStarMetaAttrNameDump() {
    return true;
  }

  @Override
  public void setProcessingInterface(final boolean anInterface) {
  }

  @Override
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
