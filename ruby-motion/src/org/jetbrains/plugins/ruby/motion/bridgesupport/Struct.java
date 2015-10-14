package org.jetbrains.plugins.ruby.motion.bridgesupport;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class Struct extends Sealable {
  private Map<String, String> myFields = new LinkedHashMap<String, String>();
  private final String myName;

  public Struct(String name) {
    myName = name;
  }

  public String getName() {
    return myName;
  }

  public Collection<String> getFields() {
    return myFields.keySet();
  }

  public void addField(String name, String declaredType) {
    checkSeal();
    myFields.put(name, declaredType);
  }

  public String getFieldType(String name) {
    return myFields.get(name);
  }
}
