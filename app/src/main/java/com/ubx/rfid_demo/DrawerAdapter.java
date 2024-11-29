package com.ubx.rfid_demo;

// DrawerAdapter.java
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import com.ubx.rfid_demo.ui.main.SitePagerAdapter;


public class DrawerAdapter extends BaseAdapter {

    private Context context;
    private String[] items;
    private int[] icons;

    public DrawerAdapter(Context context, String[] items, int[] icons) {
        this.context = context;
        this.items = items;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_item, parent, false);  // Usar el nuevo layout
        }

        // Obtener el TextView y el ImageView del layout
        TextView textView = convertView.findViewById(R.id.menu_text);
        ImageView imageView = convertView.findViewById(R.id.menu_icon);

        // Establecer el texto y el icono
        textView.setText(items[position]);
        imageView.setImageResource(icons[position]);

        return convertView;
    }
}