package ppy.app.papaya;

public class CouponInfo {
    private String couponId;
    private String couponName;
    private String couponInfo;
    private String couponPhoto;
    private String couponType;

    public CouponInfo(String name, String info, String photo, String couponType, String couponId) {
        this.couponName = name;
        this.couponInfo = info;
        this.couponPhoto = photo;
        this.couponType = couponType;
        this.couponId = couponId;
    }
    public CouponInfo(String couponName, String couponInfo, String couponPhoto, String couponType) {
        this.couponId = couponId;
        this.couponName = couponName;
        this.couponInfo = couponInfo;
        this.couponPhoto = couponPhoto;
        this.couponType = couponType;
    }

    public String getCouponId() {return  couponId;}

    public String getCouponName() {
        return couponName;
    }

    public String getCouponInfo() {
        return couponInfo;
    }

    public String getCouponPhoto() {
        return couponPhoto;
    }

    public String getCouponType() {
        return couponType;
    }
}