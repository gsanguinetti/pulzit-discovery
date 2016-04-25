package com.pulzit.discovery.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pulzit.discovery.R;
import com.squareup.picasso.Picasso;

import se.walkercrou.places.Place;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlaceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlaceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaceFragment extends Fragment {

    private String placeName;
    private double lat;
    private double lng;
    private String vicinity;
    private String placeId;
    private String placeIconURL;

    private static final String ARG_PLACE_NAME = "argPlaceName";
    private static final String ARG_PLACE_LAT = "argPlaceLat";
    private static final String ARG_PLACE_LNG = "argPLaceLng";
    private static final String ARG_PLACE_VICINITY = "argPlaceVicinity";
    private static final String ARG_PLACE_ID = "argPlaceId";
    private static final String ARG_PLACE_ICON = "argPlaceIcon";


    private OnFragmentInteractionListener mListener;

    public PlaceFragment() {
        // Required empty public constructor
    }

    public static PlaceFragment newInstance(String placeName, String vicinity, String placeId,
                                            double lat, double lng, String placeIconURL) {
        PlaceFragment fragment = new PlaceFragment();
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

        placeNameTextView.setText(placeName);
        placeVicinityTextView.setText(vicinity);
        Picasso.with(getActivity()).load(placeIconURL).into(placeIcon);
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        void onFragmentInteraction(Uri uri);
    }
}
