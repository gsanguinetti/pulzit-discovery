package com.pulzit.discovery.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.pulzit.discovery.R;

import java.util.List;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    GooglePlaces client;

    GetPlacesAsyncTask getPlacesAsyncTask;

    Button searchButton;
    EditText keywordsTextView;
    PlaceTypesSpinner placeTypesSpinner;
    Spinner orderBySpinner;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        client = new GooglePlaces("AIzaSyAhI56ol5QeRBUODqV-H0LBCaJM5IJ20u8");

        keywordsTextView = (EditText) view.findViewById(R.id.place_query_edittext);
        placeTypesSpinner = (PlaceTypesSpinner) view.findViewById(R.id.place_type_select);
        orderBySpinner = (Spinner) view.findViewById(R.id.order_by_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner,
                getContext().getResources().getStringArray(R.array.order_by_entries));
        orderBySpinner.setAdapter(adapter);

        searchButton = (Button) view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PlacesQuery query = buildPlaceSearch();
                if(validateFields(query)) {
                    doSearch(query);
                }
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private boolean validateFields(PlacesQuery query) {
        if(query.types == null) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getContext().getString(R.string.select_any_place))
                    .setTitle(getContext().getString(R.string.select_places))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return false;
        }
        return true;
    }

    private PlacesQuery buildPlaceSearch() {
        LatLngBounds latLngBounds = mListener.getSearchableArea();
        PlacesQuery query = new PlacesQuery();
        query.lat = latLngBounds.getCenter().latitude;
        query.lng = latLngBounds.getCenter().longitude;
        query.radius = SphericalUtil.computeDistanceBetween(latLngBounds.getCenter(),
                latLngBounds.northeast);
        if (keywordsTextView.getText() != null && !keywordsTextView.getText().toString().equals("")) {
            query.keywords = keywordsTextView.getText().toString();
        }
        String types = placeTypesSpinner.getSelectedString();
        if (types != null && !types.equals("")) {
            query.types = placeTypesSpinner.getSelectedString();
        }
        query.orderBy = orderBySpinner.getSelectedItem().toString();

        return query;
    }

    void doSearch(PlacesQuery query) {
        getPlacesAsyncTask = new GetPlacesAsyncTask();
        getPlacesAsyncTask.execute(query);
        onStartSearch();
    }

    public void onStartSearch() {
        searchButton.setEnabled(false);
        keywordsTextView.setEnabled(false);
        placeTypesSpinner.setEnabled(false);
        orderBySpinner.setEnabled(false);
        mListener.onStartSearch();
    }

    public void onFinishSearch() {
        searchButton.setEnabled(true);
        keywordsTextView.setEnabled(true);
        placeTypesSpinner.setEnabled(true);
        orderBySpinner.setEnabled(true);
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
        void onStartSearch();

        void onFinishSearchFailed();

        void onFinishSearchSuccessful(List<Place> places);

        LatLngBounds getSearchableArea();

    }

    private class PlacesQuery {
        double lat;
        double lng;
        double radius;
        String types;
        String lang = "es";
        String orderBy;
        String keywords;
    }

    private class GetPlacesAsyncTask extends AsyncTask<PlacesQuery, Integer, List<Place>> {

        @Override
        protected List<Place> doInBackground(PlacesQuery... params) {
            PlacesQuery query = params[0];
            if (query.keywords != null) {
                return client.getNearbyPlaces(query.lat, query.lng, query.radius,
                        Param.name("language").value(query.lang),
                        Param.name("types").value(query.types),
                        Param.name("rankby").value(query.orderBy),
                        Param.name("keyword").value(query.keywords));
            } else {
                return client.getNearbyPlaces(query.lat, query.lng, query.radius,
                        Param.name("language").value(query.lang),
                        Param.name("types").value(query.types),
                        Param.name("rankBy").value(query.orderBy));
            }
        }

        @Override
        protected void onCancelled(List<Place> places) {
            super.onCancelled(places);
            mListener.onFinishSearchFailed();
            onFinishSearch();
        }

        @Override
        protected void onPostExecute(List<Place> places) {
            super.onPostExecute(places);
            for (Place place : places) {
                Log.d("Place:", place.getName());
            }
            mListener.onFinishSearchSuccessful(places);
            onFinishSearch();
        }
    }
}
