package com.brahvim.androidgamecontroller.client;

import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HotspotStatus {
    public static int AP_STATE_DISABLING = 10;
    public static int AP_STATE_DISABLED = 11;
    public static int AP_STATE_ENABLING = 12;
    public static int AP_STATE_ENABLED = 13;
    public static int AP_STATE_FAILED = 14;

    private static HotspotStatus INSTANCE;

    private WifiManager man;
    private Method method_getWifiApState = null;

    /*
    public enum Status {
        AP_STATE_DISABLING(10),
        AP_STATE_DISABLED(11),
        AP_STATE_ENABLING(12),
        AP_STATE_ENABLED(13),
        AP_STATE_FAILED(14);

        private int value;

        Status(int p_value) {
            this.value = p_value;
        }
    }
     */

    private HotspotStatus(WifiManager p_man) throws NoSuchMethodException {
        this.man = p_man;
        this.method_getWifiApState = this.man.getClass()
          .getDeclaredMethod("getWifiApState");
    }

    public static boolean init(WifiManager p_man) {
        try {
            HotspotStatus.INSTANCE = new HotspotStatus(p_man);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

    public static HotspotStatus getInstance() {
        return HotspotStatus.INSTANCE;
    }

    // "The" query:
    public int query() {
        this.method_getWifiApState.setAccessible(true);

        int ret = -1;
        try {
            ret = (Integer)this.method_getWifiApState.invoke(this.man, (Object[])null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return ret;
    }

    // region Queries!
    public boolean isBeingEnabled() {
        return this.query() == HotspotStatus.AP_STATE_ENABLING;
    }

    public boolean isFailed() {
        return this.query() == HotspotStatus.AP_STATE_FAILED;
    }

    public boolean isEnabled() {
        return this.query() == HotspotStatus.AP_STATE_ENABLED;
    }

    public boolean isBeingDisabled() {
        return this.query() == HotspotStatus.AP_STATE_DISABLED;
    }

    public boolean isDisabled() {
        return this.query() == HotspotStatus.AP_STATE_DISABLING;
    }
    // endregion
}
