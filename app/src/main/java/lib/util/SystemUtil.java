package lib.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class SystemUtil {
	protected static UUID uuid;

	/** 获取设备唯一标识 */
	public static String getDeviceIMEI(Context context){
		String	Imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)) .getDeviceId();
		return Imei;
	}

	/** 获取设备唯一标识 */
	public static UUID getDeviceUUID(Context context){
		if(uuid == null) {
			synchronized (SystemUtil.class) {
				if( uuid == null) {
					final String id = PreferenceUtils.getInstance(context).getString(PreferenceUtils.KEY_DEVICE_ID,null);
					if (id != null) {
						// Use the ids previously computed and stored in the prefs file
						uuid = UUID.fromString(id);
					} else {
						final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
						// Use the Android ID unless it's broken, in which case fallback on deviceId,
						// unless it's not available, then fallback on a random number which we store
						// to a prefs file
						try {
							if (androidId != null) {
								uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
							} else {
								final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
								uuid = (deviceId != null) ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
							}
						} catch (UnsupportedEncodingException e) {
							throw new RuntimeException(e);
						}
						// Write the value out to the prefs file
						PreferenceUtils.getInstance(context).save(PreferenceUtils.KEY_DEVICE_ID, uuid.toString());
					}
				}
			}
		}
		return uuid;
	}
}
