package org.angularjs

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.util.ProcessingContext
import com.intellij.patterns.XmlPatterns
import com.intellij.patterns.PlatformPatterns
import com.intellij.codeInsight.completion.XmlAttributeInsertHandler

public open class DirectiveCompletionContributor(): CompletionContributor() {
    {
        //attribute value
        extend(CompletionType.BASIC, PlatformPatterns.psiElement()?.inside(XmlPatterns.xmlAttributeValue()!!), object : CompletionProvider<CompletionParameters>() {
            protected override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet): Unit {
                if (parameters.getCompletionType() != CompletionType.BASIC)
                    return


            }
        })

        extend(CompletionType.BASIC, PlatformPatterns.psiElement()?.inside(XmlPatterns.xmlAttribute()!!), object : CompletionProvider<CompletionParameters>() {
            protected override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet): Unit {
                if (parameters.getCompletionType() != CompletionType.BASIC)
                    return

                directiveNames.forEach {
                    name ->
                    result.addElement(LookupElementBuilder.create("ng-" + name)!!.withInsertHandler(XmlAttributeInsertHandler.INSTANCE)!!)
                }
            }
        })
        //will come back to tag autocomplete later...
        /*        extend(CompletionType.BASIC, PlatformPatterns.psiElement()?.inside(XmlPatterns.xmlTag()!!), object : CompletionProvider<CompletionParameters>() {
                    protected override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext?, result: CompletionResultSet): Unit {
                        if (parameters.getCompletionType() != CompletionType.BASIC)
                            return

                        result.addElement(LookupElementBuilder.create("johntag")!!.withInsertHandler(XmlAttributeInsertHandler.INSTANCE)!!)
                    }
                })*/
    }

    val directiveNames = array<String>(
"animate",
"app",
"bind",
"bind-html-unsafe",
"bind-template",
"change",
"checked",
"class",
"class-even",
"class-odd",
"click",
"cloak",
"controller",
"csp",
"dblclick",
"disabled",
"false-value",
"form",
"hide",
"href",
"if",
"include",
"init",
"keypress",
"list",
"minlength",
"maxlength",
"model",
"mousedown",
"mouseup",
"mouseover",
"mouseout",
"mousemove",
"mouseenter",
"mouseleave",
"multiple",
"non-bindable",
"options",
"pattern",
"pluralize",
"readonly",
"repeat",
"required",
"selected",
"show",
"src",
"srcset",
"submit",
"style",
"swipe",
"switch",
"switch-when",
"switch-default",
"transclude",
"true-value",
"value",
"view")

    //
    //    public override fun fillCompletionVariants(parameters: CompletionParameters?, result: CompletionResultSet?): Unit {
    //        result?.addElement(LookupElementBuilder.create("ng-click")!!)
    //    }
    //
    //
    //    public override fun beforeCompletion(context: CompletionInitializationContext) {
    //        super<CompletionContributor>.beforeCompletion(context)
    //
    //        val offset: Int = context.getStartOffset()
    //        val attributeValue = PsiTreeUtil.findElementOfClassAtOffset(context.getFile(), offset, javaClass<XmlAttributeValue>(), true)
    //        if (attributeValue != null && offset == (attributeValue.getTextRange()?.getStartOffset())!!)
    //        {
    //            context.setDummyIdentifier("")
    //        }
    //    }
}
