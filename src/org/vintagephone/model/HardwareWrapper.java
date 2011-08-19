package org.vintagephone.model;

import java.util.ArrayList;
import java.util.Collection;

import org.vintagephone.hardware.HardwareProvider;
import org.vintagephone.hardware.HardwareProvider.HardwareEventListener;
import org.vintagephone.hardware.impl.ioio.IOIOHardwareProvider;

import android.util.Log;

public class HardwareWrapper
{
    private static final String Tag = "HardwareWrapper";
    private static final long RING_INTERVAL = 1000; 
    private static final long MAX_RING_CYCLES = 12; 
    
    private final HardwareProvider m_hardwareProvider;
 
    
    private final Collection<HardwareEventListener > m_listeners = new ArrayList<HardwareEventListener>();
    private final Thread m_ringThread;
    
    private final Object m_ringLock = new Object();
    private volatile boolean m_isRinging = false;
    
    private volatile boolean m_isTerminated;
    private volatile Runnable m_ringingCallback;
    private volatile Runnable m_ringExpiredCallback;
    
    
    public HardwareWrapper()
    {
        m_hardwareProvider = new IOIOHardwareProvider();
        m_hardwareProvider.setListener( new HardwareEventListener()
        {
            public void hookStateChanged(boolean isOnHook)
            {
                for (HardwareEventListener listener : m_listeners )
                {
                    listener.hookStateChanged( isOnHook );
                }
            }
        });
        
        m_ringThread = new Thread( new RingerTask() );
    }
    
    public void startRinging( Runnable ringingCallback, Runnable ringExpiredCallback )
    {
        synchronized ( m_ringLock )
        {
            m_isRinging = true;
            m_ringingCallback = ringingCallback;
            m_ringExpiredCallback = ringExpiredCallback;
            
            m_ringLock.notify();
        }
    }

    public void stopRinging()
    {
        synchronized ( m_ringLock )
        {
            if ( m_isRinging )
            {
                m_isRinging = false;
            
                m_ringLock.notify();
            }
        }        
    }
    
    public void initialize()
    {
        m_hardwareProvider.initialize();
        m_ringThread.start();
    }

    public void addListener( HardwareEventListener listener)
    {
        m_listeners.add( listener );        
    }
    
    private class RingerTask
        implements Runnable
    {
        public void run()
        {
            try
            {
                int ringCycleCount = 0;
                
                while ( !m_isTerminated )
                {
                    synchronized ( m_ringLock )
                    {
                        if ( m_isRinging )
                        {
                            if ( m_ringingCallback != null ) m_ringingCallback.run();
                            
                            m_hardwareProvider.setRinging( true );
                            
                            Thread.sleep( RING_INTERVAL );
                            
                            m_hardwareProvider.setRinging( false );
                            
                            Thread.sleep( RING_INTERVAL/2 );
                            
                            ringCycleCount += 1;
                            if ( ringCycleCount > MAX_RING_CYCLES ) 
                            {
                                if (m_ringExpiredCallback != null) m_ringExpiredCallback.run();
                                
                                m_isRinging = false;                            
                            }
                        }
                        else
                        {
                            ringCycleCount = 0;
                        }
                        
                        m_ringLock.wait( RING_INTERVAL/2 );
                    }
                }
            }
            catch (InterruptedException e )
            {
                Log.e(Tag, "Ringer thread interrupted", e );
            }
        }
    }
}
