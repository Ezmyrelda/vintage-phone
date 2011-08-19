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

import java.io.File;
import java.io.FileFilter;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

/**
 * 
 * This class is used to generate various words that operator might say.
 * 
 * @author Basil Shikin
 *
 */
public class SpeechGenerator
{
    private final static String VOICE_ROOT = "/sdcard/Android/data/vp/voice";
    
    private final static String Tag = "SpeechGenerator";
    
    private final static Random s_random = new Random();

    private volatile CountDownLatch m_playbackLatch = null;
    private volatile MediaPlayer m_activePlayer = null;
    
    public void sayHello()
    {
        sayFolder("hello", true );
    }
    
    public void sayCallingName( final String name)
    {
        final File personFile = new File( VOICE_ROOT + "/calling_person", name.toLowerCase() + ".wav" );
        
        if ( personFile.exists() )
        {
            sayFile( personFile, true  );
        }
        else
        {
            Log.e(Tag, "No calling person file for " + name );
        }
    }
    
    public void sayDialingNumber( final String number )
    {
        sayFolder("dialing", true); // TODO SPELL DIGITS
    }
    
    public void sayCallTerminated()
    {
        sayFolder("terminated", true );
    }
    
    public void sayCallPlaced()
    {
        sayFolder( "placed", true );
    }
    
    public void saySorry()
    {
        sayFolder( "sorry", false );
    }
    
    public void stopTalking()
    {
        if ( m_playbackLatch != null ) m_playbackLatch.countDown();
        if ( m_activePlayer != null )
        {
            m_activePlayer.pause();
            m_activePlayer.stop();
        }
    }
    
    private void sayFolder( final String folderName, final boolean waitUnilSaid)
    {
        final File folder = new File( VOICE_ROOT, folderName );
        if ( folder.isDirectory() )
        {
            final File[] files = folder.listFiles( new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    final String name = pathname.getName();
                    return !name.startsWith(".") && name.endsWith(".wav");
                }
            });
            
            if ( files != null && files.length > 0 )
            {
                final File file = files [ s_random.nextInt( files.length ) ];
                
                sayFile( file, waitUnilSaid );
            }
            else
            {
                Log.e(Tag, "No folder for " + folderName + " is empty");
            }
        }
    }
    
    private void sayFile( final File file, final boolean waitUnilSaid )
    {
        try
        {
            final MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource( file.getAbsolutePath() );
            mediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );            
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            m_activePlayer = mediaPlayer;
            
            if ( waitUnilSaid )
            {
                m_playbackLatch = new CountDownLatch( 1 );
                mediaPlayer.setOnCompletionListener( new OnCompletionListener()
                {
                    public void onCompletion(MediaPlayer mp)
                    {
                        m_playbackLatch.countDown();
                    }
                } );  
                m_playbackLatch.await( 5, TimeUnit.SECONDS );
            }
        }
        catch ( Exception e )
        {
            Log.e(Tag, "Unable to play " + file, e );
        }
    }
}
