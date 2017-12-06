package com.intellij.lang.javascript.flex.importer;

import java.util.LinkedHashMap;

/**
 * @author Maxim.Mossienko
*/
class LabelInfo extends LinkedHashMap<Integer, String> {
  int count;

  String labelFor(int target) {
    if (containsKey(target)) return get(target);
    final String s = "L" + (++count);
    put(target, s);
    return s;
  }
}
