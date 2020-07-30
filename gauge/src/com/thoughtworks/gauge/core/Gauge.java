/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.core;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.thoughtworks.gauge.reference.ReferenceCache;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Gauge {
  private static final Map<Module, GaugeService> gaugeProjectHandle = new ConcurrentHashMap<>();
  private static final HashMap<String, HashSet<Module>> linkedModulesMap = new HashMap<>();
  private static final Map<Module, ReferenceCache> moduleReferenceCaches = new ConcurrentHashMap<>();

  public static void addModule(Module module, GaugeService gaugeService) {
    HashSet<Module> modules = getSubModules(module);
    if (modules.isEmpty()) modules.add(module);
    modules.forEach(m -> gaugeProjectHandle.put(m, gaugeService));
  }

  public static GaugeService getGaugeService(Module module, boolean moduleDependent) {
    if (module == null) {
      return null;
    }
    GaugeService service = gaugeProjectHandle.get(module);
    if (service != null) return service;
    Set<Module> modules = getSubModules(module);
    for (Module m : modules) {
      service = gaugeProjectHandle.get(m);
      if (service != null) {
        addModule(module, service);
        return service;
      }
    }
    return moduleDependent ? null : getGaugeService();
  }

  public static ReferenceCache getReferenceCache(Module module) {
    ReferenceCache referenceCache = moduleReferenceCaches.get(module);
    if (referenceCache == null) {
      referenceCache = new ReferenceCache();
      moduleReferenceCaches.put(module, referenceCache);
    }
    return referenceCache;
  }

  public static HashSet<Module> getSubModules(Module module) {
    String value = getProjectGroupValue(module);
    HashSet<Module> modules = linkedModulesMap.get(value);
    if (modules != null) return modules;
    modules = new HashSet<>();
    for (Module m : ModuleManager.getInstance(module.getProject()).getModules()) {
      if (getProjectGroupValue(m).contains(value)) {
        modules.add(m);
        addToModulesMap(m, value);
      }
    }
    return modules;
  }

  private static void addToModulesMap(Module module, String name) {
    if (!linkedModulesMap.containsKey(name)) {
      linkedModulesMap.put(name, new HashSet<>());
    }
    linkedModulesMap.get(name).add(module);
  }

  @NotNull
  private static String getProjectGroupValue(Module module) {
    String[] values = ModuleManager.getInstance(module.getProject()).getModuleGroupPath(module);
    return values == null || values.length < 1 ? module.getName() : values[0];
  }

  private static GaugeService getGaugeService() {
    Iterator<GaugeService> iterator = gaugeProjectHandle.values().iterator();
    return iterator.hasNext() ? iterator.next() : null;
  }

  public static void disposeComponent(Module module) {
    if (module == null) return;
    String value = getProjectGroupValue(module);
    linkedModulesMap.remove(value);
    GaugeService service = gaugeProjectHandle.get(module);
    if (service != null && service.getGaugeProcess().isAlive()) service.getGaugeProcess().destroy();
  }
}
