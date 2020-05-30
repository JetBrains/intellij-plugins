package com.jetbrains.cidr.cpp.diagnostics

import java.text.SimpleDateFormat
import java.util.*

private val DATE_FORMAT_SECONDS = SimpleDateFormat("yyyyMMdd-HHmmss")
private val DATE_FORMAT_MS = SimpleDateFormat("yyyyMMdd-HHmmss.SSS")

fun formatCurrentTime(): String {
  return DATE_FORMAT_SECONDS.format(Date())
}

fun formatCurrentTimeMS(): String {
  return DATE_FORMAT_MS.format(Date())
}
