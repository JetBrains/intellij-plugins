package org.angularjs;

import com.intellij.find.FindResult;
import com.intellij.psi.PsiElement;
import com.intellij.usages.Usage;

/**
* Created with IntelliJ IDEA.
* User: johnlindquist
* Date: 3/27/13
* Time: 9:56 AM
* To change this template use File | Settings | File Templates.
*/
public class AngularItem {
    private String itemName;

    public Usage getUsage() {
        return usage;
    }

    private Usage usage;
    private final PsiElement element;

    public PsiElement getElement() {
        return element;
    }

    AngularItem(String itemName, Usage usage, PsiElement element) {
        this.itemName = itemName;
        this.usage = usage;
        this.element = element;
    }


    public String getItemName() {
        return itemName;
    }

    public String getItemType() {
        return element.getText();
    }
}
