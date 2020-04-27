package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.flex.model.bc.JpsFlexCompilerOptions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.ex.JpsElementBase;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class JpsFlexCompilerOptionsImpl extends JpsElementBase<JpsFlexCompilerOptionsImpl> implements JpsFlexCompilerOptions {

  private @NotNull final Map<String, String> myOptions = new THashMap<>();
  private @NotNull ResourceFilesMode myResourceFilesMode = ResourceFilesMode.All;
  private @NotNull String myFilesToIncludeInSWC = "";
  private @NotNull String myAdditionalConfigFilePath = "";
  private @NotNull String myAdditionalOptions = "";

  JpsFlexCompilerOptionsImpl() {
  }

  private JpsFlexCompilerOptionsImpl(final JpsFlexCompilerOptionsImpl original) {
    myOptions.putAll(original.myOptions);
    myResourceFilesMode = original.myResourceFilesMode;
    myFilesToIncludeInSWC = original.myFilesToIncludeInSWC;
    myAdditionalConfigFilePath = original.myAdditionalConfigFilePath;
    myAdditionalOptions = original.myAdditionalOptions;
  }

  @Override
  @NotNull
  public JpsFlexCompilerOptionsImpl createCopy() {
    return new JpsFlexCompilerOptionsImpl(this);
  }

  @Override
  public void applyChanges(@NotNull final JpsFlexCompilerOptionsImpl modified) {
    // todo use setters instead, because this method must throw events
    myOptions.clear();
    myOptions.putAll(modified.myOptions);
    myResourceFilesMode = modified.myResourceFilesMode;
    myFilesToIncludeInSWC = modified.myFilesToIncludeInSWC;
    myAdditionalConfigFilePath = modified.myAdditionalConfigFilePath;
    myAdditionalOptions = modified.myAdditionalOptions;
  }

// ------------------------------------

  @Override
  @Nullable
  public String getOption(@NotNull String name) {
    return myOptions.get(name);
  }

  @Override
  @NotNull
  public Map<String, String> getAllOptions() {
    return Collections.unmodifiableMap(myOptions);
  }

  @Override
  @NotNull
  public ResourceFilesMode getResourceFilesMode() {
    return myResourceFilesMode;
  }

  @Override
  public void setResourceFilesMode(@NotNull final ResourceFilesMode resourceFilesMode) {
    myResourceFilesMode = resourceFilesMode;
  }

  @Override
  @NotNull
  public Collection<String> getFilesToIncludeInSWC() {
    if (myFilesToIncludeInSWC.isEmpty()) return Collections.emptyList();
    return StringUtil.split(myFilesToIncludeInSWC, CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
  }

  @Override
  @NotNull
  public String getAdditionalConfigFilePath() {
    return myAdditionalConfigFilePath;
  }

  @Override
  @NotNull
  public String getAdditionalOptions() {
    return myAdditionalOptions;
  }

  @Override
  public void setAdditionalOptions(@NotNull final String additionalOptions) {
    myAdditionalOptions = additionalOptions;
  }

// ------------------------------------

  public State getState(/*final @Nullable ComponentManager componentManager*/) {
    State state = new State();
    /*putOptionsCollapsingPaths(myOptions, state.options, componentManager);*/
    for (Map.Entry<String, String> entry : myOptions.entrySet()) {
      state.options.put(entry.getKey(), entry.getValue());
    }

    state.resourceFilesMode = myResourceFilesMode;
    //state.filesToIncludeInSWC = FlexIdeBuildConfigurationImpl.collapsePaths(componentManager, myFilesToIncludeInSWC);
    state.filesToIncludeInSWC = myFilesToIncludeInSWC;
    state.additionalConfigFilePath = myAdditionalConfigFilePath;
    state.additionalOptions = myAdditionalOptions;
    return state;
  }

  public void loadState(State state) {
    assert myOptions.isEmpty();

    for (Map.Entry<String, String> entry : state.options.entrySet()) {
      myOptions.put(entry.getKey(), entry.getValue());
    }
    // filter out options that are not known in current IDEA version
    /*
    for (Map.Entry<String, String> entry : state.options.entrySet()) {
      if (CompilerOptionInfo.idExists(entry.getKey())) {
        // no need in expanding paths, it is done automatically even if macros is not in the beginning of the string
        myOptions.put(entry.getKey(), entry.getValue());
      }
    }
    */
    myResourceFilesMode = state.resourceFilesMode;
    myFilesToIncludeInSWC = state.filesToIncludeInSWC;
    myAdditionalConfigFilePath = state.additionalConfigFilePath;
    myAdditionalOptions = state.additionalOptions;
  }

  @Tag("compiler-options")
  public static class State {
    @Property(surroundWithTag = false)
    @MapAnnotation
    public Map<String, String> options = new THashMap<>();
    public ResourceFilesMode resourceFilesMode = ResourceFilesMode.All;
    public String filesToIncludeInSWC = "";
    public String additionalConfigFilePath = "";
    public String additionalOptions = "";
  }
}
