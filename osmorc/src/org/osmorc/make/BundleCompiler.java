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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.frameworkintegration.CachingBundleInfoProvider;
import org.osmorc.frameworkintegration.LibraryHandler;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.BundleManifest;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a compiler step that builds up a bundle. Depending on user settings the compiler either uses a user-edited
 * manifest or builds up a manifest using bnd.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id$
 */
public class BundleCompiler implements PackagingCompiler {
    private final Logger logger = Logger.getInstance("#org.osmorc.make.BundleCompiler");

    @Nullable
    private static String getOutputPath(final Module m, CompileContext context) {
        final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(m);
        VirtualFile moduleCompilerOutputPath = extension.getCompilerOutputPath();

        String path;
        if (moduleCompilerOutputPath == null) {
            // get the url
            String outputPathUrl = extension.getCompilerOutputUrl();

            // create the paths
            // FIX  	 IDEADEV-40112
            File f = new File(VfsUtil.urlToPath(outputPathUrl));
            if (!f.exists() && !f.mkdirs()) {
                context.addMessage(CompilerMessageCategory.ERROR, OsmorcBundle.getTranslation("faceteditor.cannot.create.outputpath"), null, 0,0);
                return null;
            }

            path = f.getParentFile().getPath() + File.separator + "bundles";
        }
        else {
            path = moduleCompilerOutputPath.getParent().getPath() + File.separator + "bundles";
        }

        File f = new File(path);
        if (!f.exists()) {
            if (!f.mkdirs()) {
              context.addMessage(CompilerMessageCategory.ERROR, "Could not create output path: " + path + " Please check file permissions.", null, 0,0 );
              return null;
            }
        }
        return path;
    }


