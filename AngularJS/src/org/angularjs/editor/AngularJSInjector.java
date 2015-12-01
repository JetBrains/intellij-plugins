package org.angularjs.editor;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.impl.source.xml.XmlTextImpl;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTokenType;
import org.angularjs.codeInsight.attributes.AngularAttributesRegistry;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSInjector implements MultiHostInjector {
  private static final Logger LOG = Logger.getInstance(AngularJSInjector.class);

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    // check that we have angular directives indexed before injecting
    final Project project = context.getProject();
    if (!AngularIndexUtil.hasAngularJS(project)) return;

    final PsiElement parent = context.getParent();
    if (context instanceof XmlAttributeValueImpl && parent instanceof XmlAttribute) {
      final String value = context.getText();
      final int start = value.startsWith("'") || value.startsWith("\"") ? 1 : 0;
      final int end = value.endsWith("'") || value.endsWith("\"") ? 1 : 0;
      final int length = value.length();
      if (AngularAttributesRegistry.isAngularExpressionAttribute((XmlAttribute)parent) && length > 1) {
        registrar.startInjecting(AngularJSLanguage.INSTANCE).
          addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(start, length - end)).
          doneInjecting();
        return;
      }
      if (AngularAttributesRegistry.isJSONAttribute((XmlAttribute)parent) && length > 1) {
        registrar.startInjecting(JsonLanguage.INSTANCE).
          addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(start, length - end)).
          doneInjecting();
        return;
      }
      if ((AngularAttributesRegistry.isEventAttribute(((XmlAttribute)parent).getName(), project) ||
           AngularAttributesRegistry.isVariableAttribute(((XmlAttribute)parent).getName(), project)) &&
          length > 1) {
        registrar.startInjecting(JavascriptLanguage.INSTANCE).
          addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(start, length - end)).
          doneInjecting();
        return;
      }
    }

    if (context instanceof XmlTextImpl || context instanceof XmlAttributeValueImpl) {
      final String start = AngularJSBracesUtil.getInjectionStart(project);
      final String end = AngularJSBracesUtil.getInjectionEnd(project);

      if (AngularJSBracesUtil.hasConflicts(start, end, context)) return;

      final String text = context.getText();
      int startIndex;
      int endIndex = -1;
      do {
        startIndex = text.indexOf(start, endIndex);
        if (startIndex < 0) return;

        int afterStart = startIndex + start.length();
        endIndex = new MatchingEndFinder(start, end, text, afterStart).find();
        endIndex = endIndex > 0 ? endIndex : ElementManipulators.getValueTextRange(context).getEndOffset();
        final PsiElement injectionCandidate = startIndex >= 0 ? context.findElementAt(startIndex) : null;
        if (injectionCandidate != null && injectionCandidate.getNode().getElementType() != XmlTokenType.XML_COMMENT_CHARACTERS &&
           !(injectionCandidate instanceof OuterLanguageElement)) {
          if (afterStart > endIndex) {
            LOG.error("Braces: " + start + "," + end + "\n" +
                      "Text: \"" + text + "\"" + "\n" +
                      "Interval: (" + afterStart + "," + endIndex + ")" + "\n" +
                      "File: " + context.getContainingFile().getName() + ", language:" + context.getContainingFile().getLanguage());
          }
          registrar.startInjecting(AngularJSLanguage.INSTANCE).
                    addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(afterStart, endIndex)).
                    doneInjecting();
        }
      } while (startIndex >= 0);
    }
  }

  private static class MatchingEndFinder {
    private final String myStartSymbol;
    private final String myEndSymbol;
    private int myNumStarts;
    private String myText;
    private int myAfterStartIdx;

    public MatchingEndFinder(String startSymbol, String endSymbol, String text, int afterStartIdx) {
      assert afterStartIdx >= 0;
      myStartSymbol = startSymbol;
      myEndSymbol = endSymbol;
      myNumStarts = 1;
      myText = text;
      myAfterStartIdx = afterStartIdx;
    }

    public int find() {
      while (myNumStarts > 0) {
        -- myNumStarts;
        int nextEndIdx = myText.indexOf(myEndSymbol, myAfterStartIdx);
        if (nextEndIdx == -1) return -1;
        final int numStarts = StringUtil.getOccurrenceCount(myText.substring(myAfterStartIdx, nextEndIdx), myStartSymbol);
        if (numStarts > 0) {
          myNumStarts += numStarts;
          myAfterStartIdx = nextEndIdx + myEndSymbol.length();
        }
        if (myNumStarts == 0) return nextEndIdx;
      }
      return -1;
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlTextImpl.class, XmlAttributeValueImpl.class);
  }
}
