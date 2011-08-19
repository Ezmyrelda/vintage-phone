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
 * 
 * This interface defines a phone service 
 * @author Basil Shikin
 *
 */
public interface PhoneProvider
{
    public enum CallAnswer
    {
        ACCEPT,
        RINGING,
        REJECT
    }
    
    /**
     * Initialize the phone. This method should connect to the service provider,
     * initialize sound system, etc.
     * 
     * @return True if initialized
     * @throws Exception 
     */
    public void initialize() throws Exception;
    
    /**
     * Terminate phone.
     */
    public void shutdown();
    
    /**
     * Place a call to a given number. The call should by placed in the
     * same thread. Once connection is established (or failed), control should be
     * released.
     * 
     * @param number String with number to dial. Number format is "+CountryRegionLocal".
     * 
     * @return New phone call. 
     * 
     * @throws Exception In case unable to place a call.
     */
    public PhoneCall placeCall( String number ) throws Exception;
    
    /**
     * Hang up given call
     * 
     * @param phoneCall Call to terminate.
     * 
     * @throws Exception In case unable to terminate a call 
     */
    public void hangupCall( PhoneCall phoneCall ) throws Exception;


    /**
     * Answer given incoming call
     * 
     * @param pendingCall   Call to answer to
     * @param answer        Answer to publish
     * 
     * @throws Exception In case unable to answer the call. 
     */
    public void answerCall(PhoneCall pendingCall, CallAnswer answer) throws Exception;
    
    /**
     * Set a listener that will be notified of phone events.
     * 
     * @param listener Listener to use.
     */
    public void setListener( PhoneListener listener );
}
