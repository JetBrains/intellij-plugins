package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.AssetCounter;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.javascript.flex.FlexMxmlLanguageAttributeNames;
import com.intellij.psi.xml.XmlElement;
import com.intellij.xml.util.ColorSampleLookupValue;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

final class BaseWriter {
  private static final int EMPTY_CLASS_OR_PROPERTY_NAME = 0;
  
  final int ARRAY;
  final int P_FUD_RANGE_ID;

  private final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter();
  private boolean stringWriterFinished;

  private final int startPosition;

  private final BlockDataOutputStream blockOut;
  private final PrimitiveAmfOutputStream out;

  private final Scope rootScope = new Scope();
  final NullContext nullContext;

  private final AssetCounter assetCounter;

  public BaseWriter(PrimitiveAmfOutputStream out, AssetCounter assetCounter) {
    this.out = out;
    blockOut = out.getBlockOut();
    this.assetCounter = assetCounter;

    stringWriter.startChange();

    ARRAY = getNameReference("array");
    P_FUD_RANGE_ID = getNameReference("$fud_r");

    assert blockOut.getLastMarker() == null;
    startPosition = out.size();

    nullContext = new NullContext(rootScope);
  }

  public AssetCounter getAssetCounter() {
    return assetCounter;
  }

  public PrimitiveAmfOutputStream getOut() {
    return out;
  }

  public BlockDataOutputStream getBlockOut() {
    return blockOut;
  }

  public int getPreallocatedId() {
    return nullContext.id;
  }

  public boolean isIdPreallocated() {
    return nullContext.getId() != -1;
  }

  public StaticObjectContext createStaticContext(@Nullable Context parentContext, int referencePosition) {
    if (parentContext == null || parentContext.getBackSibling() == null) {
      return new StaticObjectContext(referencePosition, out, nullContext.getId(), rootScope);
    }
    else {
      return parentContext.getBackSibling().reinitialize(referencePosition, nullContext.getId());
    }
  }

  public void resetAfterMessage() {
    if (!stringWriterFinished) {
      stringWriter.finishChange();
    }
  }

  public void endObject() {
    out.write(EMPTY_CLASS_OR_PROPERTY_NAME);
  }

  public int allocateAbsoluteStaticObjectId() {
    return rootScope.referenceCounter++;
  }

  public void writeMessageHeader(ProjectComponentReferenceCounter projectComponentReferenceCounter) throws IOException {
    final ByteRange range = blockOut.startRange();

    if (projectComponentReferenceCounter.total.isEmpty()) {
      out.write(Amf3Types.NULL);
    }
    else {
      out.write(projectComponentReferenceCounter.total);
    }

    stringWriter.writeTo(out);
    stringWriterFinished = true;

    out.writeShort(rootScope.referenceCounter);

    blockOut.endRange(range);
    blockOut.insert(startPosition, range);
  }

  public void prepend(ByteRange range) {
    blockOut.insert(startPosition, range);
  }

  public int getNameReference(String classOrPropertyName) {
    return stringWriter.getReference(classOrPropertyName);
  }

  public void classOrPropertyName(String classOrPropertyName) {
    stringWriter.write(classOrPropertyName, out);
  }

  public void vectorHeader(String elementType) {
    out.write(Amf3Types.VECTOR_OBJECT);
    stringWriter.write(elementType, out);
  }

  public void idMxmlProperty(String value) {
    classOrPropertyName(FlexMxmlLanguageAttributeNames.ID);
    out.write(PropertyClassifier.ID);
    out.writeAmfUtf(value, false);
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

  public void objectReference(int reference) {
    out.write(AmfExtendedTypes.OBJECT_REFERENCE);
    out.writeUInt29(reference);
  }

  public void objectReference(Context context) {
    objectReference(context.getOrAllocateId());
  }

  public void objectHeader(int className) {
    out.write(AmfExtendedTypes.OBJECT);
    out.writeUInt29(className);
  }

  public void objectHeader(String className) {
    out.write(AmfExtendedTypes.OBJECT);
    stringWriter.write(className, out);
  }

  public void mxmlObjectHeader(String className) {
    stringWriter.write(className, out);
    out.allocateClearShort();
  }

  public void documentFactoryReference(int reference) {
    out.write(AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE);
    out.writeUInt29(reference);
  }

  public void documentReference(int reference) {
    out.write(AmfExtendedTypes.DOCUMENT_REFERENCE);
    out.writeUInt29(reference);
  }

  public void projectClassReference(int reference) {
    out.write(AmfExtendedTypes.PROJECT_CLASS_REFERENCE);
    out.writeUInt29(reference);

    assetCounter.viewCount++;
  }

  public void classReference(String className) {
    out.write(AmfExtendedTypes.CLASS_REFERENCE);
    classOrPropertyName(className);
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

  public void arrayHeader(int length) {
    out.write(Amf3Types.ARRAY);
    out.writeShort(length);
  }

  public int arrayHeader() {
    out.write(Amf3Types.ARRAY);
    return out.allocateClearShort();
  }

  public BaseWriter newInstance(String className, int argumentsLength, boolean rollbackable) {
    out.write(ExpressionMessageTypes.NEW);
    classOrPropertyName(className);
    out.write((argumentsLength << 1) | (rollbackable ? 1 : 0));
    return this;
  }

  public BaseWriter referableHeader(int reference) {
    out.write(AmfExtendedTypes.REFERABLE);
    out.writeShort(reference + 1);
    return this;
  }

  public int componentFactory(int reference) {
    out.write(AmfExtendedTypes.COMPONENT_FACTORY);
    out.writeUInt29(reference);
    int sizePosition = out.allocateShort();
    out.allocateShort(); // object table size
    return sizePosition;
  }

  public int referableHeader() {
    out.write(AmfExtendedTypes.REFERABLE);
    return out.allocateClearShort();
  }

  public BaseWriter property(int propertyName) {
    out.writeUInt29(propertyName);
    return this;
  }

  public BaseWriter property(String propertyName) {
    classOrPropertyName(propertyName);
    return this;
  }

  public void typeMarker(int typeMarker) {
    out.write(typeMarker);
  }
}
