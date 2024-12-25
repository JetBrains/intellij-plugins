/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.resolve;

import com.intellij.protobuf.lang.psi.PbSymbol;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.intellij.protobuf.lang.psi.util.PbPsiUtil.isPackageElement;

public final class PbResolveResult implements ResolveResult {

  public enum ResultType {
    PACKAGE,
    ELEMENT
  }

  private final PbSymbol symbol;
  private final QualifiedName name;
  private final ResultType resultType;
  private final boolean validResult;

  PbResolveResult(@NotNull PbSymbol element, QualifiedName name, ResultType resultType) {
    this(element, name, resultType, true);
  }

  PbResolveResult(
      @NotNull PbSymbol symbol, QualifiedName name, ResultType resultType, boolean validResult) {
    this.symbol = symbol;
    this.name = name;
    this.resultType = resultType;
    this.validResult = validResult;
  }

  public static PbResolveResult create(PbSymbol symbol) {
    ResultType type = isPackageElement(symbol) ? ResultType.PACKAGE : ResultType.ELEMENT;
    return new PbResolveResult(symbol, symbol.getQualifiedName(), type);
  }

  public ResultType getResultType() {
    return resultType;
  }

  @Override
  public @Nullable PbSymbol getElement() {
    return symbol;
  }

  public @Nullable QualifiedName getName() {
    return name;
  }

  @Override
  public boolean isValidResult() {
    return validResult;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    PbResolveResult other = (PbResolveResult) obj;

    return Objects.equals(this.symbol, other.symbol)
        && Objects.equals(this.name, other.name)
        && Objects.equals(this.resultType, other.resultType)
        && Objects.equals(this.validResult, other.validResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.symbol, this.name, this.resultType, this.validResult);
  }

  @Override
  public String toString() {
    return String.format(
        "%s: element=%s; name=%s; resultType=%s; validResult=%s",
        this.getClass().getName(), symbol, name, resultType, validResult);
  }
}
