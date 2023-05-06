package se.joakimloxdal.rattuppgang;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ScrollView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class whereSchouldIStand extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private JSONArray stations;
    private static final LatLngBounds POS_BOUNDS = new LatLngBounds(
            new LatLng( 58.725451, 18.912964), new LatLng( 59.651092, 17.094727));
    private static ListAdapter listAdapterFront;
    private static ListAdapter listAdapterMiddle;
    private static ListAdapter listAdapterBack;

    private ArrayList<Station> allStations = new ArrayList<>();
    private ArrayList<Entrance> targetStationFrontEntrances;
    private ArrayList<Entrance> targetStationMiddleEntrances;
    private ArrayList<Entrance> targetStationBackEntrances;

    private mapsHandler mapsHandler = new mapsHandler();
    private AdView mAdView;


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_schould_istand);

        MobileAds.initialize(this, "ca-app-pub-8649329105788285~1264414474");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        // Load data from stations json file and add to adapter
        String json_string = loadJSONFromAsset(getApplicationContext());
        try {
            stations = new JSONArray(json_string);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Add all stations to allStations ArrayList
        for (int i=0; i<stations.length(); i++){
            Station temp_station = null;
            try {
                JSONObject temp_station_JSON = stations.getJSONObject(i);
                temp_station = new Station(temp_station_JSON.getString("namn"), temp_station_JSON.getInt("level"), temp_station_JSON.getJSONArray("linje"));
                temp_station.addEntrances(temp_station_JSON.getJSONArray("uppgangar"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(temp_station != null){
                allStations.add(temp_station); // Add to allStations
            }
        }

        // Set station search adapters
        ArrayAdapter<String> stationAdapter = getAdapter(this, stations);
        AutoCompleteTextView fromView = (AutoCompleteTextView)
                findViewById(R.id.from_autoCompleteTextView);
        fromView.setAdapter(stationAdapter);

        AutoCompleteTextView toView = (AutoCompleteTextView)
                findViewById(R.id.to_autoCompleteTextView);
        toView.setAdapter(stationAdapter);

        // Set address search adapter
        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        PlaceAutocompleteAdapter mPlaceAutoCompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                POS_BOUNDS, null );
        AutoCompleteTextView autocompletePlaceText = (AutoCompleteTextView)
                findViewById(R.id.adressText);
        autocompletePlaceText.setAdapter(mPlaceAutoCompleteAdapter);

        // Hide all that is needed
        findViewById(R.id.mainScrollView).setVisibility(View.INVISIBLE);

    }

    public void toggleAddress(View view){
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
        if (cb.isChecked()){
            findViewById(R.id.adressText).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.adressText).setVisibility(View.INVISIBLE);
        }
    }

    public void sendEmail(View view){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","support@joakimloxdal.se", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        intent.putExtra(Intent.EXTRA_TEXT, "Meddelande");
        startActivity(Intent.createChooser(intent, "Choose an Email client :"));
    }



    public static void updateAdapters(){
        listAdapterFront.notifyDataSetChanged();
        listAdapterMiddle.notifyDataSetChanged();
        listAdapterBack.notifyDataSetChanged();
    }

    public void calculateWhereToStand(View view) throws Exception {
        AutoCompleteTextView adressView = findViewById(R.id.adressText);
        targetStationFrontEntrances = new ArrayList<Entrance>();
        targetStationMiddleEntrances = new ArrayList<Entrance>();
        targetStationBackEntrances = new ArrayList<Entrance>();

        // Hide keyboard
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        AutoCompleteTextView fromText = (AutoCompleteTextView) findViewById(R.id.from_autoCompleteTextView);
        AutoCompleteTextView toText = (AutoCompleteTextView) findViewById(R.id.to_autoCompleteTextView);

        Station from = null;
        Station to = null;

        // Data handling
        if(allStations!=null){
            String fromTextString = fromText.getText().toString();
            String toTextString = toText.getText().toString();
            for (int i = 0; i < allStations.size(); i++) {
                Station station = allStations.get(i);
                String name = station.getName();

                if(fromTextString.equals(name))
                    from = station;

                if(toTextString.equals(name))
                    to = station;
            }
        }

        // Alert if one or both stations don't exist in stations list
        if(from == null || to == null){
            alert(this, "Någon av stationerna existerar inte i vår databas!");
            return;
        }

        // Check if they're on the same line
        if(!from.onSameLine(to)){
            alert(this, "Stationerna är inte på samma linje!");
            return;
        }

        // Convert "upp", "ned" to "fram", "bak
        boolean up = (from.getLevel() < to.getLevel());
        ArrayList<Entrance> entrances = to.getEntrances();
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);


        for(int i = 0; i<entrances.size(); i++){
            Entrance entrance = entrances.get(i);

            String placement = entrance.getPlacement();
            String coordinates = entrance.getCoordinates();

            if(placement.equals("upp")){
                if(up){
                    targetStationFrontEntrances.add(entrance);
                    int index = targetStationFrontEntrances.indexOf(entrance);
                    if(cb.isChecked()) {
                        if(coordinates != null){
                            entrance.setDistanceToAddress("Laddar...");
                            mapsHandler.getDistance(adressView.getText().toString(), coordinates, targetStationFrontEntrances, index);
                        }else
                            entrance.setDistanceToAddress("Det finns tyvärr ingen data om var denna station ligger");
                    }else{
                        entrance.setDistanceToAddress("");
                    }
                }else{
                    targetStationBackEntrances.add(entrance);
                    int index = targetStationBackEntrances.indexOf(entrance);
                    if(cb.isChecked()) {
                        if (coordinates != null) {
                            entrance.setDistanceToAddress("Laddar...");
                            mapsHandler.getDistance(adressView.getText().toString(), coordinates, targetStationBackEntrances, index);
                        }else
                            entrance.setDistanceToAddress("Det finns tyvärr ingen data om var denna station ligger");
                    }else{
                        entrance.setDistanceToAddress("");
                    }
                }
            }else if(placement.equals("ned")){
                if(up){
                    targetStationBackEntrances.add(entrance);
                    int index = targetStationBackEntrances.indexOf(entrance);

                    if(cb.isChecked()) {
                        if (coordinates != null){
                            entrance.setDistanceToAddress("Laddar...");
                            mapsHandler.getDistance(adressView.getText().toString(), coordinates, targetStationBackEntrances, index);
                        }else
                            entrance.setDistanceToAddress("Det finns tyvärr ingen data om var denna station ligger");
                    }else{
                        entrance.setDistanceToAddress("");
                    }

                }else{
                    targetStationFrontEntrances.add(entrance);
                    int index = targetStationFrontEntrances.indexOf(entrance);

                    if(cb.isChecked()) {
                        if (coordinates != null){
                            entrance.setDistanceToAddress("Laddar...");
                            mapsHandler.getDistance(adressView.getText().toString(), coordinates, targetStationFrontEntrances, index);
                        }else
                            entrance.setDistanceToAddress("Det finns tyvärr ingen data om var denna station ligger");
                    }else{
                        entrance.setDistanceToAddress("");
                    }
                }
            }else{
                targetStationMiddleEntrances.add(entrance);
                int index = targetStationMiddleEntrances.indexOf(entrance);

                if(cb.isChecked()) {
                    if (coordinates != null) {
                        entrance.setDistanceToAddress("Laddar...");
                        mapsHandler.getDistance(adressView.getText().toString(), coordinates, targetStationMiddleEntrances, index);
                    }else
                        entrance.setDistanceToAddress("Det finns tyvärr ingen data om var denna station ligger");
                }else{
                    entrance.setDistanceToAddress("");
                }
            }
        }

        Entrance emptyEntrance = new Entrance("Inga uppgångar här", "Ingenstans");
        emptyEntrance.setDistanceToAddress("Tryck på 'Anmäl fel' uppe till höger om detta är felaktigt");
        if(targetStationBackEntrances.size()==0){
            targetStationBackEntrances.add(emptyEntrance);
        }
        if(targetStationFrontEntrances.size()==0){
            targetStationFrontEntrances.add(emptyEntrance);
        }
        if(targetStationMiddleEntrances.size()==0){
            targetStationMiddleEntrances.add(emptyEntrance);
        }

        // Create ArrayAdapters for each List
        listAdapterFront = new ListAdapter(this, android.R.layout.simple_list_item_1, targetStationFrontEntrances);
        listAdapterMiddle = new ListAdapter(this, android.R.layout.simple_list_item_1, targetStationMiddleEntrances);
        listAdapterBack = new ListAdapter(this, android.R.layout.simple_list_item_1, targetStationBackEntrances);

        ListView listViewFront = (ListView)
                findViewById(R.id.listViewFram);
        ListView listViewMiddle = (ListView)
                findViewById(R.id.listViewMitt);
        ListView listViewBack = (ListView)
                findViewById(R.id.listViewBak);

        listViewFront.setAdapter(listAdapterFront);
        listViewMiddle.setAdapter(listAdapterMiddle);
        listViewBack.setAdapter(listAdapterBack);

        // Set correct heights for listviews
        UIUtils.setListViewHeightBasedOnItems(listViewBack);
        UIUtils.setListViewHeightBasedOnItems(listViewMiddle);
        UIUtils.setListViewHeightBasedOnItems(listViewFront);

        /* Show it all */
        findViewById(R.id.mainScrollView).setVisibility(View.VISIBLE);
    }

    public void alert(Context context, String text){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(text);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
        return;
    }

    public int numIntersects(Integer[] array1, Integer[] array2){
        Set<Integer> s1 = new HashSet<Integer>(Arrays.asList(array1));
        Set<Integer> s2 = new HashSet<Integer>(Arrays.asList(array2));
        s1.retainAll(s2);

        Integer[] result = s1.toArray(new Integer[s1.size()]);
        return(result.length);
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("data.json");

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

    public ArrayAdapter<String> getAdapter(Context context, JSONArray array){
        String[] result_list = new String[array.length()];

        // Add station names to adapter
        if(array!=null){
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonobject = null;
                try {
                    jsonobject = array.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    String name = jsonobject.getString("namn");
                    result_list[i] = name;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        /* Set adapters */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.select_dialog_singlechoice, result_list);

        return adapter;

    }

}
