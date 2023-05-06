package se.joakimloxdal.rattuppgang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "se.joakimloxdal.myfirstapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Called when the user taps the Send button */
    public void goToFirst(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, whereSchouldIStand.class);
        startActivity(intent);
    }
    public void goToSecond(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, whichShouldITake.class);
        startActivity(intent);

    }


}
