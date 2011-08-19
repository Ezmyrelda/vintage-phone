package org.vintagephone.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.vintagephone.hardware.HardwareProvider;
import org.vintagephone.phone.PhoneCall;
import org.vintagephone.phone.PhoneCall.PhoneCallStatus;
import org.vintagephone.phone.PhoneListener;

import android.content.Context;
import android.util.Log;

/**
 * This class contains logic for vintage phone life cycle module
 * 
 * @author Basil Shikin
 *
 */
public class VintagePhoneLifecycle
{
    private static final VintagePhoneLifecycle s_instance = new VintagePhoneLifecycle();

    private final ApplicationWrapper m_application;
    private final HardwareWrapper m_hardware;
    private final PhoneWrapper m_phone;
    private final OperatorWrapper m_operator;

    private final Executor m_executor;
    
    private volatile boolean m_isOnHook = true;
    private volatile boolean m_isCallInPending  = false;
    
    private Collection<VintagePhoneStatusListener> m_statusListeners = new ArrayList<VintagePhoneStatusListener>();
    
    private Runnable m_ringingCallback;
    private Runnable m_ringExpiredCallback;
    
    private VintagePhoneLifecycle()
    {
        m_application = new ApplicationWrapper();
        m_hardware = new HardwareWrapper();
        m_phone = new PhoneWrapper();
        m_operator = new OperatorWrapper();
        
        m_executor = Executors.newSingleThreadExecutor();
    }
    
    
    public void initialize( Context context ) throws Exception
    {
        fireStatus( "Intializing phone...");
        
        m_hardware.initialize();
        m_operator.initialize();
        m_application.initalize( context );
        m_phone.initialize( context );
        
        m_ringingCallback = new Runnable() {
            public void run()
            {
                try
                {
                    m_phone.signalRinging();
                } 
                catch (Exception e)
                {
                    Log.e( "VintagePhoneLifecycle", "Unable to signal ring", e );
                }                            
            }
        };
        
        m_ringExpiredCallback = new Runnable() {
            public void run()
            {
                try
                {
                    m_phone.signalBusy();
                } 
                catch (Exception e)
                {
                    Log.e( "VintagePhoneLifecycle", "Unable to signal ring", e );
                }                            
            }
        };
        m_hardware.addListener( new PhoneHookListener() );
        m_phone.addPhoneListener( new VintagePhoneListener() );
        
        fireStatus( "Phone intialized");
    }
    
    public void addStatusListener(VintagePhoneStatusListener statusListener)
    {        
        m_statusListeners.add( statusListener  );
    }

    public PhoneWrapper getPhoneWrapper()
    {
        return m_phone;
    }
    
    public OperatorWrapper getOperatorWrapper()
    {
        return m_operator;
    }
    
    public HardwareWrapper getHardwareWrapper()
    {
        return m_hardware;
    }
    


    void fireStatus(String newStatus)
    {
        for ( VintagePhoneStatusListener listener : m_statusListeners )
        {
            listener.statusUpdated( newStatus );
        }
    }

    public static VintagePhoneLifecycle getInstance()
    {
        return s_instance;
    }
    
    private class PhoneHookListener
        implements HardwareProvider.HardwareEventListener
    {
        public void hookStateChanged( boolean isOnHook )
        {
            if ( m_isOnHook != isOnHook )
            {
                if ( isOnHook )
                {
                    fireStatus( "Phone On Hook" );
                    
                    m_operator.stopTalking();
                    m_phone.terminateActiveCall();
                    m_phone.signalBusy();
                    m_hardware.stopRinging();
                    
                    m_application.sendToBack();
                }
                else
                {
                    m_application.bringToForground();
                    
                    
                    fireStatus( "Phone Off Hook" );
                    
                    if ( m_isCallInPending )
                    {
                        fireStatus( "Answering Pending Call..." );
                        
                        m_hardware.stopRinging();
                        
                        m_isCallInPending = false;
                            
                        execute("call", new Runnable() {
                            public void run()
                            {
                                m_phone.respondToCall();
                            }
                        } );
                    }
                    else
                    {
                        fireStatus( "Asking Phone Number..." );
                        
                        execute( "make_call", new Runnable() {                            
                            public void run()
                            {
                                try
                                {
                                    Thread.sleep( 1000 );
                                } 
                                catch (InterruptedException e) {  return ; }
                                
                                final String phoneNumber = m_operator.askPhoneNumber();
                                if ( phoneNumber != null )
                                {                       
                                    m_phone.call( phoneNumber );
                                }                                
                            }
                        } );
                    }
                }
            }
            
            m_isOnHook = isOnHook;
        }
    }
    
    private class VintagePhoneListener
        implements PhoneListener
    {
        
        public void callStatusChanged(PhoneCall call)
        {
            fireStatus("Call to " + call.getCallee() + " is now " + call.getCallStatus()  );
            
            if ( call.getCallStatus() == PhoneCallStatus.FAILED )
            {
                m_isCallInPending = false;
                
                m_operator.sayCallFinished();
                m_hardware.stopRinging();
            }
            
            if ( call.getCallStatus() == PhoneCallStatus.FINISHED )
            {
                m_isCallInPending = false;
                
                m_operator.sayCallFinished();
                m_hardware.stopRinging();
            }
        }

        public void incomingCallRecieved(PhoneCall call)
        {
            m_application.bringToForground();
            
            fireStatus("Incoming call from " + call.getCaller() );
            
            if ( m_isOnHook )
            {
                m_isCallInPending = true;
                
                m_hardware.startRinging( m_ringingCallback, m_ringExpiredCallback );
            }
            else
            {
                m_phone.signalBusy();
            }
        }
    }
    
    private void execute(String debugName, Runnable runnable)
    {
        m_executor.execute( runnable );
    }
}
