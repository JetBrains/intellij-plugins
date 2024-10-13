// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import lombok.Getter;

@Getter
public class FieldsWithGetter {
  private int bar;

  public class InstanceField {
    @Getter
    private int bar;
    @Getter
    private boolean Baz;
    @Getter
    private int fooBar;
    private int fieldWithoutGetter;

  }
  @Getter
  public class AllInstanceFields {
    private int bar;
    private boolean Baz;
    private int fooBar;
    private static int staticFieldWithoutGetter;

  }
  public class StaticField {
    @Getter
    private static int bar;
    private int fieldWithoutGetter;

  }
}