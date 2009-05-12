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
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.LibraryBundlificationRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Application wide settings which apply to all Osmorc driven projects.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 */
@State(
    name = "Osmorc",
    storages = {
        @Storage(
            id = "Osmorc",
            file = "$APP_CONFIG$/osmorc.xml"
        )}
)
public class ApplicationSettings implements PersistentStateComponent<ApplicationSettings>
{

  @AbstractCollection(elementTag = "frameworkDefinition")
  public List<FrameworkInstanceDefinition> getFrameworkInstanceDefinitions()
  {
    return _frameworkInstanceDefinitions;
  }

  public void setFrameworkInstanceDefinitions(List<FrameworkInstanceDefinition> frameworkInstanceDefinitions)
  {
    _frameworkInstanceDefinitions = frameworkInstanceDefinitions;
  }

  public void addFrameworkInstanceDefinition(FrameworkInstanceDefinition frameworkInstanceDefinition)
  {
    _frameworkInstanceDefinitions.add(frameworkInstanceDefinition);
  }

  public void removeFrameworkInstanceDefinition(FrameworkInstanceDefinition frameworkInstanceDefinition)
  {
    _frameworkInstanceDefinitions.remove(frameworkInstanceDefinition);
  }

  @AbstractCollection(elementTag = "libraryBundlificationRule")
  public List<LibraryBundlificationRule> getLibraryBundlificationRules()
  {
    return _libraryBundlificationRules;
  }

  public void setLibraryBundlificationRules(List<LibraryBundlificationRule> libraryBundlificationRules)
  {
    _libraryBundlificationRules = libraryBundlificationRules;
  }


  public @Nullable FrameworkInstanceDefinition getFrameworkInstance(@Nullable String name)
  {
    if (name != null)
    {
      for (FrameworkInstanceDefinition frameworkInstanceDefinition : _frameworkInstanceDefinitions)
      {
        if (name.equals(frameworkInstanceDefinition.getName()))
        {
          return frameworkInstanceDefinition;
        }
      }
    }
    return null;
  }

  public ApplicationSettings getState()
  {
    return this;
  }

  public void loadState(ApplicationSettings state)
  {
    XmlSerializerUtil.copyBean(state, this);
  }

  public ApplicationSettings createCopy()
  {
    ApplicationSettings copy = new ApplicationSettings();
    XmlSerializerUtil.copyBean(this, copy);

    List<FrameworkInstanceDefinition> copiedDefinitions = new ArrayList<FrameworkInstanceDefinition>();
    for (FrameworkInstanceDefinition definition : _frameworkInstanceDefinitions)
    {
      FrameworkInstanceDefinition copiedDefinition = new FrameworkInstanceDefinition();
      XmlSerializerUtil.copyBean(definition, copiedDefinition);
      copiedDefinitions.add(copiedDefinition);
    }
    copy.setFrameworkInstanceDefinitions(copiedDefinitions);


    List<LibraryBundlificationRule> copiedRules = new ArrayList<LibraryBundlificationRule>();
    for (LibraryBundlificationRule libraryBundlificationRule : _libraryBundlificationRules)
    {
      LibraryBundlificationRule copiedRule = new LibraryBundlificationRule();
      XmlSerializerUtil.copyBean(libraryBundlificationRule, copiedRule);
      copiedRules.add(copiedRule);
    }
    copy.setLibraryBundlificationRules(copiedRules);
    return copy;
  }

  public ApplicationSettings()
  {
    _libraryBundlificationRules.add(new LibraryBundlificationRule());
  }

  private List<FrameworkInstanceDefinition> _frameworkInstanceDefinitions = new ArrayList<FrameworkInstanceDefinition>()
      ;
  private List<LibraryBundlificationRule> _libraryBundlificationRules = new ArrayList<LibraryBundlificationRule>();
}
