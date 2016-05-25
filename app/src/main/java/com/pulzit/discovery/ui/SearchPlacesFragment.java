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
import com.pulzit.discovery.domain.PlacesQuery;
import com.pulzit.discovery.global.UtilConstants;
import com.pulzit.discovery.services.GetPlacesAsyncTask;
import com.pulzit.discovery.services.OnPlaceSearchFinishedListener;

import java.util.List;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchPlacesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchPlacesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchPlacesFragment extends Fragment implements OnPlaceSearchFinishedListener {

    private OnFragmentInteractionListener onFragmentInteractionListener;
    GooglePlaces googlePlacesClient;

    GetPlacesAsyncTask getPlacesAsyncTask;

    Button searchButton;
    EditText keywordsTextView;
    PlaceTypesSpinner placeTypesSpinner;
    Spinner orderBySpinner;

    public SearchPlacesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SearchPlacesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchPlacesFragment newInstance() {
        SearchPlacesFragment fragment = new SearchPlacesFragment();
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

        googlePlacesClient = new GooglePlaces("AIzaSyAhI56ol5QeRBUODqV-H0LBCaJM5IJ20u8");

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
        if(query.getTypes() == null) {
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
        LatLngBounds latLngBounds = onFragmentInteractionListener.getSearchableArea();
        PlacesQuery query = new PlacesQuery();
        query.setLat(latLngBounds.getCenter().latitude);
        query.setLng(latLngBounds.getCenter().longitude);
        query.setLang(UtilConstants.PLACES_SEARCH_LANG);
        query.setRadius(SphericalUtil.computeDistanceBetween(latLngBounds.getCenter(),
                latLngBounds.northeast));
        query.setOrderBy(orderBySpinner.getSelectedItem().toString());
        if (keywordsTextView.getText() != null && !keywordsTextView.getText().toString().equals("")) {
            query.setKeywords(keywordsTextView.getText().toString());
            //Google places api does not allow keyword search ordered by distance
            query.setOrderBy("prominence");

        }
        String types = placeTypesSpinner.getSelectedString();
        if (types != null && !types.equals("")) {
            query.setTypes(placeTypesSpinner.getSelectedString());
        }

        return query;
    }

    void doSearch(PlacesQuery query) {
        getPlacesAsyncTask = new GetPlacesAsyncTask(getActivity(), this);
        getPlacesAsyncTask.execute(query);
        onStartSearch();
    }

    public void onStartSearch() {
        searchButton.setEnabled(false);
        keywordsTextView.setEnabled(false);
        placeTypesSpinner.setEnabled(false);
        orderBySpinner.setEnabled(false);
        onFragmentInteractionListener.onStartSearch();
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
            onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFragmentInteractionListener = null;
    }

    @Override
    public void onPlacesSearchFinished(List<Place> places) {
        for (Place place : places) {
            Log.d("Place:", place.getName());
        }
        onFragmentInteractionListener.onFinishSearchSuccessful(places);
        onFinishSearch();
    }

    @Override
    public void onPlacesSearchCancelled(List<Place> places) {
        onFragmentInteractionListener.onFinishSearchFailed();
        onFinishSearch();
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
}
