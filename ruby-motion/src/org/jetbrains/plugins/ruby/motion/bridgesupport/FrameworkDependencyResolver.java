/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion.bridgesupport;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.motion.RubyMotionUtil;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Dennis.Ushakov
 */
public class FrameworkDependencyResolver {
  private static final Logger LOG = Logger.getInstance(InheritanceInfoHolder.class);
  private final Map<String, Map<String, List<String>>> myDependencyInfo = new HashMap<>();
  private final BridgeSupportLoader myLoader;

  public static FrameworkDependencyResolver getInstance() {
    return ServiceManager.getService(FrameworkDependencyResolver.class);
  }

  public FrameworkDependencyResolver(final BridgeSupportLoader loader) {
    myLoader = loader;
    final Stream<Path> childrenStream;
    try {
      childrenStream = Files.list(getScriptsDir());
    }
    catch (IOException e) {
      LOG.error(e);
      return;
    }
    
    childrenStream.forEach((child) -> {
      final String name = child.getFileName().toString();
      if (name.endsWith(".yaml") && name.startsWith("dependencies.")) {
        try {
          final InputStream is = Files.newInputStream(child);
          try {
            final Map map = RubyUtil.loadYaml(is);
            if (map == null) {
              return;
            }
            
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
    });
  }

  public Collection<Framework> getFrameworks(final Module module) {
    final String sdkVersion = RubyMotionUtil.getInstance().getSdkVersion(module);
    final String[] frameworks = RubyMotionUtil.getInstance().getRequiredFrameworks(module);
    Map<String, List<String>> dependencyInfo = myDependencyInfo.get(sdkVersion);

    if (dependencyInfo == null) {
      LOG.warn("Could not find dependency info for version: '" + sdkVersion + "'");
      if (RubyMotionUtil.getInstance().isOSX(module)) {
        dependencyInfo = myDependencyInfo.get("10.10");
      } else if (!RubyMotionUtil.getInstance().isAndroid(module)) {
        dependencyInfo = myDependencyInfo.get("9.3");
      }
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

  @NotNull
  static Path getScriptsDir() {
    URL dirUrl = FrameworkDependencyResolver.class.getResource("/rb/motion");

    final URI uri;
    try {
      uri = dirUrl.toURI();
    }
    catch (URISyntaxException e) {
      throw new AssertionError("Incorrect scripts dir uri");
    }

    try {
      FileSystems.newFileSystem(uri, new HashMap<>());
    }
    catch (IllegalArgumentException | FileSystemAlreadyExistsException | IOException ignore) {
    }
    
    final Path path = Paths.get(uri);
    
    assert Files.exists(path);
    return path;
  }
}
