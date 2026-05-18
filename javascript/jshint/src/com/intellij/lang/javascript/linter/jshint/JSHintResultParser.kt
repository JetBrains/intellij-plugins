package com.intellij.lang.javascript.linter.jshint

import com.intellij.execution.ExecutionException
import com.intellij.lang.javascript.linter.JSLinterError
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.NlsSafe
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parses JSHint checkstyle XML output format into JSLinterError objects.
 *
 * JSHint checkstyle format:
 * ```xml
 * <?xml version="1.0" encoding="utf-8"?>
 * <checkstyle version="4.3">
 *   <file name="path/to/file.js">
 *     <error line="1" column="5" severity="warning" message="Missing semicolon." source="jshint.W033" />
 *   </file>
 * </checkstyle>
 * ```
 */
internal object JSHintResultParser {

  @Throws(ExecutionException::class)
  fun parse(xmlOutput: String): List<JSLinterError> {
    if (xmlOutput.isBlank()) {
      return emptyList()
    }

    try {
      val factory = DocumentBuilderFactory.newInstance()
      val builder = factory.newDocumentBuilder()
      val document = builder.parse(InputSource(StringReader(xmlOutput)))

      val errors = mutableListOf<JSLinterError>()
      val errorNodes = document.getElementsByTagName("error")

      for (i in 0 until errorNodes.length) {
        val errorNode = errorNodes.item(i)
        val attributes = errorNode.attributes

        val line = attributes.getNamedItem("line")?.nodeValue?.toIntOrNull() ?: continue
        val column = attributes.getNamedItem("column")?.nodeValue?.toIntOrNull() ?: continue

        @NlsSafe
        val message = attributes.getNamedItem("message")?.nodeValue ?: continue

        // Extract code from source attribute (e.g., "jshint.W033" -> "W033")
        val source = attributes.getNamedItem("source")?.nodeValue
        val code = source?.substringAfterLast('.', "")

        errors.add(JSLinterError(line, column, message, code))
      }

      return errors
    }
    catch (e: Exception) {
      logger<JSHintResultParser>().warn("Failed to parse JSHint checkstyle XML output: $xmlOutput", e)
      throw ExecutionException(JSHintBundle.message("jshint.inspection.message.failed.to.parse.output", e.message ?: ""), e)
    }
  }
}