    /**
     * Deletes the jar file of a bundle when it is outdated.
     *
     * @param compileContext the compile context
     * @param s              ??
     * @param validityState  the validity state of the item that is outdated
     */
    public void processOutdatedItem(CompileContext compileContext, String s,
                                    @Nullable final ValidityState validityState) {
        // delete the jar file of the module in case the stuff is outdated
        // TODO: find a way to update jar files so we can speed this up
        if (validityState != null) {
            ApplicationManager.getApplication().runReadAction(new Runnable() {
                public void run() {
                    BundleValidityState myvalstate = (BundleValidityState) validityState;
                    //noinspection ConstantConditions
                    String jarUrl = myvalstate.getOutputJarUrl();
                    if (jarUrl != null) {
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
    public ProcessingItem[] getProcessingItems(final CompileContext compileContext) {
        return ApplicationManager.getApplication().runReadAction(new Computable<ProcessingItem[]>() {
            public ProcessingItem[] compute() {
                // find and add all dependent modules to the list of stuff to be compiled
                CompileScope compilescope = compileContext.getCompileScope();
                Module[] affectedModules = compilescope.getAffectedModules();
                if (affectedModules.length == 0) {
                    return ProcessingItem.EMPTY_ARRAY;
                }
                Project project = affectedModules[0].getProject();
                Module[] modules = ModuleManager.getInstance(project).getModules();
                THashSet<Module> thashset = new THashSet<Module>();

                for (Module module : modules) {
                    if (!OsmorcFacet.hasOsmorcFacet(module)) {
                        continue;
                    }
                    thashset.add(module);
                }

                ProcessingItem[] result = new ProcessingItem[thashset.size()];
                int i = 0;
                for (Object aThashset : thashset) {
                    Module module = (Module) aThashset;
                    if (module.getModuleFile() != null) {
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
                                    ProcessingItem[] processingItems) {
        try {
            for (ProcessingItem processingItem : processingItems) {
                Module module = ((BundleProcessingItem) processingItem).getModule();
                buildBundle(module, compileContext.getProgressIndicator(), compileContext);
            }

        }
        catch (IOException ioexception) {
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
    private static void buildBundle(final Module module, final ProgressIndicator progressIndicator,
                                    final CompileContext compileContext)
            throws IOException {
        // create the jar file
        final File jarFile = new File(VfsUtil.urlToPath(getJarFileName(module)));
        if (jarFile.exists()) { //noinspection ResultOfMethodCallIgnored
            jarFile.delete();
        }
        FileUtil.createParentDirs(jarFile);

        // get the build recipe
        final BuildRecipe buildrecipe = (new ReadAction<BuildRecipe>() {
            protected void run(Result<BuildRecipe> result) {
                result.setResult(getBuildRecipe(module));
            }
        }).execute().getResultObject();

        final BndWrapper wrapper = new BndWrapper();
        final OsmorcFacetConfiguration configuration = OsmorcFacet.getInstance(module).getConfiguration();
        final List<String> classPaths = new ArrayList<String>();

        // get all the classpaths together
        try {
            buildrecipe.visitInstructionsWithExceptions(new BuildInstructionVisitor() {
                public boolean visitInstruction(BuildInstruction buildinstruction)
                        throws IOException {
                    ProgressManager.checkCanceled();
                    if (buildinstruction instanceof FileCopyInstruction) {
                        FileCopyInstruction filecopyinstruction = (FileCopyInstruction) buildinstruction;
                        File file2 = filecopyinstruction.getFile();
                        if (file2 == null || !file2.exists()) {
                            return true;
                        }

                        String s2 = FileUtil.toSystemDependentName(file2.getPath());
                        classPaths.add(VfsUtil.pathToUrl(s2));
                    }
                    return true;
                }
            }, false);
        }
        catch (ProcessCanceledException e) {
            // Fix for OSMORC-118
            compileContext.addMessage(CompilerMessageCategory.INFORMATION, "Process canceled.", null, 0, 0);
            return;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
            // e.printStackTrace();
        }


        // build a bnd file or use a provided one.
        String bndFileUrl = "";
        Map<String, String> additionalProperties = new HashMap<String, String>();
        if (configuration.isOsmorcControlsManifest() || configuration.isUseBndFile() ) {
            if (configuration.isUseBndFile()) {
                File bndFile = findFileInModuleContentRoots(configuration.getBndFileLocation(), module);
                if (bndFile == null || !bndFile.exists()) {
                    compileContext.addMessage(CompilerMessageCategory.ERROR,
                            String.format("The bnd file \"%s\" for module \"%s\" does not exist.",
                                    configuration.getBndFileLocation(), module.getName()),
                            configuration.getBndFileLocation(), 0, 0);
                    return;
                } else {
                    bndFileUrl = VfsUtil.pathToUrl(bndFile.getPath());
                }
            } else {
                // fully osmorc controlled, no bnd file.
                bndFileUrl = makeBndFile(module, configuration.asManifestString(), compileContext);
              if ( bndFileUrl == null ) {
                  // couldnt create bnd file.
                  return;
              }
            }
        } else {
            boolean manifestExists = false;
            BundleManager bundleManager = ServiceManager.getService(module.getProject(), BundleManager.class);
            BundleManifest bundleManifest = bundleManager.getBundleManifest(module);
            if (bundleManifest != null) {
                PsiFile manifestFile = bundleManifest.getManifestFile();
                if (manifestFile != null) {
                    String manifestFilePath = manifestFile.getVirtualFile().getPath();
                    if (manifestFilePath != null) {
                        bndFileUrl = makeBndFile(module, "-manifest " + manifestFilePath + "\n", compileContext);
                        manifestExists = true;
                    }
                }
            }
            if (!manifestExists) {
                compileContext.addMessage(CompilerMessageCategory.ERROR,
                        "Manifest file for module " + module.getName() + ": '" + OsmorcFacet.getInstance(module).getManifestLocation() +
                                "' does not exist or cannot be found. Check that file exists and is not excluded from the module.", null, 0, 0);
                return;
            }
        }

        if (!configuration.isOsmorcControlsManifest() ||
                (configuration.isOsmorcControlsManifest() && !configuration.isUseBndFile())) {
            // in this case we manually add all the classpaths as resources
            StringBuilder pathBuilder = new StringBuilder();
            // add all the classpaths to include resources, so stuff from the project gets copied over.
            // XXX: one could argue if this should be done for a non-osmorc build
            for (int i = 0; i < classPaths.size(); i++) {
                String classPath = classPaths.get(i);
                String relPath = FileUtil.getRelativePath(new File(VfsUtil.urlToPath(bndFileUrl)),
                        new File(VfsUtil.urlToPath(classPath)));
                if (i != 0) {
                    pathBuilder.append(",");
                }
                pathBuilder.append(relPath);
            }

            // now include the paths from the configuration
            List<Pair<String, String>> list = configuration.getAdditionalJARContents();
            for (Pair<String, String> stringStringPair : list) {
                pathBuilder.append(",").append(stringStringPair.second).append(" = ").append(stringStringPair.first);
            }

            // and tell bnd what resources to include
            additionalProperties.put("Include-Resource", pathBuilder.toString());

            if (!configuration.isIgnorePatternValid()) {
                compileContext.addMessage(CompilerMessageCategory.ERROR,
                        "The file ignore pattern in the facet configuration is invalid.", null, 0, 0);
                return;
            }

            // add the ignore pattern for the resources
            if (!"".equals(configuration.getIgnoreFilePattern())) {
                additionalProperties.put("-donotcopy", configuration.getIgnoreFilePattern());
            }

        }

        wrapper.build(compileContext, bndFileUrl, ArrayUtil.toStringArray(classPaths), jarFile.getPath(), additionalProperties);

        if (!configuration.isUseBndFile()) {
            // finally bundlify all the libs for this one
            bundlifyLibraries(module, progressIndicator, compileContext);
        }
    }

    @Nullable
    private static String makeBndFile(Module module, String contents, CompileContext compileContext) throws IOException {
      final String outputPath = getOutputPath(module, compileContext);
      if ( outputPath == null ) {
        return null;
      }
      File tmpFile = File.createTempFile("osmorc", ".bnd", new File(outputPath));
        // create one
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
        bos.write(contents.getBytes());
        bos.close();
        tmpFile.deleteOnExit();
        return VfsUtil.pathToUrl(tmpFile.getPath());

    }


    protected static
    @Nullable
    File findFileInModuleContentRoots(String file, Module module) {
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        for (VirtualFile root : manager.getContentRoots()) {
            VirtualFile result = VfsUtil.findRelativeFile(file, root);
            if (result != null) {
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
    public static VirtualFile getManifestFile(@NotNull Module module) {
        OsmorcFacet facet = OsmorcFacet.getInstance(module);
        // FIXES Exception (http://ea.jetbrains.com/browser/ea_problems/17161)
        if ( facet == null ) {
            return null;
        }
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        for (VirtualFile root : manager.getContentRoots()) {
            VirtualFile result = VfsUtil.findRelativeFile(facet.getManifestLocation(), root);
            // IDEADEV-40357
//            if (result != null) {
//                result = result.findChild("MANIFEST.MF");
//            }
            if (result != null) {
                return result;
            }
        }
        return null;
    }


    /**
     * @return a description (prolly not used)
     */
    @NotNull
    public String getDescription() {
        return "bundle compile";
    }

    /**
     * Checks the configuration.
     *
     * @param compileScope the compilescope
     * @return true if the configuration is valid, false otherwise
     */
    public boolean validateConfiguration(CompileScope compileScope) {
        return true;
    }

    /**
     * Recreates a validity state from a data input stream
     *
     * @param in stream containing the data
     * @return the validity state
     * @throws IOException in case something goes wrong
     */
    public ValidityState createValidityState(DataInput in) throws IOException {
        return new BundleValidityState(in);
    }

    /**
     * Creates a build recipe for the given module
     *
     * @param module the module
     * @return the build recipe
     */
    static BuildRecipe getBuildRecipe(Module module) {
        BuildRecipe buildrecipe = DeploymentUtil.getInstance().createBuildRecipe();

        // okay this is some hacking. we try to re-use the settings for building jars here and emulate
        // the jar settings dialog... YEEHA..

        final CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
        if (extension != null) {
            final String url = extension.getCompilerOutputUrl();
            final File root = new File(FileUtil.toSystemDependentName(VfsUtil.urlToPath(url)));
            buildrecipe.addFileCopyInstruction(root, true, "");
        }
        return buildrecipe;
    }

    /**
     * Bundlifies all libraries that belong to the given module and that are not bundles. The bundles are cached, so if
     * the source library does not change, it will not be bundlified again.
     *
     * @param module         the module whose libraries are to be bundled.
     * @param indicator      a progress indicator.
     * @param compileContext
     * @return a string array containing the urls of the bundlified libraries.
     */
    public static String[] bundlifyLibraries(Module module, ProgressIndicator indicator,
                                             @NotNull CompileContext compileContext) {
        ArrayList<String> result = new ArrayList<String>();
        final ModuleRootManager manager = ModuleRootManager.getInstance(module);
        LibraryHandler libraryHandler = ServiceManager.getService(LibraryHandler.class);
        ModifiableRootModel model = new ReadAction<ModifiableRootModel>() {
            protected void run(Result<ModifiableRootModel> result) throws Throwable {
                ModifiableRootModel model = manager.getModifiableModel();
                result.setResult(model);
                // this breaks the build as the resulting order entries are all in disposed state and cannot be used
                // afterwards.
                //  model.dispose();
                // therefore the model is returned, and disposed after reading.
            }
        }.execute().getResultObject();

        OrderEntry[] entries = model.getOrderEntries();
        try {
            for (OrderEntry entry : entries) {
                if (entry instanceof JdkOrderEntry) {
                    continue; // do not bundlify JDKs
                }

                if (entry instanceof LibraryOrderEntry &&
                        libraryHandler.isFrameworkInstanceLibrary((LibraryOrderEntry) entry)) {
                    continue; // do not bundlify framework instance libraries
                }

                BndWrapper wrapper = new BndWrapper();
                String[] urls = entry.getUrls(OrderRootType.CLASSES);
                for (String url : urls) {
                    url = convertJarUrlToFileUrl(url);


                    if (CachingBundleInfoProvider.canBeBundlified(url)) {
                        indicator.setText("Bundling non-OSGi libraries for module: " + module.getName());
                        indicator.setText2(url);
                        // ok it is not a bundle, so we need to bundlify
                      final String outputPath = getOutputPath(module, compileContext);
                      if ( outputPath == null ) {
                         // couldnt create output path, abort here..
                         break;
                      }
                      String bundledLocation = wrapper.wrapLibrary(compileContext, url, outputPath);
                        // if no bundle could (or should) be created, we exempt this library
                        if (bundledLocation != null) {
                            result.add(fixFileURL(bundledLocation));
                        }
                    } else {
                      if (CachingBundleInfoProvider.isBundle(url)) {
                        result.add(fixFileURL(url));
                      }
                    }
                }
            }
          return ArrayUtil.toStringArray(result);
        }
        finally {
            model.dispose();
        }
    }

    /**
     * Converts a jar url gained from OrderEntry.getUrls or Library.getUrls into a file url that can be processed.
     *
     * @param url the url to be converted
     * @return the converted url
     */
    public static String convertJarUrlToFileUrl(String url) {
        // urls end with !/ we cut that
        // XXX: not sure if this is a hack
        url = url.replaceAll("!.*", "");
        url = url.replace("jar://", "file://");
        return url;
    }

    /**
     * On Windows a file url must have at least 3 slashes at the beginning. 2 for the protocoll separation and one for
     * the empty host (e.g.: file:///c:/bla instead of file://c:/bla). If there are only two the drive letter is
     * interpreted as the host of the url which naturally doesn't exist. On Unix systems it's the same case, but since
     * all paths start with a slash, a misinterpretation of part of the path as a host cannot occur.
     *
     * @param url The URL to fix
     * @return The fixed URL
     */
    public static String fixFileURL(String url) {
        return url.startsWith("file:///") ? url : url.replace("file://", "file:///");
    }

    /**
     * Builds the name of the jar file for a given module.
     *
     * @param module the module
     * @return the name of the jar file that will be produced for this module by this compiler, or
     *         null if the module does not have an Osmorc facet attached.
     */
    @Nullable
    public static String getJarFileName(final Module module) {
        final OsmorcFacet facet = OsmorcFacet.getInstance(module);
        if (facet != null) {
            return facet.getConfiguration().getJarFileLocation();
        }
        return null;
    }

}
