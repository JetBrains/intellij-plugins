package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class InheritanceInfoHolder {
  private static final Logger LOG = Logger.getInstance(InheritanceInfoHolder.class);
  private Map<String, Map<String, String>> myInheritanceInfo = new HashMap<>();

  public static InheritanceInfoHolder getInstance() {
    return ServiceManager.getService(InheritanceInfoHolder.class);
  }

  public InheritanceInfoHolder() {
    final String path = RubyUtil.getScriptFullPath("rb/motion");
    final File file = new File(path);
    if (file.exists() && file.isDirectory()) {
      for (File child : file.listFiles()) {
        final String name = child.getName();
        if (name.endsWith(".yaml") && name.startsWith("inheritance.")) {
          try {
            final FileInputStream is = new FileInputStream(child);
            try {
              final Map map = RubyUtil.loadYaml(is);
              final Map<String, String> result = new HashMap<>();
              for (Object key : map.keySet()) {
                result.put(key.toString(), map.get(key).toString());
              }
              myInheritanceInfo.put(name.replaceAll("inheritance.", "").replaceAll(".yaml", ""), result);
            } finally {
              is.close();
            }
          } catch (IOException e) {
            LOG.error(e);
          }
        }
      }
    }

  }

  @Nullable
  public String getInheritance(final String className, final String frameworkVersion) {
    final Map<String, String> info = myInheritanceInfo.get(frameworkVersion);
    if (info == null) return null;
    return info.get(className);
  }
}
