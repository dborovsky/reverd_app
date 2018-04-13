package com.reverdapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.reverdapp.R;

import java.util.ArrayList;

public class CountryAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<String> country;
    public CountryAdapter(Context context,ArrayList<String> country) {
        this.context = context;
        this.country = country;
    }
    @Override
    public int getCount() {
        return country.size();
    }

    @Override
    public Object getItem(int position) {
        return country.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        final Holder holder;
        if(convertView == null){
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.row_country, parent,false);
            holder.tvCountryName = (TextView) convertView.findViewById(R.id.row_country_tv_country_name);
            convertView.setTag(holder);
        }else{
            
            holder = (Holder) convertView.getTag();
        }


        holder.tvCountryName.setTextColor(Color.BLACK);
        holder.tvCountryName.setText(country.get(position));
        return convertView;
    }
    
    class Holder{
        TextView tvCountryName;
    }

}
