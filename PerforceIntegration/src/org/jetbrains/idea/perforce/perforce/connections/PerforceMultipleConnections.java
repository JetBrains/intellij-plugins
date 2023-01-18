package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.diff.impl.patch.formove.FilePathComparator;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.ConnectionId;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PerforceMultipleConnections implements PerforceConnectionMapper {
  @Nullable private final String myP4ConfigValue;

  private final TreeMap<VirtualFile, P4ConnectionParameters> myParametersMap;
  private final P4ConnectionParameters myDefaultParameters;
  private final Map<VirtualFile, P4ParametersConnection> myConnectionMap = new HashMap<>();
  private final Object myLock = new Object();
  private final Map<VirtualFile, File> myConfigsMap;

  public PerforceMultipleConnections(@Nullable String p4ConfigValue,
                                     P4ConnectionParameters defaultParameters,
                                     Map<VirtualFile, P4ConnectionParameters> parametersMap,
                                     Map<VirtualFile, File> configsMap) {
    myP4ConfigValue = p4ConfigValue;
    myDefaultParameters = defaultParameters;
    myConfigsMap = configsMap;
    
    myParametersMap = new TreeMap<>(FilePathComparator.getInstance());
    myParametersMap.putAll(parametersMap);
  }

  public P4ConnectionParameters getDefaultParameters() {
    return myDefaultParameters;
  }

  @Nullable
  public String getP4ConfigValue() {
    return myP4ConfigValue;
  }

  @Override
  public P4ParametersConnection getConnection(@NotNull VirtualFile file) {
    // only caches for depot roots (see what added), but lets check first
    synchronized (myLock) {
      final P4ParametersConnection cached = myConnectionMap.get(file);
      if (cached != null) return cached;
    }

    final Map.Entry<VirtualFile, P4ConnectionParameters> entry = myParametersMap.floorEntry(file);
    if (entry == null) {
      return null;
    }
    synchronized (myLock) {
      return createOrGetConnectionByParameters(entry.getKey(), entry.getValue());
    }
  }

  private P4ParametersConnection createOrGetConnectionByParameters(final VirtualFile vf, final P4ConnectionParameters parameters) {
    synchronized (myLock) {
      if (myConnectionMap.containsKey(vf)) {
        return myConnectionMap.get(vf);
      }
      final P4ConnectionParameters withParent = parameters.hasProblems() ? parameters : new P4ConnectionParameters(parameters);
      final P4ParametersConnection connection = new P4ParametersConnection(withParent, new ConnectionId(myP4ConfigValue, vf.getPath()));
      myConnectionMap.put(vf, connection);
      return connection;
    }
  }

  @Override
  public Map<VirtualFile, P4Connection> getAllConnections() {
    synchronized (myLock) {
      for (Map.Entry<VirtualFile, P4ConnectionParameters> entry : myParametersMap.entrySet()) {
        createOrGetConnectionByParameters(entry.getKey(), entry.getValue());
      }
      return Collections.unmodifiableMap(myConnectionMap);
    }
  }

  public TreeMap<VirtualFile, P4ConnectionParameters> getParametersMap() {
    return myParametersMap;
  }

  public boolean hasAnyErrors() {
    if (myDefaultParameters.hasProblems()) return true;
    for (P4ConnectionParameters parameters : myParametersMap.values()) {
      if (parameters.hasProblems()) return true;
    }
    return false;
  }

  public Map<VirtualFile, File> getConfigsMap() {
    return myConfigsMap;
  }
}
