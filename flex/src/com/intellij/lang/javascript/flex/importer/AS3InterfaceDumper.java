// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.importer;

import com.intellij.lang.javascript.JSFlexAdapter;
import com.intellij.lang.javascript.JSKeywordSets;
import com.intellij.lang.javascript.dialects.ECMAL4LanguageDialect;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author Maxim.Mossienko
*/
class AS3InterfaceDumper extends AbstractDumpProcessor {
  private int memberCount;
  private boolean isInterface;
  private boolean myDumpTypeRef;

  private static final @NonNls Set<String> doNotNeedQoting =
    Set.of("null", "NaN", "undefined", "true", "false", "Infinity", "-Infinity");

  @Override
  public void dumpToplevelAnonymousMethod(final @NotNull Abc abc, final @NotNull MethodInfo m) {}

  @Override
  public void dumpTopLevelTraits(final Abc abc, final @NotNull Traits t, final String indent) {
    t.dump(abc, indent, "", this);
  }

  private static final Lexer ourLexer = new JSFlexAdapter(ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER);

  @Override
  public boolean doDumpMember(final @NotNull MemberInfo memberInfo) {
    if (memberInfo.name == null) return false;
    if (memberInfo.name.name != null) {
      if(memberInfo.name.name.contains(Abc.$CINIT)) return false;
      if (!StringUtil.isJavaIdentifier(memberInfo.name.name)) return false;

      if (!JSKeywordSets.IDENTIFIER_TOKENS_SET.contains(identifierType(memberInfo.name.name)) ) {
        return false;
      }
      if (isInterface) {
        return memberInfo.parentTraits.init != memberInfo;
      }
    }
    return true;
  }

  public static synchronized IElementType identifierType(String name) {
    ourLexer.start(name);

    if (ourLexer.getTokenEnd() != name.length()) {
      return null;
    }
    return ourLexer.getTokenType();
  }

  @Override
  public void appendMethodSeparator() {
    append((++memberCount % 5) == 0? "\n":"");
  }

  @Override
  public void appendFieldSeparator() {
    appendMethodSeparator();
  }

  @Override
  public String getAbcInSwfIndent() {
    return "";
  }

  @Override
  public void processValue(final Multiname typeName, final Object valueObject) {
    append(" = ");
    append(getValueRepr(valueObject));
  }

  protected static String getValueRepr(Object valueObject) {
    if (valueObject == null) return null;
    @NonNls String value = valueObject.toString();
    char ch;

    if (needsQuoting(value)) {
      boolean doQoute = true;

      if(value.indexOf('.') != -1) {
        try {
          Double.parseDouble(value);
          doQoute=false;
        } catch (NumberFormatException ex) {}
      } else if (!value.isEmpty() && (Character.isDigit(ch = value.charAt(0)) || (ch == '-' && value.length() > 1 && Character.isDigit(value.charAt(1))))) {
        try {
          Integer.parseInt(value);
          doQoute=false;
        } catch (NumberFormatException ex) {}
      }

      if (doQoute) {
        value = "\"" + quote(value) + "\"";
      }
    }
    return value;
  }

  @Override
  public void dumpStat(final @NotNull String stat) {}
  private static boolean needsQuoting(final String value) {
    return !doNotNeedQoting.contains(value);
  }

  @Override
  public boolean doDumpMetaData(final @NotNull MetaData md) {
    return !md.name.contains("__");
  }

  @Override
  public void processParameter(@NotNull String name, @Nullable Multiname type, String parentName, @Nullable Multiname value, boolean rest) {
    if (rest) {
      append("... ");
      append(name);
    }
    else {
      append(name);
      append(":");
      dumpTypeRef(type, parentName, true);
      if (value != null) {
        processValue(type, value);
      }
    }
  }

  @Override
  protected void dumpTypeRef(Multiname name, String parentName, boolean referenceNameRequested) {
    myDumpTypeRef = true;
    super.dumpTypeRef(name, parentName,referenceNameRequested);
    myDumpTypeRef = false;
  }

  @Override
  public boolean doStarTypeDumpInExtends() {
    return false;
  }

  @Override
  public boolean doStarMetaAttrNameDump() {
    return false;
  }

  @Override
  public void setProcessingInterface(final boolean anInterface) {
    isInterface = anInterface;
  }

  @Override
  public void hasError(final @NotNull String error) {
    sb.append("/*" + error + "*/");
  }

  @Override
  public void processMultinameAsPackageName(Multiname name, String parentName) {
    append(getMultinameAsPackageName(name, parentName));
  }

  protected String getTypeRef(Multiname name, String parentName, boolean serialize) {
    try {
      myDumpTypeRef = true;
      String qName = getMultinameAsPackageName(name, parentName);
      if (qName != null && !qName.isEmpty() && serialize) {
        if ("...".equals(qName)) return null;
        return serializeQName(qName);
      }

      return qName;
    } finally {
      myDumpTypeRef = false;
    }
  }

  protected @NotNull String serializeQName(String qName) {
    JSType type = JSNamedTypeFactory.createType(qName, JSTypeSource.EMPTY_AS, JSContext.INSTANCE);
    return JSTypeUtils.serializeType(type);
  }

  protected String getMultinameAsPackageName(Multiname name, String parentName) {
    if (name.nsset == null || (name.nsset.length == 1 && name.nsset[0].equals(Abc.PUBLIC_NS))) {
      return name.name;
    }
    else {
      final String validNsName = name.getValidNsName(classNameTable);
      final int prefLength = "::".length();

      if (Multiname.hasNamespace(validNsName) ||
          (parentName != null && //parentName.equals(validNsName + "::" + name.name);
            parentName.regionMatches(0, validNsName, 0, validNsName.length()) &&
            parentName.regionMatches(validNsName.length(), "::", 0, prefLength) &&
            parentName.regionMatches(validNsName.length() + prefLength, name.name, 0, name.name.length()) &&
            parentName.length() == validNsName.length() + prefLength + name.name.length())
        ) {
        return name.name;
      }

      if (parentName != null &&
          !myDumpTypeRef &&
          willDumpNsName(name, parentName, false, false, isTopLevelObject(parentName), validNsName)) {
        return name.name;
      }
      return validNsName + "." + name.name;
    }
  }

  @Override
  protected String appendModifiers(MemberInfo member, String attr) {
    @NonNls String s = attr;

    s += "native ";

    String nsName = member.name.getNsName(member);
    if (nsName != null && !nsName.isEmpty()) s += nsName + " ";

    if (member.isFinal) s+= "final ";
    if (member.isOverride) s+= "override ";
    return s;
  }

  private static boolean isTopLevelObject(String parentName) {
    return parentName != null && parentName.startsWith("script");
  }

  private static boolean willDumpNsName(Multiname name, String parentName, boolean memberInParentNs, boolean constructor, boolean topLevelObject, String nsName) {
    return name != null && name.hasNotEmptyNs() && parentName != null &&
        ((!constructor && !topLevelObject && !memberInParentNs) || nsName.contains("private"));
  }

  @Override
  public void processFunction(MethodInfo methodInfo, boolean referenceNameRequested, Abc abc, String indent, String attr) {
    super.processFunction(methodInfo, referenceNameRequested, abc, indent, attr);
    append(";\n");
  }

  @Override
  public void processVariable(SlotInfo info, String indent, String attr) {
    super.processVariable(info, indent, attr);
    append(";\n");
  }

  @Override
  protected boolean dumpRestParameter() {
    return true;
  }
}
