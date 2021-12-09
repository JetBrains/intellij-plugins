/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.perforce;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class BranchSpec {
  private final Map<String, List<String>> myForm;
  private static final ArrayList<String> EMPTY = new ArrayList<>();

  public BranchSpec(Map<String, List<String>> form) {
    myForm = form;
  }

  public String getDescription() {
    return getField(PerforceRunner.DESCRIPTION);
  }

  private String getField(final String fieldName) {
    final List<String> values = getListField(fieldName);
    if (values.isEmpty()) {
      return "";
    }
    else {
      return values.get(0);
    }
  }

  private List<String> getListField(final String fieldName) {
    return myForm.getOrDefault(fieldName, EMPTY);
  }

  public String getOwner() {
    return getField(PerforceRunner.OWNER);
  }

  public List<String> getViews() {
    return getListField(PerforceRunner.VIEW);
  }
}
