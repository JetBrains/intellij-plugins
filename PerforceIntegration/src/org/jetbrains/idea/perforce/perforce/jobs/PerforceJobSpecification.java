package org.jetbrains.idea.perforce.perforce.jobs;

import java.util.*;

public class PerforceJobSpecification {
  private final Map<Integer, PerforceJobField> myFields;

  public PerforceJobSpecification(List<PerforceJobField> fields) {
    myFields = new HashMap<>();
    for (PerforceJobField field : fields) {
      myFields.put(field.getCode(), field);
    }
  }

  public Collection<PerforceJobField> getFields() {
    return myFields.values();
  }

  public PerforceJobField getFieldByCode(final int code) {
    return myFields.get(code);
  }
}
