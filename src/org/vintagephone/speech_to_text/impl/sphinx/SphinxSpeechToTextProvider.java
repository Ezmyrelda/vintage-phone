package org.vintagephone.speech_to_text.impl.sphinx;

import org.vintagephone.speech_to_text.SpeechToTextListener;
import org.vintagephone.speech_to_text.SpeechToTextProvider;

public class SphinxSpeechToTextProvider implements SpeechToTextProvider
{
    private final SphinxSpeechRecognizer m_recognizer;

    public SphinxSpeechToTextProvider()
    {
        m_recognizer = new SphinxSpeechRecognizer();       
    }
    
    public void initialize()
    {
        m_recognizer.initialize();
    }

    public void startListening()
    {
        m_recognizer.startListening();
    }

    public void stopListening( final boolean recognizeLast )
    {
        m_recognizer.stopListening( recognizeLast  );
    }

    public void setListener(SpeechToTextListener listener)
    {
        m_recognizer.setListener( listener );
    }    
}
