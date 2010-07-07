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

package org.osmorc.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.eclipse.osgi.framework.internal.core.Constants;
import org.eclipse.osgi.service.resolver.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.BundleException;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderRegistry;
import org.osmorc.settings.ApplicationSettings;
import org.osmorc.settings.ProjectSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class BundleManagerImpl implements BundleManager {
    public BundleManagerImpl(ModuleManager moduleManager, ManifestHolderRegistry manifestHolderRegistry,
                             FrameworkIntegratorRegistry frameworkIntegratorRegistry,
                             ProjectSettings projectSettings, ApplicationSettings applicationSettings) {
        this.moduleManager = moduleManager;
        this.manifestHolderRegistry = manifestHolderRegistry;
        this.frameworkIntegratorRegistry = frameworkIntegratorRegistry;
        this.projectSettings = projectSettings;
        this.applicationSettings = applicationSettings;
    }

    public Object findBundle(String bundleSymbolicName) {
        Module[] modules = moduleManager.getModules();
        for (Module module : modules) {
            BundleManifest bundleManifest = getBundleManifest(module);
            if (bundleManifest != null && bundleSymbolicName.equals(bundleManifest.getBundleSymbolicName())) {
                return module;
            }
        }

        List<Library> libraries = getFrameworkInstanceLibraries();
        for (Library library : libraries) {
            BundleManifest bundleManifest = getBundleManifest(library);
            if (bundleManifest != null && bundleSymbolicName.equals(bundleManifest.getBundleSymbolicName())) {
                return library;
            }

        }

        return null;
    }

    public BundleManifest getBundleManifest(String bundleSymbolicName) {
        Object bundle = findBundle(bundleSymbolicName);

        if (bundle != null) {
            return getBundleManifest(bundle);
        }
        return null;
    }

    public BundleManifest getBundleManifest(@NotNull Object bundle) {
        return getManifestHolder(bundle).getBundleManifest();
    }

    public void addOrUpdateBundle(@NotNull Object bundle) {
        createInitialState();
        addOrUpdateBundleInternal(bundle);
        _state.resolve();
    }

    private void addOrUpdateBundleInternal(Object bundle) {
        BundleManifest bundleManifest = getBundleManifest(bundle);
        if (bundleManifest != null) {
            try {
                BundleDescription oldDescription = getBundleDescription(bundle);
                BundleDescription bundleDescription = createBundleDescription(bundleManifest,
                        oldDescription != null ? oldDescription.getBundleId() : _state.getHighestBundleId() + 1);

                if (bundleDescription != null) {
                    getManifestHolder(bundle).setBundleID(bundleDescription.getBundleId());
                    bundleDescription.setUserObject(bundle);
                    if (oldDescription != null) {
                        _state.updateBundle(bundleDescription);
                    } else {
                        _state.addBundle(bundleDescription);
                    }
                }
            }
            catch (IOException e) {
                LOG.debug(e);
            }
        }
    }

    @Nullable
    public BundleDescription getBundleDescription(Object bundle) {
        long bundleID = getManifestHolder(bundle).getBundleID();
        return bundleID >= 0 ? _state.getBundle(bundleID) : null;
    }

    public Collection<Object> determineBundleDependencies(@NotNull Object bundle) {
        createInitialState();
        Collection<Object> result = new HashSet<Object>();

        BundleDescription bundleDescription = getBundleDescription(bundle);
        if (bundleDescription == null && bundle instanceof Module && OsmorcFacet.hasOsmorcFacet((Module) bundle)) {
            addOrUpdateBundle(bundle);
            bundleDescription = getBundleDescription(bundle);
        }

        if (bundleDescription != null) {
            List<BundleDescription> resolvedRequires = getResolvedRequires(bundle);
            for (BundleDescription resolvedRequire : resolvedRequires) {
                Object resolvedRequiredBundle = resolvedRequire.getUserObject();
                result.add(resolvedRequiredBundle);
                result.addAll(determineReexportedRequiredBundlesOnLibraries(resolvedRequiredBundle));
            }
            List<ExportPackageDescription> resolvedImports = getResolvedImports(bundle);
            for (ExportPackageDescription resolvedImport : resolvedImports) {
                result.add(resolvedImport.getExporter().getUserObject());
            }

            result.addAll(getFragments(result));
            HostSpecification hostSpecification = bundleDescription.getHost();
            if (hostSpecification != null && hostSpecification.getHosts() != null) {
                for (BundleDescription hostBundleDescription : hostSpecification.getHosts()) {
                    result.add(hostBundleDescription.getUserObject());
                }
            }
            result.remove(bundle);
        }

        return result;
    }

    public List<BundleDescription> getResolvedRequires(@NotNull Object bundle) {
        List<BundleDescription> result = new ArrayList<BundleDescription>();
        BundleDescription bundleDescription = getBundleDescription(bundle);
        if (bundleDescription != null) {
          ContainerUtil.addAll(result, bundleDescription.getResolvedRequires());
          HostSpecification hostSpecification = bundleDescription.getHost();
          if (hostSpecification != null && hostSpecification.getHosts() != null) {
            for (BundleDescription hostBundleDescription : hostSpecification.getHosts()) {
              result.addAll(getResolvedRequires(hostBundleDescription.getUserObject()));
            }
          }
        }

        return result;
    }

    private List<BundleSpecification> getRequiredBundles(@NotNull Object bundle) {
        List<BundleSpecification> result = new ArrayList<BundleSpecification>();
        BundleDescription bundleDescription = getBundleDescription(bundle);
        if (bundleDescription != null) {
          ContainerUtil.addAll(result, bundleDescription.getRequiredBundles());
          HostSpecification hostSpecification = bundleDescription.getHost();
          if (hostSpecification != null && hostSpecification.getHosts() != null) {
            for (BundleDescription hostBundleDescription : hostSpecification.getHosts()) {
              result.addAll(getRequiredBundles(hostBundleDescription.getUserObject()));
            }
          }
        }

        return result;
    }

    public List<ExportPackageDescription> getResolvedImports(@NotNull Object bundle) {
        List<ExportPackageDescription> result = new ArrayList<ExportPackageDescription>();
        BundleDescription bundleDescription = getBundleDescription(bundle);
        if (bundleDescription != null) {
          ContainerUtil.addAll(result, bundleDescription.getResolvedImports());
          HostSpecification hostSpecification = bundleDescription.getHost();
          if (hostSpecification != null && hostSpecification.getHosts() != null) {
            for (BundleDescription hostBundleDescription : hostSpecification.getHosts()) {
              result.addAll(getResolvedImports(hostBundleDescription.getUserObject()));
            }
          }
        }

        return result;
    }

    private Collection<Object> getFragments(Collection<Object> bundles) {
        Collection<Object> result = new HashSet<Object>();

        for (Object bundle : bundles) {
            BundleDescription bundleDescription = getBundleDescription(bundle);
            if (bundleDescription != null) {
                BundleDescription[] fragments = bundleDescription.getFragments();
                if (fragments != null) {
                    for (BundleDescription fragment : fragments) {
                        result.add(fragment.getUserObject());
                    }
                }
            }
        }

        return result;
    }

    private Collection<Object> determineReexportedRequiredBundlesOnLibraries(@NotNull Object bundle) {
        Collection<Object> result = new HashSet<Object>();
        if (bundle instanceof Library) {
            List<BundleDescription> resolvedRequires = getResolvedRequires(bundle);
            List<BundleSpecification> requiredBundles = getRequiredBundles(bundle);
            if (requiredBundles != null && requiredBundles.size() > 0) {
                for (BundleSpecification requiredBundle : requiredBundles) {
                    if (requiredBundle.isExported()) {
                        for (BundleDescription resolvedRequire : resolvedRequires) {
                            if (requiredBundle.isSatisfiedBy(resolvedRequire)) {
                                Object resolvedRequiredBundle = resolvedRequire.getUserObject();
                                result.add(resolvedRequiredBundle);
                                result.addAll(determineReexportedRequiredBundlesOnLibraries(resolvedRequiredBundle));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public boolean isReexported(@NotNull Object reexportCandidate, @NotNull Object exporter) {
        BundleDescription reexportCandidateDescription = getBundleDescription(reexportCandidate);

        if (reexportCandidateDescription != null) {
            HostSpecification hostSpecification = reexportCandidateDescription.getHost();
            if (hostSpecification != null && hostSpecification.getHosts() != null) {
                for (BundleDescription hostBundleDescription : hostSpecification.getHosts()) {
                    if (isReexported(hostBundleDescription.getUserObject(), exporter)) {
                        return true;
                    }
                }
            }

            BundleDescription exporterDescription = getBundleDescription(exporter);
            if (exporterDescription != null) {
                BundleSpecification[] requiredBundles = exporterDescription.getRequiredBundles();
                if (requiredBundles != null && requiredBundles.length > 0) {
                    for (BundleSpecification requiredBundle : requiredBundles) {
                        if (requiredBundle.isSatisfiedBy(reexportCandidateDescription)) {
                            return requiredBundle.isExported();
                        }
                    }
                }
            }
        }
        return false;
    }

    @NotNull
    public Collection<Object> getHostBundles(@NotNull Object bundle) {
        Collection<Object> result = new ArrayList<Object>();
        BundleDescription bundleDescription = getBundleDescription(bundle);
        if (bundleDescription != null && bundleDescription.getHost() != null) {
            HostSpecification hostSpecification = bundleDescription.getHost();
            BundleDescription[] hosts = hostSpecification.getHosts();
            if (hosts != null) {
                for (BundleDescription host : hosts) {
                    result.add(host.getUserObject());
                }
            }
        }

        return result;
    }

    public boolean reloadFrameworkInstanceLibraries(boolean onlyIfFrameworkInstanceSelectionChanged) {
        String frameworkInstanceName = projectSettings.getFrameworkInstanceName();
        if (!onlyIfFrameworkInstanceSelectionChanged ||
                (frameworkInstanceName != null && !frameworkInstanceName.equals(_currentFrameworkInstanceName))) {
            if (_state == null) {
                createInitialState();
            } else {
                List<Long> longs = manifestHolderRegistry.getLibraryBundleIDs();
                for (Long libraryBundleID : longs) {
                    _state.removeBundle(libraryBundleID);
                }
                manifestHolderRegistry.clearLibraryManifestHolders();
                loadFrameworkInstanceLibraryBundles();
                _state.resolve();
            }
            return true;
        }
        return false;
    }

    private BundleDescription createBundleDescription(BundleManifest bundleManifest, long bundleID) throws IOException {
        BundleDescription bundleDescription = null;
        try {
            InputStream inputStream =
                    new ByteArrayInputStream((bundleManifest.getManifestFile().getText() + "\n").getBytes());
            Manifest manifest = new Manifest(inputStream);
            Attributes mainAttributes = manifest.getMainAttributes();
            Properties properties = new Properties();
            for (Object attributeKey : mainAttributes.keySet()) {
                properties.put(attributeKey.toString(), mainAttributes.get(attributeKey));
            }

            // TODO: Replace this creation of BundleDescription with one without additional parsing as soon as Osmorc parses all necessary headers, attributes and directives.
            bundleDescription =
                    StateObjectFactory.defaultFactory.createBundleDescription(_state, properties, null, bundleID);
        }
        catch (BundleException e) {
            LOG.debug(e);
        }
        catch (NumberFormatException e) {
            LOG.debug(e);
        }
        catch (IllegalArgumentException e) {
            LOG.debug(e);
        }
        return bundleDescription;
    }

    private synchronized void createInitialState() {
        if (_state == null) {
            _state = StateObjectFactory.defaultFactory.createState(true);
            Module[] modules = moduleManager.getModules();
            for (Module module : modules) {
                addOrUpdateBundleInternal(module);
            }

            loadFrameworkInstanceLibraryBundles();
            Properties platformProperties = new Properties();
            platformProperties.put(Constants.OSGI_RESOLVER_MODE, Constants.DEVELOPMENT_MODE);
            _state.setPlatformProperties(platformProperties);
            _state.resolve();
        }
    }

    private void loadFrameworkInstanceLibraryBundles() {
        _currentFrameworkInstanceName = projectSettings.getFrameworkInstanceName();
        if (_currentFrameworkInstanceName != null) {
            List<Library> libraries = getFrameworkInstanceLibraries();
            for (Library library : libraries) {
                addOrUpdateBundleInternal(library);
            }
        }
    }

    private List<Library> getFrameworkInstanceLibraries() {
        FrameworkInstanceDefinition frameworkInstanceDefinition =
                applicationSettings.getFrameworkInstance(projectSettings.getFrameworkInstanceName());
        List<Library> libraries = null;
        if (frameworkInstanceDefinition != null) {
            FrameworkIntegrator frameworkIntegrator =
                    frameworkIntegratorRegistry.findIntegratorByInstanceDefinition(frameworkInstanceDefinition);
            libraries = frameworkIntegrator.getFrameworkInstanceManager().getLibraries(frameworkInstanceDefinition);
        }
        if (libraries == null) {
            libraries = Collections.emptyList();
        }
        return libraries;
    }

    protected ManifestHolder getManifestHolder(Object bundle) {
        return manifestHolderRegistry.getManifestHolder(bundle);
    }

  @NotNull
  public String getDisplayName(@NotNull Object bundle) {
    if (bundle instanceof Module) {
      return ((Module)bundle).getName();
    }
    if ( bundle instanceof Library ) {
      final String libName = ((Library)bundle).getName();
      if ( libName == null ) {
        final VirtualFile[] files = ((Library)bundle).getFiles(OrderRootType.CLASSES);
        if ( files.length > 0 ) {
          final VirtualFile file = files[0];
          if ( file != null) {
            return file.getName();
          }
        }
        return "unnamed library";
      }
      else {
        return libName;
      }
    }
    return bundle.toString();
  }

  private final ModuleManager moduleManager;
    private final ManifestHolderRegistry manifestHolderRegistry;
    private final FrameworkIntegratorRegistry frameworkIntegratorRegistry;
    private final ProjectSettings projectSettings;
    private final ApplicationSettings applicationSettings;
    private State _state;

    private static final Logger LOG = Logger.getInstance("org.osmorc.BundleManagerImpl");
    private String _currentFrameworkInstanceName;
}
