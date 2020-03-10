// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model

import com.intellij.coldFusion.model.CfmlDocUtil.bold
import com.intellij.coldFusion.model.CfmlUtil.getCfmlLangInfo
import com.intellij.coldFusion.model.info.CfmlAttributeDescription
import com.intellij.coldFusion.model.info.CfmlLangInfo
import com.intellij.coldFusion.model.info.CfmlTagDescription
import com.intellij.coldFusion.model.info.CfmlTypesInfo
import com.intellij.coldFusion.model.psi.CfmlComponent
import com.intellij.coldFusion.model.psi.CfmlFunction
import com.intellij.coldFusion.model.psi.CfmlProperty
import com.intellij.openapi.project.Project
import com.intellij.util.containers.isNullOrEmpty
import com.intellij.xml.util.XmlStringUtil
import java.util.*

object CfmlDocUtil {

  @JvmStatic
  fun tagDescription(tagName: String, project: Project): String? {
    if (!getCfmlLangInfo(project).tagAttributes.containsKey(tagName)) {
      return null
    }
    val tagDescription: CfmlTagDescription = getCfmlLangInfo(project).tagAttributes[tagName] ?: return null
    return p {
      "Tag name: ${tagName.bold()}"
    } +
           p {
             "Is end tag required: ${tagDescription.isEndTagRequired}"
           } +
           p {
             "Description: ${tagDescription.description.escape()}"
           } +
           p {
             "For more information visit " +
             ahref { CfmlLangInfo.getInstance(project).onlineDocumentationLink }
           } +
           addAttributes(tagDescription)
  }

  @JvmStatic
  fun attributeDescription(name: String, cfmlAttributeDescription: CfmlAttributeDescription?, project: Project): String {
    cfmlAttributeDescription ?: return "This attribute is unknown."
    return p {
      "Attribute ${cfmlAttributeDescription.name.bold()} for tag ${name.bold()}"
    } +
           p {
             "This tag is " + (if (cfmlAttributeDescription.isRequired) "required" else "optional").bold() + "."
           } +
           p {
             "Type: " + CfmlTypesInfo.typeFromInt(cfmlAttributeDescription.type)
           } +
           p {
             "Description: " + if (cfmlAttributeDescription.description.isNullOrEmpty()) "No description for this attribute" else cfmlAttributeDescription.description
           }
  }


  private fun addAttributes(tagDescription: CfmlTagDescription): String {
    if (tagDescription.attributes.isNullOrEmpty()) return ""
    return table("width:100%;padding:5px;border-spacing:5px;") {
      tr {
        th("text-align:left;") { "Attribute" } +
        th("text-align:left;") { "Description" }
      } +
      tagDescription.attributes.toMutableList().map {
        tr {
          td {
            div { it.name.bold() } +
            div { "${CfmlTypesInfo.typeFromInt(it.type)}, ${if (it.isRequired) "required" else "optional"}" }
          } +
          td {
            if (it.description.isNullOrEmpty()) "No description for this attribute" else it.description
          }
        }
      }.joinToString("\n")
    }
  }

  private fun String?.escape(): String? {
    this ?: return null
    return XmlStringUtil.escapeString(this)
      .unescapeHtmlTag("li")
      .unescapeHtmlTag("ul")
  }

  private fun String.unescapeHtmlTag(htmlTag: String): String {
    return this.replace("&lt;$htmlTag&gt;", "<$htmlTag>").replace("&lt;/$htmlTag&gt;", "</$htmlTag>")
  }

  private fun table(style: String = "", textBlock: () -> String): String {
    return "<table style=\"$style\">${textBlock()}</table>"
  }

  private fun tr(textBlock: () -> String): String {
    return "<tr valign=\"top\">${textBlock()}</tr>"
  }

  private fun td(textBlock: () -> String): String {
    return "<td>${textBlock()}</td>"
  }

  private fun th(style: String = "", textBlock: () -> String): String {
    return "<th style=\"$style\">${textBlock()}</th>"
  }

  private fun p(textBlock: () -> String): String {
    return "<p>${textBlock()}</p>"
  }

  private fun div(textBlock: () -> String): String {
    return "<div>${textBlock()}</div>"
  }

  private fun ahref(optionalName: String? = null, link: () -> String): String {
    return "<a href=\"${link()}\">${optionalName ?: link()}</a>"
  }

  private fun String.bold(): String {
    return "<b>$this</b>"
  }

  private fun String.faint(): String {
    return "<span style=\"font-weight: lighter;\">$this</span>"
  }

  @JvmStatic
  fun componentDescription(component: CfmlComponent, project: Project): String {
    var description = p { (component.name ?: "Unknown component name").bold() }

    val hintDescription = component.description
    if (hintDescription != null) {
      description += p { hintDescription }
    }

    val init = Arrays.stream(component.getFunctionsWithSupers(false)).filter { f: CfmlFunction -> f.name == "init" }
           .findFirst()
    if (init.isPresent) {
      description += "<p></p>" + functionDescription(init.get(), project)
    }
    return description
  }

  @JvmStatic
  fun functionDescription(function: CfmlFunction, project: Project): String {
    val info = function.functionInfo
    var paramsLabel = "Params: ".faint()
    return p {
      info.name.trim().bold() + if (info.parameters.isEmpty()) "()" else "(...)" + " : " + (info.returnType ?: "any")
    } + p {
      info.description?.trim() ?: ""
    } + table {
      info.parameters.map {
        tr {
          td {
            paramsLabel
          } + td {
            paramsLabel = ""
            it.presetableText.bold() + "  "
          } + td {
            it.description?.trim() ?: ""
          }
        }
      }.joinToString("")
    }
  }

  @JvmStatic
  fun propertyDescription(property: CfmlProperty, project: Project): String {
    val info = property.propertyInfo
    return p {
      info.name.trim().bold() + " : " + (info.type ?: "any")
    } + p {
      info.description?.trim() ?: ""
    }
  }
}