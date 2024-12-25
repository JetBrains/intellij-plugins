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
package org.osmorc.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.jps.model.LibraryBundlificationRule;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Application wide settings which apply to all Osmorc driven projects.
 *
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
@State(name = "Osmorc", storages = @Storage(value = "osmorc.xml", roamingType = RoamingType.DISABLED))
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings> {
  private List<FrameworkInstanceDefinition> myInstances = new ArrayList<>();
  private List<LibraryBundlificationRule> myRules = List.of(new LibraryBundlificationRule());

  public static ApplicationSettings getInstance() {
    return ApplicationManager.getApplication().getService(ApplicationSettings.class);
  }

  @Override
  public ApplicationSettings getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull ApplicationSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @SuppressWarnings("deprecation")
  @AbstractCollection(elementTag = "frameworkDefinition")
  public List<FrameworkInstanceDefinition> getFrameworkInstanceDefinitions() {
    return myInstances;
  }

  public void setFrameworkInstanceDefinitions(List<FrameworkInstanceDefinition> instances) {
    myInstances = instances;
  }

  @SuppressWarnings("deprecation")
  @AbstractCollection(elementTag = "libraryBundlificationRule")
  public List<LibraryBundlificationRule> getLibraryBundlificationRules() {
    return myRules;
  }

  public void setLibraryBundlificationRules(List<LibraryBundlificationRule> rules) {
    myRules = rules;
    if (myRules == null || myRules.isEmpty()) {
      myRules = List.of(new LibraryBundlificationRule());
    }
  }

  public @Nullable FrameworkInstanceDefinition getFrameworkInstance(@Nullable String name) {
    if (name != null) {
      for (FrameworkInstanceDefinition frameworkInstanceDefinition : myInstances) {
        if (name.equals(frameworkInstanceDefinition.getName())) {
          return frameworkInstanceDefinition;
        }
      }
    }
    return null;
  }

  public @NotNull List<FrameworkInstanceDefinition> getActiveFrameworkInstanceDefinitions() {
    Set<String> names = ContainerUtil.map2Set(FrameworkIntegratorRegistry.getInstance().getFrameworkIntegrators(), FrameworkIntegrator::getDisplayName);
    return ContainerUtil.filter(myInstances, definition -> names.contains(definition.getFrameworkIntegratorName()));
  }
}
