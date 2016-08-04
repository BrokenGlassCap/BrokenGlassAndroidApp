package ru.sbrf.zsb.android.rorb;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import ru.sbrf.zsb.android.fragments.AddressFragment;
import ru.sbrf.zsb.android.fragments.MapListFragment;
import ru.sbrf.zsb.android.helper.AddressAdapter;
import ru.sbrf.zsb.android.helper.Utils;

public class AddressActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String EXTRA_ADDRESS_ID = "ru.sbrf.zsb.rorb.address_id";
    private int mAddressId;
    private AddressAdapter mAdapter;
    private ListView listView;
    private SearchView mSearchView;
    private AddressList mAddressList;
    private ProgressBar mPropgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_address);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(this) != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }*/


        mAddressId = getIntent().getIntExtra(EXTRA_ADDRESS_ID, 0);

        selectItem(0);

        //new LoadAddressTask().execute("");
    }

    private void selectItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new AddressFragment();
                break;
            case 1:
                fragment = new MapListFragment();
                break;
            default:
                break;
        }

        FragmentManager fm = getFragmentManager();

        if (fragment != null) {
            fm.beginTransaction()
                    .add(R.id.address_content_frame, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.address_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.address_search_button);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Toast.makeText(AddressActivity.this, "Отмена поиска", Toast.LENGTH_SHORT).show();
                new LoadAddressTask().execute("");
                return false;
            }
        });
        setupSearchView(searchItem);
        return true;
    }

    protected boolean isAlwaysExpanded() {
        return false;
    }

    private void setupSearchView(MenuItem searchItem) {
        if (isAlwaysExpanded()) {
            mSearchView.setIconifiedByDefault(false);
        } else {
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }

        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //if (searchManager != null) {
        //   List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

        //   SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        //for (SearchableInfo inf : searchables) {
        //    if (inf.getSuggestAuthority() != null
        //            && inf.getSuggestAuthority().startsWith("applications")) {
        //        info = inf;
        //    }
        //}

        //   mSearchView.setSearchableInfo(info);
        //}

        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                //if (NavUtils.getParentActivityName(this) != null) {
                //    NavUtils.navigateUpFromSameTask(this);
                // }
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        new LoadAddressTask().execute(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (Utils.isNullOrWhitespace(newText)) {
            new LoadAddressTask().execute("");
        }
        return false;
    }

    private class LoadAddressTask extends AsyncTask<String, Void, Void> {
    private String mFilter;
        @Override
        protected Void doInBackground(String... params) {
            mFilter = params[0].toUpperCase();
            mAddressList = AddressList.get(AddressActivity.this);
            if (Utils.isNullOrWhitespace(mFilter)) {
                mAddressList = mAddressList.getListWithLocation(true);
            } else {
                mAddressList = mAddressList.useFilter(mFilter);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listView.setAdapter(null);
            mPropgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (Utils.isNullOrWhitespace(mFilter) && mAddressId != 0) {
                for (int i = 0; i < mAddressList.size(); i++) {
                    if (mAddressId == mAddressList.get(i).getId()) {
                        AddressActivity.this.listView.setSelection(i);
                        break;
                    }
                }
            }

            mAdapter = new AddressAdapter(AddressActivity.this, R.layout.address_item, mAddressList);
            listView.setAdapter(mAdapter);
            mPropgress.setVisibility(View.INVISIBLE);
        }
    }


}
