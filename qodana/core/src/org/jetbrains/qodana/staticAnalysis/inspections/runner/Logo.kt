package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.application.ApplicationInfo
import org.jline.terminal.TerminalBuilder
import kotlin.math.abs

private val logoLines = """                                        
          _              _       
         /\ \           /\ \     
        /  \ \         /  \ \    
       / /\ \ \       / /\ \ \   
      / / /\ \ \     / / /\ \ \  
     / / /  \ \_\   / / /  \ \_\ 
    / / / _ / / /  / / /   / / / 
   / / / /\ \/ /  / / /   / / /  
  / / /__\ \ \/  / / /___/ / /   
 / / /____\ \ \ / / /____\/ /    
 \/________\_\/ \/_________/     
                                                   
                                        """.lines()

private val textLines = """
   Documentation
   https://jb.gg/qodana-docs
   Contact us at
   qodana-support@jetbrains.com
   Or via our issue tracker
   https://jb.gg/qodana-issue
   Or share your feedback at our forum
   https://jb.gg/qodana-forum

""".lines()

fun printProductHeader() {
  val productName = getQodanaProductName()
  warnDeprecated(productName)
  var qodanaInfo = listOf("", "   $productName ${ApplicationInfo.getInstance().fullVersion}")
  qodanaInfo = qodanaInfo + textLines
  val defaultTerminalLength = 80
  val terminalWidth = TerminalBuilder.terminal().width.takeIf { it > 0 } ?: defaultTerminalLength
  val logoWidth = (logoLines.lastOrNull()?.lengthOnTerminal() ?: 0)
  val reasonablePlaceForInfo = 28
  if (logoWidth + reasonablePlaceForInfo > terminalWidth) {
    println(qodanaInfo.joinToString(System.lineSeparator()))
  }

  qodanaInfo = qodanaInfo.map { it.chunked(terminalWidth - logoWidth - 1) }.flatten()

  printLogoAndInfo(logoLines, qodanaInfo)
}

private fun warnDeprecated(productName: String) {
  if (productName == "Qodana Deprecated common") {
    println("""
        The Docker image 'jetbrains/qodana' support will be stopped on November, 19th. 
        New Qodana images can check your Java, Kotlin, Python, PHP, and JavaScript code!
        Please upgrade to the latest images for technologies used in your project""".trimIndent())
  }
}

private fun printLogoAndInfo(logoLines: List<String>, qodanaInfo: List<String>) {
  var alignedLogoLines: List<String> = logoLines
  var alignedQodanaInfo: List<String> = qodanaInfo
  val dif = abs(logoLines.size - (qodanaInfo.size))

  if (logoLines.size < qodanaInfo.size) {
    val emptyLine = " ".repeat(logoLines.first().length)
    alignedLogoLines = List(dif / 2) { emptyLine } + logoLines + List(dif / 2 + dif % 2) { emptyLine }
  }
  else if (qodanaInfo.size < logoLines.size) {
    val emptyLine = ""
    alignedQodanaInfo = List(dif / 2) { emptyLine } + qodanaInfo + List(dif / 2 + dif % 2) { emptyLine }
  }

  println(alignedLogoLines.zip(alignedQodanaInfo).joinToString(separator = System.lineSeparator()) { it.first + it.second })
}