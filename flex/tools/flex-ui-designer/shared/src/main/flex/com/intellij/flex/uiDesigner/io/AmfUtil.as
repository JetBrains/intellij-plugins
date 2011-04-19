package com.intellij.flex.uiDesigner.io {
import flash.utils.IDataInput;

public final class AmfUtil {
  public static function readUInt29(input:IDataInput):int {
    var b:int = input.readByte() & 0xFF;
    if (b < 128) {
      return b;
    }

    var value:int = (b & 0x7F) << 7;
    if ((b = input.readByte() & 0xFF) < 128) {
      return value | b;
    }

    value = (value | (b & 0x7F)) << 7;
    if ((b = input.readByte() & 0xFF) < 128) {
      return (value | b);
    }

    return (((value | (b & 0x7F)) << 8) | (input.readByte() & 0xFF));
  }

  public static function readUtf(input:IDataInput):String {
    return input.readUTFBytes(readUInt29(input));
  }
}
}