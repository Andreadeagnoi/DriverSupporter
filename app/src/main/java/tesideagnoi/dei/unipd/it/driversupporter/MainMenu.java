package tesideagnoi.dei.unipd.it.driversupporter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Button launchSession = (Button) findViewById(R.id.newSession);
        launchSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent launchNewSession = new Intent(myContext(),InfoViewerActivity.class);
                startActivity(launchNewSession);
            }
        });
        Button setup = (Button) findViewById(R.id.setupButton);
        setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchSetupRPM = new Intent(myContext(),Setup.class);
                startActivity(launchSetupRPM);
            }
        });
        Button engineRPM = (Button) findViewById(R.id.engineRPM);
        engineRPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchRPMViewer = new Intent(myContext(),EngineRPMViewer.class);
                startActivity(launchRPMViewer);
            }
        });
    }

    public Context myContext(){
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
