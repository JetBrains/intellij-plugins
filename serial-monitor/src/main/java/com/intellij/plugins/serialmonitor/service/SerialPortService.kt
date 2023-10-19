package com.intellij.plugins.serialmonitor.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.Parity
import com.intellij.plugins.serialmonitor.SerialMonitorConnectCollector
import com.intellij.plugins.serialmonitor.SerialMonitorException
import com.intellij.plugins.serialmonitor.StopBits
import com.intellij.plugins.serialmonitor.service.SerialPortsListener.Companion.SERIAL_PORTS_TOPIC
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.intellij.util.ConcurrencyUtil
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import jssc.SerialPort
import jssc.SerialPort.*
import jssc.SerialPortEventListener
import jssc.SerialPortException
import jssc.SerialPortList
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.Comparator

@Service
class SerialPortService : Disposable {

  private val portWatcher: ScheduledExecutorService = ConcurrencyUtil.newSingleScheduledThreadExecutor("Serial Port Watcher")
    .apply { scheduleWithFixedDelay({ rescanPorts() }, 0, 500, TimeUnit.MILLISECONDS) }

  @Volatile
  private var portNames: Set<String> = emptySet()
  private val connections: MutableMap<String, SerialConnection> = ConcurrentHashMap()

  private val NAME_COMPARATOR = object : Comparator<String> {

    private fun splitName(s: String): Pair<String, Int> {
      var digitIdx = s.length - 1
      var num = 0
      var mul = 1
      while (digitIdx >= 0 && s[digitIdx].isDigit()) {
        num += mul * (s[digitIdx] - '0')
        mul *= 10
        digitIdx--
      }
      return s.substring(0, digitIdx + 1) to num
    }

    override fun compare(name1: String, name2: String): Int {
      val (base1, idx1) = splitName(name1)
      val (base2, idx2) = splitName(name2)
      val result = String.CASE_INSENSITIVE_ORDER.compare(base1, base2)
      return if (result == 0) idx1 - idx2 else result
    }
  }

  private fun rescanPorts() {
    val portList = TreeSet(NAME_COMPARATOR).apply { addAll(SerialPortList.getPortNames()) }
    var changeDetected = portList != portNames
    if (changeDetected) {
      for (name in portNames) {
        if (!portList.contains(name)) {
          //port disappeared
          connections[name]?.closeSilently(false)
        }
      }
    }
    for (name in portList) {
      if (connections[name]?.getStatus() == PortStatus.UNAVAILABLE_DISCONNECTED) {
        connections[name]?.setStatus(PortStatus.DISCONNECTED)
        changeDetected = true
      }
    }
    portNames = portList
    if (changeDetected) {
      portMessageTopic().portsStatusChanged()
    }
  }

  private fun portMessageTopic(): SerialPortsListener =
    ApplicationManager.getApplication().messageBus.syncPublisher(SERIAL_PORTS_TOPIC)

  fun getPortsNames(): Set<String> = portNames

  fun newConnection(portName: String): SerialConnection? {
    if (connections.containsKey(portName)) return null
    val serialConnection = SerialConnection(portName)
    connections[portName] = serialConnection
    return serialConnection
  }

  fun portStatus(name: String?): PortStatus {
    if (!portNames.contains(name)) {
      return if (connections.containsKey(name)) PortStatus.UNAVAILABLE_DISCONNECTED else PortStatus.UNAVAILABLE
    }
    return connections[name]?.getStatus() ?: PortStatus.READY
  }

  inner class SerialConnection(val portName: @NlsSafe String) : Disposable {

    var dataListener: Consumer<ByteArray>? = null

    @Volatile
    private var port: SerialPort? = null

    @Volatile
    private var status: PortStatus = PortStatus.DISCONNECTED

    private var localEcho: Boolean = false

    override fun dispose() {
      closeSilently(true)
      connections.remove(portName, this)
      application.executeOnPooledThread(::rescanPorts)
    }

    fun getStatus(): PortStatus = status
    internal fun setStatus(value: PortStatus) {
      status = value
    }

    @Throws(SerialMonitorException::class)
    fun close(portAvailable: Boolean) {
      try {
        port?.closePort()
      }
      catch (e: SerialPortException) {
        throw SerialMonitorException(e.localizedMessage)
      }
      catch (e: Throwable) {
        throw SerialMonitorException(portName, e)
      }
      finally {
        status = if (portAvailable) PortStatus.DISCONNECTED else PortStatus.UNAVAILABLE_DISCONNECTED
        port = null
        portMessageTopic().portsStatusChanged()
      }
    }

    fun closeSilently(portAvailable: Boolean) {
      try {
        close(portAvailable)
      }
      catch (_: Throwable) {
      }
    }

    private val listener: SerialPortEventListener = SerialPortEventListener { event ->
      if (event.eventType and MASK_RXCHAR != 0) {
        val readBytes = port?.readBytes()
        if (readBytes?.isNotEmpty() == true) {
          dataListener?.accept(readBytes)
        }
      }
    }

    @RequiresBackgroundThread
    @Throws(SerialMonitorException::class)
    fun connect(baudRate: Int, bits: Int, stopBits: StopBits, parity: Parity, localEcho: Boolean) {
      this.status = PortStatus.CONNECTING
      this.localEcho = localEcho
      val newPort = SerialPort(portName)
      portMessageTopic().portsStatusChanged()
      try {
        val portStopBits = when (stopBits) {
          StopBits.BITS_2 -> STOPBITS_2
          StopBits.BITS_1_5 -> STOPBITS_1_5
          else -> STOPBITS_1
        }
        val portParity = when (parity) {
          Parity.EVEN -> PARITY_EVEN
          Parity.ODD -> PARITY_ODD
          else -> PARITY_NONE
        }
        newPort.openPort()
        newPort.addEventListener(listener, MASK_RXCHAR or MASK_ERR)

        if (!newPort.setParams(baudRate, bits, portStopBits, portParity)) {
          SerialMonitorConnectCollector.logConnect(baudRate, false)
          throw SerialMonitorException(SerialMonitorBundle.message("serial.port.parameters.wrong"))
        }
        newPort.setFlowControlMode(FLOWCONTROL_NONE)
        newPort.setRTS(false)
        port = newPort
        SerialMonitorConnectCollector.logConnect(baudRate, true)
        status = PortStatus.CONNECTED
        portMessageTopic().portsStatusChanged()
      }
      catch (e: Throwable) {
        SerialMonitorConnectCollector.logConnect(baudRate, false)
        try {
          newPort.closePort()
        }
        catch (_: Throwable) {
        }
        status = PortStatus.UNAVAILABLE_DISCONNECTED
        portMessageTopic().portsStatusChanged()
        if (e is SerialPortException) {
          throw SerialMonitorException(e.localizedMessage)
        }
        else {
          throw SerialMonitorException(newPort.portName, e)
        }
      }
    }

    fun write(data: ByteArray) {
      port?.writeBytes(data)
      if (localEcho) {
        dataListener?.accept(data)
      }
    }
  }

  override fun dispose() {
    portWatcher.shutdown()
  }

}

