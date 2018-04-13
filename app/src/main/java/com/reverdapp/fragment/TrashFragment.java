package com.reverdapp.fragment;

import java.util.ArrayList;

import com.reverdapp.R;
import com.reverdapp.adapter.TrashAdapter;
import com.reverdapp.model.TrashListModel;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.view.HomeActivity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class TrashFragment extends Fragment implements OnItemClickListener{

	private ListView lvMessages;
	private TrashAdapter adapter;
	private Menu menu;
	private ArrayList<TrashListModel> trashLists = new ArrayList<TrashListModel>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.fragment_trash, null);
		lvMessages = (ListView) view.findViewById(R.id.fragment_trash_lv_message_list);
		
		
		
		for(int i=0;i<0;i++){
			TrashListModel trashListModel = new TrashListModel();
			trashLists.add(trashListModel);
		}
		
		
		adapter = new TrashAdapter(getActivity(),trashLists);
		lvMessages.setAdapter(adapter);
		
		lvMessages.setOnItemClickListener(this);
		
		//((HomeActivity)getActivity()).setTrashFragment(this);
		((BaseActivity)getActivity()).setActionbarTextColorWhite();
		((BaseActivity)getActivity()).setTitle(getString(R.string.trash_title));
		return view;
	}
	

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);
		this.menu = menu;
		
		setActionbarWithEditOption();

	}
	
	private void setActionbarWithEditOption(){
		
		menu.findItem(R.id.action_edit).setVisible(true);
		menu.findItem(R.id.action_trash).setVisible(false);
	}
	
	private void setActionbarWithoutEditOption(){
		
		menu.findItem(R.id.action_edit).setVisible(false);
		menu.findItem(R.id.action_trash).setVisible(true);
	}
	
	public void setEditActionBack(){
		adapter.setEdit(false);
		setActionbarWithEditOption();
		adapter.notifyDataSetChanged();
	}
	public boolean isEdit(){
		return adapter.getEdit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(item.getItemId() == R.id.action_edit){
			
			adapter.setEdit(true);
			setActionbarWithoutEditOption();
			adapter.notifyDataSetChanged();
			
		}else if(item.getItemId() == R.id.action_trash){
			
			setEditActionBack();
			Toast.makeText(getActivity(), getString(R.string.selected_messages_deleted), Toast.LENGTH_LONG).show();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if(!hidden){
			((BaseActivity)getActivity()).setActionbarTextColorWhite();
			((BaseActivity)getActivity()).setTitle(getString(R.string.trash_title));
		}
	}



	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

		if(adapter.getEdit()){
			if(trashLists.get(position).isSelected())
				trashLists.get(position).setSelected(false);
			else
				trashLists.get(position).setSelected(true);
			
			adapter.notifyDataSetChanged();
			
		}
	}
}
