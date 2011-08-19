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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.pjsip.pjsua.Callback;

import android.content.Context;

/**
 * Lifecycle manager for PJSIP stack.
 * 
 * <b>Please note:</b> this implementation is based on Regis Montoya's CSipSimple.
 * 
 * @author Basil Shikin
 *
 */
public class PjsipManager
{
    private static final String TAG = "PjsipManager";

    static
    {
        try 
        {
            // Try to load the stack
            System.loadLibrary( "pjsipjni" );
        }
        catch (UnsatisfiedLinkError e) 
        {
            android.util.Log.e(TAG, "Unable to load PJSIP library", e);
        }
    }
    
    private final ExecutorService m_executor;

    private final PjsipCallback m_callback;
    
    private final PjsipAudioManager m_audioManager;
    private final PjsipStackLifecycle m_stackLifecycle;
    private final PjsipAccountInitializer m_accountInitializer; 
    private final PjsipCallManager m_callManager;
    
    private Integer m_tcpTranportId = null;
    private Integer m_udpTranportId = null;
    
    private volatile PjsipAccount m_activeAccount;


    
    
    PjsipManager( Context context )
    {
        final ThreadFactory tf = new ThreadFactory() {
            public Thread newThread(Runnable r)
            {
                return new Thread(r, "pjsip");
            }
            
        };
        m_executor = Executors.newSingleThreadExecutor( tf );
        
        m_callManager = new PjsipCallManager( this );
        m_accountInitializer = new PjsipAccountInitializer( this );
        m_stackLifecycle = new PjsipStackLifecycle( this );
        m_audioManager = new PjsipAudioManager( context );
        m_callback = new PjsipCallback( m_audioManager, this );
    }
    
    /**
     * Start PJSIP stack
     * 
     * @throws Exception In case unable to perform operation 
     */
    void startStack() throws Exception
    {
        final Future<Boolean> future = m_executor.submit( new Callable<Boolean>()
        {
            public Boolean call() throws Exception
            {
                m_audioManager.initialize();
                
                return m_stackLifecycle.startStack();
            }
        });
        
        boolean success = future.get();
        if ( !success ) throw new Exception("cant_start_stack");
    }

    
    /**
     * Stop sip service
     * 
     * @throws Exception In case unable to perform operation
     */
    void stopStack() throws Exception 
    {
        final Future<Boolean> future = m_executor.submit( new Callable<Boolean>()
        {
            public Boolean call() throws Exception
            {
                return m_stackLifecycle.stopStack();
            }
        });
        
        boolean success = future.get();
        if ( !success ) throw new Exception("cant_stop_stack");
    }
    
    /**
     * Initialize given account. Provided account will be used as
     *  
     * @param account Account to register
     * 
     * @throws Exception In case unable to perform operation
     */
    void initializeAccount(final PjsipAccount account) throws Exception
    {
        final Future<Boolean> future = m_executor.submit( new Callable<Boolean>()
        {
            public Boolean call() throws Exception
            {
                return m_accountInitializer.initalize( account);
            }
        });
        
        final Boolean success = future.get();
        if ( !success ) throw new Exception("cant_initalize_account");
    }
    
    /**
     * Place a given call
     * 
     * @param phoneCall call to place
     * 
     * @throws Exception In case unable to place the call
     */
    void placeCall( final PjsipPhoneCall phoneCall ) throws Exception
    {
        final Future<Boolean> future = m_executor.submit( new Callable<Boolean>()
        {
            public Boolean call() throws Exception
            {
                return m_callManager.placeCall( phoneCall );
            }
        });
        
        final boolean success = future.get();
        if ( !success ) throw new Exception("cant_place_call");
    }
    
    /**
     * Terminate given call
     * 
     * @param phoneCall Call to terminate
     * 
     * @throws Exception In case unable to terminate call 
     */
    void terminateCall(final PjsipPhoneCall phoneCall) throws Exception
    {
        final Future<Boolean> future = m_executor.submit( new Callable<Boolean>()
        {
            public Boolean call() throws Exception
            {
                return m_callManager.terminateCall(phoneCall);
            }
        });
        
        final boolean success = future.get();
        if ( !success ) throw new Exception("cant_terminate_call");
    }
    
    /**
     * Answer an incoming phone call with given code
     * 
     * @param phoneCall  Call to answer
     * @param code       Code to answer the phone with
     * 
     * @throws Exception In case unable to answer the call
     */
    void answerCall( final PjsipPhoneCall phoneCall, final int code ) throws Exception
    {
        final Future<Boolean> future = m_executor.submit( new Callable<Boolean>()
        {
            public Boolean call() throws Exception
            {
                return m_callManager.answerCall( phoneCall, code);
            }
        });
        
        final boolean success = future.get();
        if ( !success ) throw new Exception("cant_answer_call");
    }
    
    void execute(Runnable runnable)
    {
        m_executor.execute( runnable );
    }

    Integer getTcpTranportId()
    {
        return m_tcpTranportId;
    }

    Integer getUdpTranportId()
    {
        return m_udpTranportId;
    }
    
    void setIncomingCallListener(IncomingCallListener listener)
    {
        m_callback.setIncomingCallListener( listener );
    }
    
    void setActiveAccount(PjsipAccount account)
    {
        m_activeAccount = account;
    }
    
    PjsipPhoneCall getActiveCall()
    {
        return m_callManager.getActiveCall();
    }
    
    PjsipAccount getActiveAccount()
    {
        return m_activeAccount;
    }

    Callback getCallback()
    {
        return m_callback;
    }

    PjsipPhoneCall createIncomingCall(int accountId, int callId) throws Exception
    {
        return m_callManager.createIncomingCall(accountId, callId);
    }
    
    PjsipPhoneCall createOutgoingCall(String number, PjsipAccount account) throws Exception
    {
        return m_callManager.createOutgoingCall(number, account);
    }
    
    void setUdpTransportId(Integer udpTranportId)
    {
        m_udpTranportId = udpTranportId;
    }

    void setTcpTransportId(Integer tcpTranportId)
    {
        m_tcpTranportId = tcpTranportId;
    }
    
    
    interface IncomingCallListener
    {
        void incomingCallDetected( PjsipPhoneCall call );
    }
}
