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
package org.vintagephone.hardware.impl.ioio;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalInput.Spec.Mode;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.vintagephone.hardware.HardwareProvider;

import android.util.Log;

/**
 * This calss is used to control and respond to android events
 * @author Basil Shikin
 *
 */
class IOIOHardwareProviderThread
    extends IOIOThreadBase
{
    private static final Executor s_callbackExecutor = Executors.newSingleThreadExecutor();
    
    private DigitalOutput m_ringer;
    private DigitalInput m_hookSensor;

    private volatile HardwareProvider.HardwareEventListener m_listener;
    
    private volatile boolean m_isRinging = false;
    private volatile boolean m_isOnHook = true;
    
    
    
    void setListener(HardwareProvider.HardwareEventListener listener)
    {
        m_listener = listener;
    }

    void setRinging(boolean isRinging)
    {
        m_isRinging = isRinging;
    }

    @Override
    protected void setup() throws ConnectionLostException
    {
        super.setup();
        
        m_ringer = m_ioio.openDigitalOutput( 1, m_isRinging );
        m_hookSensor = m_ioio.openDigitalInput( 48, Mode.PULL_UP );
    }
    
    @Override
    protected void loop() throws ConnectionLostException
    {
        m_ringer.write( m_isRinging );
        
        try
        {
            final boolean isOnHookNow = m_hookSensor.read();
            if ( isOnHookNow != m_isOnHook )
            {      
                s_callbackExecutor.execute( new Runnable()
                {
                    public void run()
                    {
                        if ( m_listener != null ) m_listener.hookStateChanged( isOnHookNow );                        
                    }
                });
            }
            m_isOnHook = isOnHookNow;
        }
        catch ( Exception e )
        {
            Log.e("IOIOHardwareProviderThread", "Unable to read hook state",e);
        }
        
        try 
        {
            sleep(100);
        } 
        catch (InterruptedException e) 
        {
        }
    }
}
