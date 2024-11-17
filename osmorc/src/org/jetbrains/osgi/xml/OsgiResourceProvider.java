// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.xml;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import org.jetbrains.annotations.NotNull;

final class OsgiResourceProvider implements StandardResourceProvider {
  @Override
  public void registerResources(@NotNull ResourceRegistrar registrar) {
    ClassLoader classLoader = OsgiResourceProvider.class.getClassLoader();
    registrar.addStdResource("http://www.osgi.org/xmlns/scr/v1.0.0", "schemas/scr-1.0.0.xsd", classLoader);
    registrar.addStdResource("http://www.osgi.org/xmlns/scr/v1.1.0", "schemas/scr-1.1.0.xsd", classLoader);
    registrar.addStdResource("http://www.osgi.org/xmlns/scr/v1.2.0", "schemas/scr-1.2.0.xsd", classLoader);
  }
}
