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
package org.osmorc.manifest.lang.psi;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.jetbrains.osgi.project.BundleManifest;
import org.jetbrains.osgi.project.BundleManifestCache;
import org.osgi.framework.Constants;
import org.osmorc.i18n.OsmorcBundle;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class BundleReference extends PsiReferenceBase<HeaderValuePart> implements EmptyResolveMessageProvider {
  public BundleReference(@NotNull HeaderValuePart element) {
    super(element);
  }

  @Override
  public @Nullable PsiElement resolve() {
    return ResolveCache.getInstance(myElement.getProject()).resolveWithCaching(this, RESOLVER, false, false);
  }

  @Override
  public @NotNull String getUnresolvedMessagePattern() {
    return OsmorcBundle.message("cannot.resolve.bundle", getCanonicalText());
  }

  private static final ResolveCache.AbstractResolver<BundleReference, PsiElement> RESOLVER =
    new ResolveCache.AbstractResolver<>() {
      @Override
      public PsiElement resolve(@NotNull BundleReference reference, boolean incompleteCode) {
        String text = reference.getCanonicalText();
        HeaderValuePart refElement = reference.getElement();

        if (!StringUtil.isEmptyOrSpaces(text) && refElement.isValid()) {
          Module module = ModuleUtilCore.findModuleForPsiElement(refElement);
          if (module != null) {
            Ref<PsiElement> result = Ref.create();
            String refText = text.replaceAll("\\s", "");
            BundleManifestCache cache = BundleManifestCache.getInstance();
            ModuleRootManager manager = ModuleRootManager.getInstance(module);

            manager.orderEntries().forEachModule(module1 -> {
              BundleManifest manifest = cache.getManifest(module1);
              if (manifest != null && refText.equals(manifest.getBundleSymbolicName())) {
                result.set(getTarget(manifest));
                return false;
              }
              return true;
            });
            if (!result.isNull()) return result.get();

            manager.orderEntries().forEachLibrary(library -> {
              for (VirtualFile libRoot : library.getFiles(OrderRootType.CLASSES)) {
                BundleManifest manifest = cache.getManifest(libRoot, refElement.getManager());
                if (manifest != null && refText.equals(manifest.getBundleSymbolicName())) {
                  result.set(getTarget(manifest));
                  return false;
                }
              }
              return true;
            });
            if (!result.isNull()) return result.get();
          }
        }

        return null;
      }

      private PsiElement getTarget(BundleManifest manifest) {
        PsiFile source = manifest.getSource();

        if (source instanceof ManifestFile) {
          Header header = ((ManifestFile)source).getHeader(Constants.BUNDLE_SYMBOLICNAME);
          if (header != null) {
            return header;
          }
        }

        return source;
      }
    };
}
