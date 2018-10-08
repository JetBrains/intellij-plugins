// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.jps.incremental.groovy;

import com.intellij.openapi.application.PathManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class GriffonBuilderExtension implements GroovyBuilderExtension {

  @NotNull
  @Override
  public Collection<String> getCompilationClassPath(@NotNull CompileContext context, @NotNull ModuleChunk chunk) {
    return Collections.emptyList();
  }

  @NotNull
  @Override
  public Collection<String> getCompilationUnitPatchers(@NotNull CompileContext context, @NotNull ModuleChunk chunk) {
    for (JpsModule module : chunk.getModules()) {
      if (shouldInjectGriffon(module)) {
        return Collections.singleton("org.jetbrains.groovy.compiler.rt.GriffonInjector");
      }
    }
    return Collections.emptyList();
  }

  private static boolean shouldInjectGriffon(JpsModule module) {
    for (String rootUrl : module.getContentRootsList().getUrls()) {
      File root = JpsPathUtil.urlToFile(rootUrl);
      if (new File(root, "griffon-app").isDirectory() &&
          new File(root, "application.properties").isFile()) {
        return true;
      }
    }

    return false;
  }

}
