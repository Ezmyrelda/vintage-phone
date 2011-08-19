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
import java.util.List;

import org.pjsip.pjsua.pjmedia_srtp_use;
import org.pjsip.pjsua.pjsip_transport_type_e;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_config;
import org.pjsip.pjsua.pjsua_logging_config;
import org.pjsip.pjsua.pjsua_media_config;
import org.pjsip.pjsua.pjsua_transport_config;

import android.util.Log;

/**
 * This is a helper class that is used to start and stop pjsip stack. 
 * 
 * <p>
 * <b>Please note:</b> this implementation is based on Regis Montoya's CSipSimple.
 * <p>
 * <b>Please note:</b> this class is for internal use only.
 * 
 * @author Basil Shikin
 *
 */
class PjsipStackLifecycle
{
    private static final String TAG = "PjsipStackLifecycle";

    private final PjsipManager m_manager;
    
    PjsipStackLifecycle(PjsipManager manager)
    {
        m_manager = manager;
    }

    boolean startStack()
    {
        Log.i(TAG, "Starting PJSIP stack...");
        
        int status;
        status = pjsua.create();

        Log.d(TAG, "PJSUA creation status: " + status);

        //
        // General configuration
        //
        pjsua_config cfg = new pjsua_config();
        pjsua.config_default(cfg);
        
        cfg.setCb(pjsuaConstants.WRAPPER_CALLBACK_STRUCT);
        pjsua.setCallbackObject( m_manager.getCallback() );
        
        Log.d(TAG, "Callback attached to PJSUA");
        
        pjsua.set_use_compact_form( pjsua.PJ_FALSE );
        pjsua.set_no_update( pjsua.PJ_TRUE );
        cfg.setUser_agent(pjsua.pj_str_copy( "VintagePhone-" + android.os.Build.DEVICE + "-" + android.os.Build.VERSION.SDK ) );
        cfg.setThread_cnt( 3 );
        cfg.setUse_srtp( pjmedia_srtp_use.swigToEnum( 0 ) );
        cfg.setSrtp_secure_signaling(0);

        pjsua_logging_config logCfg = new pjsua_logging_config();
        pjsua.logging_config_default(logCfg);
        logCfg.setConsole_level( 3 );
        logCfg.setLevel( 3 );
        logCfg.setMsg_logging(pjsuaConstants.PJ_TRUE);

        pjsua_media_config mediaCfg = new pjsua_media_config();
        pjsua.media_config_default(mediaCfg);
        mediaCfg.setChannel_count(1);
        mediaCfg.setSnd_auto_close_time( 1 );


        mediaCfg.setEc_tail_len( 200 ); // 200 if echo cancellation 
        mediaCfg.setEc_options( 2 );
        mediaCfg.setNo_vad( 1 ); 
        mediaCfg.setQuality( 4 );
        mediaCfg.setClock_rate( 16000 );
        mediaCfg.setAudio_frame_ptime( 20 );       
        mediaCfg.setHas_ioqueue( 0 );

        mediaCfg.setEnable_ice( 0 );

        //
        // Initialize
        //
        status = pjsua.csipsimple_init(cfg, logCfg, mediaCfg);
        
        if (status == pjsuaConstants.PJ_SUCCESS) 
        {
            Log.i(TAG, "PJUSA initlaized." );
        
            //
            // Add transports
            //
            pjsip_transport_type_e transportType;
            
            Log.d(TAG, "Initializing UDP transport..." );
            
            transportType  = pjsip_transport_type_e.PJSIP_TRANSPORT_UDP;
            Integer udpTranportId = createTransport(transportType, 0 );
            if (udpTranportId != null) 
            {
                m_manager.setUdpTransportId( udpTranportId ); 
            }
            else
            {
                Log.e(TAG, "Unable to create UDP transport");
                
                return false;
            }

            // We need a local account to not have the
            // application lost when direct call to the IP
            int[] udp_acc_id = new int[1];
            pjsua.acc_add_local(udpTranportId, pjsua.PJ_FALSE, udp_acc_id);

            Log.d(TAG, "UDP account created (" + udp_acc_id + " )");

            
            Log.d(TAG, "Initializing TCP transport..." );
            
            transportType = pjsip_transport_type_e.PJSIP_TRANSPORT_TCP;
            Integer tcpTranportId = createTransport(transportType, 0);
            if (tcpTranportId != null) 
            {
                m_manager.setTcpTransportId( tcpTranportId ); 
            }
            else
            {
                Log.e(TAG, "Unable to create TCP transport");
                
                return false;
            }

            // We need a local account to not have the
            // application lost when direct call to the IP
            int[] tcp_acc_id = new int[1];
            pjsua.acc_add_local(tcpTranportId, pjsua.PJ_FALSE, tcp_acc_id);

            Log.d(TAG, "TCP account created (" + tcp_acc_id + " )");

            Log.d(TAG, "Initializing RTP transport..." );

            pjsua_transport_config rtpConfig = new pjsua_transport_config();
            pjsua.transport_config_default( rtpConfig );
            rtpConfig.setPort( 4000 );
            
            status = pjsua.media_transports_create(rtpConfig );
            if (status == pjsuaConstants.PJ_SUCCESS) 
            {
                status = pjsua.start();
                if (status == pjsua.PJ_SUCCESS) 
                {
                    initCodecs();

                    Log.i(TAG, "PJSIP stack started");
                    
                    return true;
                }
                else
                {
                    Log.e( TAG, "Unable to start PJSUSA: " + pjsua.get_error_message(status).getPtr() );
                    
                    return false;
                }
            }
            else
            {
                Log.e( TAG , "Unable ito initalize transports: " + pjsua.get_error_message(status).getPtr() );
                
                return false;
            }
        }
        else
        {
            Log.e(TAG, "Unable to initlaize PJUSA:" + pjsua.get_error_message(status).getPtr() );
            
            return false;
        }
    }

