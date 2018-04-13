package com.reverdapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.model.CountryAreaModel;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.LogConfig;

import java.util.ArrayList;

public class CountryAreaAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener {

	private static final String TAG = LogConfig.genLogTag("CountryAreaAdapter");

	private ArrayList<CountryAreaModel> countryAreaList;
	private Context context;
	private ReverdApp reverdApp;

	public CountryAreaAdapter(Context context, ArrayList<CountryAreaModel> countryAreaList) {
		this.context = context;
		this.countryAreaList = countryAreaList;
		this.reverdApp = (ReverdApp) context.getApplicationContext();
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return countryAreaList.get(groupPosition).getAreaModelList().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	private static final int[] EMPTY_STATE_SET = {};
	private static final int[] GROUP_EXPANDED_STATE_SET = { android.R.attr.state_expanded };
	private static final int[][] GROUP_STATE_SETS = {
			EMPTY_STATE_SET, // 0
			GROUP_EXPANDED_STATE_SET // 1
	};

	private void saveSelectedCountriesToPreferences()
	{
		// Get selected countries.
		Cursor cursorCountry = reverdApp.getDatabase().getSelectedCountries(1);
		String selectedCountryCode = "";

		if (cursorCountry!=null && cursorCountry.getCount()>0) {
			for(int i=0;i<cursorCountry.getCount();i++) {
				cursorCountry.moveToPosition(i);
				selectedCountryCode = selectedCountryCode + cursorCountry.getString(cursorCountry.getColumnIndex(DatabaseField.COUNTRY_CODE));
				if(i<cursorCountry.getCount()-1)
					selectedCountryCode = selectedCountryCode +",";
			}
		}

        final AppPreferences ap = new AppPreferences(context);
        ap.set(AppPreferences.PREF_SELECTED_COUNTRY_CODES, selectedCountryCode);

		Log.d(TAG, "selectedCountryCode = " + selectedCountryCode);
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean arg2, View convertView,
			ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.row_setting_area, parent, false);
		}

		CheckBox cbArea = (CheckBox) convertView.findViewById(R.id.row_setting_area_cb_area);
		TextView tvAreaName = (TextView) convertView.findViewById(R.id.row_setting_area_tv_area);

		ExpandableListView v= (ExpandableListView)parent;

		final int isChk = countryAreaList.get(groupPosition).areaModelList.get(childPosition).isChecked();
		if(isChk == 1)
			 cbArea.setChecked(true);
		 else
			 cbArea.setChecked(false);

		String text = countryAreaList.get(groupPosition).getAreaModelList().get(childPosition).getAreaCode()+" "+countryAreaList.get(groupPosition).getAreaModelList().get(childPosition).getAreaName();

		//Log.d(TAG, "Drawing " + groupPosition + ", " + text);

		tvAreaName.setText(text);
/*
		 cbArea.setTag(R.string.put_extra_group_position,groupPosition);
		 cbArea.setTag(R.string.put_extra_child_position,childPosition);
*/

		Log.d(TAG, "getChildView: CountryName = " + countryAreaList.get(groupPosition).getCountryName() + ", groupPosition = " + groupPosition + ", childPosition = " + childPosition + ", isChk = " + isChk);

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return countryAreaList.get(groupPosition).getAreaModelList().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return countryAreaList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return countryAreaList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.row_setting_country, parent, false);
		}

		final View ind = convertView.findViewById(R.id.row_navigation);

		if (ind != null)
		{
			final ImageView indicator = (ImageView) ind;
			if (getChildrenCount(groupPosition) == 0)
			{
				indicator.setVisibility(View.INVISIBLE);
			}
			else
			{
				indicator.setVisibility(View.VISIBLE);
				int stateSetIndex = (isExpanded ? 1 : 0);
				Drawable drawable = indicator.getDrawable();
				drawable.setState(GROUP_STATE_SETS[stateSetIndex]);
			}
		}

		TextView tvCountryName = (TextView) convertView.findViewById(R.id.row_setting_country_tv_country_name);
		CheckBox cbCountry = (CheckBox) convertView.findViewById(R.id.row_setting_country_cb_country);

		tvCountryName.setText(countryAreaList.get(groupPosition).getCountryName() + " " + countryAreaList.get(groupPosition).getCountryCode());

		if (ind != null) {
			ind.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					if (isExpanded) ((ExpandableListView) parent).collapseGroup(groupPosition);
					else ((ExpandableListView) parent).expandGroup(groupPosition, true);
				}
			});
		}

		if (countryAreaList.get(groupPosition).isSelected() == 1) {
			cbCountry.setChecked(true);
		} else {
			cbCountry.setChecked(false);
		}
		cbCountry.setTag(groupPosition);

		Log.d(TAG, "getGroupView: CountryName = " + countryAreaList.get(groupPosition).getCountryName() + ", groupPosition = " + groupPosition + ", isExpanded = " + isExpanded);

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		CheckBox cbArea = (CheckBox) v.findViewById(R.id.row_setting_area_cb_area);
		final boolean state = !cbArea.isChecked();
		cbArea.setChecked(state);

		if (state) {
			countryAreaList.get(groupPosition).getAreaModelList().get(childPosition).setChecked(1);
		}
		else {
			countryAreaList.get(groupPosition).getAreaModelList().get(childPosition).setChecked(0);
		}

		CountryAreaModel cam = countryAreaList.get(groupPosition);

		ArrayList<CountryAreaModel.AreaModel> am = cam.getAreaModelList();

		CountryAreaModel.AreaModel areamodel = am.get(childPosition);

		// select * from Area where is_selected=1;
		reverdApp.getDatabase().insertArea(
				cam.getCountryCode(),
				cam.getCountryName(),
				areamodel.getAreaCode(),
				areamodel.getAreaName(),
				state);

		Log.d(TAG, "Clicked child: groupPosition = " + groupPosition + ", childPosition = " + childPosition + ", state = " + state + ", checked = " + countryAreaList.get(groupPosition).getAreaModelList().get(childPosition).isChecked());
		return true;
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		final CheckBox cbCountry = (CheckBox) v.findViewById(R.id.row_setting_country_cb_country);

		final boolean state = cbCountry.isChecked();

		final CountryAreaModel cam = countryAreaList.get(groupPosition);

		boolean newState = false;
		if (state) {
			newState = false;
		}
		else {
			newState = true;
		}

		cbCountry.setChecked(newState);
		countryAreaList.get(groupPosition).setSelected((newState) ? 1 : 0);

		reverdApp.getDatabase().insertCountry(
                cam.getCallingCode(),
				cam.getCountryCode(),
				cam.getCountryName(),
				newState,
				(int) id);

		saveSelectedCountriesToPreferences();

		Log.d(TAG, "Clicked group: groupPosition = " + groupPosition + ", state = " + state + ", newState = " + newState);

		return true;
	}
}
