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
package org.vintagephone.hardware.impl.ioio;

import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.exception.ConnectionLostException;
import android.util.Log;

/**
 * 
 * Based on implementation in AbstractIOIOActivity
 *
 */
public class IOIOThreadBase extends Thread {
    /** Subclasses should use this field for controlling the IOIO. */
    protected IOIO m_ioio;
    private boolean m_abort = false;

    /** Not relevant to subclasses. */
    @Override
    public final void run() {
        super.run();
        while (true) {
            try {
                synchronized (this) {
                    if (m_abort) {
                        break;
                    }
                    m_ioio = IOIOFactory.create();
                }
                m_ioio.waitForConnect();
                setup();
                while (true) {
                    loop();
                }
            } catch (ConnectionLostException e) {
                if (m_abort) {
                    break;
                }
            } catch (Exception e) {
                Log.e("AbstractIOIOActivity",
                        "Unexpected exception caught", e);
                m_ioio.disconnect();
                break;
            } finally {
                try {
                    m_ioio.waitForDisconnect();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Subclasses should override this method for performing operations to
     * be done once as soon as IOIO communication is established. Typically,
     * this will include opening pins and modules using the openXXX()
     * methods of the {@link #m_ioio} field.
     */
    protected void setup() throws ConnectionLostException {
    }

    /**
     * Subclasses should override this method for performing operations to
     * be done repetitively as long as IOIO communication persists.
     * Typically, this will be the main logic of the application, processing
     * inputs and producing outputs.
     */
    protected void loop() throws ConnectionLostException {
    }

    /** Not relevant to subclasses. */
    public synchronized final void abort() {
        m_abort = true;
        if (m_ioio != null) {
            m_ioio.disconnect();
        }
    }
}