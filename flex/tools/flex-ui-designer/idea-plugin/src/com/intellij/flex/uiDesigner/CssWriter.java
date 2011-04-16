package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.*;
import com.intellij.injected.editor.DocumentWindow;
import com.intellij.javascript.flex.css.FlexCssElementDescriptorProvider;
import com.intellij.javascript.flex.css.FlexCssPropertyDescriptor;
import com.intellij.javascript.flex.css.FlexStyleIndexInfo;
import com.intellij.lang.ASTNode;
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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlToken;
import com.intellij.xml.XmlElementDescriptor;

import java.awt.*;

public class CssWriter {
  private static final Logger LOG = Logger.getInstance(CssWriter.class.getName());

  private PrimitiveAmfOutputStream propertyOut;
  private final CustomVectorWriter rulesetVectorWriter = new CustomVectorWriter();
  private final CustomVectorWriter declarationVectorWriter = new CustomVectorWriter();

  private final StringRegistry.StringWriter stringWriter;

  private ProblemsHolder problemsHolder;

  public CssWriter(StringRegistry.StringWriter stringWriter) {
    this.stringWriter = stringWriter;
  }

  public byte[] write(VirtualFile file, Module module, ProblemsHolder problemsHolder) {
    Document document = FileDocumentManager.getInstance().getDocument(file);
    assert document != null;
    CssFile cssFile = (CssFile)PsiDocumentManager.getInstance(module.getProject()).getPsiFile(document);
    return write(cssFile, document, module, problemsHolder);
  }

  public byte[] write(CssFile cssFile, Module module, ProblemsHolder problemsHolder) {
    return write(cssFile, PsiDocumentManager.getInstance(module.getProject()).getDocument(cssFile), module, problemsHolder);
  }

  public byte[] write(CssFile cssFile, Document document, Module module, ProblemsHolder problemsHolder) {
    this.problemsHolder = problemsHolder;

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
          problemsHolder.add(e, declaration.getPropertyName());
        }
        catch (Throwable e) {
          problemsHolder.add(e);
        }

