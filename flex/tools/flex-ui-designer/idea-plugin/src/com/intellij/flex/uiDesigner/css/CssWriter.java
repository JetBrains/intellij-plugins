package com.intellij.flex.uiDesigner.css;

import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.mxml.AmfExtendedTypes;
import com.intellij.flex.uiDesigner.mxml.AsCommonTypeNames;
import com.intellij.injected.editor.DocumentWindow;
import com.intellij.javascript.flex.css.FlexCssElementDescriptorProvider;
import com.intellij.javascript.flex.css.FlexCssPropertyDescriptor;
import com.intellij.javascript.flex.css.FlexStyleIndexInfo;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.CssTermTypes;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlToken;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class CssWriter {
  private static final Logger LOG = Logger.getInstance(CssWriter.class.getName());

  protected PrimitiveAmfOutputStream propertyOut;
  private final CustomVectorWriter rulesetVectorWriter = new CustomVectorWriter();
  private final CustomVectorWriter declarationVectorWriter = new CustomVectorWriter();

  protected final StringRegistry.StringWriter stringWriter;

  private ProblemsHolder problemsHolder;

  private RequiredAssetsInfo requiredAssetsInfo;

  public CssWriter(StringRegistry.StringWriter stringWriter, ProblemsHolder problemsHolder) {
    this.stringWriter = stringWriter;
    this.problemsHolder = problemsHolder;
  }

  public RequiredAssetsInfo getRequiredAssetsInfo() {
    return requiredAssetsInfo;
  }

  public byte[] write(VirtualFile file, Module module) {
    Document document = FileDocumentManager.getInstance().getDocument(file);
    assert document != null;
    CssFile cssFile = (CssFile)PsiDocumentManager.getInstance(module.getProject()).getPsiFile(document);
    problemsHolder.setCurrentFile(file);
    try {
      return write(cssFile, document, module);
    }
    finally {
      problemsHolder.setCurrentFile(null);
    }
  }

  public byte[] write(CssFile cssFile, Module module) {
    problemsHolder.setCurrentFile(cssFile.getVirtualFile());
    try {
      return write(cssFile, PsiDocumentManager.getInstance(module.getProject()).getDocument(cssFile), module);
    }
    finally {
      problemsHolder.setCurrentFile(null);
    }
  }

  private byte[] write(CssFile cssFile, Document document, Module module) {
    requiredAssetsInfo = null;

    rulesetVectorWriter.prepareIteration();

    CssStylesheet stylesheet = cssFile.getStylesheet();
    CssRuleset[] rulesets = stylesheet.getRulesets();

    final DocumentWindow documentWindow = document instanceof DocumentWindow ? (DocumentWindow)document : null;
    for (CssRuleset ruleset : rulesets) {
      PrimitiveAmfOutputStream rulesetOut = rulesetVectorWriter.getOutputForIteration();

      int textOffset = ruleset.getTextOffset();
      if (documentWindow != null) {
        rulesetOut.writeUInt29(documentWindow.injectedToHostLine(document.getLineNumber(textOffset)) + 1);
        textOffset = documentWindow.injectedToHost(textOffset);
      }
      else {
        rulesetOut.writeUInt29(document.getLineNumber(textOffset) + 1);
      }
      rulesetOut.writeUInt29(textOffset);

      writeSelectors(ruleset, rulesetOut, module);

      declarationVectorWriter.prepareIteration();
      for (CssDeclaration declaration : ruleset.getBlock().getDeclarations()) {
        CssPropertyDescriptor propertyDescriptor = declaration.getDescriptor();
        CssTermList value = declaration.getValue();

        propertyOut = declarationVectorWriter.getOutputForIteration();
        try {
          stringWriter.write(declaration.getPropertyName(), propertyOut);

          textOffset = declaration.getTextOffset();
          propertyOut.writeUInt29(documentWindow == null ? textOffset : documentWindow.injectedToHost(textOffset));

          if (propertyDescriptor == null || !(propertyDescriptor instanceof FlexCssPropertyDescriptor)) {
            writeUndefinedPropertyValue(value);
          }
          else {
            writePropertyValue(value, ((FlexCssPropertyDescriptor)propertyDescriptor).getStyleInfo());
          }
          continue;
        }
        catch (RuntimeException e) {
          problemsHolder.add(declaration, e, declaration.getPropertyName());
        }
        catch (Throwable e) {
          problemsHolder.add(e);
        }

        declarationVectorWriter.rollbackLastIteration();
      }

      if (!declarationVectorWriter.isEmpty()) {
        declarationVectorWriter.writeTo(rulesetOut);
      }
      else {
        rulesetVectorWriter.rollbackLastIteration();
      }
    }

    PrimitiveAmfOutputStream outputForCustomData = rulesetVectorWriter.getOutputForCustomData();
    CssNamespace[] namespaces = stylesheet.getNamespaces();
    outputForCustomData.write(namespaces.length);
    if (namespaces.length > 0) {
      for (CssNamespace cssNamespace : namespaces) {
        stringWriter.writeNullable(cssNamespace.getPrefix(), outputForCustomData);
        stringWriter.writeNullable(cssNamespace.getUri(), outputForCustomData);
      }
    }

    return IOUtil.getBytes(rulesetVectorWriter);
  }

  private void writeSelectors(CssRuleset ruleset, PrimitiveAmfOutputStream out, Module module) {
    CssSelector[] selectors = ruleset.getSelectorList().getSelectors();
    out.write(selectors.length);

    for (CssSelector selector : selectors) {
      PsiElement[] simpleSelectors = selector.getElements();
      out.write(simpleSelectors.length);

      for (int i = 0, simpleSelectorsLength = simpleSelectors.length; i < simpleSelectorsLength; i++) {
        CssSimpleSelector simpleSelector = (CssSimpleSelector)simpleSelectors[i];

        // subject
        if (simpleSelector.isUniversalSelector()) {
          out.write(0);
        }
        else {
          XmlElementDescriptor typeSelectorDescriptor = FlexCssElementDescriptorProvider.getTypeSelectorDescriptor(simpleSelector, 
          module);
          final String subject = simpleSelector.getElementName();
          assert subject != null;
          if (typeSelectorDescriptor == null) {
            if (!subject.equals("global")) {
              LOG.warn("unqualified type selector " + simpleSelector.getText());
            }
            stringWriter.writeNullable(subject, out);
            out.write(0);
          }
          else {
            stringWriter.writeNullable(typeSelectorDescriptor.getQualifiedName(), out);
            stringWriter.writeNullable(subject, out);
            stringWriter.writeNullable(simpleSelector.getNamespaceName(), out);
          }
        }

        // conditions
        CssSelectorSuffix[] selectorSuffixes = simpleSelector.getSelectorSuffixes();
        out.write(selectorSuffixes.length);
        for (CssSelectorSuffix selectorSuffix : selectorSuffixes) {
          if (selectorSuffix instanceof CssClass) {
            out.write(FlexCssConditionKind.CLASS);
          }
          else if (selectorSuffix instanceof CssIdSelector) {
            out.write(FlexCssConditionKind.ID);
          }
          else if (selectorSuffix instanceof CssPseudoClass) {
            out.write(FlexCssConditionKind.PSEUDO);
          }
          else {
            LOG.error("unknown selector suffix " + selectorSuffix.getText());
          }

          stringWriter.writeNullable(selectorSuffix.getName(), out);
        }
      }
    }
  }

  private void writePropertyValue(CssTermList value, FlexStyleIndexInfo info) throws InvalidPropertyException {
    final String type = info.getType();
    //noinspection ConstantConditions
    final ASTNode node = value.getFirstChild().getFirstChild().getNode();
    assert type != null;
    if (type.equals(JSCommonTypeNames.UINT_TYPE_NAME)) {
      final String format = info.getFormat();
      assert format != null;
      if (format.equals(FlexCssPropertyDescriptor.COLOR_FORMAT)) {
        // IDEA-59632
        if (value.getText().equals("0")) {
          propertyOut.write(CssPropertyType.NUMBER);
          propertyOut.writeAmfInt(0);
        }
        else {
          propertyOut.write(CssPropertyType.COLOR_INT);
          writeColor(value);
        }
      }
      else {
        writeNumberValue(node, true);
      }
    }
    else if (type.equals(JSCommonTypeNames.INT_TYPE_NAME)) {
      writeNumberValue(node, true);
    }
    else if (type.equals(JSCommonTypeNames.NUMBER_CLASS_NAME)) {
      writeNumberValue(node, false);
    }
    else if (type.equals(JSCommonTypeNames.STRING_CLASS_NAME)) {
      // special case: ClassReference(null);
      if (node.getElementType() == CssElementTypes.CSS_FUNCTION) {
        propertyOut.write(Amf3Types.NULL);
      }
      else {
        writeStringValue(node, info);
      }
    }
    else if (type.equals(JSCommonTypeNames.BOOLEAN_CLASS_NAME)) {
      writeBooleanValue(node);
    }
    else if (type.equals(AsCommonTypeNames.CLASS)) {
      // Class, brokenImageSkin: Embed(source="Assets.swf",symbol="__brokenImage"); or brokenImageBorderSkin: ClassReference("mx.skins.halo.BrokenImageBorderSkin");
      // or ClassReference(null);
      //noinspection ConstantConditions
      writeFunctionValue((CssFunction)value.getFirstChild().getFirstChild(), info);
    }
    else if (type.equals(JSCommonTypeNames.OBJECT_CLASS_NAME) || type.equals(JSCommonTypeNames.ANY_TYPE)) {
      writeUndefinedPropertyValue(value);
    }
    else if (type.equals(JSCommonTypeNames.ARRAY_CLASS_NAME)) {
      final String arrayType = info.getArrayType();
      boolean isInt;
      if (arrayType == null) {
        writeUndefinedPropertyValue(value);
      }
      else if ((isInt = (arrayType.equals(JSCommonTypeNames.INT_TYPE_NAME) || arrayType.equals(JSCommonTypeNames.UINT_TYPE_NAME))) ||
               arrayType.equals(JSCommonTypeNames.NUMBER_CLASS_NAME)) {
        propertyOut.write(CssPropertyType.ARRAY_OF_NUMBER);
        CssTerm[] terms = PsiTreeUtil.getChildrenOfType(value, CssTerm.class);
        assert terms != null;
        declarationVectorWriter.writeArrayValueHeader(terms.length);
        writeNumberTerms(terms, isInt);
      }
      else {
        LOG.warn("unknown arrayType: " + arrayType + " " + info.getAttributeName());
        writeUndefinedPropertyValue(value);
      }
    }
    else {
      LOG.warn("unknown type: " + type + " " + info.getAttributeName());
      writeUndefinedPropertyValue(value);
    }
  }

  private void writeBooleanValue(ASTNode node) {
    final int amfType;
    if (node.getElementType() == CssElementTypes.CSS_IDENT) {
      amfType = node.getChars().charAt(0) == 't' ? Amf3Types.TRUE : Amf3Types.FALSE;
    }
    else {
      assert node.getElementType() == CssElementTypes.CSS_STRING;
      node = node.getFirstChildNode();
      // IDEA-72194
      final CharSequence chars = node.getChars();
      if (cssStringEquals(chars, "true")) {
        amfType = Amf3Types.TRUE;
      }
      else if (cssStringEquals(chars, "false")) {
        amfType = Amf3Types.FALSE;
      }
      else {
        propertyOut.write(Amf3Types.STRING);
        writeCssStringToken(chars);
        return;
      }
    }

    propertyOut.write(CssPropertyType.BOOL);
    propertyOut.write(amfType);
  }

  private static boolean cssStringEquals(CharSequence chars, CharSequence value) {
    return chars.length() == (value.length() + 2) && StringUtil.startsWith(chars, 1, value);
  }

  private void writeStringValue(ASTNode node, final FlexStyleIndexInfo info) {
    final boolean stripQuotes;
    if (node.getElementType() == CssElementTypes.CSS_STRING) {
      stripQuotes = true;
      node = node.getFirstChildNode();
    }
    else {
      stripQuotes = false;
    }

    final CharSequence chars = node.getChars();
    if (info.getEnumeration() == null) {
      propertyOut.write(Amf3Types.STRING);
      if (stripQuotes) {
        writeCssStringToken(chars);
      }
      else {
        propertyOut.writeAmfUtf(chars);
      }
    }
    else {
      propertyOut.write(AmfExtendedTypes.STRING_REFERENCE);
      stringWriter.write(stripQuotes ? chars.subSequence(1, chars.length() - 1).toString() : chars.toString(), propertyOut);
    }
  }

  private void writeCssStringToken(CharSequence chars) {
    propertyOut.writeAmfUtf(chars, false, 1, chars.length() - 1);
  }

  private void writeNumberValue(ASTNode node, final boolean isInt) {
    writeNumberValue(node, isInt, true);
  }

  // In Flex css number cannot be hex (#ddaabb, allowable only for Color)
  private void writeNumberValue(ASTNode node, final boolean isInt, boolean writeCssTypeMarker) {
    if (writeCssTypeMarker) {
      propertyOut.write(CssPropertyType.NUMBER);
    }

    final IElementType elementType = node.getElementType();
    boolean isNegative = false;
    if (elementType == CssElementTypes.CSS_NUMBER_TERM) {
      node = node.getFirstChildNode();
      // todo honor unit, IDEA-72089
    }
    else if (elementType == CssElementTypes.CSS_MINUS) {
      isNegative = true;
      node = node.getTreeNext().getFirstChildNode();
    }
    else if (elementType == CssElementTypes.CSS_HASH) {
      final CharSequence chars = node.getChars();
      if (chars.length() > (1 + 6)) {
        propertyOut.writeAmfUInt(IOUtil.parseLong(chars, 1, false, 16));
      }
      else {
        propertyOut.writeAmfUInt(IOUtil.parseInt(chars, 1, false, 16));
      }
      return;
    }
    else {
      throw new IllegalArgumentException("unknown number value type " + elementType);
    }

    IOUtil.writeAmfIntOrDouble(propertyOut, node.getChars(), isNegative, isInt);
  }

  private void writeColor(PsiElement value) {
    Color color = CssUtil.getColor(value);
    assert color != null;
    propertyOut.writeAmfUInt(color.getRGB());
  }

  /**
   * If there is no descriptor (FlexCssPropertyDescriptor) for property, then:
   * 1) property is outdated and unused, but it have forgotten remove it
   * 2) developer is too lazy
   * 5
   */
  private void writeUndefinedPropertyValue(CssTermList value) throws InvalidPropertyException {
    CssTerm[] terms = PsiTreeUtil.getChildrenOfType(value, CssTerm.class);
    assert terms != null && terms.length > 0;
    CssTermType termType = terms[0].getTermType();
    if (termType == CssTermTypes.COLOR) {
      // todo test for CSS_FUNCTION — rgb()
      if (terms.length == 1) {
        propertyOut.write(CssPropertyType.COLOR_INT);
        writeColor(value);
      }
      else {
        propertyOut.write(CssPropertyType.ARRAY_OF_COLOR);
        declarationVectorWriter.writeArrayValueHeader(terms.length);
        for (CssTerm colorTerm : terms) {
          writeColor(colorTerm);
        }
      }
    }
    else if (termType == CssTermTypes.IDENT) {
      //noinspection ConstantConditions
      final ASTNode node = value.getFirstChild().getFirstChild().getNode();
      if (node.getElementType() == CssElementTypes.CSS_FUNCTION) {
        writeFunctionValue((CssFunction)node, null);
      }
      else {
        assert terms.length == 1;
        String v = value.getText();
        if (v.charAt(0) == 't') {
          propertyOut.write(CssPropertyType.BOOL);
          propertyOut.write(Amf3Types.TRUE);
        }
        else if (v.charAt(0) == 'f') {
          propertyOut.write(CssPropertyType.BOOL);
          propertyOut.write(Amf3Types.FALSE);
        }
        else {
          propertyOut.write(Amf3Types.STRING);
          propertyOut.writeAmfUtf(StringUtil.stripQuotesAroundValue(v));
        }
      }
    }
    else if (termType == CssTermTypes.NUMBER || termType == CssTermTypes.NEGATIVE_NUMBER) {
      if (terms.length > 1) {
        propertyOut.write(CssPropertyType.ARRAY_OF_NUMBER);
        declarationVectorWriter.writeArrayValueHeader(terms.length);
      }
      else {
        propertyOut.write(CssPropertyType.NUMBER);
      }
      writeNumberTerms(terms, false);
    }
    else {
      throw new IllegalArgumentException("unknown css term type: " + termType);
    }
  }

  private void writeNumberTerms(CssTerm[] terms, boolean isInt) {
    for (CssTerm nTerm : terms) {
      //noinspection ConstantConditions
      writeNumberValue(nTerm.getFirstChild().getNode(), isInt, false);
    }
  }

  private void writeFunctionValue(CssFunction cssFunction, @Nullable FlexStyleIndexInfo info) throws InvalidPropertyException {
    String functionName = cssFunction.getFunctionName();
    switch (functionName.charAt(0)) {
      case 'C':
        writeClassReference((ASTNode)cssFunction, info);
        break;

      case 'E':
        writeEmbed(cssFunction);
        break;

      default:
        throw new IllegalArgumentException("unknown function: " + functionName);
    }
  }

  private void writeClassReference(ASTNode node, FlexStyleIndexInfo info) throws InvalidPropertyException {
    @SuppressWarnings("ConstantConditions")
    ASTNode valueNode = node.findChildByType(CssElementTypes.CSS_TERM_LIST).getFirstChildNode().getFirstChildNode();
    // ClassReference(null);
    if (valueNode instanceof XmlToken) {
      assert valueNode.getText().equals("null");
      propertyOut.write(Amf3Types.NULL);
    }
    else {
      CssString cssString = (CssString)valueNode;
      JSClass jsClass = InjectionUtil.getJsClassFromPackageAndLocalClassNameReferences(cssString);
      if (jsClass == null) {
        final CharSequence chars = valueNode.getFirstChildNode().getChars();
        throw new InvalidPropertyException(cssString, "error.unresolved.class", chars.subSequence(1, chars.length() - 1));
      }

      writeClassReference(jsClass, info, cssString);
    }
  }

  protected void writeClassReference(JSClass jsClass, FlexStyleIndexInfo info, CssString cssString) throws InvalidPropertyException {
    propertyOut.write(AmfExtendedTypes.CLASS_REFERENCE);
    stringWriter.write(jsClass.getQualifiedName(), propertyOut);
  }

  private void writeEmbed(CssFunction cssFunction) throws InvalidPropertyException {
    CssTerm[] terms = PsiTreeUtil.getChildrenOfType(PsiTreeUtil.getRequiredChildOfType(cssFunction, CssTermList.class), CssTerm.class);
    VirtualFile source = null;
    String symbol = null;
    String mimeType = null;
    assert terms != null;
    for (int i = 0, termsLength = terms.length; i < termsLength; i++) {
      CssTerm term = terms[i];
      final PsiElement firstChild = term.getFirstChild();
      if (firstChild == null) {
        throw new IllegalArgumentException("invalid property value");
      }
      else {
        if (firstChild instanceof CssString) {
          source = InjectionUtil.getReferencedFile(firstChild, false);
        }
        else if (firstChild instanceof XmlToken && ((XmlToken)firstChild).getTokenType() == CssElementTypes.CSS_IDENT) {
          String name = firstChild.getText();
          if (name.equals("source")) {
            source = InjectionUtil.getReferencedFile(terms[++i].getFirstChild(), false);
          }
          else if (name.equals("symbol")) {
            //noinspection ConstantConditions
            symbol = ((CssString)terms[++i].getFirstChild()).getValue();
          }
          else if (name.equals("mimeType")) {
            //noinspection ConstantConditions
            mimeType = ((CssString)terms[++i].getFirstChild()).getValue();
          }
        }
      }
    }

    if (source == null) {
      throw new InvalidPropertyException(cssFunction, FlexUIDesignerBundle.message("error.embed.source.not.specified", cssFunction.getText()));
    }

    if (requiredAssetsInfo == null) {
      requiredAssetsInfo = new RequiredAssetsInfo();
    }

    final int fileId;
    final boolean isSwf = InjectionUtil.isSwf(source, mimeType);
    if (isSwf) {
      fileId = EmbedSwfManager.getInstance().add(source, symbol, requiredAssetsInfo);
    }
    else {
      fileId = EmbedImageManager.getInstance().add(source, mimeType, requiredAssetsInfo);
    }

    propertyOut.write(isSwf ? AmfExtendedTypes.SWF : AmfExtendedTypes.IMAGE);
    propertyOut.writeUInt29(fileId);
  }

  private static final class FlexCssConditionKind {
    public static final int CLASS = 0;
    public static final int ID = 1;
    public static final int PSEUDO = 2;
  }
}
