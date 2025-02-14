package com.chenhao.youbike.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.chenhao.youbike.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private View view;
    private FragmentActivity myContext;
    public InfoWindowAdapter(FragmentActivity aContext) {
        this.myContext = aContext;
        LayoutInflater inflater = (LayoutInflater) myContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.info_map, null);
    }
    @Override
    public View getInfoWindow(Marker marker) {
        final String title = marker.getTitle();
        final TextView titleUi = ((TextView) view.findViewById(R.id.text_place));
        if (title != null) {
            titleUi.setText(title);
        } else {
            titleUi.setText("");
            titleUi.setVisibility(View.GONE);
        }

        final String snippet = marker.getSnippet();
        final TextView snippetUi = ((TextView) view
                .findViewById(R.id.text_info));
        final ImageView snippetImage =  view.findViewById(R.id.image_bike);

        if (snippet != null) {
            snippetUi.setText(snippet);
            snippetImage.setImageResource(R.drawable.biker);
        } else {
            snippetUi.setText("");
            snippetImage.setImageResource(R.drawable.biker);
        }

        return view;


    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
