package ppy.app.papaya;

public class Alarm {
    private String alarmsPhoto;
    private String alarmsType;
    private String alarmsInfo;

    public Alarm() {}  // Firestore 需要空建構子

    public Alarm(String alarmsPhoto, String alarmsType, String alarmsInfo) {
        this.alarmsPhoto = alarmsPhoto;
        this.alarmsType = alarmsType;
        this.alarmsInfo = alarmsInfo;
    }

    public String getAlarmsPhoto() {
        return alarmsPhoto;
    }

    public String getAlarmsType() {
        return alarmsType;
    }

    public String getAlarmsInfo() {
        return alarmsInfo;
    }
}
