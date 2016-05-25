package com.pulzit.discovery.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.pulzit.discovery.R;
import com.pulzit.discovery.services.PulzitService;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowPlaceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShowPlaceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowPlaceFragment extends Fragment {

    private String placeName;
    private double lat;
    private double lng;
    private String vicinity;
    private String placeId;
    private String placeIconURL;

    private ProgressBar gettingNodeProgressBar;
    private View noTwitterlayoutView;
    private View twitterOptionslayoutView;

    private static final String ARG_PLACE_NAME = "argPlaceName";
    private static final String ARG_PLACE_LAT = "argPlaceLat";
    private static final String ARG_PLACE_LNG = "argPLaceLng";
    private static final String ARG_PLACE_VICINITY = "argPlaceVicinity";
    private static final String ARG_PLACE_ID = "argPlaceId";
    private static final String ARG_PLACE_ICON = "argPlaceIcon";

    private OnFragmentInteractionListener fragmentInteractionListener;

    public ShowPlaceFragment() {
        // Required empty public constructor
    }

    public static ShowPlaceFragment newInstance(String placeName, String vicinity, String placeId,
                                                double lat, double lng, String placeIconURL) {
        ShowPlaceFragment fragment = new ShowPlaceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLACE_NAME, placeName);
        args.putString(ARG_PLACE_ID, placeId);
        args.putString(ARG_PLACE_VICINITY, vicinity);
        args.putDouble(ARG_PLACE_LAT, lat);
        args.putDouble(ARG_PLACE_LNG, lng);
        args.putString(ARG_PLACE_ICON, placeIconURL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            placeId = getArguments().getString(ARG_PLACE_ID);
            placeName = getArguments().getString(ARG_PLACE_NAME);
            vicinity = getArguments().getString(ARG_PLACE_VICINITY);
            lat = getArguments().getDouble(ARG_PLACE_LAT);
            lng = getArguments().getDouble(ARG_PLACE_LNG);
            placeIconURL = getArguments().getString(ARG_PLACE_ICON);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_place, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView placeNameTextView = (TextView) view.findViewById(R.id.place_title);
        TextView placeVicinityTextView = (TextView) view.findViewById(R.id.place_vicinity);
        ImageView placeIcon = (ImageView) view.findViewById(R.id.place_type_icon);

        gettingNodeProgressBar = (ProgressBar) view.findViewById(R.id.find_tw_account_progress_bar);
        noTwitterlayoutView = view.findViewById(R.id.no_twitter_options);
        twitterOptionslayoutView = view.findViewById(R.id.twitter_account_layout);

        placeNameTextView.setText(placeName);
        placeVicinityTextView.setText(vicinity);
        Picasso.with(getActivity()).load(placeIconURL).into(placeIcon);

        Button searchTwUserButton = (Button) view.findViewById(R.id.search_twitter_user);
        searchTwUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchUsersActivity.start(getActivity(), placeName, new LatLng(lat, lng),
                        placeId);
            }
        });

        PulzitService.getInstance().getDiscoveryPlace(placeId, new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                handleTwitterDataView(result.data);
            }

            @Override
            public void failure(TwitterException e) {
                handleNoTwitterDataView();
            }
        });
    }

    private void handleNoTwitterDataView() {
        gettingNodeProgressBar.setVisibility(View.GONE);
        noTwitterlayoutView.findViewById(R.id.no_twitter_options).setVisibility(View.VISIBLE);
        noTwitterlayoutView.findViewById(R.id.no_suitable_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentInteractionListener.onPlaceDismissed();
            }
        });
        noTwitterlayoutView.findViewById(R.id.no_account_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentInteractionListener.onMissingAccountForPlace();
            }
        });
    }

    private void handleTwitterDataView(final User user) {
        gettingNodeProgressBar.setVisibility(View.GONE);
        twitterOptionslayoutView.setVisibility(View.VISIBLE);

        TextView nameView = (TextView) twitterOptionslayoutView.findViewById(R.id.twitterName);
        TextView accountView = (TextView) twitterOptionslayoutView.findViewById(R.id.twitterAccount);
        ImageView twitterPic = (ImageView) twitterOptionslayoutView.findViewById(R.id.twitterPic);

        nameView.setText(user.name);
        accountView.setText(user.screenName);
        Picasso.with(getActivity()).load(user.profileImageUrl).into(twitterPic);

        twitterOptionslayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserTimelineActivity.start(getActivity(), user.screenName);
            }
        });


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            fragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentInteractionListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onPlaceDismissed();

        void onMissingAccountForPlace();
    }
}
