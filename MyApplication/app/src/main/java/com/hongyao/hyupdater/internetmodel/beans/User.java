package com.hongyao.hyupdater.internetmodel.beans;

import java.io.Serializable;

/*{
    "downloadurl": "https://hongyao-ota-new-1317283996.cos.ap-guangzhou.myqcloud.com/%E6%99%AE%E8%80%90%E5%B0%94-S13_80_P503_EYYB/S13_80_P503_EYYB_16_2_V1.03_KC_20240227/S13_80_P503_EYYB_16_2_V1.03_KC_20231009/update.zip?q-sign-algorithm=sha1&q-ak=AKIDNrqwcTUJQ4d8giymm7oj_0Ab-iQU51i03NjUAKjVb5zLI2tueiUiUnATdJCiEX_X&q-sign-time=1718184198%3B1718185998&q-key-time=1718184198%3B1718185998&q-header-list=host&q-url-param-list=&q-signature=d2e67b8a585adc4c6913ce94252a33b2002ac9f0&x-cos-security-token=e42kCq2sj5JJvE9ge9KevxswesqfIN5af8bfdfaa8ae25ac346b96b627d9397e7dt26XPg8QUMMdNP405v7H8_X2nH8LWIbJviSUMpfQnxOVnkbBJeAe2nhXEdhaW9oRhodZG5hYmPrs2S51GpFC4QumpqYnF1oG2DO0gtwRB9wYRWQg20Wg5sZTLBsR4sxiG7fM458GKxNHDMbrsL93Qj7Z1Kp6-J0gn4BBTZYZFgYSg8aD7euZtq8wmSuWENz0MYCHrTWJpSDS1d8OCqBhq1qlGVxSBYuFakxpBUDf1RQq2LBBLnxH_mK7j8PDLLkMOXIoBip8c3pc42lkjH9tUIEnpsBScs-pNh9ENl7JrfG4SuMR1SB-sDPRw0wI_UQkUzdZ0-0Z3e67togugswrw",
    "reserve": "1. System optimization; \n2. Resolve known issues",
    "version_number": 6,
    "new_version": "S13_80_P503_EYYB_16_2_V1.03_KC_20240227",
    "is_need_update": 1,
    "md5": "d2d1f4d5baedb4a34affcf305a8f0ff9"
}*/
public class User implements Serializable {
    private String downloadurl;
    private String reserve;
    private int version_number;
    private String new_version;
    private int is_need_update;
    private String md5;
    private String msg;
    private int force_update_time;

    public String getDownloadurl() {
        return downloadurl;
    }

    public void setDownloadurl(String downloadurl) {
        this.downloadurl = downloadurl;
    }

    public String getReserve() {
        return reserve;
    }

    public void setReserve(String reserve) {
        this.reserve = reserve;
    }

    public int getVersion_number() {
        return version_number;
    }

    public void setVersion_number(int version_number) {
        this.version_number = version_number;
    }

    public String getNew_version() {
        return new_version;
    }

    public void setNew_version(String new_version) {
        this.new_version = new_version;
    }

    public int getIs_need_update() {
        return is_need_update;
    }

    public void setIs_need_update(int is_need_update) {
        this.is_need_update = is_need_update;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getForce_update_time() {
        return force_update_time;
    }

    public void setForce_update_time(int force_update_time) {
        this.force_update_time = force_update_time;
    }

    @Override
    public String toString() {
        return "User{" +
                "downloadurl='" + downloadurl + '\'' +
                ", reserve='" + reserve + '\'' +
                ", version_number=" + version_number +
                ", new_version='" + new_version + '\'' +
                ", is_need_update=" + is_need_update +
                ", md5='" + md5 + '\'' +
                ", msg='" + msg + '\'' +
                ", force_update_time=" + force_update_time +
                '}';
    }
}
