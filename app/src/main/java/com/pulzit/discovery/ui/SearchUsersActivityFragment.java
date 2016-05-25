package com.pulzit.discovery.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pulzit.discovery.R;
import com.pulzit.discovery.services.PulzitService;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchUsersActivityFragment extends ListFragment implements AdapterView.OnItemClickListener,
        TwitterUserAdapter.OnItemSelectedListener {

    public static final String QUERY_EXTRA = "queryExtra";

    private TwitterUserAdapter twitterUserAdapter;
    private Callback<List<User>> listCallback;

    private ProgressBar progressBar;
    private View emptyView;

    private OnFragmentInteractionListener mListener;

    public SearchUsersActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_users, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = (ProgressBar)view.findViewById(R.id.twitterProgress);
        listCallback = new Callback<List<User>>() {
            @Override
            public void success(Result<List<User>> result) {
                if (twitterUserAdapter == null) {
                    ArrayList<User> data = new ArrayList<>(result.data);
                    twitterUserAdapter = new TwitterUserAdapter(getActivity(),
                            R.layout.twitter_user_pick_row, data);
                    twitterUserAdapter.setOnItemSelectedListener(SearchUsersActivityFragment.this);
                    getListView().setAdapter(twitterUserAdapter);
                } else {
                    twitterUserAdapter.clear();
                    twitterUserAdapter.addAll(result.data);
                    twitterUserAdapter.notifyDataSetChanged();
                }

                getListView().setOnItemClickListener(SearchUsersActivityFragment.this);

                progressBar.setVisibility(View.GONE);
                progressBar.setIndeterminate(false);

                if(result.data.size() > 0){
                    getListView().setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                }
                mListener.onSearchFinished();
            }

            @Override
            public void failure(TwitterException e) {
                emptyView.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(),getContext().getString(R.string.error_search), Toast.LENGTH_LONG)
                        .show();
                mListener.onSearchFinished();
            }
        };

        doSearch(getArguments().getString(QUERY_EXTRA));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void doSearch(String query) {
        mListener.onSearchStarted();
        if (query != null) {
            getListView().setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            PulzitService.getInstance().getCandidateTwitterAccountsForPlace(query, listCallback);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UserTimelineActivity.start(getActivity(),
                twitterUserAdapter.getItem(position).screenName);
    }

    @Override
    public void onUserItemSelected(User itemSelected) {
        mListener.onAccountSelected(itemSelected);
    }

    public interface OnFragmentInteractionListener {
        void onSearchStarted();
        void onSearchFinished();
        void onAccountSelected(User userSelected);
    }

}
