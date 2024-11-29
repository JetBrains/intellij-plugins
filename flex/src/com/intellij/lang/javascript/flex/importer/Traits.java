package com.intellij.lang.javascript.flex.importer;

import com.intellij.util.containers.BidirectionalMap;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Maxim.Mossienko
*/
class Traits {
  Object name;
  MethodInfo init;
  Traits itraits;

  Traits staticTrait;

  Multiname base;
  int flags;
  String protectedNs;
  Multiname[] interfaces;
  Map<String, MemberInfo> names = new LinkedHashMap<>();
  Map<Integer, SlotInfo> slots = new LinkedHashMap<>();
  Map<Integer, MethodInfo> methods = new LinkedHashMap<>();
  Map<Integer, MemberInfo> members = new LinkedHashMap<>();
  BidirectionalMap<String, String> usedNamespacesToNamesMap;

  @Override
  public String toString() {
    return name.toString();
  }

  public void dump(Abc abc, String indent, String attr, final FlexByteCodeInformationProcessor processor) {
    for (MemberInfo m : members.values())
      m.dump(abc, indent, attr, processor);
  }

  String getClassName() {
    final String s = name.toString();
    if (s.endsWith(Abc.$)) return s.substring(0, s.length() - 1);
    return s;
  }
}
