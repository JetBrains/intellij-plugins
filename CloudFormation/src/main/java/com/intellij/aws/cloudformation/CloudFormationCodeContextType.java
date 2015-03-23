package com.intellij.aws.cloudformation;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.json.liveTemplates.JsonContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CloudFormationCodeContextType extends TemplateContextType {
    @NonNls
    private static final String CLOUDFORMATION_TEMPLATE = "AWS_CLOUD_FORMATION";

    public CloudFormationCodeContextType() {
        super(CLOUDFORMATION_TEMPLATE, CloudFormationBundle.getString("aws.cloudformation.template.context.type"),
                JsonContextType.class);
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        return CloudFormationPsiUtils.isCloudFormationFile(file);
    }
}
