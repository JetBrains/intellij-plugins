package org.osmorc.inspection;

import org.jetbrains.annotations.NotNull;

/**
 * Result of a class/package availability check.
 *
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class AvailabilityCheckResult {
  private final ResultType myResult;
  private final String myDescription;

  public AvailabilityCheckResult() {
    this(ResultType.Ok, "");
  }

  public AvailabilityCheckResult(@NotNull ResultType result, @NotNull String description) {
    myResult = result;
    myDescription = description;
  }

  public boolean isOk() {
    return myResult == ResultType.Ok;
  }

  @NotNull
  public String getDescription() {
    return myDescription;
  }

  @NotNull
  public ResultType getResult() {
    return myResult;
  }

  public enum ResultType {
    SymbolIsNotImported,
    SymbolIsNotExported,
    Ok
  }
}
