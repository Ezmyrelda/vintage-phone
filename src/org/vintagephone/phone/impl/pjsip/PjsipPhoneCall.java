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

import org.vintagephone.phone.PhoneCall;


/**
 * This object represents a phone call handled by PJSIP 
 * 
 * <p>
 * <b>Please note:</b> this implementation is based on Regis Montoya's CSipSimple.
 * 
 * @author Basil Shikin
 *
 */
public class PjsipPhoneCall
    extends PhoneCall
{
    private final String m_callee;
    private final PjsipAccount m_account;
    
    private int m_pjsipCallId;
    
    private PjsipPhoneCallStatusListener m_listener;
    
    public PjsipPhoneCall(String caller, String callee, PjsipAccount account )
    {
        super( caller, callee );
        
        m_callee = callee;
        m_account= account;
    }

    public String getCallee()
    {
        return m_callee;
    }

    public PjsipAccount getPjsipAccount()
    {
        return m_account;
    }

    
    public int getPjsipCallId()
    {
        return m_pjsipCallId;
    }

    void setPjsipCallId(int pjsipCallId)
    {
        m_pjsipCallId = pjsipCallId;
    }

    void setCallStatus( PhoneCallStatus status)
    {
        if ( m_callStatus != status )
        {
            m_callStatus = status;
        
            if ( m_listener != null ) m_listener.phoneCallStatusChanged( status );
        }
    }
    
    void setStatusListener( PjsipPhoneCallStatusListener listener )
    {
        m_listener = listener;
    }
    
    interface PjsipPhoneCallStatusListener
    {
        void phoneCallStatusChanged( PhoneCallStatus newStatus );
    }
}
