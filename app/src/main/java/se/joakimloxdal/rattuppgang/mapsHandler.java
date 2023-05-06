package se.joakimloxdal.rattuppgang;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by joakimloxdal on 2018-10-16.
 */

public class mapsHandler {
    private static String GMAPS_URL = "https://maps.googleapis.com/maps/api/distancematrix/";
    private static String API_KEY = "AIzaSyAtMAYxH3l0z1vwjokqCt4Ww0RUxdmSmf0";


    public void getDistance(String address, String pos, ArrayList<Entrance> list, int index) throws Exception {

        String url_string = GMAPS_URL + "json?mode=walking&units=metric&origins="+ address +"&destinations="+ pos +"&key=" + API_KEY;

        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMethod("GET");
        requestPackage.setUrl(url_string);

        System.out.println("Received distance request from " + address + " to " + pos);

        Downloader downloader = new Downloader(list, index); //Instantiation of the Async task
        downloader.execute(requestPackage);

    }

    private class Downloader extends AsyncTask<RequestPackage, String, String> {
        private ArrayList<Entrance> list;
        private int index;

        public Downloader(ArrayList<Entrance> list, int index){
            this.list = list;
            this.index = index;
        }

        @Override
        protected String doInBackground(RequestPackage... params) {
            return HttpManager.getData(params[0]);
        }

        //The String that is returned in the doInBackground() method is sent to the
        // onPostExecute() method below. The String should contain JSON data.
        @Override
        protected void onPostExecute(String result) {
            JSONObject json_result = null;
            try {
                json_result = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                System.out.println(json_result);
                JSONObject row = json_result.getJSONArray("rows").getJSONObject(0);
                JSONObject element = row.getJSONArray("elements").getJSONObject(0);
                String value = element.getJSONObject("distance").getString("text");
                setValues(value);
            } catch (JSONException e) {
                e.printStackTrace();
                setValues("Kunde inte hämta data");
            }
        }

        public void setValues(String value){
            System.out.println(value);
            Entrance entrance = list.get(this.index);
            entrance.setDistanceToAddress(value + " ifrån addressen");
            list.set(index, entrance);
            whereSchouldIStand.updateAdapters();
        }

    }
}
