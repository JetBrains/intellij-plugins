package com.intellij.coldFusion.UI.config;

import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * @author Nadya Zabrodina
 */
@Deprecated
@Tag("mappings")
public class CfmlMappingsTemporalyConfigOld implements Cloneable {
  @NotNull
  private Map<String, String> serverMappings = new HashMap<String, String>();

  public CfmlMappingsTemporalyConfigOld(@NotNull Map<String, String> mappings) {
    serverMappings = mappings;
  }

  @NotNull
  @Property(surroundWithTag = false)
  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false, keyAttributeName = "logical_path",
                 entryTagName = "mapping", valueAttributeName = "directory", surroundValueWithTag = false)
  public Map<String, String> getServerMappings() {
    return serverMappings;
  }

  public void setServerMappings(@NotNull Map<String, String> serverMappings) {
    this.serverMappings = serverMappings;
  }

  public void putToServerMappings(String logPath, String dir) {
    this.serverMappings.put(logPath, dir);
  }

  public CfmlMappingsTemporalyConfigOld() {
  }

  public List<String> mapVirtualToReal(@NotNull String virtualPath) {
    List<String> result = new LinkedList<String>();

    Set<Map.Entry<String, String>> entries = getServerMappings().entrySet();
    for (Map.Entry<String, String> entry : entries) {
      StringTokenizer st_lp = new StringTokenizer(entry.getKey(), "\\/");
      StringTokenizer st = new StringTokenizer(virtualPath, ".");
      int numberOfTokens = st.countTokens();

      if (numberOfTokens < st_lp.countTokens()) {
        continue;
      }

      boolean checkFailed = false;
      while (st_lp.hasMoreTokens()) {
        if (!st_lp.nextToken().equals(st.nextToken())) {
          checkFailed = true;
          break;
        }
      }

      if (checkFailed) {
        continue;
      }

      StringBuilder relativePath = new StringBuilder(entry.getValue());

      while (st.hasMoreTokens()) {
        relativePath.append(File.separatorChar);
        relativePath.append(st.nextToken());
      }

      result.add(relativePath.toString());
    }
    return result;
  }

  @Override
  public CfmlMappingsTemporalyConfigOld clone() {
    HashMap<String, String> newServerMappings = new HashMap<String, String>();
    newServerMappings.putAll(getServerMappings());
    return new CfmlMappingsTemporalyConfigOld(newServerMappings);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final CfmlMappingsTemporalyConfigOld that = (CfmlMappingsTemporalyConfigOld)o;

    if (!getServerMappings().equals(that.getServerMappings())) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return getServerMappings().hashCode();
  }
}
