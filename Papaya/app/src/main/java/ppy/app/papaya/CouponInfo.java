package ppy.app.papaya;

public class CouponInfo {
    private String couponName;
    private String couponInfo;
    private String couponPhoto;
    private String couponType;

    public CouponInfo() {}

    public CouponInfo(String couponName, String couponInfo, String couponPhoto, String couponType) {
        this.couponName = couponName;
        this.couponInfo = couponInfo;
        this.couponPhoto = couponPhoto;
        this.couponType = couponType;
    }

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