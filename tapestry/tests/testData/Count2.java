package com.testapp.components;

import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.annotations.SupportsInformalParameters;

@SupportsInformalParameters
public class Count2
{
  @Parameter(defaultPrefix = "literal")
  private String pageTitle = "Default";
}