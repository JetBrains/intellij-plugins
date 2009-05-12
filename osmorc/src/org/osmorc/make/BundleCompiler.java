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

import aQute.bnd.main.bnd;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.compiler.make.BuildInstruction;
import com.intellij.openapi.compiler.make.BuildInstructionVisitor;
import com.intellij.openapi.compiler.make.BuildRecipe;
import com.intellij.openapi.compiler.make.FileCopyInstruction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.deployment.DeploymentUtil;
import com.intellij.openapi.deployment.ModuleLink;
import com.intellij.openapi.deployment.PackagingMethod;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.ZipUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Constants;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.LibraryBundlificationRule;
import org.osmorc.frameworkintegration.LibraryHandler;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ApplicationSettings;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a compiler step that builds up a bundle. Depending on user settings the compiler either uses a user-edited
 * manifest or builds up a manifest using bnd.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id$
 */
public class BundleCompiler implements PackagingCompiler
{
  private static String getOutputPath(Module m)
  {
    VirtualFile moduleCompilerOutputPath =
        CompilerModuleExtension.getInstance(m).getCompilerOutputPath();

    // okay there is some strange thing going on here. The method getCompilerOutputPath() returns null
    // but getCompilerOutputPathUrl() returns something. I assume that we cannot get a VirtualFile object for a non-existing
    // path, so we need to make sure the compiler output path exists.

    if (moduleCompilerOutputPath == null)
    {
      // get the url
      String outputPathUrl = CompilerModuleExtension.getInstance(m).getCompilerOutputUrl();

      // create the paths
      try
      {
        VfsUtil.createDirectories(VfsUtil.urlToPath(outputPathUrl));
      }
      catch (IOException e)
      {
        Messages.showErrorDialog(m.getProject(), OsmorcBundle.getTranslation("error"),
            OsmorcBundle.getTranslation("faceteditor.cannot.create.outputpath"));
        throw new IllegalStateException("Cannot create output path");
      }

      // now try again to get VirtualFile object for it
      moduleCompilerOutputPath =
          CompilerModuleExtension.getInstance(m).getCompilerOutputPath();
      if (moduleCompilerOutputPath == null)
      {
        // this should not happen
        throw new IllegalStateException("Cannot access compiler output path.");
      }
    }


    String path = moduleCompilerOutputPath.getParent().getPath() + File.separator + "bundles";
    File f = new File(path);
    if (!f.exists())
    {
      f.mkdirs();
    }
    return path;
  }

  Logger logger = Logger.getInstance("#org.osmorc.make.BundleCompiler");

  /**
   * Deletes the jar file of a bundle when it is outdated.
   *
   * @param compileContext the compile context
   * @param s              ??
   * @param validityState  the validity state of the item that is outdated
   */
  public void processOutdatedItem(CompileContext compileContext, String s, @Nullable final ValidityState validityState)
  {
    // delete the jar file of the module in case the stuff is outdated
    // TODO: find a way to update jar files so we can speed this up
    if (validityState != null)
    {
      ApplicationManager.getApplication().runReadAction(new Runnable()
      {
        public void run()
        {
          BundleValidityState myvalstate = (BundleValidityState) validityState;
          //noinspection ConstantConditions
          String jarUrl = myvalstate.getOutputJarUrl();
          if (jarUrl != null)
          {
            FileUtil.delete(new File(jarUrl));
          }
        }
      }
      );
    }
  }

  /**
   * Returns all processingitems (== Bundles to be created) for the givne compile context
   *
   * @param compileContext the compile context
   * @return a list of bundles that need to be compiled
   */
  @NotNull
  public ProcessingItem[] getProcessingItems(final CompileContext compileContext)
  {
    return ApplicationManager.getApplication().runReadAction(new Computable<ProcessingItem[]>()
    {
      public ProcessingItem[] compute()
      {
        // find and add all dependent modules to the list of stuff to be compiled
        CompileScope compilescope = compileContext.getCompileScope();
        Module affectedModules[] = compilescope.getAffectedModules();
        if (affectedModules.length == 0)
        {
          return ProcessingItem.EMPTY_ARRAY;
        }
        Project project = affectedModules[0].getProject();
        Module modules[] = ModuleManager.getInstance(project).getModules();
        THashSet<Module> thashset = new THashSet<Module>();

        for (Module module : modules)
        {
          if (!OsmorcFacet.hasOsmorcFacet(module))
          {
            continue;
          }
          thashset.add(module);
        }

        ProcessingItem[] result = new ProcessingItem[thashset.size()];
        int i = 0;
        for (Object aThashset : thashset)
        {
          Module module = (Module) aThashset;
          if (module.getModuleFile() != null)
          {
            result[i++] = new BundleProcessingItem(module);
          }

        }
        return result;
      }
    });
  }

