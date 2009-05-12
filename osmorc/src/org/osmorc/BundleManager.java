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

package org.osmorc;

import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.BundleManifest;

import java.util.Collection;
import java.util.List;

/**
 * The bundle manager allows for queries over the bundles which are known in the current project.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
public interface BundleManager
{
  // TODO: not used outside, refactor it out
  Object findBundle(String bundleSymbolicName);

  BundleManifest getBundleManifest(String bundleSymbolicName);

  BundleManifest getBundleManifest(@NotNull Object bundle);

  void addOrUpdateBundle(@NotNull Object bundle);

  @Nullable
  BundleDescription getBundleDescription(Object bundle);

  Collection<Object> determineBundleDependencies(@NotNull Object bundle);

  boolean isReexported(@NotNull Object reexportCandidate, @NotNull Object exporter);

  boolean reloadFrameworkInstanceLibraries(boolean onlyIfFrameworkInstanceSelectionChanged);

  Collection<Object> getHostBundles(@NotNull Object bundle);

  List<BundleDescription> getResolvedRequires(@NotNull Object bundle);

  List<ExportPackageDescription> getResolvedImports(@NotNull Object bundle);
}
