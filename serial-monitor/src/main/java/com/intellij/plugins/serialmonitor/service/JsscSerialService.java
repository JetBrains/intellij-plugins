package com.intellij.plugins.serialmonitor.service;

import com.google.common.base.Suppliers;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.plugins.serialmonitor.SerialMonitorException;
import com.intellij.plugins.serialmonitor.SerialPortProfile;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.util.Consumer;
import jssc.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static jssc.SerialPort.*;

/**
 * @author Dmitry_Cherkas
 */
public class JsscSerialService implements Disposable {

  // Memorizing available ports for better performance
  private static final Supplier<List<String>>
    portNamesSupplier = Suppliers.memoizeWithExpiration(JsscSerialService::doGetPortNames, 3000, TimeUnit.MILLISECONDS);

  private final ConcurrentMap<String, SerialConnection> openPorts = new ConcurrentHashMap<>();

  public boolean isPortValid(String portName) {
    List<String> availablePortNames = portNamesSupplier.get();
    return availablePortNames.contains(portName);
  }

  public boolean isConnected(String name) {
    SerialConnection connection = openPorts.get(name);
    return connection != null && connection.mySerialPort.isOpened();
  }

  @NotNull
  public static List<String> getPortNames() {
    return portNamesSupplier.get();
  }

  private static List<String> doGetPortNames() {
    return
      ProgressManager.getInstance().computeInNonCancelableSection(
        () -> Arrays.asList(SerialPortList.getPortNames())
      );
  }

  public synchronized void connect(SerialPortProfile settings,
                                   Consumer<byte[]> dataListener,
                                   SerialConnectionListener connectListener) throws SerialMonitorException {
    String portName = settings.getPortName();
    int dataBits = settings.getBits();
    int stopBits = switch (settings.getStopBits()) {
      case BITS_2 -> STOPBITS_2;
      case BITS_1_5 -> STOPBITS_1_5;
      default -> STOPBITS_1;
    };
    int parity = switch (settings.getParity()) {
      case EVEN -> PARITY_EVEN;
      case ODD -> PARITY_ODD;
      default -> PARITY_NONE;
    };
    try {
      SerialPort port = new SerialPort(portName);
      port.openPort();
      boolean res = port.setParams(settings.getBaudRate(), dataBits, stopBits, parity, true, true);
      if (!res) {
        throw new SerialMonitorException(SerialMonitorBundle.message("serial.port.parameters.wrong"));
      }
      port.addEventListener(new MySerialPortEventListener(port, dataListener, connectListener), MASK_ERR | MASK_RXCHAR);
      openPorts.put(portName, new SerialConnection(port, connectListener));
    }
    catch (SerialPortException e) {
      SerialPort port = e.getPort();
      if (port != null &&
          port.getPortName().startsWith("/dev") &&
          SerialPortException.TYPE_PERMISSION_DENIED.equals(e.getExceptionType())) {
        throw new SerialMonitorException(SerialMonitorBundle.message("serial.port.permissions.denied", portName));
      }
      else {
        throw new SerialMonitorException(SerialMonitorBundle.message("serial.port.open.error", portName, e.getExceptionType()));
      }
    }
  }

  public synchronized void close(String portName) throws SerialMonitorException {
    SerialConnection serialConnection = openPorts.remove(portName);
    if (serialConnection != null) {
      try {
        if (serialConnection.mySerialPort.isOpened()) {
          serialConnection.mySerialPort.removeEventListener();
          serialConnection.mySerialPort.closePort();  // close the port
        }
        serialConnection.myListener.updateStatus(SerialConnectionListener.PortStatus.DISCONNECTED);
      }
      catch (SerialPortException e) {
        throw new SerialMonitorException(e.getMessage(), e);
      }
    }
  }

  public void write(@NotNull String portName, byte[] bytes) throws SerialMonitorException {
    try {
      SerialConnection connection = openPorts.get(portName);
      if (connection != null) {
        connection.mySerialPort.writeBytes(bytes);
      }
    }
    catch (SerialPortException e) {
      throw new SerialMonitorException(e.getMessage(), e);
    }
  }


  @Override
  public void dispose() {
    openPorts.values().forEach(connection -> {
      try {
        connection.mySerialPort.closePort();
      }
      catch (SerialPortException e) {
        Logger.getInstance(JsscSerialService.class).debug(e);
      }
    });
  }

  private static class MySerialPortEventListener implements SerialPortEventListener {
    private final SerialPort myPort;
    private final Consumer<byte[]> myDataListener;
    private final SerialConnectionListener myConnectionListener;

    private MySerialPortEventListener(SerialPort port,
                                      Consumer<byte[]> dataListener,
                                      SerialConnectionListener connectionListener) {
      myPort = port;
      myDataListener = dataListener;
      myConnectionListener = connectionListener;
    }

    @Override
    public synchronized void serialEvent(SerialPortEvent serialEvent) {
      try {
        byte[] buf = myPort.readBytes(serialEvent.getEventValue());
        if (buf.length > 0) {
          myDataListener.consume(buf);
        }
      }
      catch (SerialPortException e) {
        myConnectionListener.updateStatus(SerialConnectionListener.PortStatus.FAILURE);
      }
    }
  }

  public static @NotNull JsscSerialService getInstance() {
    return ApplicationManager.getApplication().getService(JsscSerialService.class);
  }

  private static class SerialConnection {
    private final SerialPort mySerialPort;
    private final SerialConnectionListener myListener;

    private SerialConnection(SerialPort port, SerialConnectionListener listener) {
      mySerialPort = port;
      myListener = listener;
    }
  }
}
