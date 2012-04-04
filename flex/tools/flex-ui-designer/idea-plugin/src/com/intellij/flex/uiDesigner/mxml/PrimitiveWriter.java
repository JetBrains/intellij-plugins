package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.javascript.flex.css.FlexCssPropertyDescriptor;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.psi.xml.XmlElement;
import com.intellij.xml.util.ColorSampleLookupValue;
import org.jetbrains.annotations.Nullable;

public class PrimitiveWriter {
  protected final StringRegistry.StringWriter stringWriter;

  protected final PrimitiveAmfOutputStream out;

  public PrimitiveWriter(PrimitiveAmfOutputStream out, StringRegistry.StringWriter stringWriter) {
    this.out = out;

    this.stringWriter = stringWriter;
    stringWriter.startChange();
  }

  public void stringReference(int reference) {
    out.write(AmfExtendedTypes.STRING_REFERENCE);
    out.writeUInt29(reference);
  }

  public void stringReference(String reference) {
    out.write(AmfExtendedTypes.STRING_REFERENCE);
    stringWriter.write(reference, out);
  }

  public void string(CharSequence value) {
    out.write(Amf3Types.STRING);
    out.writeAmfUtf(value, false);
  }

  public void color(XmlElement element, String value, boolean isPrimitiveStyle) throws InvalidPropertyException {
    out.write(AmfExtendedTypes.COLOR_STYLE);
    if (value.charAt(0) == '#') {
      if (isPrimitiveStyle) {
        out.write(CssPropertyType.COLOR_INT);
      }
      value = value.substring(1);
    }
    else if (value.charAt(0) == '0' && value.length() > 2 && value.charAt(1) == 'x') {
      if (isPrimitiveStyle) {
        out.write(CssPropertyType.COLOR_INT);
      }
      value = value.substring(2);
    }
    else {
      final String colorName = value.toLowerCase();
      String hexCodeForColorName = ColorSampleLookupValue.getHexCodeForColorName(colorName);
      if (hexCodeForColorName == null) {
        try {
          long v = Long.parseLong(colorName);
          if (isPrimitiveStyle) {
            out.write(CssPropertyType.COLOR_INT);
          }
          out.writeAmfUInt(v);
          return;
        }
        catch (NumberFormatException e) {
          // Why themeColor for theme halo valid for any other theme? But it is compiler behavior, see
          // example http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/spark/components/Form.html
          // or our test EmbedSwfAndImageFromCss
          if (colorName.equalsIgnoreCase("halogreen")) {
            hexCodeForColorName = "#80FF4D";
          }
          else if (colorName.equalsIgnoreCase("haloblue")) {
            hexCodeForColorName = "#009DFF";
          }
          else if (colorName.equalsIgnoreCase("haloorange")) {
            hexCodeForColorName = "#FFB600";
          }
          else if (colorName.equalsIgnoreCase("halosilver")) {
            hexCodeForColorName = "#AECAD9";
          }
          else {
            throw new InvalidPropertyException(element, "invalid.color.name", colorName);
          }
        }
      }

      if (isPrimitiveStyle) {
        out.write(CssPropertyType.COLOR_STRING);
        stringWriter.writeNullable(colorName, out);
      }

      value = hexCodeForColorName.substring(1);
    }

    if (value.length() > 6) {
      out.writeAmfUInt(Long.parseLong(value, 16));
    }
    else {
      out.writeAmfUInt(Integer.parseInt(value, 16));
    }
  }

  public boolean writeIfApplicable(XmlElementValueProvider valueProvider,
                                   PrimitiveAmfOutputStream out,
                                   AnnotationBackedDescriptor descriptor) throws InvalidPropertyException {
    return writeIfApplicable(valueProvider, descriptor.getType(), out, descriptor, false, false);
  }

  public boolean writeIfApplicable(XmlElementValueProvider valueProvider,
                                   String type,
                                   PrimitiveAmfOutputStream out,
                                   @Nullable AnnotationBackedDescriptor descriptor,
                                   boolean isStyle,
                                   boolean emptyNumericAs0) throws InvalidPropertyException {
    if (type.equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
      writeString(valueProvider, descriptor);
    }
    else if (type.equals(JSCommonTypeNames.NUMBER_CLASS_NAME) ||
             type.equals(JSCommonTypeNames.INT_TYPE_NAME) ||
             type.equals(JSCommonTypeNames.UINT_TYPE_NAME)) {
      final String trimmed = valueProvider.getTrimmed();
      if (trimmed.isEmpty()) {
        if (emptyNumericAs0) {
          out.writeAmfInt(0);
          return true;
        }
        else {
          throw new InvalidPropertyException(valueProvider.getElement(), "invalid.numeric.value");
        }
      }

      if (type.equals(JSCommonTypeNames.NUMBER_CLASS_NAME)) {
        out.writeAmfDouble(trimmed);
      }
      else if (descriptor != null && FlexCssPropertyDescriptor.COLOR_FORMAT.equals(descriptor.getFormat())) {
        color(valueProvider.getElement(), trimmed, isStyle);
      }
      else {
        out.writeAmfInt(trimmed);
      }
    }
    else if (type.equals(JSCommonTypeNames.BOOLEAN_CLASS_NAME)) {
      out.writeAmfBoolean(valueProvider.getTrimmed());
    }
    else {
      return false;
    }

    return true;
  }

  void writeString(XmlElementValueProvider valueProvider, @Nullable AnnotationBackedDescriptor descriptor) {
    if (descriptor != null && descriptor.isEnumerated()) {
      stringReference(valueProvider.getTrimmed());
    }
    else {
      CharSequence v = writeIfEmpty(valueProvider);
      if (v != null) {
        string(v);
      }
    }
  }

  @Nullable
  CharSequence writeIfEmpty(XmlElementValueProvider valueProvider) {
    CharSequence v = valueProvider.getSubstituted();
    if (v == XmlElementValueProvider.EMPTY) {
      stringReference(XmlElementValueProvider.EMPTY);
      return null;
    }
    else {
      return v;
    }
  }
}
