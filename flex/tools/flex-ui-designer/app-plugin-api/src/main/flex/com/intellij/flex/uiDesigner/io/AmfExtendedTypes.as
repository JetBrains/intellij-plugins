package com.intellij.flex.uiDesigner.io {
public final class AmfExtendedTypes {
  public static const DOCUMENT_FACTORY_REFERENCE:int = 46;
  public static const DOCUMENT_REFERENCE:int = 49;
  public static const STRING_REFERENCE:int = 44;
  public static const CLASS_REFERENCE:int = 43;
  public static const IMAGE:int = 47;
  public static const SWF:int = 48;

  // http://opensource.adobe.com/wiki/display/flexsdk/MXML+Vector+Support, additional info — fixed and object reference
  public static const MXML_ARRAY:int = 50;
  // IDEA-73108
  public static const ARRAY_IF_LENGTH_GREATER_THAN_1:int = 56;
  public static const MXML_VECTOR:int = 51;
  public static const OBJECT_REFERENCE:int = 45;

  // IDEA-73801
  public static const REFERABLE_PRIMITIVE:int = 57;

  public static const XML_LIST:int = 58;
}
}