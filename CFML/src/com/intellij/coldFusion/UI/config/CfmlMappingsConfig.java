// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.config;

import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XMap;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

@Tag("mapps")
public class CfmlMappingsConfig implements Cloneable {
  private @NotNull Map<String, String> serverMappings = new HashMap<>();

  public CfmlMappingsConfig(@NotNull Map<String, String> mappings) {
    serverMappings = mappings;
  }

  @Property(surroundWithTag = false)
  @XMap(keyAttributeName = "logical_path", entryTagName = "mapping", valueAttributeName = "directory")
  public @NotNull Map<String, String> getServerMappings() {
    return serverMappings;
  }

  public void setServerMappings(@NotNull Map<String, String> serverMappings) {
    this.serverMappings = serverMappings;
  }

  public void putToServerMappings(String logPath, String dir) {
    this.serverMappings.put(logPath, dir);
  }

  public CfmlMappingsConfig() {
  }

  public List<String> mapVirtualToReal(@NotNull String virtualPath) {
    List<String> result = new LinkedList<>();

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
  public CfmlMappingsConfig clone() {
    HashMap<String, String> newServerMappings = new HashMap<>();
    newServerMappings.putAll(getServerMappings());
    return new CfmlMappingsConfig(newServerMappings);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final CfmlMappingsConfig that = (CfmlMappingsConfig)o;

    if (!getServerMappings().equals(that.getServerMappings())) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return getServerMappings().hashCode();
  }
}
