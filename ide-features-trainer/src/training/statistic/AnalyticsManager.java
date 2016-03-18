package training.statistic;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by jetbrains on 02/02/16.
 */
public class AnalyticsManager {

    public static AnalyticsManager INSTANCE = new AnalyticsManager();

    public static AnalyticsManager getInstance() {
        return INSTANCE;
    }

    public AnalyticsManager() {

    }

    public void getUid() throws IOException {
        // wmic command for diskdrive id: wmic DISKDRIVE GET SerialNumber
        // wmic command for cpu id : wmic cpu get ProcessorId
        Process process = Runtime.getRuntime().exec(new String[]{"wmic", "bios", "get", "serialnumber"});
        process.getOutputStream().close();
        Scanner sc = new Scanner(process.getInputStream());
        String property = sc.next();
        String serial = sc.next();
        System.out.println(property + ": " + serial);

    }

    public static void main(String[] args) {
        try {
            INSTANCE.getUid();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
