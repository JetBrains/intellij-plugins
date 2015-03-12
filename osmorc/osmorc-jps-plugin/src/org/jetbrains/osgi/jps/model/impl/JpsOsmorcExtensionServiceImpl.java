/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package org.jetbrains.osgi.jps.model.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.model.LibraryBundlificationRule;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;

import java.util.List;

/**
 * @author michael.golubev
 */
public class JpsOsmorcExtensionServiceImpl extends JpsOsmorcExtensionService {
  private OsmorcGlobalExtensionProperties myGlobalProperties = new OsmorcGlobalExtensionProperties();

  @Override
  public void setGlobalProperties(@NotNull OsmorcGlobalExtensionProperties globalProperties) {
    myGlobalProperties = globalProperties;
  }

  @NotNull
  @Override
  public List<LibraryBundlificationRule> getLibraryBundlificationRules() {
    return myGlobalProperties.myLibraryBundlificationRules;
  }
}
