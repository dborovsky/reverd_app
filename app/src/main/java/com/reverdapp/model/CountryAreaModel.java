package com.reverdapp.model;

import java.util.ArrayList;

public class CountryAreaModel {

	public ArrayList<AreaModel> areaModelList;

	private int callingCode;
	private String countryCode;
	private String countryName;
	private int isSelected;

	public class AreaModel {
		
		private int isChecked = 0;
		private String areaCode;
		private String areaName;
		
		public int isChecked() {
			return isChecked;
		}
		public void setChecked(int isChecked) {
			this.isChecked = isChecked;
		}
		public String getAreaCode() {
			return areaCode;
		}
		public void setAreaCode(String areaCode) {
			this.areaCode = areaCode;
		}
		public String getAreaName() {
			return areaName;
		}
		public void setAreaName(String areaName) {
			this.areaName = areaName;
		}
	}

	/** Indicates if any area is selected. */
	public boolean hasAreaSelected()
	{
		for (AreaModel am: areaModelList)
		{
			if (am.isChecked() == 1) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<AreaModel> getAreaModelList() {
		return areaModelList;
	}

	public void setAreaModelList(ArrayList<AreaModel> areaModelList) {
		this.areaModelList = areaModelList;
	}

    public void setCallingCode(int c) {
        callingCode = c;
    }

    public int getCallingCode() {
        return callingCode;
    }

    public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public int isSelected() {
		return isSelected;
	}

	public void setSelected(int isSelected) {
		this.isSelected = isSelected;
	}
}
