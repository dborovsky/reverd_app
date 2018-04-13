package com.reverdapp.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.reverdapp.R;
import com.reverdapp.model.TrashListModel;

public class TrashAdapter extends BaseAdapter{

	private Context context;
	private boolean isEdit = false;
	private ArrayList<TrashListModel> trashLists;
	
	public TrashAdapter(Context context,ArrayList<TrashListModel> trashLists) {
		this.context = context;
		this.trashLists = trashLists;
	}
	@Override
	public int getCount() {
		return trashLists.size();
	}

	@Override
	public Object getItem(int position) {
		return trashLists.get(position);
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
			convertView = LayoutInflater.from(context).inflate(R.layout.row_trash_messages, parent,false);
			holder.cbEdit = (CheckBox) convertView.findViewById(R.id.row_trash_list_cb_edit);
			convertView.setTag(holder);
			
		}else{
			
			holder = (Holder) convertView.getTag();
		}
		
		
		if(isEdit){
			holder.cbEdit.setVisibility(View.VISIBLE);
			holder.cbEdit.setTag(position);
			
			if(trashLists.get(position).isSelected())
				holder.cbEdit.setChecked(true);
			else
				holder.cbEdit.setChecked(false);
		}
		else{
			
			holder.cbEdit.setVisibility(View.GONE);
		}
		
		holder.cbEdit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					trashLists.get(Integer.valueOf(holder.cbEdit.getTag().toString())).setSelected(true);
				else
					trashLists.get(Integer.valueOf(holder.cbEdit.getTag().toString())).setSelected(false);
				
				
				
			}
		});

		return convertView;
	}
	
	class Holder{
		TextView tvNumberName,tvmessageDetail;
		CheckBox cbEdit;
	}
	
	public void setEdit(boolean isEdit){
		this.isEdit = isEdit;
	}

	public boolean getEdit(){
		return isEdit;
	}

}
