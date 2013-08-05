/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.make;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Verifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Constants;
import org.osmorc.StacktraceUtil;
import org.osmorc.frameworkintegration.LibraryBundlificationRule;
import org.osmorc.settings.ApplicationSettings;
import org.osmorc.util.OrderedProperties;

import java.io.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which wraps bnd and integrates it into IntelliJ.
 * <p/>
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class BndWrapper {
  /**
   * Wraps an existing jar file using bnd's analyzer. This class will check and use any applying bundlification rules
   * for this library that have been set up in Osmorcs library bundlification dialog.
   */
  @Nullable
  public String wrapLibrary(Module module, @NotNull CompileContext compileContext, final String sourceJarUrl, File targetDir) {
    String messagePrefix = "[" + module.getName() + "] ";
    try {
      File sourceFile = new File(VfsUtilCore.urlToPath(sourceJarUrl));
      if (!sourceFile.exists()) {
        compileContext.addMessage(CompilerMessageCategory.WARNING, messagePrefix +
                                                                   "The library " +
                                                                   sourceFile.getPath() +
                                                                   " does not exist. Please check your module settings. Ignoring missing library.",
                                  null, 0, 0);
        return null;
      }
      if (sourceFile.isDirectory()) {
        // ok it's an exploded directory, we cannot bundle it.
        return null;
      }

      File targetFile = new File(targetDir.getPath() + File.separator + sourceFile.getName());
      Map<String, String> additionalProperties = new HashMap<String, String>();

      // okay try to find a rule for this nice package:
      long lastModified = Long.MIN_VALUE;
      ApplicationSettings settings = ServiceManager.getService(ApplicationSettings.class);
      for (LibraryBundlificationRule bundlificationRule : settings.getLibraryBundlificationRules()) {
        if (bundlificationRule.appliesTo(sourceFile.getName())) {
          if (bundlificationRule.isDoNotBundle()) {
            return null; // make it quick in this case
          }
          additionalProperties.putAll(bundlificationRule.getAdditionalPropertiesMap());
          // if a rule applies which has been changed recently we need to re-bundle the file
          lastModified = Math.max(lastModified, bundlificationRule.getLastModified());

          // if stop after this rule is true, we will no longer try to find any more matching rules
          if (bundlificationRule.isStopAfterThisRule()) {
            break;
          }
        }
      }


      if (!targetFile.exists() || targetFile.lastModified() < sourceFile.lastModified() ||
          targetFile.lastModified() < lastModified) {
        if (doWrap(module, compileContext, sourceFile, targetFile, additionalProperties)) {
          return VfsUtilCore.pathToUrl(targetFile.getCanonicalPath());
        }
      }
      else {
        // Fixes IDEADEV-39099. When the wrapper does not return anything the library is not regarded
        // as a bundle.
        return VfsUtilCore.pathToUrl(targetFile.getCanonicalPath());
      }
    }
    catch (final Exception e) {
      // There is some reported issue where a lot of exceptions have been thrown which caused a ton of popup
      // boxes, so we better put this into the compile context as normal error message. Can't reproduce the issue
      // but i think it's stil the better way.
      // IDEA-27101
      // IDEA-69149 - Changed this form ERROR to WARNING, as a non-bundlified library might not be fatal (especially when importing a ton of libs from maven)
      compileContext.addMessage(CompilerMessageCategory.WARNING,
                                MessageFormat
                                  .format(messagePrefix + "There was an unexpected problem when trying to bundlify {0}: {1}", sourceJarUrl,
                                          StacktraceUtil.stackTraceToString(e)), null, 0, 0);
    }
    return null;
  }

  /**
   * Internal function which does the actual wrapping. This is 90% borrowed from bnd's source code.
   *
   * @param module
   * @param compileContext the compile context
   * @param inputJar       the input file
   * @param outputJar      the output file
   * @param properties     properties for the manifest. these may contain bnd instructions
   * @return true if the bundling was successful, false otherwise.
   * @throws Exception in case something goes wrong.
   */
  private boolean doWrap(@NotNull Module module,
                         @NotNull final CompileContext compileContext,
                         @NotNull File inputJar,
                         @NotNull final File outputJar,
                         @NotNull Map<String, String> properties) throws Exception {
    final String messagePrefix = "[" + module.getName() + "][Library " + inputJar.getName() + "] ";

    String sourceFileUrl = VfsUtilCore.pathToUrl(inputJar.getPath());
    Analyzer analyzer = new ReportingAnalyzer(compileContext, sourceFileUrl);
    analyzer.setPedantic(false);
    analyzer.setJar(inputJar);
    Jar dot = analyzer.getJar();
    analyzer.putAll(properties, false);
    if (analyzer.getProperty(Constants.IMPORT_PACKAGE) == null) {
      analyzer.setProperty(Constants.IMPORT_PACKAGE, "*;resolution:=optional");
    }
    if (analyzer.getProperty(Constants.BUNDLE_SYMBOLICNAME) == null) {
      Pattern p = Pattern.compile("(" + Verifier.SYMBOLICNAME.pattern() + ")(-[0-9])?.*\\.jar");
      String base = inputJar.getName();
      Matcher m = p.matcher(base);
      if (m.matches()) {
        base = m.group(1);
      }
      else {
        compileContext.addMessage(CompilerMessageCategory.ERROR,
                                  messagePrefix + "Can not calculate name of output bundle, rename jar or use -properties", sourceFileUrl,
                                  0, 0);
        return false;
      }

      analyzer.setProperty(Constants.BUNDLE_SYMBOLICNAME, base);
    }
    if (analyzer.getProperty(Constants.EXPORT_PACKAGE) == null) {
      // avoid spurious error messages about string starting with ","
      // String export = analyzer.calculateExportsFromContents(dot).replaceFirst("^\\s*,", "");
      analyzer.setProperty(Constants.EXPORT_PACKAGE, "*");
//      analyzer.setProperty(Constants.EXPORT_PACKAGE, export);
    }
    analyzer.mergeManifest(dot.getManifest());
    String version = analyzer.getProperty(Constants.BUNDLE_VERSION);
    if (version != null) {
      version = Analyzer.cleanupVersion(version);
      analyzer.setProperty(Constants.BUNDLE_VERSION, version);
    }
    analyzer.calcManifest();
    Jar jar = analyzer.getJar();
    final File f = FileUtil.createTempFile("tmpbnd", ".jar");
    jar.write(f);
    jar.close();
    analyzer.close();

    // IDEA-26817 delete the old bundle, so the renameTo later works...
    if (outputJar.exists()) {
      if (!outputJar.delete()) {
        compileContext.addMessage(CompilerMessageCategory.ERROR,
                                  messagePrefix + "Could not delete outdated generated bundle. Is " + outputJar.getPath() + " writable?",
                                  null, 0, 0);
        return false;
      }
    }

    final Ref<Boolean> result = new Ref<Boolean>(false);
    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
      public void run() {
        result.set(ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
          public Boolean compute() {
            // this should work in 99% of the cases
            if (!f.renameTo(outputJar)) {
              // and this is for the remaining 1%.
              VirtualFile src = LocalFileSystem.getInstance().findFileByIoFile(f);
              if (src == null) {
                compileContext.addMessage(CompilerMessageCategory.ERROR,
                                          messagePrefix +
                                          "No jar file was created. This should not happen. Is " +
                                          f.getPath() +
                                          " writable?", null, 0,
                                          0);
                return false;
              }
              // make sure the parent folder exists:
              File parentFolder = outputJar.getParentFile();
              if (!parentFolder.exists()) {
                if (!parentFolder.mkdirs()) {
                  compileContext
                    .addMessage(CompilerMessageCategory.ERROR,
                                messagePrefix + "Cannot create output folder. Is " + parentFolder.getPath() + " writable?",
                                null, 0, 0);
                  return false;
                }
              }

              // now get the target folder
              VirtualFile target = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(parentFolder);
              if (target == null) {
                // this actually should not happen but since we are bound by murphy's law, we check this as well
                // and believe it or not it DID happen.
                compileContext.addMessage(CompilerMessageCategory.ERROR, messagePrefix + "Output path " +
                                                                         parentFolder.getPath() +
                                                                         " was created but cannot be found anymore. This should not happen.",
                                          null, 0, 0);
                return false;
              }
              // IDEA-26817: target must be the dir, not the the file, so:
              // and then put this in. This should produce the correct result.
              try {
                VfsUtilCore.copyFile(this, src, target, outputJar.getName());
              }
              catch (IOException e) {
                compileContext
                  .addMessage(CompilerMessageCategory.ERROR, messagePrefix + "Could not copy " + src + " to " + target, null, 0, 0);
                return false;
              }
            }
            return true;
          }
        }));
      }
    }, ModalityState.defaultModalityState());
    return result.get();
  }

  /**
   * Builds the jar file for the given module. This is called inside a compile run.
   */
  public static boolean build(@NotNull Module module,
                              @NotNull CompileContext compileContext,
                              @NotNull File bndFile,
                              @NotNull List<String> classPathUrls,
                              @NotNull String outputPath) throws Exception {
    String prefix = "[" + module.getName() + "] ";

    ReportingBuilder builder = new ReportingBuilder(compileContext, VfsUtilCore.pathToUrl(bndFile.getPath()), module);
    builder.setPedantic(false);
    builder.setProperties(bndFile);

    File[] classPath = new File[classPathUrls.size()];
    for (int i = 0; i < classPathUrls.size(); i++) {
      classPath[i] = new File(VfsUtilCore.urlToPath(classPathUrls.get(i)));
    }
    builder.setClasspath(classPath);

    // Check if the manifest version is missing (IDEADEV-41174)
    String manifest = builder.getProperty(aQute.bnd.osgi.Constants.MANIFEST);
    if (manifest != null) {
      File manifestFile = builder.getFile(manifest);
      if (manifestFile != null && manifestFile.canRead()) {
        Properties props = new Properties();
        FileInputStream fileInputStream = new FileInputStream(manifestFile);
        try {
          props.load(fileInputStream);
          String value = props.getProperty(Attributes.Name.MANIFEST_VERSION.toString());
          if (StringUtil.isEmptyOrSpaces(value)) {
            String message = "Your manifest does not contain a Manifest-Version entry. This may produce an empty manifest in the resulting bundle.";
            compileContext.addMessage(CompilerMessageCategory.WARNING, prefix + message, VfsUtilCore.pathToUrl(manifestFile.getAbsolutePath()), 0, 0);
          }
        }
        catch (Exception ex) {
          String message = "There was a problem reading your manifest.";
          compileContext.addMessage(CompilerMessageCategory.WARNING, prefix + message, VfsUtilCore.pathToUrl(manifestFile.getAbsolutePath()), 0, 0);
        }
        finally {
          fileInputStream.close();
        }
      }
    }

    File output = new File(outputPath);
    Jar jar = builder.build();
    jar.setName(output.getName());
    jar.write(output);
    builder.close();

    return true;
  }

  /**
   * Generates a bnd file from the given contents map and returns it.
   */
  @NotNull
  public static File makeBndFile(@NotNull Module module,
                                 @NotNull Map<String, String> contents,
                                 @NotNull File outputDir) throws IOException {
    File tmpFile = FileUtil.createTempFile(outputDir, "osmorc.", ".bnd", true);

    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
    try {
      OrderedProperties props = OrderedProperties.fromMap(contents);
      String comments = "Bnd file generated by Osmorc for build of module " + module.getName() + " in project " + module.getProject().getName();
      props.store(bos, comments);
    }
    finally {
      bos.close();
    }

    tmpFile.deleteOnExit();
    return tmpFile;
  }

  /**
   * Creates Osmorc output dir relative to a module's one.
   */
  @Nullable
  public static File getOutputDir(@NotNull File moduleOutputDir, @NotNull CompileContext context) {
    File outputDir = new File(moduleOutputDir.getParent(), "bundles");
    if (!outputDir.exists() && !outputDir.mkdirs()) {
      String message = "Could not create output directory: " + outputDir + ". Please check file permissions.";
      context.addMessage(CompilerMessageCategory.ERROR, message, null, 0, 0);
      return null;
    }
    return outputDir;
  }
}
