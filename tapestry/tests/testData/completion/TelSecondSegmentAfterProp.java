package com.testapp.components;

import org.apache.tapestry5.annotations.Property;

import java.util.Date;

public class TelSecondSegmentAfterProp {
  public Date getCurrentTime()
  {
      return new Date();
  }

  @Property
  private String someProp = "pp";

  private String justField = "ff";

}