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
package org.osmorc.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleActivator;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.manifest.BundleManifest;

/**
 * Inspection that reports classes implementing BundleActivator which are not registered in the manifest / facet
 * config.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id$
 */
public class UnregisteredActivatorInspection extends LocalInspectionTool
{

  @Nls
  @NotNull
  public String getGroupDisplayName()
  {
    return "Osmorc";
  }

  public boolean isEnabledByDefault()
  {
    return true;
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel()
  {
    return HighlightDisplayLevel.ERROR;
  }

  @Nls
  @NotNull
  public String getDisplayName()
  {
    return "Bundle Activator not registered";
  }

  @NonNls
  @NotNull
  public String getShortName()
  {
    return "osmorcUnregisteredActivator";
  }

  @NotNull
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly)
  {
    return new JavaElementVisitor()
    {
      public void visitReferenceExpression(PsiReferenceExpression expression)
      {
      }

      @Override
      public void visitClass(PsiClass psiClass)
      {
        if (OsmorcFacet.hasOsmorcFacet(psiClass))
        {
          PsiType[] types = psiClass.getSuperTypes();
          for (PsiType type : types)
          {
            if (type.equalsToText(BundleActivator.class.getName()))
            {
              // okay extends bundle activator
              OsmorcFacetConfiguration configuration = OsmorcFacet.getInstance(psiClass).getConfiguration();

              // if manifest is manually written, look it up in the manifest file
              if (configuration.isManifestManuallyEdited())
              {
                BundleManager bundleManager = ServiceManager.getService(psiClass.getProject(), BundleManager.class);
                Module module = ModuleUtil.findModuleForPsiElement(psiClass);
                if (!isActivatorRegistered(bundleManager, module, psiClass.getQualifiedName()))
                {
                  holder
                      .registerProblem(psiClass.getNameIdentifier(), "Bundle activator is not registered in manifest.",
                          ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
              }
              else
              {
                // automagically, so look it up in the configuration
                String configuredActivator = configuration.getBundleActivator();
                if (!configuredActivator.equals(psiClass.getQualifiedName()))
                {
                  holder.registerProblem(psiClass.getNameIdentifier(),
                      "Bundle activator is not set up in facet configuration.",
                      ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
              }
            }
          }
        }
      }
    };
  }

  private boolean isActivatorRegistered(BundleManager manager, Object bundle, String activatorName)
  {
    BundleManifest manifest = manager.getBundleManifest(bundle);
    if (manifest != null)
    {
      String manifestActivator = manifest.getBundleActivator();
      return manifestActivator != null && manifestActivator.equals(activatorName);
    }
    return true;
  }


}
