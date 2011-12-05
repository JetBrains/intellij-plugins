package com.jetbrains.actionscript.profiler.model;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.actionscript.profiler.sampler.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * User: Maxim
 * Date: 04.09.2010
 * Time: 23:38:08
 */
public class ProfilingConnection {
  private static final Logger LOG = Logger.getInstance(ProfilingConnection.class.getName());
  private ServerSocket myServerSocket;
  private ServerSocket myPolicyServerSocket;
  private OutputStream myOutputStream;
  private DataInputStream myInputStream;
  private PacketProcessor myCurrentPacketProcessor;
  private final Map<String, PacketProcessor> myInitialString2ProcessorsMap = new HashMap<String, PacketProcessor>();
  private final Callback myIoHandler;
  private final int myPort;
  private static int ourAgentVersion = 3;
  private boolean myAbortingSocketConnection;
  private boolean myDisposed;

  public ProfilingConnection(int port, ProfilerDataConsumer sampleProcessor, Callback ioHandler) {
    myPort = port;
    myInitialString2ProcessorsMap.put(
      PolicyFileRequestProcessor.POLICY_FILE_REQUEST,
      new PolicyFileRequestProcessor(port)
    );

    BatchSamplesProcessor samplesProcessor = new BatchSamplesProcessor(sampleProcessor);
    myInitialString2ProcessorsMap.put(
      BatchSamplesProcessor.BATCH_MARKER,
      samplesProcessor
    );

    myInitialString2ProcessorsMap.put(
      BatchSamplesProcessor.CREATE_OBJECT_SAMPLE_MARKER,
      samplesProcessor
    );

    myInitialString2ProcessorsMap.put(
      BatchSamplesProcessor.SAMPLE_MARKER,
      samplesProcessor
    );

    myInitialString2ProcessorsMap.put(
      BatchSamplesProcessor.DELETE_OBJECT_SAMPLE_MARKER,
      samplesProcessor
    );

    myInitialString2ProcessorsMap.put(
      FinishCommandProcessor.END_COMMAND_MARKER,
      new FinishCommandProcessor()
    );

    myInitialString2ProcessorsMap.put(
      VersionHandShakeProcessor.VERSION_COMMAND_MARKER,
      new VersionHandShakeProcessor()
    );

    myInitialString2ProcessorsMap.put(
      SampleInfoProcessor.COMMAND_MARKER,
      new SampleInfoProcessor(sampleProcessor)
    );

    myIoHandler = ioHandler;
  }

