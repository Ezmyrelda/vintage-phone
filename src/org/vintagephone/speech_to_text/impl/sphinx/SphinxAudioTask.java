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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * This class is used to read audio data from input audio device
 * <p>
 * 	This implementation is based on PocketSphinxDemo by David Huggins-Daines <dhuggins@cs.cmu.edu>.
 * </p>
 * 
 * @author Basil Shikin.
 */
class SphinxAudioTask 
	implements Runnable 
{
    private static final int DEFAULT_BLOCK_SIZE = 1024;
    private static final String Tag = "SphinxAudioThread";
    
    
    // Parent objects
    private final BlockingQueue<short[]> m_audioQueue;
    private final AudioRecord 		 m_audioRecord;
    
    // State variables
    private int m_blockSize = DEFAULT_BLOCK_SIZE;
    private volatile boolean m_isTerminated = false;
    
    SphinxAudioTask() 
    {
        m_audioQueue = new LinkedBlockingQueue<short[]>();
	
        m_audioRecord = new AudioRecord( MediaRecorder.AudioSource.DEFAULT, 
    					     8000, // Sample rate
    					     AudioFormat.CHANNEL_IN_MONO,
    					     AudioFormat.ENCODING_PCM_16BIT, 
    					     8192 ); // Min. buffer size
    }
    
    
    int getBlockSize() 
    {
        return m_blockSize;
    }
    
    void setBlockSize(int block_size) 
    {
        m_blockSize = block_size;
    }
    
    short[] readNext( boolean waitForData ) 
        throws InterruptedException
    {
        if ( waitForData )
        {
            return m_audioQueue.take();
        }
        else
        {
            return m_audioQueue.poll();
        }
    }
    
    void stop() 
    {
        m_isTerminated = true;        
    }
    
    public void run() 
    {
        m_audioRecord.startRecording();
        while ( !m_isTerminated ) 
        {
            final int read = readBlock();
            if ( read <= 0) break;
        }
        
        m_audioRecord.stop();
        m_audioRecord.release();
    }

    private int readBlock()
    {
        short[] buffer = new short[this.m_blockSize];
        final int read = this.m_audioRecord.read(buffer, 0, buffer.length);

        Log.d(Tag, "Read " + read + " samples");

        if (read > 0)
        {
            m_audioQueue.add(buffer);
        }

        return read;
    }
}
