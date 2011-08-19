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
package org.vintagephone;

import org.vintagephone.model.VintagePhoneStatusListener;
import org.vintagephone.service.PhoneService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

/**
 * @author Basil Shikin
 *
 */
public class PhoneActivity
    extends Activity
{
    protected static final String TAG = "PhoneActivity";

    private TextView m_statusView;
    
    private PhoneService m_phoneService;
    private boolean m_IsPhoneServiceBound;
    
    private ServiceConnection m_phoneServiceConnection = new ServiceConnection() {
        
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
            m_phoneService = ((PhoneService.PhoneServiceBinder)service).getService();
            if ( m_phoneService != null )
            {
                m_phoneService.addPhoneListener(new VintagePhoneStatusListener()
                {
                    public void statusUpdated(String newStatus)
                    {
                        displayStatus(newStatus);
                    }
                }); 
            }
        }

        public void onServiceDisconnected(ComponentName className) 
        {
            m_phoneService = null;
        }
    };

    

    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
        
        unbindPhoneService();
    }
    
    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        m_statusView = (TextView) findViewById(R.id.statusText );
        m_statusView.setText( "Starting...");
        
        bindPhoneService();
    }

    void bindPhoneService() 
    {
        if ( !m_IsPhoneServiceBound )
        {
            final Intent intent = new Intent(this, PhoneService.class);
            
            startService( intent );
            bindService( intent, m_phoneServiceConnection, Context.BIND_AUTO_CREATE);
            
            m_IsPhoneServiceBound = true;
        }
    }

    void unbindPhoneService() 
    {
        if (m_IsPhoneServiceBound) 
        {
            unbindService(m_phoneServiceConnection);
            m_IsPhoneServiceBound = false;
        }
    }
    

    private void displayStatus( final String status )
    {
        if ( m_statusView != null )
        {
            m_statusView.post( new Runnable()
            {
                public void run()
                {
                    m_statusView.setText( status );                
                }
            } );
        }
    }
}
