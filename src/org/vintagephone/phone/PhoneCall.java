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
package org.vintagephone.phone;

/**
 * This object placed or recieved phone call.
 * 
 * @author Basil Shikin
 */
public abstract class PhoneCall
{
    public enum PhoneCallStatus
    {
        /**
         * Ringing up the number
         */
        RINGING,
        
        /**
         * Call is in progress
         */
        ACTIVE,
        
        /**
         * Call is ringing, but has not been picked up yet
         */
        PENDING,
        
        /**
         * Line is busy
         */
        BUSY,
        
        /**
         * Call has failed
         */
        FAILED, 
        
        /**
         * Call has finished
         */
        FINISHED
    }
    
    protected final String m_callee; // He is being called
    protected final String m_caller; // He started the call
    
    protected volatile PhoneCallStatus m_callStatus = PhoneCallStatus.FAILED;
    
    
    protected PhoneCall(String caller, String callee)
    {
        m_caller = caller;
        m_callee = callee;
    }

    public PhoneCallStatus getCallStatus()
    {
        return m_callStatus;
    }

    public String getCaller()
    {
        return m_caller;
    }

    public String getCallee()
    {
        return m_callee;
    }
}
