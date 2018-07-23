package com.testapp.components;

import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.ComponentClassResolver;


@SupportsInformalParameters
public class Count3
{
  @Parameter(defaultPrefix = "literal")
  private String pageTitle = "Default";

  @Contribute( ComponentClassResolver.class )
  public static void setupLibraryMapping(Configuration<LibraryMapping> configuration)
  {
    configuration.add(new LibraryMapping("wf", "org.apache.tapestry5.upload"));
  }
}