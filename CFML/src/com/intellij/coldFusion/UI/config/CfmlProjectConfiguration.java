// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.config;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "CfmlProjectConfiguration", storages = @Storage("cfml.xml"))
public class CfmlProjectConfiguration implements PersistentStateComponent<CfmlProjectConfiguration.State> {
  private State myState = new State();

  public static CfmlProjectConfiguration getInstance(Project project) {
    return project.getService(CfmlProjectConfiguration.class);
  }

  @Override
  @Nullable
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(@NotNull State state) {
    myState = state;
    state.migrateIfNeeded();
  }

  /*
  public CfmlMappingsConfig getMappings() {
    if (myState.getMappings().serverMappings.size() == 0) {
      return getDefaultMappings(myProject);
    }
    return myState.getMappings();
  }

  public void setMappings(CfmlMappingsConfig mappings) {
    myModifiedByUser = true;
    if (mappings == null) {
      mappings = new CfmlMappingsConfig();
    }
    myState.setMappings(mappings);
  }

  public void setLanguageLevel(String languageLevel) {
    myModifiedByUser = true;
    myState.setLanguageLevel(languageLevel);
  }

  public String getLanguageLevel() {
    return myState.getLanguageLevel();
  }
  */

  public static class State {
    private CfmlMappingsConfig myMapps = new CfmlMappingsConfig();

    private String myLanguageLevel = CfmlLanguage.CF10;

    public State() {
    }

    public State(CfmlMappingsConfig mappings) {
      myMapps = mappings;
    }


    @Property(surroundWithTag = false)
    public CfmlMappingsConfig getMapps() {
      return myMapps;
    }

    public void setMapps(@Nullable CfmlMappingsConfig mappings) {
      myMapps = mappings;
    }

    private Element myMappingsElement;

    @Tag("mappings")
    public Element getMappingsElement() {
      return myMappingsElement;
    }

    public void setMappingsElement(@Nullable Element element) {
      myMappingsElement = element;
    }

    private Element mySlashMappingElement;

    @Tag("mappings2")
    public Element getSlashMappingElement() {
      return mySlashMappingElement;
    }

    public void setSlashMappingElement(@Nullable Element element) {
      mySlashMappingElement = element;
    }


    @Tag("language_level")
    public @NlsSafe String getLanguageLevel() {
      return myLanguageLevel;
    }

    public void setLanguageLevel(String languageLevel) {
      myLanguageLevel = languageLevel;
    }


    public void updateAndAddIfNeeded(String logicalPath, String directory) {
      if (logicalPath == null || directory == null) {
        return;
      }
      myMapps.putToServerMappings(StringUtil.startsWithChar(directory, '/') || StringUtil.startsWithChar(directory, '\\')
                                  ? directory
                                  : "/" + directory, logicalPath);
    }

    public void migrateIfNeeded() {
      if (mySlashMappingElement == null && myMappingsElement == null) {
        return;
      }
      Element firstChild = mySlashMappingElement != null ? mySlashMappingElement.getChild("CfmlMappingsConfig") : null;
      if (firstChild == null) {
        firstChild = myMappingsElement.getChild("CfmlMappingsConfig");
      }
      if (firstChild != null) {
        for (Element e : JDOMUtil.getChildren(firstChild.getChild("server_mappings"), "mapping")) {
          updateAndAddIfNeeded(e.getAttributeValue("logical_path"), e.getAttributeValue("directory"));
        }
      }
      else {
        for (Element e : myMappingsElement.getChildren("mapping")) {
          myMapps.putToServerMappings(e.getAttributeValue("logical_path"), e.getAttributeValue("directory"));
        }
      }
      setMappingsElement(null);
      setSlashMappingElement(null);
    }


    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final State state = (State)o;

      if (myLanguageLevel != null ? !myLanguageLevel.equals(state.myLanguageLevel) : state.myLanguageLevel != null) return false;

      CfmlMappingsConfig m1 = myMapps != null ? myMapps : new CfmlMappingsConfig();
      CfmlMappingsConfig m2 = state.myMapps != null ? state.myMapps : new CfmlMappingsConfig();
      return Comparing.equal(m1, m2);
    }

    @Override
    public int hashCode() {
      int result = myMapps != null ? myMapps.hashCode() : 0;
      result = 31 * result + (myLanguageLevel != null ? myLanguageLevel.hashCode() : 0);
      return result;
    }
  }
}
