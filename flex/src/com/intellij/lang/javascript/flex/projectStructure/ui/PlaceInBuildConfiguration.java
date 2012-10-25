package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.PlaceInProjectStructure;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureElement;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.ui.navigation.Place;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ksafonov
 */
public class PlaceInBuildConfiguration extends PlaceInProjectStructure {

  private final BuildConfigurationProjectStructureElement myStructureElement;
  private final String myTabName;
  @Nullable private final Object myLocationOnTab;

  public PlaceInBuildConfiguration(BuildConfigurationProjectStructureElement structureElement,
                                   @NotNull String tabName,
                                   @Nullable Object locationOnTab) {
    myStructureElement = structureElement;
    myTabName = tabName;
    myLocationOnTab = locationOnTab;
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
    Place place = FlexProjectStructureUtil.createPlace(myStructureElement.getModule(), myStructureElement.getBC(), myTabName);
    if (myLocationOnTab != null) {
      place.putPath(FlexBCConfigurable.LOCATION_ON_TAB, myLocationOnTab);
    }
    return ProjectStructureConfigurable.getInstance(myStructureElement.getModule().getProject()).navigateTo(place, true);
  }
}
