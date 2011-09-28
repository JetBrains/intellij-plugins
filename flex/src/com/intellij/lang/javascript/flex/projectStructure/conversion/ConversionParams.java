package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * User: ksafonov
 */
public class ConversionParams {
  public String projectSdkName;
  public String projectSdkType;
  private final Map<String, String> myIdeaSkds = new HashMap<String, String>();
  private final Map<String, String> myExistingFlexIdeSkds = new HashMap<String, String>();
  private Map<String, String> myFlexIdeSdksToCreate = new HashMap<String, String>();

  public void addIdeaSdk(@NotNull String name, @NotNull String homePath) {
    myIdeaSkds.put(name, homePath);
  }

  @Nullable
  public String getIdeaSdkHomePath(@NotNull String name) {
    return myIdeaSkds.get(name);
  }

  public void addExistingFlexIdeSdk(@NotNull String homePath, @NotNull String id) {
    myExistingFlexIdeSkds.put(homePath, id);
  }

  @Nullable
  public String getExistingFlexIdeSdkId(@NotNull String homePath) {
    return myExistingFlexIdeSkds.get(homePath);
  }

  @NotNull
  public String requireFlexIdeSdk(@NotNull String homePath) {
    String id = UUID.randomUUID().toString();
    myFlexIdeSdksToCreate.put(homePath, id);
    return id;
  }

  public Map<String, String> getFlexIdeSdksToCreate() {
    return myFlexIdeSdksToCreate;
  }
}
