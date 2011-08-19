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

import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjsip_cred_info;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_acc_config;

import android.net.Uri;
import android.util.Log;

/**
 * This is a helper class that is used to initialize PJSIP accounts 
 * 
 * <p>
 * <b>Please note:</b> this implementation is based on Regis Montoya's CSipSimple.
 * <p>
 * <b>Please note:</b> this class is for internal use only.
 * 
 * @author Basil Shikin
 *
 */
class PjsipAccountInitializer
{
    private static final String TAG = "PjsipAccountInitializer";
    
    private final PjsipManager m_manager;

    PjsipAccountInitializer( PjsipManager pjsipManager)
    {
        m_manager = pjsipManager;
    }

    
    boolean initalize( final PjsipAccount account )
    {
        int status = pjsuaConstants.PJ_FALSE;
        
        final Integer accountId = addAccount( account );

        if ( accountId != null )
        {
            status = pjsua.acc_set_online_status(accountId, 1);
            
            if ( status == pjsuaConstants.PJ_SUCCESS )
            {
                account.setPjsipAccountId( accountId );
                
                m_manager.setActiveAccount( account );
                return true;
            }
            else
            {
                Log.e( TAG, "Unable to set account online");
            }
        }

        return false;
    }
    
    private Integer addAccount( final PjsipAccount account )
    {
        final String regUri = "sip:" + account.getDomain();
        final String[] proxies = new String[] { regUri + ";transport=udp;lr" } ;
        final String sipAccount = "<sip:" + Uri.encode( account.getUsername() ) + "@" + 
                              account.getDomain() + ">";
        
        Log.i( TAG, "Initializing account " + sipAccount );
        
        final pjsua_acc_config configuration = new pjsua_acc_config();
        pjsua.acc_config_default(configuration);
        
        configuration.setPriority( 100 );
        configuration.setId(pjsua.pj_str_copy( sipAccount ));
        
        configuration.setTransport_id ( m_manager.getUdpTranportId() );
        
        configuration.setReg_uri(pjsua.pj_str_copy( regUri ) );
        
        configuration.setPublish_enabled( 0 );
        
        configuration.setReg_timeout( 1800 );
        
        configuration.setReg_delay_before_refresh( -1 );
        
        configuration.setAllow_contact_rewrite( pjsuaConstants.PJ_TRUE );
        configuration.setContact_rewrite_method( 1 ); // Callcentric-specific
        
        Log.d( TAG, "Creating " + proxies.length + " proxies");
        
        configuration.setProxy_cnt( proxies.length);
        
        pj_str_t[] prxs = configuration.getProxy();
        int i = 0;
        for(String proxy : proxies) 
        {
            Log.d( TAG, "Adding proxy: " + proxy);
            prxs[i] = pjsua.pj_str_copy(proxy);
            i += 1;
        }
        
        configuration.setProxy(prxs);
        
        configuration.setReg_use_proxy( 3 );

        configuration.setCred_count(1);
        pjsip_cred_info cred_info = configuration.getCred_info();
            
        cred_info.setRealm(pjsua.pj_str_copy( "*") );
        cred_info.setUsername(pjsua.pj_str_copy( account.getUsername() ) );

        cred_info.setData_type( 0 ); // Plain text password
        cred_info.setData(pjsua.pj_str_copy( account.getPassword() ) );
        
        final int[] accId = new int[1];
        final int status = pjsua.acc_add( configuration, pjsuaConstants.PJ_TRUE, accId);
        
        if ( status == pjsuaConstants.PJ_SUCCESS)
        {
            Log.d(TAG, "Account added, new account id: " + accId[0] );
            
            return accId[0];
        }
        else
        {
            Log.d(TAG, "Unable to add account: " + pjsua.get_error_message(status).getPtr() );
            
            return null;
        }
    }
}
