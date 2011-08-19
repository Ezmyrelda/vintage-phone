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

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;

/**
 * This is a helper class that is used to manage audio configration required by pjsip. 
 * 
 * <p>
 * <b>Please note:</b> this implementation is based on Regis Montoya's CSipSimple.
 * <p>
 * <b>Please note:</b> this class is for internal use only.
 * 
 * @author Basil Shikin
 *
 */
class PjsipAudioManager
{
    private static final String TAG = "PjsipAudioManager";
    
    private final AudioManager m_audioManager;
    private final OnAudioFocusChangeListener m_focusChangeListener; 
    
    private int m_streamType;
    private int m_defaultVolume;
    private int m_callVolume;
    
    PjsipAudioManager( Context context )
    {
        m_audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        
        m_focusChangeListener = new OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int arg0)
            {
                Log.d(TAG, "Audio focus changed");                
            }
        };
        
        
        
    }
    
    void initialize()
    {
        m_streamType = AudioManager.STREAM_MUSIC; // Archos specific
        
        m_defaultVolume = (int)(m_audioManager.getStreamMaxVolume( m_streamType )*0.8);
        m_callVolume = (int)(m_audioManager.getStreamMaxVolume( m_streamType )*0.8);
        
        m_audioManager.setStreamVolume(m_streamType, m_defaultVolume, 0);
        
    }

    boolean enableAudioIn()
    {
        //Audio routing
        int targetMode = AudioManager.MODE_IN_CALL;

        Log.d( TAG, "Enabling audio in. Target mode: " + targetMode);
        
        m_audioManager.setMode(targetMode);
        m_audioManager.setSpeakerphoneOn( false );
        m_audioManager.setMicrophoneMute( false );
        

        
        m_audioManager.requestAudioFocus(m_focusChangeListener, m_streamType, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        m_audioManager.setStreamSolo( m_streamType, true);
        m_audioManager.setStreamVolume(m_streamType, m_callVolume, 0);
        
//        pjsua.conf_adjust_rx_level(0, 2.5F); // Speaker
        
        return true;
    }
    
    public void clearAudioIn()
    {
        final int streamType = AudioManager.STREAM_MUSIC; // Archos specific
        
        m_audioManager.setStreamSolo(streamType, false);
        m_audioManager.abandonAudioFocus(m_focusChangeListener);
        
        m_audioManager.setStreamVolume(m_streamType, m_defaultVolume, 0);
    }

}
