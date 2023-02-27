// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import java.util.regex.Pattern

// Based on Terraform sources
object RegistryModuleUtil {
  private val KnownGitHosts = setOf("github.com", "bitbucket.org")

  fun parseRegistryModule(source: String): RegistryModule? {
    val (host, rest) = parseFriendlyHost(source)
    if (host != null) {
      if (host.toLowerCase() in KnownGitHosts) return null
      if (!isValidHost(host)) return null
    }

    val m = ModuleSourceRE.matcher(rest)
    if (!m.find()) return null

    return RegistryModule(host, m.group(1), m.group(2), m.group(3), m.group(4))
  }

  private fun isValidHost(given: String): Boolean {
    val split = given.split(':', limit = 2)
    val host = split[0]
    if (split.size == 2) {
      val port = split[1].toIntOrNull() ?: return false
      if (port > 65535) return false
    }
    if (host.isEmpty()) return false

    val parts = host.split('.')
    for (part in parts) {
      if (part.isEmpty()) return false
      if (part.startsWith("xn--")) return false
    }
    try {
      java.net.IDN.toASCII(host)
    } catch (e: Exception) {
      return false
    }
    return true
  }

  private fun parseFriendlyHost(source: String): Pair<String?, String> {
    val parts = source.split('/', limit = 2)
    if (parts.isEmpty()) return null to ""
    val host = parts[0]
    return if (isValidUrlHost(host)) {
      host to (parts.getOrNull(1) ?: "")
    } else {
      null to source
    }
  }

  private fun isValidUrlHost(host: String): Boolean {
    val indexOfColon = host.indexOf(':')
    if (indexOfColon != -1) {
      if (host.lastIndexOf(':') != indexOfColon) {
        // more than one colon
        return false
      }
      if (indexOfColon == host.length - 1) {
        // ends with colon
        return false
      }
      val port = host.substring(indexOfColon + 1).toIntOrNull() ?: return false
      if (port > 65535) return false
      return isValidUrlHost(host.substring(0, indexOfColon))
    }
    val parts = host.split('.')
    if (parts.size < 2) return false
    return parts.all { isValidUrlHostPart(it) }
  }

  private inline fun isValidUrlHostPart(part: String): Boolean {
    val length = part.length
    if (length == 0 || length > 63) return false
    val matcher = HostLabelSubRe.matcher(part)
    return matcher.find()
  }

  private const val nameSubRe = "[0-9A-Za-z](?:[0-9A-Za-z-_]{0,62}[0-9A-Za-z])?"
  private const val providerSubRe = "[0-9a-z]{1,64}"
  private val ModuleSourceRE = Pattern.compile("^(${nameSubRe})/(${nameSubRe})/(${providerSubRe})(?://(.*))?$")


  private const val urlLabelEndSubRe = "[0-9A-Za-z]"
  private const val urlLabelMidSubRe = "[0-9A-Za-z-]"
  private const val urlLabelUnicodeSubRe = "[^[:ascii:]]"
  private val hostLabelSubRe = "" +
      // Match valid initial char, or unicode char
      "(?:" + urlLabelEndSubRe + "|" + urlLabelUnicodeSubRe + ")" +
      // Optionally, match 0 to 61 valid URL or Unicode chars,
      // followed by one valid end char or unicode char
      "(?:" +
      "(?:" + urlLabelMidSubRe + "|" + urlLabelUnicodeSubRe + "){0,61}" +
      "(?:" + urlLabelEndSubRe + "|" + urlLabelUnicodeSubRe + ")" +
      ")?"
  private val HostLabelSubRe = Pattern.compile("^$hostLabelSubRe$", Pattern.CASE_INSENSITIVE)

  // registry/regsrc/module.go:Module
  data class RegistryModule(val host: String?, val namespace: String, val name: String, val provider: String, val submodule: String?) {
    override fun toString(): String {
      val prefix = if (host != null) "$host/" else ""
      return formatWithPrefix(prefix, true)
    }

    private fun formatWithPrefix(hostPrefix: String, preserveCase: Boolean): String {
      val suffix = if (submodule != null) "//$submodule" else ""
      val str = "$hostPrefix$namespace/$name/$provider$suffix"
      if (!preserveCase) return str.toLowerCase()
      return str
    }
  }
}