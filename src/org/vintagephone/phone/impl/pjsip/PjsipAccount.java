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

/**
 * This class represents a pjsip account 
 * 
 * <p>
 * <b>Please note:</b> this implementation is based on Regis Montoya's CSipSimple.
 * <p>
 * <b>Please note:</b> this class is for internal use only.
 * 
 * @author Basil Shikin
 *
 */
public class PjsipAccount
{
    private final String m_domain;
    private final String m_username;
    private final String m_password;
    
    private int m_pjsipAccountId;
    
    
    public PjsipAccount(String domain, String username, String password)
    {
        m_domain = domain;
        m_username = username;
        m_password = password;
    }

    public String getDomain()
    {
        return m_domain;
    }
    
    public String getUsername()
    {
        return m_username;
    }
    
    public String getPassword()
    {
        return m_password;
    }
    
    int getPjsipAccountId()
    {
        return m_pjsipAccountId;
    }
    
    void setPjsipAccountId(int accountId)
    {
        m_pjsipAccountId = accountId;
    }
   

}
