package com.pulzit.discovery.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.pulzit.discovery.R;

import java.net.URLEncoder;

public class PlaceTypesSpinner extends Spinner {

    private CharSequence[] entries;
    private boolean[] selected;
    private MultiSpinnerListener listener;

    public PlaceTypesSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PlaceTypesSpinner);
        entries = a.getTextArray(R.styleable.PlaceTypesSpinner_android_entries);
        if (entries != null) {
            selected = new boolean[entries.length]; // false-filled by default
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                R.layout.spinner,
                new String[]{getContext().getString(R.string.select_places)});
        setAdapter(adapter);
        a.recycle();
    }

    private DialogInterface.OnMultiChoiceClickListener mOnMultiChoiceClickListener = new DialogInterface.OnMultiChoiceClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            selected[which] = isChecked;
        }
    };

    private DialogInterface.OnClickListener mOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // build new spinner text & delimiter management
            StringBuffer spinnerBuffer = new StringBuffer();
            for (int i = 0; i < entries.length; i++) {
                if (selected[i]) {
                    spinnerBuffer.append(entries[i]);
                    spinnerBuffer.append(", ");
                }
            }

            // Remove trailing comma
            if (spinnerBuffer.length() > 2) {
                spinnerBuffer.setLength(spinnerBuffer.length() - 2);
            }

            // display new text
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    R.layout.spinner,
                    spinnerBuffer.toString().isEmpty() ?
                            new String[]{getContext().getString(R.string.select_places)} :
                            new String[]{spinnerBuffer.toString()});
            setAdapter(adapter);

            if (listener != null) {
                listener.onItemsSelected(selected);
            }

            // hide dialog
            dialog.dismiss();
        }
    };

    @Override
    public boolean performClick() {
        new AlertDialog.Builder(getContext())
                .setMultiChoiceItems(entries, selected, mOnMultiChoiceClickListener)
                .setPositiveButton(android.R.string.ok, mOnClickListener)
                .setTitle(getContext().getString(R.string.select_places))
                .show();
        return true;
    }

    public void setMultiSpinnerListener(MultiSpinnerListener listener) {
        this.listener = listener;
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }

    public String getSelectedString() {
        String selectedString = new String();
        for (int i = 0; i < entries.length; i++) {
            if(selected[i]) {
                selectedString = selectedString + entries[i] + "|";
            }
        }
        if (selectedString.length() > 0) {
            selectedString = selectedString.substring(0, selectedString.length() - 1);
        }
        return URLEncoder.encode(selectedString);
    }
}
