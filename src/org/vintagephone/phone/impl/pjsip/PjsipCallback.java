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
package org.vintagephone.phone.impl.pjsip;

import org.pjsip.pjsua.Callback;
import org.pjsip.pjsua.SWIGTYPE_p_p_pjmedia_port;
import org.pjsip.pjsua.SWIGTYPE_p_pjmedia_session;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_rx_data;
import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjsip_event;
import org.pjsip.pjsua.pjsip_redirect_op;
import org.pjsip.pjsua.pjsip_status_code;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsua_call_info;
import org.vintagephone.phone.PhoneCall.PhoneCallStatus;
import org.vintagephone.phone.impl.pjsip.PjsipManager.IncomingCallListener;

import android.media.MediaRecorder.AudioSource;
import android.util.Log;


/**
 * This is a helper class that is used to respond to various phone events 
 * 
 * <p>
 * <b>Please note:</b> this implementation is based on Regis Montoya's CSipSimple.
 * <p>
 * <b>Please note:</b> this class is for internal use only.
 * 
 * @author Basil Shikin
 *
 */
class PjsipCallback
    extends Callback
{
    
    private static final String TAG = "PjsipCallback";
    
    private final PjsipAudioManager m_audioManager;
    private final PjsipManager m_manager;

    private IncomingCallListener m_listener;
    
    PjsipCallback(PjsipAudioManager audioManager, PjsipManager stackLifecycle)
    {
        m_audioManager = audioManager;
        m_manager = stackLifecycle;
    }


    @Override
    public void on_incoming_call(final int acc_id, final int callId, SWIGTYPE_p_pjsip_rx_data rdata) 
    {
        Log.d( TAG, "Incomming call detected");
        
        m_manager.execute( new Runnable()
        {
            public void run()
            {
                try
                {
                    final PjsipPhoneCall  call = m_manager.createIncomingCall( acc_id, callId );
                    
                    if ( m_listener != null ) m_listener.incomingCallDetected( call );
                } 
                catch (Exception e)
                {
                    Log.e(TAG, "Unable to handle incoming call", e );
                }
            }
        });
    }
    
    
    @Override
    public void on_call_state(final int callId, final pjsip_event e) 
    {
        Log.d( TAG, "Handling call state update");
        
        m_manager.execute( new Runnable()
        {
            public void run()
            {
                final PjsipPhoneCall activeCall = m_manager.getActiveCall();
                if ( isCurrentCall( activeCall, callId ) )
                {
                    final PhoneCallStatus callStatus = createCallStatus(callId, e);
                    activeCall.setCallStatus( callStatus );
                    
                    Log.d( TAG, "Call state is now " + callStatus );
                }
                else
                {
                    Log.w(TAG, "Got update for not active call (ID " + callId + ")");
                }                
            }
        } );
    }
    
    @Override
    public void on_call_media_state(final int callId) 
    {
        Log.d(TAG, "Media state changed for call " + callId);
        
        m_manager.execute( new Runnable() {
            public void run()
            {
                pjsua_call_info pjCallInfo = new pjsua_call_info();

                final int status = pjsua.call_get_info( callId, pjCallInfo);
                if(status == pjsua.PJ_SUCCESS) 
                {
                    final int mediaStatus = pjCallInfo.getMedia_status().swigValue() ;
                    
                    Log.d(TAG, "Media status is " + mediaStatus );
                    
                    if ( mediaStatus == 1 ) // If media active
                    {
                        final int confPort = pjCallInfo.getConf_slot();
                        
                        pjsua.conf_connect(confPort, 0);
                        pjsua.conf_connect(0, confPort );
                        
                        pjsua.set_ec( 200, 2 );
                    }
                }
                else 
                {
                    Log.d( TAG, "Unable to retrieve call info for call " + callId );
                }
            }
        });
    }
    
    @Override
    public int on_setup_audio(int clockRate) 
    {
        final boolean success = m_audioManager.enableAudioIn();
        
        return success ? 0 : -1;
    }

    @Override
    public void on_teardown_audio() 
    {
        m_audioManager.clearAudioIn();
    }

    @Override
    public void on_reg_state(final int accountId) 
    {
        Log.d(TAG, "Registration state updated for account " + accountId);
    }


    @Override
    public void on_buddy_state(int buddy_id) 
    {
        Log.d(TAG, "Buddy state updated. Buddy ID: " + buddy_id );
    }

    @Override
    public void on_pager(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, pj_str_t mime_type, pj_str_t body) 
    {
        Log.d(TAG, "Pager recieved");
    }

    @Override
    public void on_pager_status(int call_id, pj_str_t to, pj_str_t body, pjsip_status_code status, pj_str_t reason) 
    {
        Log.d(TAG, "Pager status updated to " + status );
    }

    @Override
    public void on_stream_created(int call_id, SWIGTYPE_p_pjmedia_session sess, long stream_idx, SWIGTYPE_p_p_pjmedia_port p_port) 
    {
        Log.d(TAG, "Stream created");
    }
    
    @Override
    public void on_stream_destroyed(int callId, SWIGTYPE_p_pjmedia_session sess, long streamIdx) {
        Log.d(TAG, "Stream destroyed");
    }

    
    @Override
    public pjsip_redirect_op on_call_redirected(int call_id, pj_str_t target) 
    {
        Log.w(TAG, "Got call redirection to " + target.getPtr() );
        return pjsip_redirect_op.PJSIP_REDIRECT_ACCEPT;
    }
    
    @Override
    public int on_set_micro_source() 
    {
        return AudioSource.MIC;
    }
    
    void setIncomingCallListener(IncomingCallListener listener)
    {
        m_listener = listener;
    }
    
    private PhoneCallStatus createCallStatus(int callId, pjsip_event e)
    {
        // Update call state
        pjsua_call_info pj_info = new pjsua_call_info();
        final int status = pjsua.call_get_info( callId, pj_info);
        
        if(status == pjsua.PJ_SUCCESS) 
        {
            Log.d(TAG, "Creating phone state for " + pj_info.getState() );
            
            switch ( pj_info.getState() )
            {
                case PJSIP_INV_STATE_CALLING:
                case PJSIP_INV_STATE_EARLY:
                case PJSIP_INV_STATE_CONNECTING:
                    return PhoneCallStatus.RINGING;
                    
                case PJSIP_INV_STATE_CONFIRMED:
                    return PhoneCallStatus.ACTIVE;
                
                case PJSIP_INV_STATE_INCOMING:
                    return PhoneCallStatus.PENDING;
    
                default:
                    return PhoneCallStatus.FAILED;
            }
        }
        else 
        {
            Log.d( TAG, "Unable to create call status: error code: " + status);
            
            return PhoneCallStatus.FAILED;
        }
    }

    private static boolean isCurrentCall(PjsipPhoneCall activeCall, int callId)
    {
        return activeCall != null && activeCall.getPjsipCallId() == callId;
    }
}
