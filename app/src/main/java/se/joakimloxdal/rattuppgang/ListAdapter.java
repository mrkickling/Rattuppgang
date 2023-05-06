package se.joakimloxdal.rattuppgang;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by joakimloxdal on 2018-10-06.
 */

public class ListAdapter extends ArrayAdapter {
    Context context;
    ArrayList<Entrance> entrances;

    public ListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public ListAdapter(@NonNull Context context, int resource, ArrayList<Entrance> entrances){
        super(context, resource, entrances);

        this.context = context;
        this.entrances = entrances;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        //get the property we are displaying
        final Entrance entrance = entrances.get(position);

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.entrance_layout, null);

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView distance = (TextView) view.findViewById(R.id.distance_to_address);

        name.setText(entrance.getName());
        distance.setText(entrance.getDistanceToAddress());

        name.setTextColor(Color.BLACK);
        distance.setTextColor(Color.DKGRAY);


        if(entrance.getCoordinates()!=null){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q="+entrance.getCoordinates()+"("+entrance.getName()+")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(mapIntent);
                    }

                }
            });
        }

        // Generate ListView Item using TextView
        return view;
    }

}
