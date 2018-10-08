// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.groovy.compiler.rt;

import groovy.lang.GroovyResourceLoader;
import org.codehaus.griffon.cli.CommandLineConstants;
import org.codehaus.griffon.compiler.DefaultImportCompilerCustomizer;
import org.codehaus.griffon.compiler.GriffonCompilerContext;
import org.codehaus.groovy.control.CompilationUnit;

import java.io.File;

/**
 * @author aalmiray
 * @author peter
 */
public class GriffonInjector extends CompilationUnitPatcher {
  @Override
  @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
  public void patchCompilationUnit(CompilationUnit compilationUnit, GroovyResourceLoader resourceLoader, File[] srcFiles) {
    File baseDir = guessBaseDir(srcFiles);
    if (baseDir == null) {
      return;
    }

    GriffonCompilerContext.basedir = baseDir.getPath();
    GriffonCompilerContext.projectName = "IntelliJIDEARulezzzzz";
    GriffonCompilerContext.setup();
    if (!GriffonCompilerContext.getConfigOption(CommandLineConstants.KEY_DISABLE_AUTO_IMPORTS)) {
      DefaultImportCompilerCustomizer customizer = new DefaultImportCompilerCustomizer();
      customizer.collectDefaultImportsPerArtifact();
      compilationUnit.addPhaseOperation(customizer, customizer.getPhase().getPhaseNumber());
    }
  }

  private static File guessBaseDir(File srcFile) {
    File each = srcFile;
    while (each != null) {
      if (new File(each, "griffon-app").exists()) {
        return each;
      }
      each = each.getParentFile();
    }
    return null;
  }

  private static File guessBaseDir(File[] srcFiles) {
    for (File file : srcFiles) {
      File home = guessBaseDir(file);
      if (home != null) {
        return home;
      }
    }
    return null;
  }
}
