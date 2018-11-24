package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Dennis.Ushakov
 */
public class FrameworkDependencyResolver {
  private static final Logger LOG = Logger.getInstance(InheritanceInfoHolder.class);
  private Map<String, Map<String, List<String>>> myDependencyInfo = new HashMap<>();
  private final BridgeSupportLoader myLoader;

  public static FrameworkDependencyResolver getInstance() {
    return ServiceManager.getService(FrameworkDependencyResolver.class);
  }

  public FrameworkDependencyResolver(final BridgeSupportLoader loader) {
    myLoader = loader;
    final String path = RubyUtil.getScriptFullPath("rb/motion");
    final File file = new File(path);
    if (file.exists() && file.isDirectory()) {
      for (File child : file.listFiles()) {
        final String name = child.getName();
        if (name.endsWith(".yaml") && name.startsWith("dependencies.")) {
          try {
            final FileInputStream is = new FileInputStream(child);
            try {
              final Map map = RubyUtil.loadYaml(is);
              final Map<String, List<String>> result = new HashMap<>();
              for (Object key : map.keySet()) {
                final ArrayList list = (ArrayList)map.get(key);
                final List<String> stringList = new ArrayList<>(list.size());
                for (Object o : list) {
                  stringList.add(o.toString());
                }
                result.put(key.toString(), stringList);
              }
              myDependencyInfo.put(name.replaceAll("dependencies.", "").replaceAll(".yaml", ""), result);
            }
            finally {
              is.close();
            }
          }
          catch (IOException e) {
            LOG.error(e);
          }
        }
      }
    }
  }

  public Collection<Framework> getFrameworks(final Module module) {
    final String sdkVersion = RubyMotionUtil.getInstance().getSdkVersion(module);
    final String[] frameworks = RubyMotionUtil.getInstance().getRequiredFrameworks(module);
    final Map<String, List<String>> dependencyInfo = myDependencyInfo.get(sdkVersion);

    if (dependencyInfo == null) {
      LOG.warn("Could not find dependency info for version: '" + sdkVersion + "'");
    }

    final Set<Framework> result = new HashSet<>();
    final Queue<String> unsatisfied = new LinkedList<>();
    final Set<String> processed = new HashSet<>();
    Collections.addAll(unsatisfied, frameworks);
    while (!unsatisfied.isEmpty()) {
      final String name = unsatisfied.poll();
      if (processed.contains(name)) continue;

      ContainerUtil.addIfNotNull(result, myLoader.getFramework(sdkVersion, name));
      final List<String> deps = dependencyInfo != null ? dependencyInfo.get(name) : null;
      if (deps != null) {
        unsatisfied.addAll(deps);
      }
      processed.add(name);
    }
    return result;
  }
}
