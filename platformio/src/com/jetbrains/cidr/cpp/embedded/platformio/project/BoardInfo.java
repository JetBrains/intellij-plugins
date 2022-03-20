package com.jetbrains.cidr.cpp.embedded.platformio.project;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import static java.lang.String.join;

public class BoardInfo {

  public static final BoardInfo EMPTY = new BoardInfo(SourceTemplate.GENERIC);
  private final String[] myParameters;
  private final SourceTemplate myTemplate;

  public BoardInfo(final @NotNull SourceTemplate template, final @NotNull String ... parameters) {
    myParameters = parameters;
    myTemplate = template;
  }

  @Override
  public String toString() {
    return join(" ", myParameters);
  }

  @NotNull
  public String[] getParameters() {
    return myParameters;
  }

  @NotNull
  public SourceTemplate getTemplate() {
    return myTemplate;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final BoardInfo info = (BoardInfo)o;

    if (!Arrays.equals(myParameters, info.myParameters)) return false;
    return Objects.equals(myTemplate, info.myTemplate);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(myParameters);
    result = 31 * result + myTemplate.hashCode();
    return result;
  }
}
