package com.pulzit.discovery.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.pulzit.discovery.R;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

/**
 * Created by gastonsanguinetti on 27/04/16.
 */
public class TwitterUserAdapter extends ArrayAdapter<User> {

    private int userPositionSelected = -1;
    private OnItemSelectedListener onItemSelectedListener;

    public TwitterUserAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        // Creating a view of row.
        View rowView = inflater.inflate(R.layout.twitter_user_pick_row, parent, false);

        TextView userNameTextView = (TextView)rowView.findViewById(R.id.twitterName);
        final TextView userAccountTextView = (TextView)rowView.findViewById(R.id.twitterAccount);
        TextView userDescTextView = (TextView)rowView.findViewById(R.id.twitterDesc);
        ImageView userImageView = (ImageView) rowView.findViewById(R.id.twitterImage);
        RadioButton userSelected = (RadioButton) rowView.findViewById(R.id.twitterUserSelected);

        userNameTextView.setText(getItem(position).name);
        String accountText = "@" + getItem(position).screenName;
        userAccountTextView.setText(accountText);
        userDescTextView.setText(getItem(position).description);
        Picasso.with(getContext())
                .load(getItem(position).profileImageUrl)
                .into(userImageView);
        userSelected.setChecked(position == userPositionSelected);

        userSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    userPositionSelected = position;
                    notifyDataSetChanged();
                    onItemSelectedListener.onUserItemSelected(getItem(position));
                }
            }
        });

        return rowView;
    }


    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public interface OnItemSelectedListener {
        public void onUserItemSelected(User itemSelected);
    }
}
