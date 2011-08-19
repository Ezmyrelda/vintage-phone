package org.vintagephone.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings;

public class ApplicationWrapper
{
    private Context m_context;

    private PowerManager.WakeLock m_wakeLock;
    private WifiManager.WifiLock m_wifiLock;
    
    public void initalize( Context context )
    {
        m_context = context;
        
        final PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        
        m_wakeLock = powerManager.newWakeLock( PowerManager.FULL_WAKE_LOCK
                                              | PowerManager.ACQUIRE_CAUSES_WAKEUP                
                                              | PowerManager.ON_AFTER_RELEASE , "VintagePhone");
        
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        
        // Disable WiFi sleep
        final ContentResolver resolver = context.getContentResolver();
        Settings.System.putInt(resolver, Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER);
                    
        // Turn on Wifi Full mode
        m_wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "org.vintagephone.service.PhoneService");
        m_wifiLock.setReferenceCounted(false);

        if ( !m_wifiLock.isHeld() ) m_wifiLock.acquire();
        
        // Set screen brightness to medium
        Settings.System.putInt(m_context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 50);
    }
    
    public void bringToForground()
    {
        if ( m_wakeLock != null && !m_wakeLock.isHeld() ) m_wakeLock.acquire();
    }
    
    public void sendToBack()
    {
        if ( m_wakeLock != null && m_wakeLock.isHeld() ) m_wakeLock.release();
    }

}
