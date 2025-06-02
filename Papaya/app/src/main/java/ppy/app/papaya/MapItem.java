package ppy.app.papaya;

public class MapItem {
    private String mapImg;
    private String mapName;
    private String mapTel;
    private String mapAddress;

    public MapItem() {}  // Firebase 用的空建構子

    public MapItem(String mapImg, String mapName, String mapTel, String mapAddress) {
        this.mapImg = mapImg;
        this.mapName = mapName;
        this.mapTel = mapTel;
        this.mapAddress = mapAddress;
    }

    public String getMapImg() { return mapImg; }
    public String getMapName() { return mapName; }
    public String getMapTel() { return mapTel; }
    public String getMapAddress() { return mapAddress; }
}
