package org.angularjs

import com.intellij.openapi.components.ProjectComponent
import kotlin.properties.Delegates
import java.util.HashMap
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.psi.search.GlobalSearchScope

/**
 * Created by johnlindquist on 6/28/13.
 */
open class AngularJS(project:Project):AbstractProjectComponent(project){
    val directiveNames: Array<String?> = array<String?>(
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
    var attrs:jet.List<AngularAttributeDescriptor> by Delegates.notNull()
    val attrLookup:HashMap<String, AngularAttributeDescriptor> = hashMapOf()
    var attrArray:Array<XmlAttributeDescriptor> by Delegates.notNull()

    class object{
        fun getInstance(project : Project?) : AngularJS {
            return project?.getComponent(javaClass<AngularJS>())!!
        }
    }

    public override fun initComponent() {
        attrs = directiveNames.map { name -> AngularAttributeDescriptor("ng-" + name)}
        attrs.forEach {
            descriptor -> attrLookup.put(descriptor.getName()!!, descriptor)
        }
        attrArray = attrs.copyToArray()
    }
    public override fun disposeComponent() {

    }
    org.jetbrains.annotations.NonNls public override fun getComponentName(): String {
        return "AngularJSProjectComponent"
    }
    public override fun projectOpened() {

    }
    public override fun projectClosed() {

    }
}