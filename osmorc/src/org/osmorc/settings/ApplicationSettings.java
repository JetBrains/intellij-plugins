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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.osgi.jps.model.LibraryBundlificationRule;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;

import java.util.List;

/**
 * Application wide settings which apply to all Osmorc driven projects.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
@State(name = "Osmorc", storages = @Storage("osmorc.xml"))
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings> {
  private List<FrameworkInstanceDefinition> myInstances = ContainerUtil.newArrayList();
  private List<FrameworkInstanceDefinition> myActiveInstances = null;
  private List<LibraryBundlificationRule> myRules = ContainerUtil.newArrayList(new LibraryBundlificationRule());

  public static ApplicationSettings getInstance() {
    return ServiceManager.getService(ApplicationSettings.class);
  }

  @Override
  public ApplicationSettings getState() {
    return this;
  }

  @Override
  public void loadState(ApplicationSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @AbstractCollection(elementTag = "frameworkDefinition")
  public List<FrameworkInstanceDefinition> getFrameworkInstanceDefinitions() {
    return myInstances;
  }

  public void setFrameworkInstanceDefinitions(List<FrameworkInstanceDefinition> instances) {
    myInstances = instances;
    myActiveInstances = null;
  }

  @AbstractCollection(elementTag = "libraryBundlificationRule")
  public List<LibraryBundlificationRule> getLibraryBundlificationRules() {
    return myRules;
  }

  public void setLibraryBundlificationRules(List<LibraryBundlificationRule> rules) {
    myRules = rules;
    if (myRules == null || myRules.isEmpty()) {
      myRules = ContainerUtil.newArrayList(new LibraryBundlificationRule());
    }
  }

  @Nullable
  public FrameworkInstanceDefinition getFrameworkInstance(@Nullable String name) {
    if (name != null) {
      for (FrameworkInstanceDefinition frameworkInstanceDefinition : myInstances) {
        if (name.equals(frameworkInstanceDefinition.getName())) {
          return frameworkInstanceDefinition;
        }
      }
    }
    return null;
  }

  @NotNull
  public List<FrameworkInstanceDefinition> getActiveFrameworkInstanceDefinitions() {
    if (myActiveInstances == null) {
      final FrameworkIntegratorRegistry registry = FrameworkIntegratorRegistry.getInstance();
      myActiveInstances = ContainerUtil.filter(myInstances, definition -> registry.findIntegratorByInstanceDefinition(definition) != null);
    }
    return myActiveInstances;
  }
}
