package org.angularjs

import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import com.intellij.psi.PsiElement

/**
 * Created by johnlindquist on 6/26/13.
 */
public class AngularAttributeDescriptor(attrName: String?): AnyXmlAttributeDescriptor(attrName) {
    val attrName = attrName

    public override fun getDeclaration(): PsiElement? {
        //TODO: implement looking up declarations...
        return null
    }


}