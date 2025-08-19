package com.intellij.plugins.serialmonitor

import com.intellij.openapi.components.serviceAsync
import com.intellij.plugins.serialmonitor.service.SerialPort
import com.intellij.plugins.serialmonitor.service.SerialPortException
import com.intellij.plugins.serialmonitor.service.SerialPortService
import kotlinx.coroutines.flow.first
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class MockSerialPortProvider : SerialPort.SerialPortProvider {
  private val ports = AtomicReference<List<String>>(emptyList())
  private val created = mutableMapOf<String, MockSerialPort>()
  val failCreateFor = mutableSetOf<String>()

  constructor(vararg portNames: String) {
    ports.store(listOf(*portNames))
  }

  override fun scanAvailablePorts(): List<String> = ports.load()

  override fun createPort(portName: String): MockSerialPort {
    if (failCreateFor.contains(portName)) throw SerialPortException("create failed for $portName")
    return created.getOrPut(portName) { MockSerialPort(portName) }
  }

  suspend fun changePortsAndAwaitScan(action: MutableList<String>.()->Unit) {
    // Compute new ports
    val newPorts = ports.load().toMutableList()
    newPorts.action()

    // Store the new state so the scanner finds it
    ports.store(newPorts)

    // Listen until the names update to our expected state asynchronously
    // State flow also emits the current value to new collectors, then it emits changes; so unless a full update to another value which
    // propagates to the service sneaks in here, we are safe.

    serviceAsync<SerialPortService>().portNamesFlow.first {
      it == newPorts.toSet()
    }
  }
}

class MockSerialPort(private val name: String) : SerialPort {
  private var portListener: SerialPort.SerialPortListener? = null
  var connected: Boolean = false
    private set
  var lastWritten: ByteArray? = null
    private set

  var lastRTS: Boolean? = null
  var lastDTR: Boolean? = null

  var cts: Boolean = false
  var dsr: Boolean = false

  var connectedProfile: SerialPortProfile? = null

  var failOnConnect: Boolean = false
  var failOnRTS: Boolean = false
  var failOnDTR: Boolean = false
  var failOnDisconnect: Boolean = false
  var failOnGetCTS: Boolean = false
  var failOnGetDSR: Boolean = false

  override fun getSystemName(): String = name
  override fun getDescriptiveName(): String = "USB Device $name"

  override fun connect(profile: SerialPortProfile, listener: SerialPort.SerialPortListener, rts: Boolean, dtr: Boolean) {
    if (failOnConnect) throw RuntimeException("connect failed for $name")
    connected = true
    connectedProfile = profile
    portListener = listener
    lastRTS = rts
    lastDTR = dtr
  }

  override fun disconnect() {
    if (failOnDisconnect) throw SerialPortException("disconnect failed for $name")
    connected = false
  }

  override fun write(data: ByteArray): Int {
    lastWritten = data
    return data.size
  }

  override fun setRTS(value: Boolean) {
    if (failOnRTS) throw SerialPortException("RTS failure for $name")
    lastRTS = value
  }

  override fun setDTR(value: Boolean) {
    if (failOnDTR) throw SerialPortException("DTR failure for $name")
    lastDTR = value
  }

  override fun getCTS(): Boolean {
    if (failOnGetCTS) throw SerialPortException("CTS failure for $name")
    return cts
  }

  override fun getDSR(): Boolean {
    if (failOnGetDSR) throw SerialPortException("DSR failure for $name")
    return dsr
  }

  // Test helpers
  fun fireData(data: ByteArray) {
    portListener?.onDataReceived(data)
  }

  fun fireCTS(state: Boolean) {
    cts = state
    portListener?.onCTSChanged(state)
  }

  fun fireDSR(state: Boolean) {
    dsr = state
    portListener?.onDSRChanged(state)
  }
}