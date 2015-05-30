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
package org.jetbrains.osgi.xml;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import org.jetbrains.annotations.NotNull;

public class OsgiResourceProvider implements StandardResourceProvider {
  @Override
  public void registerResources(@NotNull ResourceRegistrar registrar) {
    registrar.addStdResource("http://www.osgi.org/xmlns/scr/v1.0.0", "/schemas/scr-1.0.0.xsd", getClass());
    registrar.addStdResource("http://www.osgi.org/xmlns/scr/v1.1.0", "/schemas/scr-1.1.0.xsd", getClass());
    registrar.addStdResource("http://www.osgi.org/xmlns/scr/v1.2.0", "/schemas/scr-1.2.0.xsd", getClass());
  }
}
