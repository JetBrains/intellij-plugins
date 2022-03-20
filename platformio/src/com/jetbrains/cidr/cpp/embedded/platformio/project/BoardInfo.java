package com.jetbrains.cidr.cpp.embedded.platformio.project;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class BoardInfo {

  public static final BoardInfo EMPTY = new BoardInfo(SourceTemplate.GENERIC);
  private final String[] myParameters;
  private final SourceTemplate myTemplate;

  public BoardInfo(SourceTemplate template, @NotNull String ... parameters) {
    myParameters = parameters;
    myTemplate = template;
  }

  @Override
  public String toString() {
    return String.join(" ", myParameters);
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BoardInfo info = (BoardInfo)o;

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
