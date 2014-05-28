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

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.osmorc.build.LocalPackageCollector;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a compiler step that builds up a bundle. Depending on user settings the compiler either uses a user-edited
 * manifest or builds up a manifest using bnd.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public class BundleCompiler {
  /**
   * Condition which matches order entries that are not representing a framework library.
   */
  public static final Condition<OrderEntry> NOT_FRAMEWORK_LIBRARY_CONDITION = new Condition<OrderEntry>() {
    @Override
    public boolean value(OrderEntry entry) {
      if ((entry instanceof LibraryOrderEntry)) {
        LibraryOrderEntry libEntry = (LibraryOrderEntry)entry;
        if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(libEntry.getLibraryLevel())) {
          String libraryName = libEntry.getLibraryName();
          if (libraryName != null && libraryName.startsWith("Osmorc:")) {
            return false;
          }
        }
      }
      return true;
    }
  };


  @Nullable
  private static File getBndFile(Module module, OsmorcFacet facet, CompileContext context, File moduleOutputDir) {
    String prefix = "[" + module.getName() + "] ";
    OsmorcFacetConfiguration configuration = facet.getConfiguration();

    if (configuration.isUseBndFile()) {
      String fileLocation = configuration.getBndFileLocation();
      File bndFile = findFileInModuleContentRoots(fileLocation, module);
      if (bndFile != null && bndFile.canRead()) {
        return bndFile;
      }
      else {
        String message = String.format("The bnd file \"%s\" for module \"%s\" does not exist.", fileLocation, module.getName());
        context.addMessage(CompilerMessageCategory.ERROR, prefix + message, fileLocation, 0, 0);
        return null;
      }
    }

    // use a linked hash map to keep the order of properties.
    Map<String, String> buildProperties = new LinkedHashMap<String, String>();
    if (configuration.isManifestManuallyEdited() || configuration.isOsmorcControlsManifest()) {
      if (configuration.isOsmorcControlsManifest()) {
        // fully osmorc controlled, no bnd file, read in all  properties
        buildProperties.putAll(configuration.getBndFileProperties());
      }
      else if (configuration.isManifestManuallyEdited()) { // manually edited manifest
        boolean manifestExists = false;
        VirtualFile manifestFile = facet.getManifestFile();
        if (manifestFile != null) {
          String manifestFilePath = manifestFile.getPath();
          if (manifestFilePath != null) {
            buildProperties.put("-manifest", manifestFilePath);
            manifestExists = true;
          }
        }
        if (!manifestExists) {
          String message = "Manifest file for module " + module.getName() + ": '" + facet.getManifestLocation() +
                           "' does not exist or cannot be found. Check that file exists and is not excluded from the module.";
          context.addMessage(CompilerMessageCategory.ERROR, prefix + message, null, 0, 0);
          return null;
        }
      }

      StringBuilder pathBuilder = new StringBuilder();

      // add all the class paths to include resources, so stuff from the project gets copied over.
      // XXX: one could argue if this should be done for a non-osmorc build
      pathBuilder.append(moduleOutputDir.getPath());

      // now include the paths from the configuration
      List<Pair<String, String>> list = configuration.getAdditionalJARContents();
      for (Pair<String, String> stringStringPair : list) {
        if (pathBuilder.length() > 0) pathBuilder.append(",");
        pathBuilder.append(stringStringPair.second).append(" = ").append(stringStringPair.first);
      }

      // and tell bnd what resources to include
      StringBuilder includedResources = new StringBuilder();
      if(!configuration.isManifestManuallyEdited()) {
        String resources = configuration.getAdditionalPropertiesAsMap().get("Include-Resource");
        if (resources != null) {
          includedResources.append(resources).append(",").append(pathBuilder);
        } else{
          includedResources.append(pathBuilder);
        }
      } else {
        includedResources.append(pathBuilder);
      }
      buildProperties.put("Include-Resource", includedResources.toString());

      // add the ignore pattern for the resources
      if (!configuration.getIgnoreFilePattern().isEmpty()) {
        if (!configuration.isIgnorePatternValid()) {
          String message = "The file ignore pattern in the facet configuration is invalid.";
          context.addMessage(CompilerMessageCategory.ERROR, prefix + message, null, 0, 0);
          return null;
        }
        buildProperties.put("-donotcopy", configuration.getIgnoreFilePattern());
      }

      if (configuration.isOsmorcControlsManifest()) {
        // support the {local-packages} instruction
        context.getProgressIndicator().setText2("Calculating local packages");
        LocalPackageCollector.addLocalPackages(moduleOutputDir, buildProperties);
      }
    }
    else if (!configuration.isUseBundlorFile()) {
      String message = "OSGi facet configuration for module " + module.getName() + " seems to be invalid. " +
                       "No supported manifest handling method is set up. Please check configuration and try again.";
      context.addMessage(CompilerMessageCategory.ERROR, prefix + message, null, 0, 0);
      return null;
    }

    File outputDir = BndWrapper.getOutputDir(moduleOutputDir, context);
    if (outputDir == null) {
      // error already reported
      return null;
    }
    try {
      return BndWrapper.makeBndFile(module, buildProperties, outputDir);
    }
    catch (IOException e) {
      String message = "Problem when generating bnd file: " + e.getMessage();
      context.addMessage(CompilerMessageCategory.ERROR, prefix + message, null, 0, 0);
      return null;
    }
  }

  @Nullable
  protected static File findFileInModuleContentRoots(String file, Module module) {
    ModuleRootManager manager = ModuleRootManager.getInstance(module);
    for (VirtualFile root : manager.getContentRoots()) {
      VirtualFile result = VfsUtilCore.findRelativeFile(file, root);
      if (result != null) {
        return new File(result.getPath());
      }
    }
    return null;
  }

  /**
   * Returns the manifest file for the given module or null if it does not exist.
   */
  @Nullable
  public static File getManifestFile(@NotNull Module module) {
    OsmorcFacet facet = OsmorcFacet.getInstance(module);
    if (facet != null) {
      String manifestLocation = facet.getManifestLocation();
      ModuleRootManager manager = ModuleRootManager.getInstance(module);
      for (String root : manager.getContentRootUrls()) {
        File file = new File(VfsUtilCore.urlToPath(root), manifestLocation);
        if (file.exists()) {
          return file;
        }
      }
    }

    return null;
  }

  /**
   * Returns a file representing the module's output path.
   */
  @Nullable
  public static File getModuleOutputDir(@NotNull Module module) {
    CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
    if (extension != null) {
      String url = extension.getCompilerOutputUrl();
      if (url != null) {
        return new File(VfsUtilCore.urlToPath(url));
      }
    }
    return null;
  }

  /**
   * Bundlifies all libraries that belong to the given module and that are not bundles and that are not modules.
   * The bundles are cached, so if * the source library does not change, it will not be bundlified again.
   * Returns a string array containing the urls of the bundlified libraries.
   */
  @NotNull
  public static String[] bundlifyLibraries(@NotNull Module module,
                                           @NotNull ProgressIndicator indicator,
                                           @NotNull CompileContext compileContext) {
    File outputDir = BndWrapper.getOutputDir(getModuleOutputDir(module), compileContext);
    if (outputDir == null) {
      // couldn't create output path, abort here..
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    String[] urls = OrderEnumerator.orderEntries(module)
      .withoutSdk()
      .withoutModuleSourceEntries()
      .withoutDepModules()
      .productionOnly()
      .runtimeOnly()
      .recursively()
      .exportedOnly()
      .satisfying(NOT_FRAMEWORK_LIBRARY_CONDITION)
      .classes().getUrls();
    List<String> result = new ArrayList<String>();
    BndWrapper wrapper = new BndWrapper();
    for (String url : urls) {
      url = convertJarUrlToFileUrl(url);
      if (CachingBundleInfoProvider.canBeBundlified(url)) { // Fixes IDEA-56666
        indicator.setText("Bundling non-OSGi libraries for module: " + module.getName());
        indicator.setText2(url);
        // ok it is not a bundle, so we need to bundlify
        String bundledLocation = wrapper.wrapLibrary(module, compileContext, url, outputDir);
        // if no bundle could (or should) be created, we exempt this library
        if (bundledLocation != null) {
          result.add(fixFileURL(bundledLocation));
        }
      }
      else if (CachingBundleInfoProvider.isBundle(url)) { // Exclude non-bundles (IDEA-56666)
        result.add(fixFileURL(url));
      }
    }
    return ArrayUtil.toStringArray(result);
  }

  /**
   * Converts a jar url gained from OrderEntry.getUrls or Library.getUrls into a file url that can be processed.
   */
  @NotNull
  public static String convertJarUrlToFileUrl(@NotNull String url) {
    // urls end with !/ we cut that
    // XXX: not sure if this is a hack
    url = url.replaceAll("!.*", "");
    url = url.replace("jar://", "file://");
    return url;
  }

  /**
   * On Windows a file url must have at least 3 slashes at the beginning. 2 for the protocol separation and one for
   * the empty host (e.g.: file:///c:/bla instead of file://c:/bla). If there are only two the drive letter is
   * interpreted as the host of the url which naturally doesn't exist. On Unix systems it's the same case, but since
   * all paths start with a slash, a misinterpretation of part of the path as a host cannot occur.
   */
  @NotNull
  public static String fixFileURL(@NotNull String url) {
    return url.startsWith("file:///") ? url : url.replace("file://", "file:///");
  }

  /**
   * Builds the name of the jar file for a given module.
   */
  @Nullable
  public static String getJarFileName(@NotNull final Module module) {
    OsmorcFacet facet = OsmorcFacet.getInstance(module);
    return facet != null ? facet.getConfiguration().getJarFileLocation() : null;
  }
}