  void connect() {
    myAbortingSocketConnection = false;
    myDisposed = false;
    ensurePolicyServedEvenOnFlashSecurityPort();
    try {
      myServerSocket = new ServerSocket(myPort);
      Socket socket = myServerSocket.accept();
      myInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
      myOutputStream = socket.getOutputStream();
      myServerSocket.close();
      myServerSocket = null;
      myIoHandler.finished("Connection established", null);
    } catch (IOException ex) {
      final boolean abortedWaitingForConnection = ex instanceof SocketException && myAbortingSocketConnection;
      if (abortedWaitingForConnection) {
        ex = new EOFException("aborted wait for connection");
      } else {
        LOG.warn(ex);
      }
      myIoHandler.finished(null, ex);
      return;
    }

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        ByteArrayOutputStream out = LOG.isDebugEnabled() ? new ByteArrayOutputStream():null;
        int bytesRead = 0;
        try {
          while(true) {
            String x = myInputStream.readUTF();
            if (x == null) break;
            if (out != null) out.write(x.getBytes());
            bytesRead += x.length();
            try {
              if (myCurrentPacketProcessor == null) {
                String marker = x;
                int i = x.indexOf('\0');
                if (i != -1) marker = x.substring(0, i + 1);
                myCurrentPacketProcessor = myInitialString2ProcessorsMap.get(marker);
                if (myCurrentPacketProcessor != null) {
                  myCurrentPacketProcessor.startingPacket(x);
                }
              }
              if (myCurrentPacketProcessor != null) {
                PacketProcessor.ProcessingResult processingResult = myCurrentPacketProcessor.process(x);
                if (processingResult == PacketProcessor.ProcessingResult.FINISHED) myCurrentPacketProcessor = null;
                if (processingResult == PacketProcessor.ProcessingResult.STOP) return;
              } else {
                LOG.warn("No processing:" + x);
              }
            } catch (Exception e) {
              LOG.error(e);
            }
          }
        } catch (IOException ex) {
          LOG.debug("Bytes read:" + bytesRead);
          myIoHandler.finished(null, ex);
          if (ex instanceof EOFException) {
            if (out != null) {
              try {
                final FileOutputStream fileOutputStream = new FileOutputStream("c:\\trace");
                fileOutputStream.write(out.toByteArray());
                fileOutputStream.close();
              } catch (IOException e) {
                LOG.warn(e);
              }
            }
            return;
          }
          LOG.error(ex);
        } catch (Throwable t) {
          LOG.error(t);
        }
      }
    });
  }

  private void ensurePolicyServedEvenOnFlashSecurityPort() {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        try {
          myPolicyServerSocket = new ServerSocket(843);
          while(true) {
            final Socket socket = myPolicyServerSocket.accept();
            final OutputStream outputStream = socket.getOutputStream();
            outputStream.write(policyFileRequestAnswer(myPort).getBytes());
            LOG.debug("policy served from 843");
            outputStream.close();
          }
        } catch (IOException e) {
          if(e instanceof SocketException && myAbortingSocketConnection) return;
          if (myPolicyServerSocket != null) LOG.error(e); // myPolicyServerSocket == null is bind failed
        }
      }
    });
  }

  private static final int START_CPU_PROFILING = 1;
  private static final int STOP_CPU_PROFILING = 2;
  private static final int CAPTURE_MEMORY_SNAPSHOT = 3;
  private static final int DO_GC = 4;

  private void clearProfilingState() {
    final PacketProcessor processor = myInitialString2ProcessorsMap.get(BatchSamplesProcessor.BATCH_MARKER);
    ((BatchSamplesProcessor)processor).clearProfilingState();
  }

  interface Callback {
    void finished(@Nullable String data, IOException ex);
  }

  final LinkedList<Callback> callbacks = new LinkedList<Callback>();

  public void stopCpuProfiling(final Callback callback) throws IOException {
    simpleCommand(new Callback() {
      public void finished(String data, IOException ex) {
        callback.finished(data, ex);
        clearProfilingState();
      }
    }, STOP_CPU_PROFILING);
  }

  public void startCpuProfiling(Callback callback) throws IOException {
    simpleCommand(callback, START_CPU_PROFILING);
  }

  public void captureMemorySnapshot(Callback callback) throws IOException {
    simpleCommand(callback, CAPTURE_MEMORY_SNAPSHOT);
  }

  private void simpleCommand(Callback callback, int startCpuProfiling) throws IOException {
    synchronized (myOutputStream) {
      callbacks.addLast(callback);
      myOutputStream.write(startCpuProfiling);
      myOutputStream.flush();
    }
  }

  public void doGc(Callback callback) throws IOException {
    simpleCommand(callback, DO_GC);
  }

  public void dispose() throws IOException {
    if (myDisposed) return;
    myAbortingSocketConnection = true;
    myDisposed = true;
    if (myServerSocket != null) {
      myServerSocket.close();
    }
    if (myPolicyServerSocket != null) myPolicyServerSocket.close();
  }

  abstract static class PacketProcessor {
    enum ProcessingResult {
      CONTINUE, FINISHED, STOP
    }

    void startingPacket(String output) {}
    abstract ProcessingResult process(String output) throws IOException;
  }

  class PolicyFileRequestProcessor extends PacketProcessor {
    static final String POLICY_FILE_REQUEST = "<policy-file-request/>\0";
    private int myPort;

    PolicyFileRequestProcessor(int port) {
      myPort = port;
    }

    @Override
    ProcessingResult process(String output) throws IOException {
      String s = policyFileRequestAnswer(myPort);
      synchronized (myOutputStream) {
        LOG.debug("policy served");     // TODO merge with FlexUnit code
        myOutputStream.write(s.getBytes());
        myOutputStream.flush();
        connect();
        return ProcessingResult.STOP;
      }
    }
  }

  private static String policyFileRequestAnswer(int port) {
    return "<?xml version=\"1.0\"?> \n" +
      "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">\n" +
      "<cross-domain-policy>\n" +
      "    <allow-access-from domain=\"*\" to-ports=\"" + port + "\" />\n" +
      "</cross-domain-policy>\0";
  }

  static class BatchSamplesProcessor extends PacketProcessor {
    private static final String BATCH_MARKER = "b\0";
    private static final String SAMPLE_MARKER = "s\0";
    private static final String CREATE_OBJECT_SAMPLE_MARKER = "c\0";
    private static final String DELETE_OBJECT_SAMPLE_MARKER = "d\0";

    private final ProfilerDataConsumer mySampleProcessor;

    private long sampleDuration = -1;
    private int frameIndex;

    private final Map<String,String> dictionary = new HashMap<String, String>(1000);
    private final Map<String,String> typeDictionary = new HashMap<String, String>(1000);
    private FrameInfo[] frames;
    private String type;
    private String specialArgs;
    static final int INDEX = SAMPLE_MARKER.length();
    private int cpuSamples;
    private int memorySamples;
    private Sample lastCpuSample;
    private Sample lastCreateObjectSample;
    private final FrameInfoBuilder frameInfoBuilder = new FrameInfoBuilder();

    public BatchSamplesProcessor(ProfilerDataConsumer sampleProcessor) {
      this.mySampleProcessor = sampleProcessor;
    }

    @Override
    ProcessingResult process(String output) throws IOException {
      if (frameIndex == -1) {
        if (output.startsWith(BATCH_MARKER)) return ProcessingResult.FINISHED;

        int i = INDEX + 1; //output.indexOf(' ', INDEX);
        final boolean cpuSample = output.startsWith(SAMPLE_MARKER);

        if (cpuSample) {
          i = output.indexOf(' ', INDEX);
          sampleDuration = Long.parseLong(output.substring(INDEX, i));
          i+=2;
        }

        if (cpuSample ||
            output.startsWith(CREATE_OBJECT_SAMPLE_MARKER) ||
            output.startsWith(DELETE_OBJECT_SAMPLE_MARKER)) {
          int i2 = output.indexOf(' ', i);
          int frameCount = Integer.parseInt(output.substring(i - 1, i2 != -1 ? i2:output.length()));
          frames = frameCount > 0 ? new FrameInfo[frameCount]: FrameInfo.EMPTY_FRAME_INFO_ARRAY;
          frameIndex = 0;
          type = output;
          specialArgs = i2 != -1 ? output.substring(i2 + 1):"";

          return maybeFinishSample();
        }
      }

      if (frames != null && frameIndex >= 0 && frameIndex < frames.length) {

        char ch = output.charAt(0);
        if (output.startsWith("u>:")) {
          int count = Integer.parseInt(output.substring(output.indexOf(':') + 1));
          Sample s = type.startsWith(CREATE_OBJECT_SAMPLE_MARKER) ? lastCreateObjectSample:type.startsWith(SAMPLE_MARKER) ? lastCpuSample:null;
          for(int i = s.frames.length - count; i < s.frames.length; ++i) {
            frames[frameIndex++] = s.frames[i];
          }
        } else {
          if (Character.isDigit(ch)) {
            output = dictionary.get(output);
          } else {
            dictionary.put("" + (dictionary.size() + 1), output);
          }

          frames[frameIndex++] = frameInfoBuilder.buildInstance(output);
        }
        return maybeFinishSample();
      }

      LOG.warn("Unexpected:" + output);

      return ProcessingResult.FINISHED;
    }

    private ProcessingResult maybeFinishSample() {
      if (frameIndex == frames.length) {
        Sample sample;
        if (type.startsWith(CREATE_OBJECT_SAMPLE_MARKER)) {
          ++memorySamples;
          final int endIndex = specialArgs.indexOf(' ');
          final int endIndex2 = specialArgs.indexOf(' ', endIndex + 1);

          final int id = Integer.parseInt(specialArgs.substring(0, endIndex));
          String className = specialArgs.substring(endIndex + 1, endIndex2);
          className = getClassName(className);
          final int size = Integer.parseInt(specialArgs.substring(endIndex2 + 1));

          sample = new CreateObjectSample(
            sampleDuration,
            frames,
            id,
            className,
            size
          );
          lastCreateObjectSample = sample;
        } else if (type.startsWith(DELETE_OBJECT_SAMPLE_MARKER)) {
          ++memorySamples;
          final int endIndex = specialArgs.indexOf(' ');
          int endIndex2 = specialArgs.indexOf(' ', endIndex + 1);
          if (endIndex2 == -1) endIndex2 = specialArgs.length();
          final int id = Integer.parseInt(specialArgs.substring(0, endIndex));
          final int size = Integer.parseInt(specialArgs.substring(endIndex + 1, endIndex2));
          String type = endIndex2 != specialArgs.length() ? specialArgs.substring(endIndex2 + 1):null;
          if (type != null) {
            type = getClassName(type);
            mySampleProcessor.process(new CreateObjectSample(sampleDuration, FrameInfo.EMPTY_FRAME_INFO_ARRAY, id, type, -size));
          } else {
            mySampleProcessor.process(new DeleteObjectSample(sampleDuration, frames, id, size));
          }
          return ProcessingResult.FINISHED;
        } else {
          ++cpuSamples;
          sample = new Sample(sampleDuration, frames);
          lastCpuSample = sample;
        }
        mySampleProcessor.process(sample);
        frameIndex = -1;
        return ProcessingResult.FINISHED;
      } else {
        return ProcessingResult.CONTINUE;
      }
    }

    private String getClassName(String className) {
      if (Character.isDigit(className.charAt(0))) {
        className = typeDictionary.get(className);
      } else {
        typeDictionary.put(String.valueOf(typeDictionary.size()), className);
      }
      return className;
    }

    void startingPacket(String output) {
      if (output.startsWith(BATCH_MARKER)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(output + "," + System.currentTimeMillis() + "," + cpuSamples + "," + memorySamples);
        }
        memorySamples = 0;
        cpuSamples = 0;        
      }

      frameIndex = -1;
      frames = null;
      type = null;
      specialArgs = null;
    }

    private void clearProfilingState() {
      dictionary.clear();
      typeDictionary.clear();
      lastCpuSample = null;
      lastCreateObjectSample = null;
      cpuSamples = 0;
      memorySamples = 0;
    }
  }

  class FinishCommandProcessor extends PacketProcessor {
    static final String END_COMMAND_MARKER = "e\0";

    @Override
    ProcessingResult process(String output) throws IOException {
      Callback callback;
      synchronized (myOutputStream) {
        callback = callbacks.removeFirst();
      }
      callback.finished(output, null);
      return ProcessingResult.FINISHED;
    }
  }

  class VersionHandShakeProcessor extends PacketProcessor {
    static final String VERSION_COMMAND_MARKER = "v\0";

    @Override
    ProcessingResult process(String output) throws IOException {
      if (Integer.parseInt(output.substring(output.lastIndexOf(' ') + 1)) != ourAgentVersion) {
        LOG.warn("Version mismatch");
        myIoHandler.finished(null, new AgentVersionMismatchProblem());
        myOutputStream.close();
        myInputStream.close();
      }
      return ProcessingResult.FINISHED;
    }
  }

  private static class SampleInfoProcessor extends PacketProcessor {
    public static final String COMMAND_MARKER = "si\0";
    private ProfilerDataConsumer myDataConsumer;

    SampleInfoProcessor(ProfilerDataConsumer dataConsumer) {
      myDataConsumer = dataConsumer;
    }

    @Override
    ProcessingResult process(String output) throws IOException {
      if (output.startsWith("EndSnapshot")) return ProcessingResult.FINISHED;
      if (output.startsWith(COMMAND_MARKER)) return ProcessingResult.CONTINUE;
      if (output.startsWith("cls:")) {
        return ProcessingResult.CONTINUE;
      }

      int i = output.indexOf(',');
      int id = Integer.parseInt(output.substring(0, i));

      while( i != -1) {
        int nextI = output.indexOf(',', i + 1);
        if (nextI == -1) nextI = output.length();
        int nextId = Integer.parseInt(output.substring(i + 1, nextI));
        myDataConsumer.referenced(id, nextId);
        if (nextI == output.length()) break;
        i = nextI;
      }
      return ProcessingResult.CONTINUE;
    }
  }
}
