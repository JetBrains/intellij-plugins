// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.importer;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.util.containers.BidirectionalMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Maxim.Mossienko
*/
class Multiname {
  private static final String PUBLIC_NS_IN_SOURCE = "public";
  private static final String AS3_VEC = "__AS3__.vec";
  String[] nsset;
  String name;

  Multiname(String[] nsset, String name) {
    this.nsset = nsset;
    this.name = name;
  }

  @Override
  public String toString() {
    String s = "";
    if (hasNotEmptyNs()) s += nsset[0] + "::";
    s += name;

    return s;
  }

  boolean hasNotEmptyNs() {
    return nsset != null && nsset.length > 0 && nsset[0] != null && !nsset[0].isEmpty();
  }

  public boolean hasNamespace() {
    return hasNotEmptyNs() && hasNamespace(nsset[0]);
  }

  public static boolean hasNamespace(String ns) {
    return ns.equals(Abc.PRIVATE_NS) || ns.equals(AS3_VEC) || ns.indexOf('$') != -1 || ns.indexOf(':') != -1;
  }

  private static final Map<String, String> predefined = new HashMap<>();
  static {
    predefined.put("http://adobe.com/AS3/2006/builtin", "AS3");
    predefined.put("http://www.adobe.com/2006/flex/mx/internal", "mx_internal");
    predefined.put(AS3_VEC, "__AS3__$vec");
    predefined.put("http://www.adobe.com/2008/actionscript/Flash10/", "flash10");
    predefined.put("http://www.adobe.com/2006/actionscript/flash/objectproxy", "object_proxy");
    predefined.put("http://www.adobe.com/2006/actionscript/flash/proxy", "flash_proxy");
  }

  private static String makeNsIdentifier(String ns, Traits parentTraits) {
    String prefix;
    String predefinedName = predefined.get(ns);

    if (predefinedName != null) prefix = predefinedName;
    else {

      int i = ns.lastIndexOf('/');
      if (i != -1) {
        int i2 = ns.lastIndexOf('/', i - 1);
        if (i2 != -1) i = i2;
      } else {
        i = ns.lastIndexOf('.');
        if (i != -1) {
          int i2 = ns.lastIndexOf('.', i - 1);
          if (i2 != -1) i = i2;
        }
      }

      prefix = i != -1 ? makeIdentifier(ns.substring(i + 1)):"ns";
    }

    int nsCount = 1;

    while(true) {
      String s = prefix + (nsCount++ == 1 ? "":nsCount);
      if (!parentTraits.names.containsKey(s)) {
        parentTraits.names.put(ns, null);
        return s;
      }
    }
  }

  public String getNsName(MemberInfo mi) {
    String ns = nsset[0];
    if (ns.isEmpty()) return PUBLIC_NS_IN_SOURCE;
    if (Abc.PRIVATE_NS.equals(ns)) return ns;
    if (mi.parentTraits.name == mi.name) return PUBLIC_NS_IN_SOURCE;

    Traits parentTraits = mi.parentTraits;
    if (ns.equals(parentTraits.protectedNs) ||
        (parentTraits.itraits != null && ns.equals(parentTraits.itraits.protectedNs))) {
      return "protected";
    }

    if (parentTraits.name instanceof Multiname parentName) {
      String parentNs = parentName.nsset[0];

      if (parentNs.equals(ns)) {
        return "internal";
      } else {
        int i = ns.indexOf(':');

        if (i != -1 &&
            ns.regionMatches(0, parentNs, 0, parentNs.length()) &&
            ns.regionMatches(parentNs.length() + 1, parentName.name, 0, parentName.name.length()) &&
            ns.charAt(parentNs.length()) == ':' &&
            ns.length() == parentNs.length() + parentName.name.length() + 1
          ) {
          return "";
        } else if (i == -1 && ns.equals(parentName.name) && parentNs.isEmpty()) {
          return "";
        }
      }
    } else if (parentTraits.name instanceof String parentName) {

      if (parentName.startsWith(Abc.SCRIPT_PREFIX)) {
        if (mi.kind == Abc.TraitType.Const) {    // namespace reference
          SlotInfo slotInfo = (SlotInfo)mi;

          if (slotInfo.type.isStarReference() && slotInfo.value instanceof String) {
            return PUBLIC_NS_IN_SOURCE;
          }
        }

        String predefinedNs = predefined.get(ns);     // for Vector class def
        if (predefinedNs != null) return predefinedNs;
        return "public"; // TODO: how to distinguish between public function / constant and not public one
      }
      if (ns.regionMatches(0, parentName, 0, ns.length())) {
        if (ns.length() < parentName.length() && parentName.charAt(ns.length()) == ':') {
          return "internal"; // mx.binding::debugDestinationStrings in mx.binding::BindingManager$
        }
      }
    }

    if (parentTraits.staticTrait != null) parentTraits = parentTraits.staticTrait;

    if (parentTraits.usedNamespacesToNamesMap == null) {
      parentTraits.usedNamespacesToNamesMap = new BidirectionalMap<>();
    }

    String varName = parentTraits.usedNamespacesToNamesMap.get(ns);
    if (varName == null) {
      varName = makeNsIdentifier(ns, parentTraits);
      parentTraits.usedNamespacesToNamesMap.put(ns, varName);
    }

    return varName;
  }

  public String getValidNsName(Set<String> classNameTable) {
    int nsIndex = 0;
    if (nsset.length > 1) {
      for (int i = 0; i < nsset.length; i++) {
        String ns = nsset[i];
        if (!ns.equals(Abc.PUBLIC_NS) && !ns.equals(Abc.PRIVATE_NS) && classNameTable.contains(ns + ":" + name)) {
          nsIndex = i;
          break;
        }
      }
    }
    return nsset[nsIndex];
  }

  private static String makeIdentifier(String s) {
    StringBuilder builder = new StringBuilder(s.length());

    for(int i = 0; i < s.length(); ++i) {
      char ch = s.charAt(i);
      if (!Character.isJavaIdentifierPart(ch)) ch = '_';

      builder.append(ch);
    }
    
    String s2 = builder.toString();
    if (// TODO: we should check !JSTokenTypes.IDENTIFIER_TOKEN_SET.contains(AS3InterfaceDumper.identifierType(s2)) once parser
        // can handle nonreserved keywords as namespace reference! 
        JSTokenTypes.IDENTIFIER != AS3InterfaceDumper.identifierType(s2)) {
      s2 = "_" + s2;
    }
    return s2;
  }

  public boolean isStarReference() {
    return "*".equals(name);
  }
}
