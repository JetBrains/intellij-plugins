package com.intellij.lang.javascript.flex.importer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim.Mossienko
*/
interface FlexByteCodeInformationProcessor {
  void dumpStat(@NotNull @NonNls String stat);
  void hasError(@NotNull String error);
  void append(@NotNull @NonNls String str);

  void processMultinameAsPackageName(@NotNull Multiname name, @Nullable String parentName);

  void dumpToplevelAnonymousMethod(final @NotNull Abc abc, final @NotNull MethodInfo m);

  void dumpTopLevelTraits(final @NotNull Abc abc, final @NotNull Traits t, final String indent);

  boolean doDumpMember(final @NotNull MemberInfo memberInfo);
  void appendMethodSeparator();
  void appendFieldSeparator();
  String getAbcInSwfIndent();
  
  boolean doDumpMetaData(final @NotNull MetaData md);

  String REST_PARAMETER_TYPE = "...";
  void processParameter(final @NotNull String name, @Nullable Multiname type, String parentName, @Nullable Multiname value, boolean rest);

  boolean doStarTypeDumpInExtends();
  boolean doStarMetaAttrNameDump();

  void setProcessingInterface(final boolean anInterface);

  String getParentName(final MemberInfo member);

  void processVariable(SlotInfo info, String indent, String attr);

  void processFunction(MethodInfo methodInfo, boolean referenceNameRequested, Abc abc, String indent, String attr);

  void processMetadata(MetaData metaData);

  void processClass(SlotInfo slotInfo, Abc abc, String attr, String indent);
}
