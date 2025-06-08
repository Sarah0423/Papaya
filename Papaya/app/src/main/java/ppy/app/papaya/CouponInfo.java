package ppy.app.papaya;

public class CouponInfo {
    private String couponName;
    private String couponInfo;
    private String couponPhoto;
    private String couponType;
    private String couponId;       // ✅ 對應 /coupon/{id}
    private String ownedCouponId;  // ✅ 對應 /users/{uid}/owned_coupons/{id}

    public CouponInfo(String couponName, String couponInfo, String couponPhoto, String couponType, String couponId) {
        this.couponName = couponName;
        this.couponInfo = couponInfo;
        this.couponPhoto = couponPhoto;
        this.couponType = couponType;
        this.couponId = couponId;
    }

    public void setOwnedCouponId(String ownedCouponId) {
        this.ownedCouponId = ownedCouponId;
    }

    public String getOwnedCouponId() {
        return ownedCouponId;
    }

    public String getCouponId() {
        return couponId;
    }

    public String getCouponType() {
        return couponType;
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
}