package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssElementGenerator;
import org.intellij.plugins.postcss.psi.PostCssOneLineAtRule;
import org.jetbrains.annotations.NotNull;

public class PostCssAddPrefixQuickFix extends LocalQuickFixBase {
  @NotNull private final String myPrefix;
  @NotNull private final Class<? extends PostCssOneLineAtRule> myClass;

  public PostCssAddPrefixQuickFix(@NotNull String messageProperty,
                                  @NotNull String prefix,
                                  @NotNull Class<? extends PostCssOneLineAtRule> elementClass) {
    super(PostCssBundle.message(messageProperty));
    myPrefix = prefix;
    myClass = elementClass;
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement startElement = descriptor.getStartElement();
    if (myClass.isInstance(startElement)) {
      TextRange textRange = descriptor.getTextRangeInElement();
      String text = textRange.substring(startElement.getText());
      for (int i = 0; i < myPrefix.length(); i++) {
        if (!StringUtil.startsWith(text, myPrefix.substring(0, i + 1))) {
          text = myPrefix + text.substring(i);
          break;
        }
      }
      String newText = textRange.replace(startElement.getText(), text);
      startElement.replace(PostCssElementGenerator.createOneLineAtRule(project, newText, myClass));
    }
  }
}