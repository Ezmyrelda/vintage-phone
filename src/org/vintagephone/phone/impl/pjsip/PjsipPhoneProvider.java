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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.vintagephone.phone.PhoneCall;
import org.vintagephone.phone.PhoneCall.PhoneCallStatus;
import org.vintagephone.phone.PhoneListener;
import org.vintagephone.phone.PhoneProvider;
import org.vintagephone.phone.impl.pjsip.PjsipManager.IncomingCallListener;
import org.vintagephone.phone.impl.pjsip.PjsipPhoneCall.PjsipPhoneCallStatusListener;

import android.content.Context;
import android.util.Log;

/**
 * Implementation of Pjsip call stack. 
 * 
 * <b>Please note:</b> this implementation is based on Regis Montoya's CSipSimple.
 * 
 * @author Basil Shikin
 *
 */
public class PjsipPhoneProvider
    implements PhoneProvider
{
    private static final String TAG = "PjsipPhoneProvider";

    private static final String VOIP_FILE = "/sdcard/Android/data/vp/book/voip.txt";
    
    private final PjsipManager m_manager;
    private PjsipAccount m_account;
    
    private PhoneListener m_listener;
    
    public PjsipPhoneProvider( Context context)
    {
        m_manager = new PjsipManager( context );
        
    }

    public void initialize() throws Exception
    {
        try
        {
            Log.i(TAG, "Initializing..." );
            
            m_account = loadAccount();
            
            m_manager.startStack();
            m_manager.initializeAccount( m_account );
            m_manager.setIncomingCallListener( new IncomingCallListener() {
                public void incomingCallDetected(PjsipPhoneCall call)
                {
                    attachCallBroadcaster( call );
                    
                    if ( m_listener != null ) m_listener.incomingCallRecieved( call );
                }
            });
        } 
        catch (Exception e)
        {
            Log.e(TAG, "Unable to initialize the stack", e );
            
            throw e;
        }
    }

    public void shutdown()
    {
        try
        {
            Log.i(TAG, "Shutting down..." );
            
            m_manager.stopStack();
        } 
        catch (Exception e)
        {
            Log.e(TAG, "Unable to stop the stack", e );
        }
    }

    public PhoneCall placeCall(String number) throws Exception
    {
        final PjsipPhoneCall phoneCall = m_manager.createOutgoingCall( number, m_account );
        attachCallBroadcaster( phoneCall );
        
        Log.i(TAG, "Placing call to " + number + "(" + phoneCall + ")" );
        
        m_manager.placeCall( phoneCall );
        
        return phoneCall;
    }

    public void hangupCall(PhoneCall phoneCall) throws Exception
    {
        if ( !(phoneCall instanceof PjsipPhoneCall) ) throw new IllegalArgumentException("Invalid phone call specified");
        
        Log.i(TAG, "Terminating " + phoneCall );
        
        m_manager.terminateCall( (PjsipPhoneCall)phoneCall );
    }
    
    public void answerCall(PhoneCall phoneCall, CallAnswer answer) throws Exception
    {
        if ( !(phoneCall instanceof PjsipPhoneCall) ) throw new IllegalArgumentException("Invalid phone call specified");
        
        Log.i(TAG, "Responding to " + phoneCall + " with: " + answer );
        
        if ( answer == CallAnswer.ACCEPT )
        {
            m_manager.answerCall( (PjsipPhoneCall)phoneCall, 200 );
        }
        else if ( answer == CallAnswer.RINGING )
        {
            m_manager.answerCall( (PjsipPhoneCall)phoneCall, 180 );
        }
        else 
        {
            m_manager.terminateCall(  (PjsipPhoneCall)phoneCall );
        }       
    }
    
    public void setListener(PhoneListener listener)
    {
        m_listener = listener;        
    }
    
    private PjsipAccount loadAccount()
    {
        FileInputStream fis = null;
        
        try
        {
            final Properties voip = new Properties();
            
            final File numbersFile = new File( VOIP_FILE );
            fis = new FileInputStream( numbersFile );
            voip.load( fis );
            
            Log.d(TAG, "Loaded account: " + voip );
            
            return new PjsipAccount( voip.getProperty("domain"),
                                     voip.getProperty("username"),
                                     voip.getProperty("password") );
        }
        catch ( Exception e )
        {
            throw new RuntimeException("cant_load_voip", e );
        }
        finally
        {
            if ( fis != null )
                try
                {
                    fis.close();
                } catch (IOException e) {} 
        }   
    }
    
    private void attachCallBroadcaster( final PjsipPhoneCall phoneCall )
    {
        phoneCall.setStatusListener( new PjsipPhoneCallStatusListener() {
            public void phoneCallStatusChanged(PhoneCallStatus newStatus)
            {
                if ( m_listener != null ) m_listener.callStatusChanged( phoneCall );
            }
        });   
    }
}
