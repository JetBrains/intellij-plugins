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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.jetbrains.plugins.ruby.motion.bridgesupport.FrameworkDependencyResolver.getScriptsDir;

/**
 * @author Dennis.Ushakov
 */
public class InheritanceInfoHolder {
  private static final Logger LOG = Logger.getInstance(InheritanceInfoHolder.class);
  private final Map<String, Map<String, String>> myInheritanceInfo = new HashMap<>();

  public static InheritanceInfoHolder getInstance() {
    return ServiceManager.getService(InheritanceInfoHolder.class);
  }

  public InheritanceInfoHolder() {
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
      if (name.endsWith(".yaml") && name.startsWith("inheritance.")) {
        try {
          final InputStream is = Files.newInputStream(child);
          try {
            final Map map = RubyUtil.loadYaml(is);
            if (map == null) {
              return;
            }
            
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
    });
  }

  @Nullable
  public String getInheritance(final String className, final String frameworkVersion) {
    final Map<String, String> info = myInheritanceInfo.get(frameworkVersion);
    if (info == null) return null;
    return info.get(className);
  }
}
