package com.gammazero.signalrocket;

/**
 * Created by Jamie on 1/2/2017.
 */

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.sax.TextElementListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AllMembersEntryAdapter extends ArrayAdapter<Item> {

    private Context context;
    private ArrayList<Item> items;
    private LayoutInflater vi;

    public AllMembersEntryAdapter(Context context,ArrayList<Item> items) {
        super(context,0, items);
        this.context = context;
        this.items = items;
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
       // View v = convertView;

        final Item i = items.get(position);
        EntryItem ei = (EntryItem) i;
        v = vi.inflate(R.layout.allmember_list_item_entry, null);
        final TextView title = (TextView) v.findViewById(R.id.allmember_list_item_entry_title);
        title.setText(ei.title);
        title.setPadding(50, 0, 0, 0);
        title.setHeight(170);
        if (! ei.subtitle.equals("")) {
            final TextView count = (TextView) v.findViewById(R.id.allmember_list_item_entry_subtitle);
            if (ei.subtitle.equals("1")) {
                count.setText("Member of " + ei.subtitle + " group");
            } else {
                count.setText("Member of " + ei.subtitle + " groups");
            }
        } else {
            title.setPadding(50,25,0,0);
        }
        return v;
    }
}