        declarationVectorWriter.rollbackLastIteration();
      }

      declarationVectorWriter.writeTo(rulesetOut);
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
    assert type != null;
    switch (type.charAt(0)) {
      case 'u':
        final String format = info.getFormat();
        assert format != null;
        if (format.equals(FlexCssPropertyDescriptor.COLOR_FORMAT)) {
          // http://youtrack.jetbrains.net/issue/IDEA-59632
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
          propertyOut.write(CssPropertyType.NUMBER);
          propertyOut.writeAmfInt(value.getText());
        }
        break;

      case 'i':
        propertyOut.write(CssPropertyType.NUMBER);
        propertyOut.writeAmfInt(value.getText());
        break;

      case 'S':
        // special case: ClassReference(null);
        //noinspection ConstantConditions
        if (value.getFirstChild().getFirstChild().getNode().getElementType() == CssElementTypes.CSS_FUNCTION) {
          propertyOut.write(CssPropertyType.NULL);
        }
        else {
          propertyOut.write(CssPropertyType.STRING);
          propertyOut.writeAmfUtf(StringUtil.stripQuotesAroundValue(value.getText()));
        }
        break;

      case 'B':
        propertyOut.write(CssPropertyType.BOOL);
        propertyOut.write(value.getText().charAt(0) == 't' ? Amf3Types.TRUE : Amf3Types.FALSE);
        break;

      case 'N':
        propertyOut.write(CssPropertyType.NUMBER);
        propertyOut.writeAmfDouble(value.getText());
        break;

      case 'C': // Class, ClassReference("mx.skins.halo.HaloFocusRect");
        //noinspection ConstantConditions
        writeClassReference(value.getFirstChild().getFirstChild());
        break;

      case 'O': // Object, like baselineShift
        writeUndefinedPropertyValue(value);
        break;

//      case 'A': // Array
      // todo support as for writeUndefinedPropertyValue
//        break;

      default:
//        throw new IllegalArgumentException("unknown property type: " + info.getType() + " and format: " + info.getFormat());
        // todo support custom type: mx.graphics.IFill, fill: #000000
        propertyOut.write(CssPropertyType.NUMBER);
        propertyOut.writeAmfInt(0);
        break;
    }
  }

  private void writeClassReference(PsiElement element) {
    String className = PsiTreeUtil.getRequiredChildOfType(element, CssTermList.class).getText();
    // ClassReference(null);
    if (className.equals("null")) {
      propertyOut.write(CssPropertyType.NULL);
    }
    else {
      propertyOut.write(CssPropertyType.CLASS_REFERENCE);
      stringWriter.write(StringUtil.stripQuotesAroundValue(className), propertyOut);
    }
  }

  private void writeColor(PsiElement value) {
    Color color = CssUtil.getColor(value);
    assert color != null;
    propertyOut.writeAmfUInt(color.getRGB());
  }

  // 5
  private void writeUndefinedPropertyValue(CssTermList value) throws InvalidPropertyException {
    CssTerm[] terms = PsiTreeUtil.getChildrenOfType(value, CssTerm.class);
    assert terms != null && terms.length > 0;
    CssTermType termType = terms[0].getTermType();
    if (termType == CssTermTypes.COLOR) {
      // todo test for CSS_FUNCTION — rgb()
      if (terms.length == 1) {
        // todo form of color: function, hex, name, string
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
      @SuppressWarnings({"ConstantConditions"}) ASTNode node = value.getFirstChild().getFirstChild().getNode();
      if (node.getElementType() == CssElementTypes.CSS_FUNCTION) {
        writeFunctionValueForUndefinedProperty((CssFunction)node);
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
          propertyOut.write(CssPropertyType.STRING);
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
      for (CssTerm nTerm : terms) {
        String v = nTerm.getText();
        if (v.indexOf('.') == -1) {
          propertyOut.writeAmfInt(Integer.valueOf(v));
        }
        else {
          propertyOut.writeAmfDouble(v);
        }
      }
    }
    else {
      throw new IllegalArgumentException("unknown css term type: " + termType);
    }
  }

  private void writeFunctionValueForUndefinedProperty(CssFunction cssFunction) throws InvalidPropertyException {
    String functionName = cssFunction.getFunctionName();
    switch (functionName.charAt(0)) {
      case 'C':
        writeClassReference(cssFunction);
        break;

      case 'E':
        writeEmbed(cssFunction);
        break;

      default:
        throw new IllegalArgumentException("unknown function: " + functionName);
    }
  }

  private void writeEmbed(CssFunction cssFunction) throws InvalidPropertyException {
    System.out.print(cssFunction.getText());
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
          source = InjectionUtil.getReferencedFile(firstChild, problemsHolder, false);
        }
        else if (firstChild instanceof XmlToken && ((XmlToken)firstChild).getTokenType() == CssElementTypes.CSS_IDENT) {
          String name = firstChild.getText();
          if (name.equals("source")) {
            source = InjectionUtil.getReferencedFile(terms[++i].getFirstChild(), problemsHolder, false);
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
      problemsHolder.add(FlexUIDesignerBundle.message("error.embed.source.not.specified", cssFunction.getText()));
      throw new IllegalArgumentException("todo");
    }

    final boolean isSwf = InjectionUtil.isSwf(source, mimeType);
    BinaryFileManager binaryFileManager = BinaryFileManager.getInstance();
    final int fileId;
    if (binaryFileManager.isRegistered(source)) {
      fileId = binaryFileManager.getId(source);
    }
    else {
      fileId = binaryFileManager.registerFile(source, isSwf ? BinaryFileType.SWF : BinaryFileType.IMAGE);
    }

    propertyOut.write(isSwf ? CssPropertyType.EMBED_SWF : CssPropertyType.EMBED_IMAGE);
    if (isSwf) {
      if (symbol == null) {
        propertyOut.write(0);
      }
      else {
        propertyOut.writeAmfUtf(symbol);
      }
    }
    propertyOut.writeUInt29(fileId);
  }

  private static final class FlexCssConditionKind {
    public static final int CLASS = 0;
    public static final int ID = 1;
    public static final int PSEUDO = 2;
  }
}
