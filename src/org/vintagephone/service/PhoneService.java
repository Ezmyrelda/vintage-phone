/**
 * Copyright (c) 2011 Basil Shikin, VintagePhone Project
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.vintagephone.service;

import org.vintagephone.PhoneActivity;
import org.vintagephone.R;
import org.vintagephone.model.VintagePhoneLifecycle;
import org.vintagephone.model.VintagePhoneStatusListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * This is a background service that handles phone lifecycle.
 * 
 * @author Basil Shikin
 *
 */
public class PhoneService 
    extends Service 
{

    protected static final String TAG = "PhoneService";

    private static final int STATUS_UPDATE_NOTIFICATION = 1;
    
    private NotificationManager m_notificationManager;
    private ConnectivityManager m_connectivityManager;
    
    private VintagePhoneLifecycle m_lifecycle;
    
    
    private String m_currentStatus = "Loading...";
    
    private IBinder m_binder;

    
    public IBinder onBind(Intent intent)
    {
        return m_binder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        
        m_binder = new PhoneServiceBinder();
        
        m_connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        m_notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        m_lifecycle = VintagePhoneLifecycle.getInstance();
        m_lifecycle.addStatusListener( new VintagePhoneStatusListener()
        {
            public void statusUpdated(String newStatus)
            {
                broadcastStatus( newStatus );
            }
        });
        
        initlaizePhone();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        
        return START_STICKY;
    }

    
    @Override
    public void onDestroy() 
    {
        super.onDestroy();
        
        m_notificationManager.cancel( STATUS_UPDATE_NOTIFICATION );
        
        shutdownPhone();
    }
    
    public void addPhoneListener(VintagePhoneStatusListener listener )
    {
        if ( listener != null )
        {
            listener.statusUpdated( m_currentStatus );
            m_lifecycle.addStatusListener( listener );
        }
    }
    
    private void initlaizePhone()
    {
        final AsyncTask<Integer, Integer, Boolean> task = new AsyncTask<Integer, Integer, Boolean>() {
            protected Boolean doInBackground(Integer... params)
            {
                try
                {
                    // Wait until device is online
                    broadcastStatus("Waiting until device is online...");
                    
                    while( true )
                    {
                        final NetworkInfo networkInfo = m_connectivityManager.getActiveNetworkInfo();
                        if ( networkInfo != null )
                        {
                            if ( networkInfo.isConnected() )  break;
                        }
                        
                        Thread.sleep( 1000 );
                    }
                    
                    broadcastStatus("Initializing phone...");
                    
                    m_lifecycle.initialize( getApplicationContext() );
                } 
                catch (Exception e)
                {
                    Log.e(TAG, "Unable to initialize phone", e );
                    
                    broadcastStatus("Initialization failed");
                }
                
                return true; 
            }
        };
        task.execute();
    }
    
    private void shutdownPhone()
    {
    }
    
    private void broadcastStatus(String status)
    {
        m_currentStatus = status;
        
        final PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, PhoneActivity.class ), Intent.FLAG_ACTIVITY_NEW_TASK);
        
        final Notification notification = new Notification( R.drawable.bell, status, System.currentTimeMillis());
        notification.setLatestEventInfo(this, "Vintage Phone", status, intent);

        m_notificationManager.notify(STATUS_UPDATE_NOTIFICATION, notification);
    }
    
    public class PhoneServiceBinder extends Binder 
    {
        public PhoneService getService() {
            return PhoneService.this;
        }
    }
}
