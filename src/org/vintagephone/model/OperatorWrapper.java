package org.vintagephone.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.vintagephone.model.SpeechRecognizer.DialNumberResult;
import org.vintagephone.model.SpeechRecognizer.DialPersonResult;
import org.vintagephone.model.SpeechRecognizer.DialTargetResult;

import android.util.Log;

public class OperatorWrapper
{
    private static final String Tag = "OperatorWrapper";


    private static final String NUMBERS_FILE = "/sdcard/Android/data/vp/book/numbers.txt";


    private final SpeechRecognizer m_speechRecognizer;
    private final SpeechGenerator m_speechGenerator;
    
    private final Properties m_numbers;
    
    private volatile boolean m_terminateInteraction = false;


    public OperatorWrapper()
    {
        m_speechRecognizer = new SpeechRecognizer();
        m_speechGenerator = new SpeechGenerator();
        
        m_numbers = new Properties();        
    }
    
    public void initialize() throws Exception
    {
        m_speechRecognizer.initialize();
        
        loadNumbers();
    }
    


    public void stopTalking()
    {
        m_terminateInteraction = true;
        m_speechRecognizer.stopListening( false );
        m_speechGenerator.stopTalking();
    }

    public String askPhoneNumber()
    {
        try
        {
            m_terminateInteraction = false;
            
            boolean lastTimeMissed = false;
            
            while ( !m_terminateInteraction )
            {
                m_speechRecognizer.startWaiting();
                
                if ( !lastTimeMissed )
                {
                    m_speechGenerator.sayHello();
                }
                else
                {
                    m_speechGenerator.saySorry();
                }
                lastTimeMissed = false;
                
                
                final DialTargetResult result = m_speechRecognizer.recognizeDialTarget();
                if ( result instanceof DialNumberResult )
                {                    
                    final String number = ((DialNumberResult)result).number;
                    
                    if ( confirmNumber ( number ) && !m_terminateInteraction)
                    {
                        m_speechGenerator.sayCallPlaced();
                        
                        return resolveNumber( number );
                    }
                    else
                    {
                        m_speechGenerator.sayCallTerminated();
                    }
                }
                else if ( result instanceof DialPersonResult )
                {
                    final String person = ((DialPersonResult)result).name;
                    
                    if ( isKnownPerson( person ) )
                    {
                        if ( confirmPerson ( person ) && !m_terminateInteraction )
                        {
                            m_speechGenerator.sayCallPlaced();
                            
                            return resolveNumberFromPerson( person );
                        }   
                        else
                        {
                            m_speechGenerator.sayCallTerminated();
                        }
                    }
                    else
                    {
                        lastTimeMissed = true;
                    }
                }
                else
                {
                    lastTimeMissed = true;
                }
                
                m_speechGenerator.stopTalking();
                m_speechRecognizer.stopListening( false );
            }
        }
        catch ( Exception e )
        {
            Log.e( Tag, "Can't wait for call request", e );
        }
        
        return null;
    }

    private String resolveNumberFromPerson(String person)
    {
        return m_numbers.getProperty( person );
    }

   
    private boolean isKnownPerson(String person)
    {
        final String normalizedPerson = person != null ? person.toLowerCase().trim() : "";
        
        Log.d(Tag, "Looking up person \"" + normalizedPerson + "\"( " + m_numbers.getProperty( normalizedPerson )  + ")" );
        
        return m_numbers.containsKey( normalizedPerson );
    }

    public void sayCallFinished()
    {
        m_speechGenerator.sayCallTerminated();
    }
    
    private boolean confirmPerson(String person) throws InterruptedException
    {
        m_speechRecognizer.startWaiting();
        
        m_speechGenerator.sayCallingName( person );
        
        return m_speechRecognizer.waitForNo( 2 );
    }
    
    private boolean confirmNumber(String number) throws InterruptedException
    {
        m_speechGenerator.sayDialingNumber( number );
        
        m_speechRecognizer.startWaiting();
        
        return m_speechRecognizer.waitForNo( 2 );
    }

    private String resolveNumber(String dialNumber)
    {
        return "number " + dialNumber;
    }
 
    private void loadNumbers() throws IOException
    {
        FileInputStream fis = null;
        
        try
        {
            final File numbersFile = new File( NUMBERS_FILE );
            fis = new FileInputStream( numbersFile );
            m_numbers.load( fis );
            
            Log.d(Tag, "Loaded numbers: " + m_numbers );
        }
        finally
        {
            if ( fis != null ) fis.close(); 
        }        
    }
}
