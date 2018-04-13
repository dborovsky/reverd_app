package com.reverdapp.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.reverdapp.R;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.view.BaseActivity;

public class LeftMenuFragment extends Fragment implements OnClickListener {

    private static final String TAG = LogConfig.genLogTag("LeftMenuFragment");

    private LinearLayout mMenuLayout;
	private ScrollView mScrollView;

	private final static int ID_BLACKLIST = R.id.ID_BLACKLIST;
    private final static int ID_CALLS = R.id.ID_CALLS;
    private final static int ID_STATS = R.id.ID_STATS;
    private final static int ID_ADD_TO_BLACKLIST = R.id.ID_ADD_TO_BLACKLIST;
    private final static int ID_ADD_TO_WHITELIST = R.id.ID_ADD_TO_WHITELIST;
    private final static int ID_COMPLAINTS = R.id.ID_COMPLAINTS;
    private final static int ID_TRASH = R.id.ID_TRASH;
    private final static int ID_SETTINGS = R.id.ID_SETTINGS;
    private final static int ID_SUBSCRIPTION = R.id.ID_SUBSCRIPTION;
    private final static int ID_HELP = R.id.ID_HELP;
    private final static int ID_PRIVACY = R.id.ID_PRIVACY;
    private final static int ID_FEEDBACK = R.id.ID_FEEDBACK;

    private void addEntry(final Activity a, final int id,
                          final int iconResourceId,
                          final int textResourceId,
                          final int colorResourceId,
                          LinearLayout target
                          ) {
        View v = LayoutInflater.from(a).inflate(
                R.layout.row_sliding_left_menu, mScrollView, false);
        v.setId(id);

        ImageView ivIcon = (ImageView) v.findViewById(R.id.row_sliding_left_menu_iv_icon);
        TextView tvName = (TextView) v.findViewById(R.id.row_sliding_left_menu_tv_menu_name);

        ivIcon.setBackgroundResource(iconResourceId);
        tvName.setText(getString(textResourceId));
        tvName.setTextColor(getResources().getColor(colorResourceId));

        v.setOnClickListener(this);

        target.addView(v);

    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_left_menu, null);

		mMenuLayout = (LinearLayout) view
				.findViewById(R.id.fragment_left_menu_ll_main);
		mScrollView = (ScrollView) view
				.findViewById(R.id.fragment_left_menu_sv_main);

        final Activity a = getActivity();

        addEntry(a, ID_CALLS,
                R.drawable.recent_calls25x25,
                R.string.recent_calls,
                R.color.slider_font_white,
                mMenuLayout);

        addEntry(a, ID_BLACKLIST,
                R.drawable.blacklist25x25,
                R.string.black_list,
                R.color.slider_font_black,
                mMenuLayout);

        addEntry(a, ID_ADD_TO_BLACKLIST,
                R.drawable.addtoblacklist25x25,
                R.string.add_to_backlist,
                R.color.slider_font_black,
                mMenuLayout);

        addEntry(a, ID_ADD_TO_WHITELIST,
                R.drawable.whitelist25x25,
                R.string.white_list,
                R.color.slider_font_white,
                mMenuLayout);

        addEntry(a, ID_COMPLAINTS,
                R.drawable.comlaint25x25,
                R.string.compaints,
                R.color.slider_font_white,
                mMenuLayout);
/*
        addEntry(a, ID_TRASH,
                R.drawable.trash25x25,
                R.string.trash,
                R.color.slider_font_white,
                mMenuLayout);
*/
        addEntry(a, ID_STATS,
                R.drawable.statistics25x25,
                R.string.stats,
                R.color.slider_font_white,
                mMenuLayout);

        addEntry(a, ID_SETTINGS,
                R.drawable.settings25x25,
                R.string.settings,
                R.color.slider_font_white,
                mMenuLayout);

        addEntry(a, ID_SUBSCRIPTION,
                R.drawable.subscribe25x25,
                R.string.subscription,
                R.color.slider_font_black,
                mMenuLayout);

        addEntry(a, ID_HELP,
                R.drawable.help25x25,
                R.string.help,
                R.color.slider_font_white,
                mMenuLayout);

        addEntry(a, ID_PRIVACY,
                R.drawable.privacy1_25x25,
                R.string.privacy,
                R.color.slider_font_white,
                mMenuLayout);

        addEntry(a, ID_FEEDBACK,
                R.drawable.feedback25x25,
                R.string.feedback,
                R.color.slider_font_white,
                mMenuLayout);

		return view;
	}

    private Fragment getCurrentFragment() {
        Fragment f = getActivity().getFragmentManager().findFragmentById(R.id.content_frame);
        return f;
    }

	@Override
	public void onClick(View v) {

		((BaseActivity)getActivity()).closeDrawer();
			final int count = getFragmentManager().getBackStackEntryCount();
			
				if (count>0) {
						int backStackId = getFragmentManager()
								.getBackStackEntryAt(0).getId();
						getFragmentManager().popBackStack(backStackId,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE);

				}

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        final Bundle bundle = new Bundle();
        Fragment newFragment = null;

        switch (v.getId()) {
            case ID_BLACKLIST:
                newFragment = new BlackListFragment();
                break;
            case ID_CALLS:
                newFragment = new RecentCallsFragment();
                break;
            case ID_STATS:
                newFragment = new StatFragment();
                break;
            case ID_ADD_TO_BLACKLIST: {
                newFragment = new AddToBlackListFragment();
                bundle.putBoolean(getString(R.string.put_extra_is_from_blacklist), false);
                newFragment.setArguments(bundle);
                break;
            }
            case ID_ADD_TO_WHITELIST:
                newFragment = new WhiteListFragment();
                break;
            case ID_COMPLAINTS:
                newFragment = new ComplaintsFragment();
                break;
            case ID_TRASH:
                newFragment = new TrashFragment();
                break;
            case ID_SETTINGS:
                newFragment = new SettingFragment();
                break;
            case ID_SUBSCRIPTION:
                newFragment = new SubscriptionFragment();
                break;
            case ID_HELP:
                newFragment = new HelpFragment();
                break;
            case ID_PRIVACY:
                newFragment = new PrivacyFragment();
                break;
            case ID_FEEDBACK:
                newFragment = new FeedbackFragment();
                break;
            default:
                Log.e(TAG, "Unknown ID " + v.getId());
                break;
        }

        newFragment.setArguments(bundle);

        final Fragment current = getCurrentFragment();

        Log.d(TAG, "Replacing fragment: " + current.getClass().getSimpleName() + " with " + newFragment.getClass().getSimpleName());

        fragmentTransaction.replace(R.id.content_frame, newFragment);

        if (!current.getClass().equals(newFragment.getClass())) {
            Log.d(TAG, "Adding fragment to backstack: " + newFragment.getClass().getSimpleName());
            fragmentTransaction.addToBackStack(newFragment.getClass().getSimpleName());
        }

        fragmentTransaction.commit();
	}

}
