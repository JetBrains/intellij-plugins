// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package test.cucumber.types;

import io.cucumber.java.ParameterType;

import java.util.UUID;

public class ParameterTypes {

  @ParameterType(value = ".*")
  public UUID uuid(String value) {
    return UUID.fromString(value);
  }
}
