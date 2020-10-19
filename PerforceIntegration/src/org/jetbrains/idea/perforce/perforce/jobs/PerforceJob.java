package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerforceJob {
  private final P4Connection myConnection;
  private final ConnectionKey myKey;

  private final Map<Integer, PerforceJobFieldValue> myStandardFields;
  private final Map<Integer, PerforceJobFieldValue> myOtherFields;

  public PerforceJob(List<PerforceJobFieldValue> standardFields, List<PerforceJobFieldValue> otherFields, @NotNull final P4Connection connection,
                     ConnectionKey key) {
    myKey = key;
    myStandardFields = new HashMap<>(standardFields.size(), 1);
    myOtherFields = new HashMap<>(otherFields.size(), 1);
    myConnection = connection;

    for (PerforceJobFieldValue field : standardFields) {
      myStandardFields.put(field.getField().getCode(), field);
    }
    for (PerforceJobFieldValue field : otherFields) {
      myOtherFields.put(field.getField().getCode(), field);
    }
  }

  public PerforceJobFieldValue getValueForStandardField(final StandardJobFields id) {
    return myStandardFields.get(id.getFixedCode());
  }

  public @NlsSafe String getName() {
    return getNameValue().getValue();
  }

  public PerforceJobFieldValue getNameValue() {
    return getValueForStandardField(StandardJobFields.name);
  }

  @NotNull
  public P4Connection getConnection() {
    return myConnection;
  }

  public ConnectionKey getConnectionKey() {
    return myKey;
  }
}
