package training.lesson;

/**
 * Created by jetbrains on 31/01/16.
 */


public class MyPair{
    private String status;
    private String date;

    public MyPair() {
        status = "";
        date = "";
    }

    public MyPair(String status, String date) {
        this.status = status;
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
