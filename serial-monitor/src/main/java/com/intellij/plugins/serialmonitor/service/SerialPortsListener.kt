package com.intellij.plugins.serialmonitor.service

import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.messages.Topic


@FunctionalInterface
interface SerialPortsListener {

  @RequiresBackgroundThread(generateAssertion = false /* IJPL-115548 */)
  fun portsStatusChanged()

  companion object {
    @JvmStatic
    val SERIAL_PORTS_TOPIC: Topic<SerialPortsListener> = Topic.create("Serial Monitor Ports", SerialPortsListener::class.java)

  }
}
