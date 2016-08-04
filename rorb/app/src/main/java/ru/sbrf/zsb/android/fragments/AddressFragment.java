package ru.sbrf.zsb.android.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import ru.sbrf.zsb.android.helper.SlidingTabLayout;
import ru.sbrf.zsb.android.rorb.R;

/**
 * Created by Oleg on 04.08.2016.
 */
public class AddressFragment extends ListFragment {
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    private ListView mList;

    public AddressFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_address, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter());
        // Give the SlidingTabLayout the ViewPager, this must be
        // done AFTER the ViewPager has had it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return R.color.colorAccent;
            }

            @Override
            public int getDividerColor(int position) {
                return R.color.colorPrimaryDark;
            }
        });
        mSlidingTabLayout.setViewPager(mViewPager);
        mList = (ListView) view.findViewById(android.R.id.list);
    }

    class SamplePagerAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return false;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Список";
            }
            else
                return "Карта";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Inflate a new layout from our resources


            int resource = R.layout.fragment_address_list_view;

            if (position == 1)
            {
                resource = R.layout.fragment_address_map_view;
            }

            View view = getActivity().getLayoutInflater().inflate
                    ( resource,
                    container, false);
            // Add the newly created View to the ViewPager
            container.addView(view);

            // Retrieve a TextView from the inflated View, and update it's text
            //TextView title = (TextView) view.findViewById(R.id.item_title);
           // title.setText(String.valueOf(position + 1));

            // Return the View
            return view;
        }

        /**
         * Destroy the item from the ViewPager. In our case this is simply
         * removing the View.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
