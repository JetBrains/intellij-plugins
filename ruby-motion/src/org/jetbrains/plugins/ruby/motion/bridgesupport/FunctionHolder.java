package org.jetbrains.plugins.ruby.motion.bridgesupport;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class FunctionHolder extends Sealable {
  private final Map<String, Function> myFunctions = new HashMap<String, Function>();

  public Collection<Function> getFunctions() {
    return myFunctions.values();
  }

  public void addFunction(Function function) {
    checkSeal();
    myFunctions.put(function.getName(), function);
  }

  public Function getFunction(final String name) {
    return myFunctions.get(name);
  }
}
