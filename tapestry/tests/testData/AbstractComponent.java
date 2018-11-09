package com.testapp.base;
import org.apache.tapestry5.annotations.Parameter;
import java.text.DateFormat;

public class AbstractComponent
{
  public String getText()
  {
    return getInheritedText();
  }
}