package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import org.intellij.plugins.postcss.PostCssBundle;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public class PostCssAddPrefixQuickFix extends LocalQuickFixBase {
  @NotNull private final String myPrefix;
  @NotNull private final Predicate<PsiElement> myPredicate;
  @NotNull private final Function<Pair<Project, String>, PsiElement> myPsiGenerator;

  public PostCssAddPrefixQuickFix(@NotNull String messageProperty, @NotNull String prefix, @NotNull Predicate<PsiElement> predicate,
                                  @NotNull Function<Pair<Project, String>, PsiElement> psiGenerator) {
    super(PostCssBundle.message(messageProperty));
    myPrefix = prefix;
    myPredicate = predicate;
    myPsiGenerator = psiGenerator;
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement startElement = descriptor.getStartElement();
    if (myPredicate.test(startElement)) {
      String text = startElement.getText();
      for (int i = 0; i < myPrefix.length(); i++) {
        if (!StringUtil.startsWith(text, myPrefix.substring(0, i + 1))) {
          text = myPrefix + text.substring(i);
          break;
        }
      }
      startElement.replace(ObjectUtils.notNull(myPsiGenerator.apply(Pair.create(project, text))));
    }
  }
}