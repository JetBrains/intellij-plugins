import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandLineShell {

  public static void main(String[] args) throws IOException, InterruptedException {
    final Socket socket = new ServerSocket(1980).accept();
    final DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

    new Thread(new Runnable() {
      public void run() {
        try {
          final BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
          String s;
          int num = 1;
          while ((s = r.readLine()) != null) {
            s += "\n";
            if (s.equals("Finish")) {
              dos.close();
              break;
            }
            dos.writeUTF(String.valueOf(num++) + ":" + s);
          }
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();

    final DataInputStream dis = new DataInputStream(socket.getInputStream());
    try {
      while (true) {
        System.out.println(dis.readUTF());
      }
    }
    catch (IOException e) {
      System.err.println(e.toString());
    }

    try {
      socket.close();
    }
    catch (IOException ignored) {
    }
  }
}

