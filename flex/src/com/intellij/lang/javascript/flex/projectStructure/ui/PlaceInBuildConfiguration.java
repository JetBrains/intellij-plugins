package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.PlaceInProjectStructure;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureElement;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.navigation.Place;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * User: ksafonov
 */
public class PlaceInBuildConfiguration extends PlaceInProjectStructure {

  private final BuildConfigurationProjectStructureElement myStructureElement;
  private final String myTabName;
  @Nullable private final Pair<String, Object> myLocation;

  public PlaceInBuildConfiguration(BuildConfigurationProjectStructureElement structureElement,
                                   @NotNull String tabName,
                                   @Nullable Pair<String, Object> location) {
    myStructureElement = structureElement;
    myTabName = tabName;
    myLocation = location;
  }

  @NotNull
  @Override
  public ProjectStructureElement getContainingElement() {
    return myStructureElement;
  }

  @Override
  public String getPlacePath() {
    return myTabName;
  }

  @NotNull
  @Override
  public ActionCallback navigate() {
    Place place = FlexProjectStructureUtil.createPlace(myStructureElement.getModule(), myStructureElement.getBc(), myTabName);
    if (myLocation != null) {
      place.putPath(myLocation.first, myLocation.second);
    }
    return ProjectStructureConfigurable.getInstance(myStructureElement.getModule().getProject()).navigateTo(place, true);
  }
}
