package com.walmart.moviesdatabase.views;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.walmart.moviesdatabase.R;
import com.walmart.moviesdatabase.models.Movies.MovieDetail;


/**
 * Main activity for the application ,
 *
 * Host 2 fragments SearchFragment and DetailFragment
 *
 * @author Bhavin
 */

public class MainActivity extends AppCompatActivity {

    public String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_container);

        if(getResources().getBoolean(R.bool.isTablet)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }


            // Create an instance of SearchFragment
            SearchFragment searchFragment = new SearchFragment();

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            searchFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, searchFragment).commit();
        }

    }

    public void onItemClick(int position, MovieDetail itemInfo) {
        // Capture the detail fragment from the activity layout
        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment);

        if (detailFragment != null) {
            // If detail frag is available, we're in two-pane layout...
            // Call a method in the DetailFragment to update its content
            detailFragment.getData(itemInfo);
        } else {
            // If the frag is not available, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected item
            DetailFragment newDetailFragment = new DetailFragment();
            Bundle args = new Bundle();
            args.putInt("Position", position);
            args.putParcelable("Item GenreInfo", itemInfo);
            newDetailFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newDetailFragment);

            // Commit the transaction
            transaction.commit();

            transaction.addToBackStack(null);
        }
    }
}