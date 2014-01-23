package org.jetbrains.jps.osmorc.build;

import aQute.bnd.osgi.Analyzer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FakeAnalyzer extends Analyzer {

  private final Map<String, String> myProps;

  /**
   * Creates a fake analyzer instance around the given map. This is mostly done so the felix bnd maven plugin code doesn't have to be
   * changed that much.
   * @param props the properties to wrap
   */
  public FakeAnalyzer(@NotNull Map<String, String> props) {
    myProps = props;
  }

  @Override
  public String getProperty(String key) {
    return myProps.get(key);
  }

  @Override
  public String getProperty(String key, String fallback) {
    return myProps.containsKey(key) ? key : fallback;
  }

  @Override
  public void setProperty(String key, String value) {
    myProps.put(key, value);
  }
}
