package com.pulzit.discovery.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.maps.model.LatLng;
import com.pulzit.discovery.R;
import com.pulzit.discovery.domain.Node;
import com.pulzit.discovery.domain.NodeData;
import com.pulzit.discovery.domain.NodeLocation;
import com.pulzit.discovery.services.PulzitService;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersActivity extends AppCompatActivity implements SearchUsersActivityFragment.
        OnFragmentInteractionListener, Firebase.CompletionListener {

    private static final String QUERY_EXTRA = "queryExtra";
    private static final String LATLNG_EXTRA = "LatLngExtra";
    private static final String PLACE_ID_EXTRA = "PlaceIdExtra";
    public static final int USER_PICK_REQUEST = 90;

    String currentQuery;
    SearchView searchUserView;
    LatLng placeLocation;
    String placeId;
    SearchUsersActivityFragment searchUsersActivityFragment;
    boolean[] accountTypes;
    DialogInterface.OnMultiChoiceClickListener onAccountTypesSelectedListener;
    DialogInterface.OnClickListener onAccountTypesConfirmedListener;

    ProgressDialog progressDialog;

    User userSelected;

    MenuItem confirmItem;

    public static void start(Activity activity, String searchQuery, LatLng location, String placeId) {
        Intent intent = new Intent(activity, SearchUsersActivity.class);
        intent.putExtra(QUERY_EXTRA, searchQuery);
        intent.putExtra(LATLNG_EXTRA, location);
        intent.putExtra(PLACE_ID_EXTRA, placeId);

        activity.startActivityForResult(intent, USER_PICK_REQUEST);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.twitter_search_user_of));

        currentQuery = getIntent().getStringExtra(QUERY_EXTRA);
        placeLocation = getIntent().getParcelableExtra(LATLNG_EXTRA);
        placeId = getIntent().getStringExtra(PLACE_ID_EXTRA);

        searchUserView = (SearchView) findViewById(R.id.searchUserView);
        searchUserView.setIconified(false);
        searchUserView.setQuery(currentQuery, true);
        searchUserView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsersActivityFragment.doSearch(searchUserView.getQuery().toString());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            searchUsersActivityFragment = new SearchUsersActivityFragment();
            Bundle args = new Bundle();
            args.putString(SearchUsersActivityFragment.QUERY_EXTRA, currentQuery);
            searchUsersActivityFragment.setArguments(args);
            fragmentTransaction.add(R.id.fragment_container, searchUsersActivityFragment,
                    SearchUsersActivityFragment.class.toString());
            fragmentTransaction.commit();
        } else {
            searchUsersActivityFragment = (SearchUsersActivityFragment)getSupportFragmentManager().
                    findFragmentByTag(SearchUsersActivityFragment.class.toString());
        }

        onAccountTypesSelectedListener = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                accountTypes[which] =isChecked;
            }
        };

        onAccountTypesConfirmedListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                processUserSelected(getSelectedAsList());
            }
        };
    }

    private void onUserSelected() {
        //Todo: seguir cuenta en tw

        CharSequence[] entries = getResources().getTextArray(R.array.channels_mgs);
        accountTypes = new boolean[entries.length];

        new AlertDialog.Builder(this)
                .setMultiChoiceItems(entries, accountTypes, onAccountTypesSelectedListener)
                .setPositiveButton(android.R.string.ok, onAccountTypesConfirmedListener)
                .setTitle(getString(R.string.select_account_types))
                .show();
    }

    private void processUserSelected(List<String> accountTypes){
        NodeData nodeData = new NodeData(userSelected.name, userSelected.followersCount,
                userSelected.description, null, null,
                userSelected.profileImageUrl, placeId);
        NodeLocation nodeLocation = new NodeLocation(placeLocation);
        Node node = new Node(userSelected.getId(), nodeData, nodeLocation);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.progress_msg));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        PulzitService.getInstance().addNode(accountTypes, node, placeId, this);
    }

    @Override
    public void onSearchStarted() {
        searchUserView.clearFocus();
    }

    @Override
    public void onSearchFinished() {
        searchUserView.clearFocus();
    }

    @Override
    public void onAccountSelected(User userSelected) {
        confirmItem.setVisible(true);
        this.userSelected = userSelected;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirm_account, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        confirmItem = menu.findItem(R.id.confirm);
        confirmItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }

        if (item.getItemId() == R.id.confirm) {
            onUserSelected();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
        if(progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if(firebaseError == null) {
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
        } else {
            Toast.makeText(SearchUsersActivity.this, R.string.error_firebase, Toast.LENGTH_LONG)
                    .show();
        }
    }

    public List<String> getSelectedAsList() {
        CharSequence[] entries = getResources().getTextArray(R.array.channels_entries);
        List<String> entriesList = new ArrayList<>();
        for (int i = 0; i < accountTypes.length; i++) {
            if(accountTypes[i]) {
                entriesList.add(entries[i].toString());
            }
        }
        return entriesList;

    }
}
