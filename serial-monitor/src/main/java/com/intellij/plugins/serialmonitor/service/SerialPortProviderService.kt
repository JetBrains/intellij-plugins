package com.intellij.plugins.serialmonitor.service

interface SerialPortProvider {
  /**
   * Return the list of available serial ports.
   * To connect to the ports, use the [createPort] method.
   */
  suspend fun scanAvailablePorts(): List<String>

  /**
   * Creates a [SerialPort] handle for a given port name.
   * @throws SerialPortException if the port is not available.
   */
  @Throws(SerialPortException::class)
  fun createPort(portName: String): SerialPort
}