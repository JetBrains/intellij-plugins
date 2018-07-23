package com.testapp.components;
import org.apache.tapestry5.annotations.Parameter;
import java.text.DateFormat;

public class TestComp<T>
{
  @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.LITERAL)
  private DateFormat format;

  @Parameter
  private T it;

  @Parameter
  private Object[] it2;
}