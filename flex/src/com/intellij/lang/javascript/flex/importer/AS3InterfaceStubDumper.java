// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.importer;

import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptFunctionStubImpl;
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptParameterStubImpl;
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptVariableStubImpl;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.lang.javascript.psi.stubs.JSReferenceListStub;
import com.intellij.lang.javascript.psi.stubs.impl.*;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

final class AS3InterfaceStubDumper extends AS3InterfaceDumper {
  private final LinkedList<StubElement> parents;
  private static final JSAttributeList.AccessType[] ourAccessTypes = JSAttributeList.AccessType.values();
  private static final JSAttributeList.ModifierType[] ourModifierTypes = JSAttributeList.ModifierType.values();

  AS3InterfaceStubDumper(StubElement parent) {
    parents = new LinkedList<>();
    parents.add(parent);
  }

  @Override
  public void processMetadata(MetaData metaData) {
    parents.addLast(new JSAttributeStubImpl(metaData.name, parents.getLast()));
    super.processMetadata(metaData);
    parents.removeLast();
  }

  @Override
  public void addMetaDataValue(String s, String s1) {
    new JSAttributeNameValuePairStubImpl(s, StringUtil.unquoteString(s1), parents.getLast());
  }

  @Override
  protected void processArgumentList(MethodInfo methodInfo, String parentName) {
    parents.add(new JSParameterListStubImpl(parents.getLast(), JSStubElementTypes.PARAMETER_LIST));
    super.processArgumentList(methodInfo, parentName);
    parents.removeLast();
  }

  @Override
    public void processParameter(@NotNull String name,
                               @Nullable Multiname type,
                               String parentName,
                               @Nullable Multiname value,
                               boolean rest) {
    new ActionScriptParameterStubImpl(
      name,
      rest,
      rest ? serializeQName(JSCommonTypeNames.ARRAY_CLASS_NAME) : getTypeRef(type, parentName, true),
      getValueRepr(value),
      parents.getLast()
    );
  }

  @Override
    public void append(@NotNull @NonNls String str) {}

  @Override
  public void processFunction(MethodInfo methodInfo, boolean referenceNameRequested, Abc abc, String indent, String attr) {
    parents.add(
      new ActionScriptFunctionStubImpl(
        methodInfo.name.name,
        methodInfo.isGetMethod() ? JSFunction.FunctionKind.GETTER :
        methodInfo.isSetMethod() ? JSFunction.FunctionKind.SETTER :
        methodInfo.parentTraits != null && methodInfo.parentTraits.name == methodInfo.name ? JSFunction.FunctionKind.CONSTRUCTOR :
        JSFunction.FunctionKind.SIMPLE,
        getMultinameAsPackageName(methodInfo.name,methodInfo.parentTraits != null ? methodInfo.parentTraits.getClassName():null),
        getTypeRef(methodInfo.returnType, methodInfo.getParentName(), true),
        "static ".equals(attr) ? JSContext.STATIC : JSContext.INSTANCE,
        getAccessType(methodInfo),
        parents.getLast()
      )
    );
    super.processFunction(methodInfo, referenceNameRequested, abc, indent, attr);
    parents.removeLast();
  }

  @Override
  public void processVariable(SlotInfo info, String indent, String attr) {
    parents.add(new JSVarStatementStubImpl(parents.getLast(), JSStubElementTypes.VAR_STATEMENT));
    super.processVariable(info, indent, attr);
    String parentName = info.getParentName();
    String qName = getMultinameAsPackageName(info.name, parentName);
    new ActionScriptVariableStubImpl(
      qName.substring(qName.lastIndexOf('.') + 1),
      info.isConst(),
      getTypeRef(info.type, parentName, true),
      getValueRepr(info.value),
      qName,
      "static ".equals(attr) ? JSContext.STATIC : JSContext.INSTANCE,
      getAccessType(info),
      parents.getLast()
    );
    parents.removeLast();
  }

