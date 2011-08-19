package org.vintagephone;

import org.vintagephone.hardware.HardwareProvider.HardwareEventListener;
import org.vintagephone.model.VintagePhoneLifecycle;
import org.vintagephone.model.VintagePhoneStatusListener;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DebugActivity extends Activity
{
    protected static final String TAG = "DebugActivity";

    private TextView m_statusView;
    private VintagePhoneLifecycle m_lifecycle;
    
    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug);
        
        m_statusView = (TextView) findViewById(R.id.statusText );
        m_statusView.setText( "Initializing with Debug...");
        
        m_lifecycle = VintagePhoneLifecycle.getInstance();
        m_lifecycle.addStatusListener( new VintagePhoneStatusListener()
        {
            public void statusUpdated(String newStatus)
            {
                displayStatus( newStatus );
            }
        } );
        initlaizePhone();
        

        final EditText callNumber = (EditText) findViewById(R.id.callNumber);
        final Button callButton = (Button) findViewById(R.id.callButton);

        callButton.setOnClickListener( new OnClickListener()
        {
            private boolean m_isCalling;
            public void onClick(View v)
            {
                if ( !m_isCalling )
                {
                    final String number = callNumber.getText().toString();
                    new Thread( new Runnable() {
                        public void run()
                        {
                            m_lifecycle.getPhoneWrapper().call( number );                            
                        }
                        
                    } ).start();
                    
                    m_isCalling = true;
                    
                    callButton.setText( "Hang Up");
                    
                }
                else
                {
                    m_lifecycle.getPhoneWrapper().terminateActiveCall();
                    m_isCalling = false;
                    
                    callButton.setText( "Place Call");
                }
            }
        } );
        
        final TextView recognizedText = (TextView) findViewById(R.id.recognizedText);
        final Button recognizeButton = (Button) findViewById(R.id.recognizeButton);
        
        recognizeButton.setOnClickListener( new OnClickListener()
        {
            public void onClick(View v)
            {                
                final String number = m_lifecycle.getOperatorWrapper().askPhoneNumber();
                recognizedText.setText( number );
            }
        } );
        
        final TextView hardwareStatusView = (TextView)findViewById( R.id.hardwareStatusText );
        final Button ringButton = (Button)findViewById( R.id.startRingingButton );
        
        ringButton.setOnClickListener( new OnClickListener()
        {
            private boolean m_isRinging;
            public void onClick(View v)
            {
                if ( !m_isRinging )
                {
                    m_lifecycle.getHardwareWrapper().startRinging( null, null );
                    m_isRinging = true;
                    
                    ringButton.setText( "Stop Ringing");
                }
                else
                {
                    m_lifecycle.getHardwareWrapper().stopRinging();
                    m_isRinging = false;
                    
                    ringButton.setText( "Start Ringing");
                }
            }
        } );
        
        hardwareStatusView.setText("Not connected");
        m_lifecycle.getHardwareWrapper().addListener( new HardwareEventListener()
        {
            public void hookStateChanged(final boolean isOnHook)
            {
                hardwareStatusView.post( new Runnable()
                {
                    public void run()
                    {
                        hardwareStatusView.setText( isOnHook ? "On hook" : "Off hook" );
                    }
                } );
            }
        } );
    }

    
    private void initlaizePhone()
    {
        final AsyncTask<Integer, Integer, Boolean> task = new AsyncTask<Integer, Integer, Boolean>() {
            protected Boolean doInBackground(Integer... params)
            {
                try
                {
                    m_lifecycle.initialize( getApplicationContext() );
                } 
                catch (Exception e)
                {
                    Log.e(TAG, "Unable to initialize phone", e );
                    
                    displayStatus("Initialization failed");
                }
                
                return true; 
            }
        };
        task.execute();
    }

    private void displayStatus( final String status )
    {
        m_statusView.post( new Runnable()
        {
            public void run()
            {
                m_statusView.setText( status );                
            }
        } );
    }
}