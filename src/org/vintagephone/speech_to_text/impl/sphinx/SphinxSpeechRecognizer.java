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
package org.vintagephone.speech_to_text.impl.sphinx;

import java.util.concurrent.atomic.AtomicBoolean;

import org.vintagephone.speech_to_text.SpeechToTextListener;

import android.util.Log;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.pocketsphinx;

/**
 * This class is used to recognize speech from input device.
 * <p>
 * This implementation is based on PocketSphinxDemo by David Huggins-Daines
 * <dhuggins@cs.cmu.edu>.
 * </p>
 * 
 * @author Basil Shikin.
 */
class SphinxSpeechRecognizer implements Runnable
{
    private static final String Tag = null;

    static 
    {
        try
        {
            System.loadLibrary("pocketsphinx_jni");
        }
        catch ( Exception e )
        {
            Log.e( Tag, "Unable to load sphinx library",e );
        }
    }
    
    // Child objects
    private SphinxAudioTask m_audioTask;   
    private Decoder m_decoder;
    
    // State objects
    private AtomicBoolean m_isListening = new AtomicBoolean( false );
    
    private Thread m_audioThread;
    private Thread m_recognizerThread;
    
    private SpeechToTextListener m_listener;
    
    private volatile boolean m_isStopped = false;
    private String m_lastHypothesis = "";
    
      
    void initialize()
    {
        pocketsphinx.setLogfile("/sdcard/Android/data/edu.cmu.pocketsphinx/pocketsphinx.log");

        final Config sphinxConfig = new Config();
        sphinxConfig.setString("-hmm", "/sdcard/Android/data/vp/hmm/hub4wsj_sc_8k-b");
        sphinxConfig.setString("-dict", "/sdcard/Android/data/vp/lm/2996.dic");
        sphinxConfig.setString("-lm", "/sdcard/Android/data/vp/lm/2996.dmp");

        sphinxConfig.setString("-rawlogdir", "/sdcard/Android/data/vp/raw");
        sphinxConfig.setFloat("-samprate", 8000.0);
        sphinxConfig.setInt("-maxhmmpf", 2000);
        sphinxConfig.setInt("-maxwpf", 10);
        sphinxConfig.setInt("-pl_window", 2);
        sphinxConfig.setBoolean("-backtrace", true);
        sphinxConfig.setBoolean("-bestpath", false);
        
        m_decoder = new Decoder(sphinxConfig);        
    }
    
    void setListener( final SpeechToTextListener listener )
    {
        m_listener = listener;
    }

    void startListening()
    {
        if ( m_isListening.compareAndSet( false, true ) )
        {
            Log.i( Tag, "Starting voice recognizer...");
            
            m_decoder.startUtt();
            
            // Start audio
            m_audioTask = new SphinxAudioTask();
            m_audioThread = startThread( m_audioTask, "audio" );
            
            // Clear state
            m_isStopped = false; 
            m_lastHypothesis = "";
            
            // Start recognizer            
            m_recognizerThread = startThread( this, "rec" );
            
            Log.i( Tag, "Voice recognizer started");
        }
    }
    
    void stopListening( boolean shouldRecognize )
    {
        if ( m_isListening.compareAndSet( true, false ) )
        {
            Log.i( Tag, "Stopping voice recognizer...");
                    
            m_isStopped = true;
            
            m_audioTask.stop();
            try
            {
                m_audioThread.join();
            } 
            catch (InterruptedException e)
            {
                Log.e( Tag, "Interrupted waiting for audio thread, shutting down", e );
                
                m_listener.errorOccured("audio_thread_interrupted");                
            }
            
            m_isStopped = true;
            try
            {
                m_recognizerThread.join();
            } 
            catch (InterruptedException e)
            {
                Log.e( Tag, "Interrupted waiting for recognizer thread, shutting down", e );
                
                m_listener.errorOccured("recognizer_thread_interrupted");
            }
            
            
            if ( shouldRecognize )
            {
                try
                {
                    Log.i( Tag, "Running voice recognition...");
                                
                    short[] buffer = m_audioTask.readNext( false ); 
                    while ( buffer != null )
                    {
                        Log.d( Tag, "Processing" + buffer.length + " samples from queue");
                        
                        m_decoder.processRaw(buffer, buffer.length, false, false);
                        
                        buffer = m_audioTask.readNext( false );
                    }
                                
                    m_decoder.endUtt();
                    
                    final Hypothesis hypothesis = this.m_decoder.getHyp();
                    if ( hypothesis != null )
                    {
                        m_listener.fullyRecognized( hypothesis.getHypstr() );
                        
                        Log.i( Tag, "Voice recognition completed (recognized \"" + hypothesis.getHypstr() + "\")" );
                    }
                    else
                    {
                        m_listener.errorOccured("no_hypothesis");
                        
                        Log.i( Tag, "Voice recognition failed");
                    }
                }
                catch ( InterruptedException e )
                {
                    Log.d( Tag, "Voice recognition failed");
                }
            }
            else
            {
                m_decoder.endUtt();
            }
            
            Log.i( Tag, "Voice recognizer stopped");
        }
    }
       
    public void run()
    {
        while ( !m_isStopped )
        {
            try
            {
                Log.d( Tag, "Reading more samples from queue");
                short[] buffer = m_audioTask.readNext( true );
                
                Log.d( Tag, "Processing" + buffer.length + " samples from queue");
                m_decoder.processRaw(buffer, buffer.length, false, false);
                
                
                final Hypothesis hypothesis = this.m_decoder.getHyp();
                if (hypothesis != null)
                {
                    final String newHypothesis = hypothesis.getHypstr();
                    
                    if ( !m_lastHypothesis.equals( newHypothesis ) )
                    {
                        Log.d( Tag, "New hypothesis discovered: " + newHypothesis );
                        
                        m_listener.partRecognized( newHypothesis );
                    }
                    
                    m_lastHypothesis = newHypothesis != null ? newHypothesis : "";
                }
            } 
            catch (InterruptedException e)
            {
                Log.d(Tag, "Interrupted in reading next block from audio queue");
            }
        }
    }
    
    private static Thread startThread( Runnable task, String debugName )
    {
        final Thread result = new Thread( task, "sphx:" + debugName);
        result.start();
        
        return result;
    }
}
