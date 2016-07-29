package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.config.HbConfig;
import com.dmarcotte.handlebars.psi.HbPsiElement;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;

public class HbInsertHandler implements InsertHandler {
    private static final HbInsertHandler instance = new HbInsertHandler();

    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        HbPsiElement element = (HbPsiElement) lookupElement.getPsiElement();
        String insertionString = lookupElement.getLookupString();
        for(String templatesLocation : HbConfig.getNormalizedTemplatesLocations()) {
            if(insertionString.indexOf(templatesLocation) == 0) {
                insertionString = insertionString.replace(templatesLocation, "");
            }
        }

        int offset = element.getNode().getText().indexOf(element.getName());

        insertionContext.getDocument().replaceString(
                element.getTextOffset() + offset,
                insertionContext.getTailOffset(),
                insertionString
        );
    }

    public static HbInsertHandler getInstance() {
        return instance;
    }
}