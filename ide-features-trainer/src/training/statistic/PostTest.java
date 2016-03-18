package training.statistic;

/**
 * Created by jetbrains on 01/02/16.
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.io.DataOutputStream;
import java.io.DataInputStream;

public class PostTest {

    public static void main(String[] args) throws UnsupportedEncodingException {
        final String server = "google-analytics.com";

        URL url = null;
        try {
            url = new URL("http://" + server + "/collect");

            HttpURLConnection urlConn = null;
            try {
                // URL connection channel.
                urlConn = (HttpURLConnection) url.openConnection();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Let the run-time system (RTS) know that we want input.
            urlConn.setDoInput(true);

            // Let the RTS know that we want to do output.
            urlConn.setDoOutput(true);

            // No caching, we want the real thing.
            urlConn.setUseCaches(false);

            try {
                urlConn.setRequestMethod("POST");
            } catch (ProtocolException ex) {
                ex.printStackTrace();
            }

            try {
                urlConn.connect();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            DataOutputStream output = null;
            DataInputStream input = null;

            try {
                output = new DataOutputStream(urlConn.getOutputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Specify the content type if needed.
            //urlConn.setRequestProperty("Content-Type",
            //  "application/x-www-form-urlencoded");

            // Construct the POST data.
            String content =
                    "v=" + URLEncoder.encode("1", "UTF-8") +
                            "&tid=" + URLEncoder.encode("UA-73167019-1", "UTF-8") +
                            "&cid=" + URLEncoder.encode("661", "UTF-8") +
                            "&geoid=" + URLEncoder.encode("DE", "UTF-8") +

                            "&t=" + URLEncoder.encode("screenview", "UTF-8") +
                            "&an=" + URLEncoder.encode("training plugin", "UTF-8") +
                            "&cd=" + URLEncoder.encode("lesson.module", "UTF-8");

            // Send the request data.
            try {
                output.writeBytes(content);
                output.flush();
                output.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Get response data.
            String str = null;
            try {
                input = new DataInputStream(urlConn.getInputStream());
                while (null != ((str = input.readLine()))) {
                    System.out.println(str);
                }
                input.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}