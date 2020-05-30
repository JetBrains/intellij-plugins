/*
 * Copyright (c) 2019, the Dart project authors. Please see the AUTHORS file
 * for details. All rights reserved. Use of this source code is governed by a
 * BSD-style license that can be found in the LICENSE file.
 *
 * This file has been automatically generated. Please do not edit it manually.
 * To regenerate the file, use the script "pkg/analysis_server/tool/spec/generate_files".
 */
package org.dartlang.analysis.server.protocol;

import java.util.List;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import java.util.ArrayList;

/**
 * An abstract superclass of all refactoring options.
 *
 * @coverage dart.server.generated.types
 */
@SuppressWarnings("unused")
public class RefactoringOptions {

  public static final RefactoringOptions[] EMPTY_ARRAY = new RefactoringOptions[0];

  public static final List<RefactoringOptions> EMPTY_LIST = new ArrayList<>();

  /**
   * Constructor for {@link RefactoringOptions}.
   */
  public RefactoringOptions() {
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RefactoringOptions) {
      RefactoringOptions other = (RefactoringOptions) obj;
      return
        true;
    }
    return false;
  }

  public static RefactoringOptions fromJson(JsonObject jsonObject) {
    return new RefactoringOptions();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    return builder.toHashCode();
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    return jsonObject;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    builder.append("]");
    return builder.toString();
  }

}
