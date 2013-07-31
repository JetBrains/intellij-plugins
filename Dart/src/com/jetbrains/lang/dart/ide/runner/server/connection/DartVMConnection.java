package com.jetbrains.lang.dart.ide.runner.server.connection;

import com.google.gson.JsonObject;
import com.intellij.util.io.socketConnection.*;
import com.intellij.util.net.NetUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class DartVMConnection {
  private ClientSocketConnection<JSONCommand, JsonResponse> myConnection;
  private int lastCommandId = 0;

  public void connect(int debuggingPort) {
    myConnection = SocketConnectionFactory.createConnection(
      NetUtils.getLoopbackAddress(),
      debuggingPort,
      1,
      new RequestResponseExternalizerFactory<JSONCommand, JsonResponse>() {
        @NotNull
        @Override
        public RequestWriter<JSONCommand> createRequestWriter(@NotNull OutputStream output)
          throws IOException {
          return new JSONRequestWriter(new OutputStreamWriter(output, "UTF-8"));
        }

        @NotNull
        @Override
        public ResponseReader<JsonResponse> createResponseReader(@NotNull InputStream input)
          throws IOException {
          return new JSONResponseReader(new BufferedReader(new InputStreamReader(input, "UTF-8")));
        }
      });
  }

  public synchronized void sendCommand(JsonObject command, @Nullable AbstractResponseToRequestHandler<JsonResponse> responseHandler) {
    myConnection.sendRequest(new JSONCommand(++lastCommandId, command), responseHandler);
  }

  public void close() {
    myConnection.close();
  }

  public void open() throws IOException {
    myConnection.open();
  }


  public void registerHandler(AbstractResponseHandler<JsonResponse> debugProcess) {
    myConnection.registerHandler(JsonResponse.class, debugProcess);
  }
}