    boolean stopStack() 
    {
        Log.i( TAG, "Stopping PJSIP stack");
        
        pjsua.csipsimple_destroy();
        
        return true;
    }
    
    private void initCodecs()
    {
        int nbrCodecs = pjsua.codecs_get_nbr();
        Log.d( TAG, "Codec nbr : " + nbrCodecs);
        
        final List<String> codecs = new ArrayList<String>();
        for (int i = 0; i < nbrCodecs; i++) 
        {
            String codecId = pjsua.codecs_get_id(i).getPtr();
            codecs.add(codecId);
            Log.d( TAG, "Added codec " + codecId);
        }
        
        for ( String codec : codecs )
        {
            final short priority = getCodecPriority(codec, (short)130);
            
            Log.d( TAG, "Updating priority for codec \"" + codec + "\": " + priority );
            
            pjsua.codec_set_priority( pjsua.pj_str_copy(codec), priority );
        }
    }
    
    
    private static short getCodecPriority(String codec, short defaultPriority)
    {
        if ( codec == null || codec.length() < 1 ) return 0;
        
        else if ( codec.equals("G729/8000/1") ) return 400;
        else if ( codec.equals("GSM/8000/1") ) return 380;
        else if ( codec.equals("iLBC/8000/1") ) return 290;
        else if ( codec.equals("speex/8000/1") ) return 270;
        else if ( codec.equals("G722/16000/1") ) return 235;
        else if ( codec.equals("speex/32000/1") ) return 220;
        else if ( codec.equals("speex/16000/1") ) return 219;
        else if ( codec.equals("PCMU/8000/1") ) return 60;
        else if ( codec.equals("PCMA/8000/1") ) return 50;
        else if ( codec.equals("SILK/8000/1") ) return 0;
        else if ( codec.equals("SILK/12000/1") ) return 0;
        else if ( codec.equals("SILK/16000/1") ) return 0;
        else if ( codec.equals("SILK/24000/1") ) return 0;
        else if ( codec.equals("CODEC2/8000/1") )  return 0;
        else if ( codec.equals("G7221/16000/1") )  return 0;
        else if ( codec.equals("G7221/32000/1") ) return 0;
        else 
        {
            Log.w( TAG, "No priority found for codec \"" + codec + "\" using default " + defaultPriority  );
            
            return defaultPriority;
        }
    }
    
    private static Integer createTransport(pjsip_transport_type_e type, int port)
    {
        pjsua_transport_config cfg = new pjsua_transport_config();
        int[] tId = new int[1];
        pjsua.transport_config_default(cfg);
        cfg.setPort(port);

        final int status = pjsua.transport_create(type, cfg, tId);
        if (status == pjsuaConstants.PJ_SUCCESS) 
        {
            return tId[0];
        }
        else
        {
            Log.e(TAG, "Unable to create transport: " +  pjsua.get_error_message(status).getPtr()  + " (" + status + ")");
            return null;
        }
    }
}
