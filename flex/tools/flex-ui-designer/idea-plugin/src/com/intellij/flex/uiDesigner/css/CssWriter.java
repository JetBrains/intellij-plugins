package com.intellij.flex.uiDesigner.css;

import com.intellij.flex.uiDesigner.*;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.mxml.AmfExtendedTypes;
import com.intellij.injected.editor.DocumentWindow;
import com.intellij.javascript.flex.css.FlexCssElementDescriptorProvider;
import com.intellij.javascript.flex.css.FlexCssPropertyDescriptor;
import com.intellij.javascript.flex.css.FlexStyleIndexInfo;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
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
  private final AssetCounter assetCounter;

  public CssWriter(StringRegistry.StringWriter stringWriter, ProblemsHolder problemsHolder, AssetCounter assetCounter) {
    this.stringWriter = stringWriter;
    this.problemsHolder = problemsHolder;
    this.assetCounter = assetCounter;
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

          writePropertyValue(value, propertyDescriptor == null || !(propertyDescriptor instanceof FlexCssPropertyDescriptor) ? null : ((FlexCssPropertyDescriptor)propertyDescriptor).getStyleInfo());
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

      for (PsiElement simpleSelector1 : simpleSelectors) {
        CssSimpleSelector simpleSelector = (CssSimpleSelector)simpleSelector1;

        // subject
        if (simpleSelector.isUniversalSelector()) {
          out.write(0);
        }
        else {
          XmlElementDescriptor typeSelectorDescriptor = FlexCssElementDescriptorProvider.getTypeSelectorDescriptor(simpleSelector, module);
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

  private void writeStringValue(ASTNode node, boolean writeCssTypeMarker, @Nullable final FlexStyleIndexInfo info) {
    final boolean stripQuotes;
    if (node.getElementType() == CssElementTypes.CSS_STRING) {
      stripQuotes = true;
      node = node.getFirstChildNode();
    }
    else {
      stripQuotes = false;
    }

    final CharSequence chars = node.getChars();
    if (info == null || info.getEnumeration() == null) {
      if (stripQuotes) {
        if (writeCssTypeMarker) {
          propertyOut.write(Amf3Types.STRING);
        }
        writeCssStringToken(chars);
      }
      else {
        if (StringUtil.equals(chars, "true")) {
          if (writeCssTypeMarker) {
            propertyOut.write(CssPropertyType.BOOL);
          }
          propertyOut.write(Amf3Types.TRUE);
        }
        else if (StringUtil.equals(chars, "false")) {
          if (writeCssTypeMarker) {
            propertyOut.write(CssPropertyType.BOOL);
          }
          propertyOut.write(Amf3Types.FALSE);
        }
        else {
          if (writeCssTypeMarker) {
            propertyOut.write(Amf3Types.STRING);
          }
          propertyOut.writeAmfUtf(chars);
        }
      }
    }
    else {
      if (writeCssTypeMarker) {
        propertyOut.write(AmfExtendedTypes.STRING_REFERENCE);
      }
      stringWriter.write(stripQuotes ? chars.subSequence(1, chars.length() - 1).toString() : chars.toString(), propertyOut);
    }
  }

  private void writeCssStringToken(CharSequence chars) {
    propertyOut.writeAmfUtf(chars, false, 1, chars.length() - 1);
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
    else if (elementType == CssElementTypes.CSS_IDENT) {
      assert StringUtil.equals(node.getChars(), "NaN");
      propertyOut.writeAmfDouble(Double.NaN);
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

  private static boolean isArray(PsiElement sibling) {
    if (sibling == null) {
      return false;
    }

    if (sibling instanceof PsiWhiteSpace) {
      sibling = sibling.getNextSibling();
      if (sibling == null) {
        return false;
      }
    }

    // ignore any other CssTerm if delimited by whitespace, according to flex compiler
    return sibling.getNode().getElementType() == CssElementTypes.CSS_COMMA;
  }

  /**
   * If there is no descriptor (FlexCssPropertyDescriptor) for property, then:
   * 1) property is outdated and unused, but it have forgotten remove it
   * 2) developer is too lazy
   * 5
   */
  private void writePropertyValue(CssTermList value, @Nullable FlexStyleIndexInfo info) throws InvalidPropertyException {
    final PsiElement firstChild = value.getFirstChild();
    if (firstChild == null) {
      throw new InvalidPropertyException(value, "invalid.value");
    }

    int lengthPosition = -1;
    final boolean writeCssTypeMarker;
    if (isArray(firstChild.getNextSibling())) {
      propertyOut.write(CssPropertyType.ARRAY);
      propertyOut.write(Amf3Types.ARRAY);
      lengthPosition = propertyOut.size(); // assume array length will be less 64
      propertyOut.write(0); // allocate for length
      propertyOut.write(1);
      writeCssTypeMarker = false;
    }
    else {
      writeCssTypeMarker = true;
    }

    int length = 0;
    boolean expectTerm = true;
    for (PsiElement child = value.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof CssTerm) {
        if (!expectTerm) {
          break;
        }

        CssTermType termType = ((CssTerm)child).getTermType();
        if (termType == CssTermTypes.COLOR) {
          if (writeCssTypeMarker) {
            propertyOut.write(CssPropertyType.COLOR_INT);
          }
          writeColor(child);
        }
        else if (termType == CssTermTypes.IDENT) {
          //noinspection ConstantConditions
          final ASTNode node = child.getFirstChild().getNode();
          if (node.getElementType() == CssElementTypes.CSS_FUNCTION) {
            writeFunctionValue((CssFunction)node, info);
          }
          else {
            writeStringValue(node, writeCssTypeMarker, info);
          }
        }
        else if (termType == CssTermTypes.NUMBER || termType == CssTermTypes.NEGATIVE_NUMBER || termType == CssTermTypes.LENGTH ||
                 termType == CssTermTypes.NEGATIVE_LENGTH) {
          // todo if termType equals CssTermTypes.LENGTH, we must respect unit
          //noinspection ConstantConditions
          writeNumberValue(child.getFirstChild().getNode(), false, writeCssTypeMarker);
        }
        else {
          throw new InvalidPropertyException("unknown css term type: " + termType + " in " + value.getText(), value);
        }

        length++;
        expectTerm = false;
      }
      else if (child.getNode().getElementType() == CssElementTypes.CSS_COMMA) {
        if (expectTerm) {
          break;
        }

        expectTerm = true;
      }
    }

    if (lengthPosition != -1) {
      length = (length << 1) | 1;
      assert length < 128;
      propertyOut.putByte(length, lengthPosition);
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
      assert StringUtil.equals(valueNode.getChars(), "null");
      propertyOut.write(Amf3Types.NULL);
    }
    else {
      CssString cssString = (CssString)valueNode;
      JSClass jsClass = InjectionUtil.getJsClassFromPackageAndLocalClassNameReferences(cssString);
      if (jsClass == null) {
        final CharSequence chars = valueNode.getFirstChildNode().getChars();
        throw new InvalidPropertyException(cssString, "unresolved.class", chars.subSequence(1, chars.length() - 1));
      }

      writeClassReference(jsClass, info, cssString);
    }
  }

  protected void writeClassReference(JSClass jsClass, FlexStyleIndexInfo info, CssString cssString) throws InvalidPropertyException {
    propertyOut.write(AmfExtendedTypes.CLASS_REFERENCE);
    stringWriter.write(jsClass.getQualifiedName(), propertyOut);
  }

  private void writeEmbed(CssFunction cssFunction) throws InvalidPropertyException {
    VirtualFile source = null;
    String symbol = null;
    String mimeType = null;

    CssTermList termList = PsiTreeUtil.getRequiredChildOfType(cssFunction, CssTermList.class);
    for (PsiElement child = termList.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof CssTerm) {
        PsiElement firstChild = child.getFirstChild();
        if (firstChild instanceof XmlToken && ((XmlToken)firstChild).getTokenType() == CssElementTypes.CSS_IDENT) {
          CharSequence name = firstChild.getNode().getChars();
          @SuppressWarnings("ConstantConditions")
          PsiElement valueElement = child.getLastChild().getFirstChild();
          if (StringUtil.equals(name, "source")) {
            source = InjectionUtil.getReferencedFile(valueElement);
          }
          else {
            String value = ((CssString)valueElement).getValue();
            if (StringUtil.equals(name, "symbol")) {
              symbol = value;
            }
            else if (StringUtil.equals(name, "mimeType")) {
              mimeType = value;
            }
            else {
              LOG.warn("unsupported embed param: " + name + "=" + value);
            }
          }
        }
        else if (firstChild instanceof CssString) {
          source = InjectionUtil.getReferencedFile(firstChild);
        }
        else {
          LOG.warn("unsupported embed statement: " + cssFunction.getNode().getChars());
        }
      }
    }

    if (source == null) {
      throw new InvalidPropertyException(cssFunction, FlashUIDesignerBundle.message("embed.source.not.specified", cssFunction.getText()));
    }

    final int fileId;
    final boolean isSwf = InjectionUtil.isSwf(source, mimeType);
    if (isSwf) {
      fileId = EmbedSwfManager.getInstance().add(source, symbol, assetCounter);
    }
    else if (InjectionUtil.isImage(source, mimeType)) {
      fileId = EmbedImageManager.getInstance().add(source, mimeType, assetCounter);
    }
    else {
      throw new InvalidPropertyException(cssFunction, FlashUIDesignerBundle.message("unsupported.embed.asset.type", cssFunction.getText()));
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
