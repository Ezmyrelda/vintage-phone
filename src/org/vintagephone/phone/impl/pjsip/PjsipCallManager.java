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

import java.util.ArrayList;
import java.util.Collection;

import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_call_info;
import org.vintagephone.phone.PhoneCall.PhoneCallStatus;

import android.util.Log;

/**
 * This is a helper class that is used to manage calls
 * 
 * <p>
 * <b>Please note:</b> this implementation is based on Regis Montoya's CSipSimple.
 * <p>
 * <b>Please note:</b> this class is for internal use only.
 * 
 * @author Basil Shikin
 *
 */
public class PjsipCallManager
{
    private static final String TAG = "PjsipCallManager";
    
    private final PjsipManager m_manager;
    
    private final Collection<PjsipPhoneCall> m_activeCalls = new ArrayList<PjsipPhoneCall>( 3 );
    private PjsipPhoneCall m_activeCall;
    
    PjsipCallManager(PjsipManager manager)
    {
        m_manager = manager;
    }

    PjsipPhoneCall getActiveCall()
    {
        return m_activeCall;
    }
    
    PjsipPhoneCall createOutgoingCall(String number, PjsipAccount account) throws Exception
    {
        synchronized ( m_activeCalls )
        {
            final String callee = "<sip:" + number + "@" + account.getDomain() + ">";

            Log.d( TAG, "Preparing to call " + callee + "...");
            
            if (pjsua.verify_sip_url( callee ) == 0) 
            {
                Log.d( TAG, "Callee " + callee + "validated");
                
                final PjsipPhoneCall phoneCall = new PjsipPhoneCall( account.getUsername(), callee, account );
                m_activeCalls.add( phoneCall );
                
                return phoneCall;
            }
            else
            {
                Log.e( TAG, "Invalid callee :" + callee );
                
                throw new Exception("invalid_callee");
            }            
        }
    }
    
    PjsipPhoneCall createIncomingCall(int accountId, int callId) throws Exception
    {
        synchronized ( m_activeCalls )
        {
            for ( PjsipPhoneCall call : m_activeCalls )
            {
                if ( call.getPjsipCallId() == callId ) return call;
            }
            
            
            final PjsipAccount activeAccount = m_manager.getActiveAccount();
            if ( accountId != activeAccount.getPjsipAccountId() )
            {
                Log.e(TAG, "Incoming call placed from unknown account " + accountId );
                throw new Exception( "unknown_account" );
            }
            
            pjsua_call_info pj_info = new pjsua_call_info();
            final int status = pjsua.call_get_info( callId, pj_info);

            if(status == pjsua.PJ_SUCCESS) 
            {
                final String caller = pj_info.getRemote_contact().getPtr();
                final PjsipPhoneCall call = new PjsipPhoneCall( caller, activeAccount.getUsername(), activeAccount);
                call.setPjsipCallId( callId );

                m_activeCalls.add( call );
                
                return call;
            }
            else 
            {
                Log.d( TAG, "Unable to create incoming call: " + status );
                
                throw new Exception( "cant_create_call");
            }
        }
    }
    
    boolean placeCall( PjsipPhoneCall phoneCall )
    {
        synchronized ( m_activeCalls )
        {
            if ( m_activeCall != null )
            {
                terminateCall( m_activeCall );
            }
            
            pj_str_t uri = pjsua.pj_str_copy( phoneCall.getCallee() );

            final byte[] userData = new byte[1];
            final int[] callId = new int[1];
            
            final int accountId = phoneCall.getPjsipAccount().getPjsipAccountId();
            final int result = pjsua.call_make_call( accountId, uri, 0, userData, null, callId );
            
            if ( result == pjsuaConstants.PJ_SUCCESS )
            {
                phoneCall.setPjsipCallId( callId[0] );
                phoneCall.setCallStatus( PhoneCallStatus.RINGING );
                
                Log.i(TAG, "Call placed. Active call ID is " + phoneCall.getPjsipCallId() );
                
                m_activeCall = phoneCall;
                
                return true;
            }
            else
            {
                phoneCall.setCallStatus( PhoneCallStatus.FAILED );
                
                return false;
            }
        }
    }
    

    boolean terminateCall(PjsipPhoneCall phoneCall)
    {
        synchronized ( m_activeCalls )
        {
            if ( m_activeCall != null && m_activeCall.getPjsipCallId() == phoneCall.getPjsipCallId() ) m_activeCall = null;
            m_activeCalls.remove( phoneCall );
            
            phoneCall.setCallStatus( PhoneCallStatus.FINISHED );
            
            final int status = pjsua.call_hangup( phoneCall.getPjsipCallId(), 0, null, null);
            return status == pjsuaConstants.PJ_SUCCESS;
        }
    }
    
    boolean answerCall(PjsipPhoneCall phoneCall, int code )
    {
        final int callId = phoneCall.getPjsipCallId();
        
        final int status = pjsua.call_answer(callId, code, null, null);
        return status == pjsuaConstants.PJ_SUCCESS;
    }
}
