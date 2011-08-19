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
package org.vintagephone.model;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vintagephone.speech_to_text.SpeechToTextListener;
import org.vintagephone.speech_to_text.SpeechToTextProvider;
import org.vintagephone.speech_to_text.impl.sphinx.SphinxSpeechToTextProvider;

import android.util.Log;

/**
 * This class is used to recognize different sentences that could be said
 * by the user.
 * 
 * @author Basil Shikin
 *
 */
public class SpeechRecognizer
{
    private static final Pattern CALL_PATTERN = Pattern.compile("^(call [a-zA-Z]+)");
    private static final Pattern DIAL_PATTERN = Pattern.compile("^(dial (((one)|(two)|(three)|(four)|(five)|(six)|(seven)|(eight)|(nine)|(zero)|(plus))\\s?)+)");
    
    private static final String Tag = "SpeechRecognizer";
    
    private final SpeechToTextProvider m_speechToTextProvider;
    private final TextListener m_textListener;
    
    private volatile CountDownLatch m_recognitionLatch;
    
    

    public SpeechRecognizer()
    {
        m_speechToTextProvider = new SphinxSpeechToTextProvider();
        m_textListener = new TextListener();
    }

    /**
     * Start speech stack and attach listeners
     */
    public void initialize()
    {
        m_speechToTextProvider.setListener( m_textListener );
        m_speechToTextProvider.initialize();        
    }

    public void stopListening( boolean recognize )
    {
        m_speechToTextProvider.stopListening( recognize );
    }
    
    public DialTargetResult recognizeDialTarget() throws InterruptedException
    {
        Log.d(Tag, "Waiting for dial target...");
               
        // Wait to recognize target
        m_recognitionLatch.await( 3, TimeUnit.SECONDS );
        
        stopListening( true );
        
        if ( m_textListener.m_stopSaid ) return null;
        
        if ( m_textListener.m_dialNumber != null )
        {
            Log.d(Tag, "Recognized dial number: " + m_textListener.m_dialNumber );
            
            return new DialNumberResult( m_textListener.m_dialNumber );
        }
        else if ( m_textListener.m_dialPerson != null )
        {
            Log.d(Tag, "Recognized dial person: " + m_textListener.m_dialPerson );
            
            return new DialPersonResult( m_textListener.m_dialPerson );
        }
        
        Log.d(Tag, "Dial target not recognized" );
        
        return null;
    }
    
    public void startWaiting()
    {        
        m_recognitionLatch = new CountDownLatch( 1 );
        m_textListener.reset();
        
        m_speechToTextProvider.startListening();
    }
    
    public boolean waitForNo(long duration) throws InterruptedException
    {
        Log.d(Tag, "Waiting for \"no\".");
        
        m_recognitionLatch.await( duration, TimeUnit.SECONDS );
        stopListening( true );
        
        return !m_textListener.m_noSaid && !m_textListener.m_stopSaid;        
    }
    
    public boolean waitForYes(int duration) throws InterruptedException
    {
        Log.d(Tag, "Waiting for \"yes\".");
        
        m_recognitionLatch = new CountDownLatch( 1 );
        m_textListener.reset();
        
        m_recognitionLatch.await( duration, TimeUnit.SECONDS );
        
        return m_textListener.m_yesSaid;    
    }
    
    static class DialTargetResult { }
    
    static class DialNumberResult
        extends DialTargetResult
    {
        public final String number;

        public DialNumberResult(String number)
        {
            this.number = number;
        }
    }
    
    static class DialPersonResult
        extends DialTargetResult
    {
        public final String name;

        public DialPersonResult(String name)
        {
            this.name = name;
        }
    }
    
    
    private class TextListener
        implements SpeechToTextListener
    {
        private static final String Tag = "TextListener";
        
        String m_dialNumber;
        String m_dialPerson;
        
        boolean m_stopSaid;
        boolean m_yesSaid;
        boolean m_noSaid;
    
        void reset()
        {
            m_dialNumber = null; 
            m_dialPerson = null; 
            m_yesSaid = false; 
            m_noSaid = false; 
            m_stopSaid = false;             
        }
        
        public void partRecognized(String text)
        {
            processText( text.toLowerCase()  );
        }
    
        public void fullyRecognized(String text)
        {
            processText( text.toLowerCase() );            
        }
    
        public void errorOccured(String error)
        {
            Log.e( Tag, "Error: " + error );
        }
        
        private void processText( String text )
        {
            Log.d( Tag, "Processing recognized text \"" + text + "\"...");
            
            if ( m_recognitionLatch != null )
            {
                boolean isRecognized = false;
                
                Matcher matcher = CALL_PATTERN.matcher( text );
                if ( matcher.matches() )
                {
                    m_dialPerson = text.substring( 5 );
                    
                    Log.i( Tag, "Text recognized as a call to \"" + m_dialPerson + "\"");
                    
                    isRecognized = true;
                }
                
                matcher = DIAL_PATTERN.matcher( text );
                if ( matcher.matches() )
                {
                    m_dialNumber = text.substring( 5 );
                    
                    Log.i( Tag, "Text recognized as a dial of to \"" + m_dialNumber + "\"");
                    
                    isRecognized = true;
                }
                
                if ( text.contains("stop") )
                {
                    m_stopSaid = true;
                    
                    isRecognized = true;
                }
                
                if ( "yes".equals( text ) )
                {
                    m_yesSaid = false;
                    
                    isRecognized = true;
                }
                
                if ( text.contains("no") )
                {
                    m_noSaid = true;
                    
                    isRecognized = true;
                }
                
                if ( isRecognized )
                {
                    m_recognitionLatch.countDown();
                }
            }
        }
    }


   



}
