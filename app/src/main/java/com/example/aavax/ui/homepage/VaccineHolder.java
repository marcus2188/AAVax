package com.example.aavax.ui.homepage;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.aavax.R;
import com.example.aavax.ui.AbsVaccineHolder;

import model.Vaccine;

public class VaccineHolder extends AbsVaccineHolder {

    private TextView vaccineName;

    public VaccineHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        vaccineName = itemView.findViewById(R.id.vaccine_name);
    }

    @Override
    public void setDetails(Vaccine vaccine) {
        vaccineName.setText(vaccine.getName());
    }

    @Override
    public void onClick(View itemView) {
        AppCompatActivity activity = (AppCompatActivity) itemView.getContext();
        Fragment myFragment = new MyVaccInfoFragment();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, myFragment).addToBackStack(null).commit();
    }
}

