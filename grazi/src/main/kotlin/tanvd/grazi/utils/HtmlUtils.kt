package tanvd.grazi.utils

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun html(body: BODY.() -> Unit) = createHTML(false).html { body { body(this) } }

var TABLE.cellpading: String
    get() = attributes["cellpadding"] ?: ""
    set(value) { attributes["cellpadding"] = value }

var TABLE.cellspacing: String
    get() = attributes["cellspacing"] ?: ""
    set(value) { attributes["cellspacing"] = value }

var TD.valign: String
    get() = attributes["valign"] ?: ""
    set(value) { attributes["valign"] = value }
