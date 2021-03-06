package com.example.aavax.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aavax.R;

import java.util.HashMap;

/**
 * holder to hold profiles in OtherProfileAdapter
 * Used in:{@link OtherProfilesAdapter}
 * Calls: {@link EditProfileFragment}
 */
public class OtherProfilesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView profileName;
    private String pId;
    private HashMap<String, String> profiles;
    private String uId;

    public OtherProfilesHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        profileName = itemView.findViewById(R.id.profile_name);
    }

    public void setDetails(String profile, int position, String uId) {
        System.out.println("SET PROFILE TEXT IS WHEN");
        System.out.println("profile name in holder" + profile);
        profileName.setText(profile);
        this.uId = uId;


    }

    public void setProfileId(String id){
        pId = id;
    }

    @Override
    public void onClick(View itemView) {
        AppCompatActivity activity = (AppCompatActivity) itemView.getContext();

        Bundle bundle = new Bundle();
        bundle.putString("pId", pId);

        Fragment myFragment = new EditProfileFragment();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, myFragment).addToBackStack(null).commit();

        myFragment.setArguments(bundle);
    }
}
