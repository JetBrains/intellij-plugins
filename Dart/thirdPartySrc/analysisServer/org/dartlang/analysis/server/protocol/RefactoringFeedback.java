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
 * An abstract superclass of all refactoring feedbacks.
 *
 * @coverage dart.server.generated.types
 */
@SuppressWarnings("unused")
public class RefactoringFeedback {

  public static final RefactoringFeedback[] EMPTY_ARRAY = new RefactoringFeedback[0];

  public static final List<RefactoringFeedback> EMPTY_LIST = new ArrayList<>();

  /**
   * Constructor for {@link RefactoringFeedback}.
   */
  public RefactoringFeedback() {
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RefactoringFeedback) {
      RefactoringFeedback other = (RefactoringFeedback) obj;
      return
        true;
    }
    return false;
  }

  public static RefactoringFeedback fromJson(JsonObject jsonObject) {
    return new RefactoringFeedback();
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
