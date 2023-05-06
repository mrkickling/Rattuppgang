package se.joakimloxdal.rattuppgang;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class whichShouldITake extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_which_should_itake);

        JSONObject contributers = null;
        // Load data from stations json file and add to adapter
        String json_string = loadJSONFromAsset(getApplicationContext());
        try {
            contributers = new JSONObject(json_string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(contributers!=null){
            // Set station search adapters
            ArrayAdapter<String> conAdapter = null;
            try {
                conAdapter = getAdapter(this, contributers);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ListView contList = (ListView)
                    findViewById(R.id.listViewContributers);
            if(conAdapter!=null){
                contList.setAdapter(conAdapter);
            }
        }
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("contributers.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }


    public ArrayAdapter<String> getAdapter(Context context, JSONObject array) throws JSONException {
        String[] result_list = new String[array.length()];

        Iterator< ? > keys = array.keys();
        int i = 0;
        while(keys.hasNext()){
            String key = (String) keys.next();
            result_list[i] = key;
            i++;
        }

        /* Set adapters */
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, result_list){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

            /*YOUR CHOICE OF COLOR*/
                textView.setTextColor(Color.BLACK);

                return view;
            }
        };

        return adapter;

    }
}
