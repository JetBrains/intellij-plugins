package tanvd.grazi.utils

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun html(body: BODY.() -> Unit) = createHTML(false).html { body { body(this) } }
