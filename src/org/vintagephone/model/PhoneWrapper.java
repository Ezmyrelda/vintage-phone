package org.vintagephone.model;

import java.util.ArrayList;
import java.util.Collection;

import org.vintagephone.phone.PhoneCall;
import org.vintagephone.phone.PhoneCall.PhoneCallStatus;
import org.vintagephone.phone.PhoneListener;
import org.vintagephone.phone.PhoneProvider;
import org.vintagephone.phone.PhoneProvider.CallAnswer;
import org.vintagephone.phone.impl.pjsip.PjsipPhoneProvider;

import android.content.Context;
import android.util.Log;

public class PhoneWrapper
{
    private static final String TAG = "PhoneWrapper";
    
    private final Collection<PhoneListener> m_phoneListeners = new ArrayList<PhoneListener>();
    
    private PhoneProvider m_phone;
    private PhoneCall m_activeCall;
    private PhoneCall m_pendingCall;
    
    
    public PhoneWrapper()
    {
        
    }
    
    public void initialize( Context context  ) throws Exception
    {
        m_phone = new PjsipPhoneProvider( context );
        m_phone.initialize();
        m_phone.setListener( new PhoneListener()
        {
            public void incomingCallRecieved(PhoneCall call)
            {
                m_pendingCall = call;
                
                for ( PhoneListener listener : m_phoneListeners )
                {
                    listener.incomingCallRecieved( call );
                }
            }
            
            public void callStatusChanged(PhoneCall call)
            {
                if ( call == m_pendingCall )
                {
                    if ( call.getCallStatus() == PhoneCallStatus.FINISHED || 
                         call.getCallStatus() == PhoneCallStatus.FAILED )
                    {
                        m_pendingCall = null;
                    }
                }
                
                for ( PhoneListener listener : m_phoneListeners )
                {
                    listener.callStatusChanged( call );
                }                
            }
        });
        
         
    }
    
    public PhoneCall getActiveCall()
    {
        return m_activeCall;
    }
    
    public void signalRinging() throws Exception
    {
        if ( m_pendingCall != null )
        {
            m_phone.answerCall( m_pendingCall, CallAnswer.RINGING );
        }
    }
    
    public void signalBusy()
    {
        if ( m_pendingCall != null )
        {
            try
            {
                m_phone.answerCall( m_pendingCall, CallAnswer.REJECT );
            } 
            catch (Exception e)
            {
                Log.e(TAG, "Unable to singal busy for call " + m_pendingCall, e );
            }
            m_pendingCall = null;
        }
    }

    public void respondToCall()
    {
        if ( m_pendingCall != null )
        {
            try
            {
                m_phone.answerCall( m_pendingCall, CallAnswer.ACCEPT );
                
                m_activeCall = m_pendingCall;
                m_pendingCall = null;
            } 
            catch (Exception e)
            {
                Log.e(TAG, "Unable to answer pending call " + m_pendingCall, e );
            }
        }
    }

    public void terminateActiveCall()
    {
        terminate( m_activeCall );
        m_activeCall = null;
        
        terminate( m_pendingCall );
        m_pendingCall = null;
    }

    public void call(String phoneNumber)
    {
        try
        {
            terminate( m_pendingCall );
            m_pendingCall = null;
            
            m_activeCall = m_phone.placeCall( phoneNumber );
        } 
        catch (Exception e)
        {
            Log.e(TAG, "Unable to call " + phoneNumber, e );
        }
    }

    public void addPhoneListener(PhoneListener listener )
    {
        m_phoneListeners.add( listener );        
    }
    
    private void terminate( PhoneCall call )
    {
        if ( call != null )
        {
            try
            {
                m_phone.hangupCall( call );
            } 
            catch (Exception e)
            {
                Log.e( TAG, "Unable to terminate the call", e );
            }
        }
    }
}
