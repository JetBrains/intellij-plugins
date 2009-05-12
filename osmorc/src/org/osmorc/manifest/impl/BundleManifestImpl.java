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
package org.osmorc.manifest.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.lang.psi.ManifestClause;
import org.osmorc.manifest.lang.psi.ManifestFile;
import org.osmorc.manifest.lang.psi.ManifestHeader;
import org.osmorc.manifest.lang.psi.ManifestHeaderValue;
import org.osmorc.valueobject.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class BundleManifestImpl implements BundleManifest
{
  public BundleManifestImpl(PsiFile manifestFile)
  {
    _manifestFile = manifestFile;
  }

  public PsiFile getManifestFile()
  {
    return _manifestFile;
  }

  public Version getBundleVersion()
  {
    return (Version) getHeaderValue(BUNDLE_VERSION);
  }

  public String getBundleSymbolicName()
  {
    return (String) getHeaderValue(BUNDLE_SYMBOLIC_NAME);
  }

  public String getBundleActivator()
  {
    return (String) getHeaderValue(BUNDLE_ACTIVATOR);
  }

  public List<String> getExportPackage()
  {
    return getAllPaths(EXPORT_PACKAGE);
  }

  public List<PsiPackage> getImportPackage()
  {
    List<PsiReference> allPathReferences = getAllPathReferences(IMPORT_PACKAGE);
    List<PsiPackage> importedPackages = new ArrayList<PsiPackage>();

    for (PsiReference pathReference : allPathReferences)
    {
      PsiPackage psiPackage = (PsiPackage) pathReference.resolve();
      if (psiPackage != null)
      {
        importedPackages.add(psiPackage);
      }
    }

    return importedPackages;
  }

  public List<Module> getRequireBundle()
  {
    List<PsiReference> allPathReferences = getAllPathReferences(REQUIRE_BUNDLE);
    List<Module> requiredBundles = new ArrayList<Module>();

    for (PsiReference pathReference : allPathReferences)
    {
      ManifestFile manifestFile = (ManifestFile) pathReference.resolve();
      if (manifestFile != null)
      {
        requiredBundles.add(ModuleUtil.findModuleForPsiElement(manifestFile));
      }
    }

    return requiredBundles;
  }

  public boolean exportsPackage(@NotNull String aPackage)
  {
    return getExportPackage().contains(aPackage);
  }

  private List<String> getAllPaths(@NotNull String headerName)
  {
    List<String> result = new ArrayList<String>();
    ManifestHeader header = findHeader(headerName);
    if (header != null)
    {
      Collection<ManifestClause> clauses = findAllChildrenOfType(header, ManifestClause.class);
      for (ManifestClause clause : clauses)
      {
        Collection<ManifestHeaderValue> headerValues = findAllChildrenOfType(clause, ManifestHeaderValue.class);
        for (ManifestHeaderValue headerValue : headerValues)
        {
          String value = headerValue.getValueText();
          if (!result.contains(value))
          {
            result.add(value);
          }
        }
      }
    }

    return result;
  }

  private List<PsiReference> getAllPathReferences(@NotNull String headerName)
  {
    List<PsiReference> result = new ArrayList<PsiReference>();
    ManifestHeader header = findHeader(headerName);
    if (header != null)
    {
      Collection<ManifestClause> clauses = findAllChildrenOfType(header, ManifestClause.class);
      for (ManifestClause clause : clauses)
      {
        Collection<ManifestHeaderValue> headerValues = findAllChildrenOfType(clause, ManifestHeaderValue.class);
        for (ManifestHeaderValue headerValue : headerValues)
        {
          PsiReference value = headerValue.getReference();
          if (!result.contains(value))
          {
            result.add(value);
          }
        }
      }
    }

    return result;
  }

  private Object getHeaderValue(@NotNull String headerName)
  {
    ManifestHeader header = findHeader(headerName);
    ManifestHeaderValue value = null;
    if (header != null)
    {

      ManifestClause clause = PsiTreeUtil.getChildOfType(header, ManifestClause.class);
      if (clause != null)
      {

        value = PsiTreeUtil.getChildOfType(clause, ManifestHeaderValue.class);
      }
    }

    return value != null ? value.getValue() : null;
  }

  private ManifestHeader findHeader(@NotNull String headerName)
  {

    ManifestHeader header = PsiTreeUtil.getChildOfType(_manifestFile, ManifestHeader.class);
    while (header != null && !headerName.equalsIgnoreCase(header.getName()))
    {
      header = PsiTreeUtil.getNextSiblingOfType(header, ManifestHeader.class);
    }
    return header;
  }

  private <T extends PsiElement> List<T> findAllChildrenOfType(
      @NotNull PsiElement element, @NotNull Class<T> elementClass)
  {
    List<T> result = new ArrayList<T>();

    T currentElement = PsiTreeUtil.getChildOfType(element, elementClass);
    while (currentElement != null)
    {
      result.add(currentElement);
      currentElement = PsiTreeUtil.getNextSiblingOfType(currentElement, elementClass);
    }
    return result;
  }


  private static final String REQUIRE_BUNDLE = "Require-Bundle";
  private static final String IMPORT_PACKAGE = "Import-Package";
  private static final String EXPORT_PACKAGE = "Export-Package";
  private static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
  private static final String BUNDLE_VERSION = "Bundle-Version";
  private static final String BUNDLE_ACTIVATION_POLICY = "Bundle-ActivationPolicy";
  private static final String BUNDLE_ACTIVATOR = "Bundle-Activator";
  private static final String BUNDLE_CATEGORY = "Bundle-Category";
  private static final String BUNDLE_CLASSPATH = "Bundle-Classpath";
  private static final String BUNDLE_CONTACT_ADDRESS = "Bundle-ContactAddress";
  private static final String BUNDLE_COPYRIGHT = "Bundle-Copyright";
  private static final String BUNDLE_DESCRIPTION = "Bundle-Description";
  private static final String BUNDLE_DOC_URL = "Bundle-DocURL";
  private static final String BUNDLE_LOCALIZATION = "Bundle-Localization";
  private static final String BUNDLE_MANIFEST_VERSION = "Bundle-ManifestVersion";
  private static final String BUNDLE_NAME = "Bundle-Name";
  private static final String BUNDLE_NATIVE_CODE = "Bundle-NativeCode";
  private static final String BUNDLE_REQUIRED_EXECUTION_ENVIRONMENT = "Bundle-RequiredExecutionEnvironment";
  private static final String BUNDLE_UPDATE_LOCATION = "Bundle-UpdateLocation";
  private static final String BUNDLE_VENDOR = "Bundle-Vendor";
  private static final String DYNAMIC_IMPORT_PACKAGE = "DynamicImport-Package";
  private static final String FRAGMENT_HOST = "Fragment-Host";
  private final PsiFile _manifestFile;
}