  /**
   * Processes a processing item (=module)
   *
   * @param compileContext  the compile context
   * @param processingItems the list of processing items
   * @return the list of processing items that remain for further processing (if any)
   */
  public ProcessingItem[] process(CompileContext compileContext,
                                  ProcessingItem[] processingItems)
  {
    try
    {
      for (ProcessingItem processingItem : processingItems)
      {
        Module module = ((BundleProcessingItem) processingItem).getModule();
        buildBundle(module, compileContext.getProgressIndicator(), compileContext);
      }

    }
    catch (IOException ioexception)
    {
      logger.error(ioexception);
    }
    return processingItems;
  }


  /**
   * Builds the bundle for a given module.
   *
   * @param module            the module
   * @param progressIndicator the progress indicator
   * @param compileContext
   * @throws IOException in case something goes wrong.
   */
  static void buildBundle(final Module module, final ProgressIndicator progressIndicator,
                          final CompileContext compileContext)
      throws IOException
  {
    // create the jar file
    final File jarFile = new File(VfsUtil.urlToPath(getJarFileName(module)));
    if (jarFile.exists())
    { //noinspection ResultOfMethodCallIgnored
      jarFile.delete();
    }
    FileUtil.createParentDirs(jarFile);

    // get the build recipe
    final BuildRecipe buildrecipe = (new ReadAction<BuildRecipe>()
    {
      protected void run(Result<BuildRecipe> result)
      {
        result.setResult(getBuildRecipe(module));
      }
    }).execute().getResultObject();

    // setup the manifest
    Manifest manifest = DeploymentUtil.getInstance().createManifest(buildrecipe);
    if (manifest == null)
    {
      manifest = new Manifest();
    }

    // in case the user manually edits the manifest, copy over all stuff from his manifest
    final OsmorcFacetConfiguration configuration = OsmorcFacet.getInstance(module).getConfiguration();
    if (!configuration.isOsmorcControlsManifest())
    {
      // manual manifest
      progressIndicator.setText2(OsmorcBundle.getTranslation("bundlecompiler.merging.manifest"));
      final VirtualFile manifestFile = getManifestFile(module);
      if (manifestFile != null)
      {
        try
        {
          Manifest fromFile = new Manifest(manifestFile.getInputStream());
          Attributes atts = fromFile.getMainAttributes();
          for (Map.Entry<Object, Object> entry : atts.entrySet())
          {
            manifest.getMainAttributes().put(entry.getKey(), entry.getValue());
          }
        }
        catch (Exception e)
        {
          ApplicationManager.getApplication().invokeLater(new Runnable()
          {
            // Fix for OSMORC-97
            public void run()
            {
              compileContext.addMessage(CompilerMessageCategory.ERROR,
                  String.format("The manifest file \"%s\" for module \"%s\" does not exist or is invalid.",
                      manifestFile.getName(), module.getName()), manifestFile.getUrl(), 0, 0);
            }
          }
          );
          return;
        }
      }
    }

    // TODO: main class support in here (maybe put into the facet)
    String mainClass = null;
    if (!Comparing.strEqual(mainClass, null))
    {
      manifest.getMainAttributes().putValue(Attributes.Name.MAIN_CLASS.toString(), mainClass);
    }

    // create a temp jar file to jar anything into
    final File tempFile =
        File.createTempFile("___" + FileUtil.getNameWithoutExtension(jarFile), ".jar", jarFile.getParentFile());
    final JarOutputStream jaroutputstream =
        new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)), manifest);

    // walk over the build instructions and copy each file into the jar
    final THashSet<String> writtenItems = new THashSet<String>();
    try
    {
      buildrecipe.visitInstructionsWithExceptions(new BuildInstructionVisitor()
      {
        public boolean visitInstruction(BuildInstruction buildinstruction)
            throws IOException
        {
          ProgressManager.getInstance().checkCanceled();
          if (buildinstruction instanceof FileCopyInstruction)
          {
            FileCopyInstruction filecopyinstruction = (FileCopyInstruction) buildinstruction;
            File file2 = filecopyinstruction.getFile();
            if (file2 == null || !file2.exists())
            {
              return true;
            }
            String s2 = FileUtil.toSystemDependentName(file2.getPath());

            if (progressIndicator != null)
            {
              //noinspection UnresolvedPropertyKey
              progressIndicator.setText2(IdeBundle.message("jar.build.processing.file.progress", s2));
            }
          }
          buildinstruction.addFilesToJar(DummyCompileContext.getInstance(), tempFile, jaroutputstream, buildrecipe,
              writtenItems, ManifestFileFilter.Instance);
          return true;
        }
      }, false);
    }
    catch (ProcessCanceledException e)
    {
      // Fix for OSMORC-118
      return;
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
      // e.printStackTrace();
    }

    String ignoreFilePatternString = configuration.getIgnoreFilePattern();
    FileFilter fileFilter = null;
    if (ignoreFilePatternString != null && ignoreFilePatternString.length() > 0)
    {
      final Pattern ignoreFilePattern = Pattern.compile(ignoreFilePatternString);
      fileFilter = new FileFilter()
      {
        public boolean accept(File pathname)
        {
          Matcher matcher = ignoreFilePattern.matcher(pathname.getAbsolutePath());
          return !matcher.find();
        }
      };
    }

    List<Pair<String, String>> jarContents = configuration.getAdditionalJARContents();
    for (Pair<String, String> jarContent : jarContents)
    {
      final File sourceFile = new File(jarContent.getFirst());
      if (sourceFile.exists())
      {
        ZipUtil
            .addFileOrDirRecursively(jaroutputstream, tempFile, sourceFile, jarContent.getSecond(), fileFilter, null);
      }
      else
      {
        ApplicationManager.getApplication().invokeLater(new Runnable()
        {

          public void run()
          {
            compileContext.addMessage(CompilerMessageCategory.WARNING,
                String.format("The file \"%s\" does not exist. It will not be copied into the JAR \"%s\"", sourceFile,
                    getJarFileName(module)), null, 0, 0);
          }
        }
        );
      }
    }

    jaroutputstream.close();


    if (configuration.isOsmorcControlsManifest())
    {
      // if we have an osmorc-controlled bundle we use bnd to wrap the stuff here..
      // XXX: prolly a better idea to create the bundle with bnd int he first place, but this doesnt work too well
      // with IDEAs build system, so for starters we do the wrapping here, which has a small performance hit...
      progressIndicator.setText2(OsmorcBundle.getTranslation("bundlecompiler.creatingmanifest"));
      bnd bnd = new bnd();

      // check if we use a bnd file or get values from facet
      // fix for OSMORC-121
      Map<String, String> additionalProperties = new HashMap<String, String>();
      if (configuration.isUseBndFile())
      {
        // load bnd file and put all info there.
        Properties p = new Properties();
        try
        {
          p.load(new FileInputStream(findFileInModuleContentRoots(configuration.getBndFileLocation(), module)));
          for (Object s : p.keySet())
          {
            additionalProperties.put((String) s, p.getProperty((String) s));
          }
        }
        catch (IOException e)
        {
          ApplicationManager.getApplication().invokeLater(new Runnable()
          {
            public void run()
            {
              compileContext.addMessage(CompilerMessageCategory.ERROR,
                  String.format("The bnd file \"%s\" for module \"%s\" does not exist or is invalid.",
                      configuration.getBndFileLocation(), module.getName()), configuration.getBndFileLocation(), 0, 0);
            }
          }
          );
          FileUtil.delete(tempFile);
          return;
        }
      }
      else
      {

        // put in the stuff from facet configuration
        additionalProperties.put(Constants.BUNDLE_SYMBOLICNAME, configuration.getBundleSymbolicName());
        additionalProperties.put(Constants.BUNDLE_ACTIVATOR, configuration.getBundleActivator());
        additionalProperties.put(Constants.BUNDLE_VERSION, configuration.getBundleVersion());
        additionalProperties.put(Constants.IMPORT_PACKAGE, "*");

        // add the properties from the facet setup
        Map<String, String> propsFromFacetSetup = configuration.getAdditionalPropertiesAsMap();
        additionalProperties.putAll(propsFromFacetSetup);
      }
      try
      {
        bnd.doWrap(null, tempFile, tempFile, null, 0, additionalProperties);
      }
      catch (Exception e)
      {
        // todo, REAL error handling here
        e.printStackTrace();
      }
    }

    try
    {
      FileUtil.rename(tempFile, jarFile);
    }
    catch (IOException ioexception)
    {
      ApplicationManager.getApplication().invokeLater(new Runnable()
      {

        public void run()
        {
          //noinspection UnresolvedPropertyKey
          String s2 =
              IdeBundle.message("jar.build.cannot.overwrite.error", FileUtil.toSystemDependentName(jarFile.getPath()),
                  FileUtil.toSystemDependentName(tempFile.getPath()));
          //noinspection UnresolvedPropertyKey
          Messages.showErrorDialog(module.getProject(), s2, IdeBundle.message("jar.build.error.title"));
        }
      }
      );
    }
    if (configuration.isOsmorcControlsManifest())
    {
      // finally bundlify all the libs for this one
      bundlifyLibraries(module, progressIndicator);
    }
  }

  private static File findFileInModuleContentRoots(String file, Module module)
  {
    ModuleRootManager manager = ModuleRootManager.getInstance(module);
    for (VirtualFile root : manager.getContentRoots())
    {
      VirtualFile result = VfsUtil.findRelativeFile(file, root);
      if (result != null)
      {
        return new File(result.getPath());
      }
    }
    return null;
  }

  /**
   * Returns the manifest file for the given module if it exists
   *
   * @param module the module
   * @return the manifest file or null if it doesnt exist
   */
  @Nullable
  public static VirtualFile getManifestFile(@NotNull Module module)
  {
    OsmorcFacet facet = OsmorcFacet.getInstance(module);
    ModuleRootManager manager = ModuleRootManager.getInstance(module);
    for (VirtualFile root : manager.getContentRoots())
    {
      VirtualFile result = VfsUtil.findRelativeFile(facet.getManifestLocation(), root);
      if (result != null)
      {
        result = result.findChild("MANIFEST.MF");
      }
      if (result != null)
      {
        return result;
      }
    }
    return null;
  }


  /**
   * @return a description (prolly not used)
   */
  @NotNull
  public String getDescription()
  {
    return "bundle compile";
  }

  /**
   * Checks the configuration.
   *
   * @param compileScope the compilescope
   * @return true if the configuration is valid, false otherwise
   */
  public boolean validateConfiguration(CompileScope compileScope)
  {
    return true;
  }

  /**
   * Recreates a validity state from a data input stream
   *
   * @param in stream containing the data
   * @return the validity state
   * @throws IOException in case something goes wrong
   */
  public ValidityState createValidityState(DataInput in) throws IOException
  {
    return new BundleValidityState(in);
  }

  /**
   * Creates a build recipe for the given module
   *
   * @param module the module
   * @return the build recipe
   */
  static BuildRecipe getBuildRecipe(Module module)
  {
    DummyCompileContext dummycompilecontext = DummyCompileContext.getInstance();

    BuildRecipe buildrecipe = DeploymentUtil.getInstance().createBuildRecipe();

    // okay this is some hacking. we try to re-use the settings for building jars here and emulate
    // the jar settings dialog... YEEHA..
    ModuleLink link = DeploymentUtil.getInstance().createModuleLink(module, module);
    link.setPackagingMethod(PackagingMethod.COPY_FILES);
    link.setURI("/");
    ModuleLink[] modules = new ModuleLink[]{link};
    //noinspection UnresolvedPropertyKey
    DeploymentUtil.getInstance().addJavaModuleOutputs(module, modules, buildrecipe, dummycompilecontext, null,
        IdeBundle.message("jar.build.module.presentable.name", module.getName()));
    return buildrecipe;
  }

  /**
   * Bundlifies all libraries that belong to the given module and that are not bundles. The bundles are cached, so if
   * the source library does not change, it will not be bundlified again.
   *
   * @param module    the module whose libraries are to be bundled.
   * @param indicator a progress indicator.
   * @return a string array containing the urls of the bundlified libraries.
   */
  public static String[] bundlifyLibraries(Module module, ProgressIndicator indicator)
  {
    ArrayList<String> result = new ArrayList<String>();
    final ModuleRootManager manager = ModuleRootManager.getInstance(module);
    LibraryHandler libraryHandler = ServiceManager.getService(LibraryHandler.class);
    OrderEntry[] entries = new ReadAction<OrderEntry[]>()
    {
      protected void run(Result<OrderEntry[]> result) throws Throwable
      {
        result.setResult(manager.getModifiableModel().getOrderEntries());
      }
    }.execute().getResultObject();

    for (OrderEntry entry : entries)
    {
      if (entry instanceof JdkOrderEntry)
      {
        continue; // do not bundlify JDKs
      }

      if (entry instanceof LibraryOrderEntry && libraryHandler.isFrameworkInstanceLibrary((LibraryOrderEntry) entry))
      {
        continue; // do not bundlify framework instance libraries
      }

      String[] urls = entry.getUrls(OrderRootType.CLASSES);
      for (String url : urls)
      {
        url = convertJarUrlToFileUrl(url);


        if (!CachingBundleInfoProvider.isBundle(url))
        {
          indicator.setText(OsmorcBundle.getTranslation("bundlecompiler.bundlifying.library"));
          indicator.setText2(url);
          // ok it is not a bundle, so we need to bundlify
          String bundledLocation = bundlify(url, module);
          // if no bundle could (or should) be created, we exempt this library
          if (bundledLocation != null)
          {
            result.add(fixFileURL(bundledLocation));
          }
        }
        else
        {
          result.add(fixFileURL(url));
        }
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Converts a jar url gained from OrderEntry.getUrls or Library.getUrls into a file url that can be processed.
   *
   * @param url the url to be converted
   * @return the converted url
   */
  public static String convertJarUrlToFileUrl(String url)
  {
    // urls end with !/ we cut that
    // XXX: not sure if this is a hack
    url = url.replaceAll("!.*", "");
    url = url.replace("jar://", "file://");
    return url;
  }

  /**
   * On Windows a file url must have at least 3 slashes at the beginning. 2 for the protocoll separation and one for the
   * empty host (e.g.: file:///c:/bla instead of file://c:/bla). If there are only two the drive letter is interpreted
   * as the host of the url which naturally doesn't exist. On Unix systems it's the same case, but since all paths start
   * with a slash, a misinterpretation of part of the path as a host cannot occur.
   *
   * @param url The URL to fix
   * @return The fixed URL
   */
  public static String fixFileURL(String url)
  {
    return url.startsWith("file:///") ? url : url.replace("file://", "file:///");
  }

  /**
   * Takes the given jar file and transforms it into a bundle. The bundle is stored inside a temp folder of the current
   * user's home.
   *
   * @param bundleFileUrl url of the file to be bundlified
   * @param module
   * @return the url from where the bundlified file can be installed. returns null if the bundlification failed for any
   *         reason.
   */
  private static String bundlify(final String bundleFileUrl, Module module)
  {
    try
    {
      File targetDir = new File(getOutputPath(module));
      File sourceFile = new File(VfsUtil.urlToPath(bundleFileUrl));

      File targetFile = new File(targetDir.getPath() + File.separator + sourceFile.getName());
      Map<String, String> additionalProperties = new HashMap<String, String>();

      // okay try to find a rule for this nice package:
      long lastModified = Long.MIN_VALUE;
      ApplicationSettings settings = ServiceManager.getService(ApplicationSettings.class);
      LibraryBundlificationRule rule = null;
      for (LibraryBundlificationRule bundlificationRule : settings.getLibraryBundlificationRules())
      {
        if (bundlificationRule.appliesTo(sourceFile.getName()))
        {
          if (bundlificationRule.isDoNotBundle())
          {
            return null; // make it quick in this case
          }
          additionalProperties.putAll(bundlificationRule.getAdditionalPropertiesMap());
          // if a rule applies which has been changed recently we need to re-bundle the file 
          lastModified = Math.max(lastModified, bundlificationRule.getLastModified());
        }
      }


      if (!targetFile.exists() || targetFile.lastModified() < sourceFile.lastModified() ||
          targetFile.lastModified() < lastModified)
      {
        bnd bnd = new bnd();
        bnd.doWrap(null, sourceFile, targetFile, null, 0, additionalProperties);
      }
      return VfsUtil.pathToUrl(targetFile.getCanonicalPath());
    }
    catch (final Exception e)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          Messages.showErrorDialog(
              OsmorcBundle.getTranslation("bundlecompiler.bundlifying.problem.message", bundleFileUrl, e.toString()),
              OsmorcBundle.getTranslation("error"));
        }
      });
      return null;
    }
  }

  /**
   * Builds the name of the jar file for a given module.
   *
   * @param module the module
   * @return the name of the jar file that will be produced for this module by this compiler
   */
  public static String getJarFileName(final Module module)
  {
    return OsmorcFacet.getInstance(module).getConfiguration().getJarFileLocation();
  }

}
