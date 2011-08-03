package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.css.CssPropertyType;
import com.intellij.flex.uiDesigner.InvalidPropertyException;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.javascript.flex.FlexMxmlLanguageAttributeNames;
import com.intellij.psi.xml.XmlElement;
import com.intellij.xml.util.ColorSampleLookupValue;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class BaseWriter {
  int ARRAY = -1;
  int P_FUD_POSITION = -1;

  private final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter();

  private int startPosition;

  private final BlockDataOutputStream blockOut;
  private final PrimitiveAmfOutputStream out;

  private final Scope rootScope = new Scope();
  private int preallocatedId = -1;

  public BaseWriter(PrimitiveAmfOutputStream out) {
    this.out = out;
    blockOut = out.getBlockOut();
  }

  public Scope getRootScope() {
    return rootScope;
  }

  public PrimitiveAmfOutputStream getOut() {
    return out;
  }

  public BlockDataOutputStream getBlockOut() {
    return blockOut;
  }

  public int getPreallocatedId() {
    return preallocatedId;
  }

  // 4
  private int preallocateIdIfNeed() {
    if (!isIdPreallocated()) {
      preallocatedId = rootScope.referenceCounter++;
    }

    return preallocatedId;
  }

  public boolean isIdPreallocated() {
    return preallocatedId != -1;
  }

  public void resetPreallocatedId() {
    preallocatedId = -1;
  }

  public StaticObjectContext createStaticContext(Context parentContext, int referencePosition) {
    if (parentContext == null || parentContext.getBackSibling() == null) {
      return new StaticObjectContext(referencePosition, out, preallocatedId, rootScope);
    }
    else {
      return parentContext.getBackSibling().reinitialize(referencePosition, preallocatedId);
    }
  }

  public DynamicObjectContext createDynamicObjectStateContext() {
    return new DynamicObjectContext(preallocatedId, rootScope);
  }

  public void reset() {
    resetAfterMessage();

    ARRAY = -1;
    P_FUD_POSITION = -1;
  }

  private void initNames() {
    ARRAY = getNameReference("array");
    P_FUD_POSITION = getNameReference("$fud_position");
  }

  public void resetAfterMessage() {
    rootScope.referenceCounter = 0;
    stringWriter.finishChange();
  }

  public void addMarker(ByteRange dataRange) {
    blockOut.addMarker(new ByteRangeMarker(blockOut.size(), dataRange));
  }

  public void beginMessage() {
    stringWriter.startChange();
    if (ARRAY == -1) {
      initNames();
    }

    assert blockOut.getNextMarkerIndex() == 0;
    startPosition = out.size();
  }

  @SuppressWarnings("MethodMayBeStatic")
  public int getObjectId(Context context) {
    if (context.getId() == -1) {
      context.setId(context.getParentScope().referenceCounter++);
      context.referenceInitialized();
    }

    return context.getId();
  }

  public int getObjectOrFactoryId(@Nullable Context context) {
    return context == null ? preallocateIdIfNeed() : getObjectId(context);
  }

  public void endMessage() throws IOException {
    int stringTableSize = stringWriter.size();
    blockOut.beginWritePrepended(stringTableSize + IOUtil.sizeOf(rootScope.referenceCounter), startPosition);
    blockOut.writePrepended(stringWriter.getCounter(), stringWriter.getByteArrayOut());
    blockOut.writePrepended(rootScope.referenceCounter);
    blockOut.endWritePrepended(startPosition);
  }

  public int getNameReference(String classOrPropertyName) {
    return stringWriter.getReference(classOrPropertyName);
  }

  public void write(String classOrPropertyName) {
    stringWriter.write(classOrPropertyName, out);
  }

  public void writeProperty(int propertyNameReference, String value) {
    out.writeUInt29(propertyNameReference);
    out.write(PropertyClassifier.PROPERTY);
    out.write(Amf3Types.STRING);
    out.writeAmfUtf(value, false);
  }

  public void writeProperty(String propertyName, int value) {
    stringWriter.writeNullable(propertyName, out);
    out.write(PropertyClassifier.PROPERTY);
    out.writeAmfInt(value);
  }

  public void writeIdProperty(String value) {
    write(FlexMxmlLanguageAttributeNames.ID);
    out.write(PropertyClassifier.ID);
    out.writeAmfUtf(value, false);
  }

  public void writeStringReference(String propertyName, String reference) {
    writeStringReference(getNameReference(propertyName), getNameReference(reference));
  }

  public void writeStringReference(int propertyName, String reference) {
    writeStringReference(propertyName, getNameReference(reference));
  }

  public void writeStringReference(int propertyName, int reference) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.PROPERTY);
    out.write(AmfExtendedTypes.STRING_REFERENCE);
    out.writeUInt29(reference);
  }

  public void writeStringReference(String reference) {
    out.write(AmfExtendedTypes.STRING_REFERENCE);
    stringWriter.write(reference, out);
  }

  public void writeString(CharSequence value) {
    out.write(Amf3Types.STRING);
    out.writeAmfUtf(value, false);
  }

  public void writeObjectReference(String propertyName, int reference) {
    writeObjectReference(getNameReference(propertyName), reference);
  }

  public void writeObjectReference(int reference) {
    out.write(AmfExtendedTypes.OBJECT_REFERENCE);
    out.writeUInt29(reference);
  }

  public void writeObjectReference(int propertyName, int reference) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.PROPERTY);
    writeObjectReference(reference);
  }

  public void writeObjectReference(int propertyName, Context context) {
    writeObjectReference(propertyName, getObjectId(context));
  }

  public void writeFixedArrayHeader(int propertyName, int size) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.FIXED_ARRAY);
    out.write(size);
  }

  public void writeObjectHeader(int propertyName, int className) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.PROPERTY);
    out.write(Amf3Types.OBJECT);
    writeObjectHeader(className);
  }

  public void writeObjectHeader(String className) {
    writeObjectHeader(getNameReference(className));
  }

  public void writeObjectHeader(String className, int reference) {
    write(className);
    out.writeShort(reference + 1);
  }

  public void writeObjectHeader(int className) {
    out.writeUInt29(className);
    out.getByteOut().allocate(2);
  }

  public void writeDocumentFactoryReference(int reference) {
    out.write(AmfExtendedTypes.DOCUMENT_FACTORY_REFERENCE);
    out.writeUInt29(reference);
  }

  public void writeClass(String className) {
    out.write(AmfExtendedTypes.CLASS_REFERENCE);
    write(className);
  }

  public void writeColor(XmlElement element, String value, boolean isPrimitiveStyle) throws InvalidPropertyException {
    out.write(AmfExtendedTypes.COLOR_STYLE_MARKER);
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
      final String hexCodeForColorName = ColorSampleLookupValue.getHexCodeForColorName(colorName);
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
          throw new InvalidPropertyException(element, "error.invalid.color.name", colorName);
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

  public void writeDeferredInstanceFromArray() {
    writeConstructorHeader("com.intellij.flex.uiDesigner.flex.DeferredInstanceFromArray");
    out.write(Amf3Types.ARRAY);
  }

  public void writeConstructorHeader(String className) {
    out.write(Amf3Types.OBJECT);
    writeObjectHeader(className);
    write("1");
  }

  public void writeConstructorHeader(String className, int reference) {
    out.write(Amf3Types.OBJECT);
    writeObjectHeader(className, reference);
    write("1");
  }

  public void writeConstructorHeader(int propertyName, String className, int constructorArgType) {
    out.writeUInt29(propertyName);
    out.write(PropertyClassifier.PROPERTY);
    writeConstructorHeader(className);
    out.write(constructorArgType);
  }
}