  @Override
  public void processClass(SlotInfo slotInfo, Abc abc, String attr, String indent) {
    parents.add(
      new ActionScriptClassStubImpl(
        slotInfo.name.name,
        slotInfo.isInterfaceClass(),
        getMultinameAsPackageName(slotInfo.name, null),
        getAccessType(slotInfo),
        parents.getLast()
      )
    );
    super.processClass(slotInfo, abc, attr, indent);
    parents.removeLast();
  }

  @NotNull
  private static JSAttributeList.AccessType getAccessType(MemberInfo memberInfo) {
    final String nsName = memberInfo.name.getNsName(memberInfo);
    JSAttributeList.AccessType accessType = JSAttributeList.AccessType.PACKAGE_LOCAL;
    if ("public".equals(nsName)) accessType = JSAttributeList.AccessType.PUBLIC;
    else if ("protected".equals(nsName)) accessType = JSAttributeList.AccessType.PROTECTED;
    else if ("private".equals(nsName)) accessType = JSAttributeList.AccessType.PRIVATE;
    else if ("internal".equals(nsName)) accessType = JSAttributeList.AccessType.PACKAGE_LOCAL;
    return accessType;
  }

  @Override
  protected void processModifierList(MemberInfo memberInfo, String attr, String indent) {
    StringTokenizer tokenizer = new StringTokenizer(attr, " ");
    List<JSAttributeList.ModifierType> modifiers = new SmartList<>();
    JSAttributeList.AccessType accessType = null;
    String ns = null;

    while(tokenizer.hasMoreTokens()) {
      String next = tokenizer.nextToken();
      boolean foundModifier = false;

      for(JSAttributeList.AccessType type: ourAccessTypes) {
        if (next.equalsIgnoreCase(type.name())) {
          accessType = type;
          foundModifier = true;
          break;
        }
      }

      if (!foundModifier) {
        for(JSAttributeList.ModifierType type: ourModifierTypes) {
          if (next.equalsIgnoreCase(type.name())) {
            modifiers.add(type);
            foundModifier = true;
            break;
          }
        }
      }

      if (!foundModifier) ns = next;
    }

    Traits parentTraits = memberInfo.parentTraits;
    if (parentTraits.staticTrait != null) {
      parentTraits = parentTraits.staticTrait;
    }

    String resolvedNs = null;
    if (parentTraits.usedNamespacesToNamesMap != null) {
      List<String> keysByValue = parentTraits.usedNamespacesToNamesMap.getKeysByValue(ns);
      resolvedNs = keysByValue != null && keysByValue.size() > 0 ? keysByValue.get(0) : null;
    }
    parents.add(new ActionScriptAttributeListStubImpl(parents.getLast(), ns, resolvedNs, accessType, modifiers.toArray(
      new JSAttributeList.ModifierType[0])));
    super.processModifierList(memberInfo, attr, indent);
    parents.removeLast();
  }

  @Override
  protected void dumpExtendsList(Traits it) {
    if (!it.base.isStarReference()) {
      String ref = getTypeRef(it.base, null, false);
      JSReferenceListStub<JSReferenceList> parent =
        JSStubElementTypes.EXTENDS_LIST.createStub(parents.getLast());

      new JSReferenceListMemberStubImpl(parent, ref);
    }
  }

  @Override
  protected void dumpInterfacesList(String indent, Traits it, boolean anInterface) {
    if (it.interfaces.length > 0) {
      JSReferenceListStubImpl parent = new JSReferenceListStubImpl(parents.getLast(), anInterface
                                                                                    ? JSStubElementTypes.EXTENDS_LIST
                                                                                    : JSStubElementTypes.IMPLEMENTS_LIST);

      for (Multiname name : it.interfaces) {
         new JSReferenceListMemberStubImpl(parent, getTypeRef(name, null, false));
      }
    }
  }
}
