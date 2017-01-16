package com.gammazero.signalrocket;

/**
 * Created by Jamie on 1/2/2017.
 */

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EntryAdapter extends ArrayAdapter<Item> {

    private Context context;
    private ArrayList<Item> items;
    private LayoutInflater vi;

    public EntryAdapter(Context context,ArrayList<Item> items) {
        super(context,0, items);
        this.context = context;
        this.items = items;
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        final Item i = items.get(position);
        if (i != null) {
            if(i.isSection()){
                SectionItem si = (SectionItem)i;
               // v = vi.inflate(R.layout.list_item_section, null);
                v = vi.inflate(R.layout.list_item_entry, null);

                v.setOnClickListener(null);
                v.setOnLongClickListener(null);
                v.setLongClickable(false);


                //final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
                final TextView sectionView = (TextView) v.findViewById(R.id.list_item_entry_title);
                sectionView.setHeight(50);
                sectionView.setTextSize(18);
                sectionView.setTextColor(Color.WHITE);
                sectionView.setBackgroundColor(Color.parseColor("#00457c"));
                sectionView.setText(si.getTitle() + "'s groups");
            }else{
                EntryItem ei = (EntryItem)i;
                v = vi.inflate(R.layout.list_item_entry, null);
                final TextView title = (TextView)v.findViewById(R.id.list_item_entry_title);
                title.setText(ei.title);
                title.setPadding(50,0,0,0);
               // title.setHeight(170);
                title.setHint(ei.subtitle);
               // final TextView subtitle = (TextView)v.findViewById(R.id.list_item_entry_summary);

              //  if (title != null)
              //      title.setText(ei.title);
              //  if(subtitle != null)
              //      subtitle.setText(ei.subtitle);
            }
        }
        return v;
    }

}
