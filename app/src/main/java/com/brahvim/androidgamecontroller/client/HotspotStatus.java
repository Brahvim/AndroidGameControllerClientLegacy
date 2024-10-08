package com.brahvim.androidgamecontroller.client;

import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HotspotStatus {
    // region Query results constants.
    public final static int AP_STATE_DISABLING = 10;
    public final static int AP_STATE_DISABLED = 11;
    public final static int AP_STATE_ENABLING = 12;
    public final static int AP_STATE_ENABLED = 13;
    public final static int AP_STATE_FAILED = 14;
    // endregion

    // Singleton instance:
    private static HotspotStatus INSTANCE;

    // region Fields.
    public WifiManager man;
    public Method method_getWifiApState = null;
    // endregion

    // "Status" enum.
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
    public static boolean isBeingEnabled() {
        return HotspotStatus.INSTANCE.query() == HotspotStatus.AP_STATE_ENABLING;
    }

    public static boolean isFailed() {
        return HotspotStatus.INSTANCE.query() == HotspotStatus.AP_STATE_FAILED;
    }

    public static boolean isEnabled() {
        return HotspotStatus.INSTANCE.query() == HotspotStatus.AP_STATE_ENABLED;
    }

    public static boolean isBeingDisabled() {
        return HotspotStatus.INSTANCE.query() == HotspotStatus.AP_STATE_DISABLED;
    }

    public static boolean isDisabled() {
        return HotspotStatus.INSTANCE.query() == HotspotStatus.AP_STATE_DISABLING;
    }
    // endregion
}
