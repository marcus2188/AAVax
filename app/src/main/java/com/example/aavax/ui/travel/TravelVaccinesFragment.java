package com.example.aavax.ui.travel;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aavax.R;
import com.example.aavax.ui.CustomMessageEvent;
import com.example.aavax.ui.FirebaseManager;
import com.example.aavax.ui.IMainActivity;
import com.example.aavax.ui.maps.MapsActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import entity.CDCThreatLevel;
import entity.VaccineLogEntry;


public class TravelVaccinesFragment extends Fragment {

    //private static final String TAG = "RemindersFragment";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private IMainActivity mIMainActivity;
    //vars
    private ArrayList<String> mMandatoryVaccines = new ArrayList<>();
    private ArrayList<String> mRecommendedVaccines = new ArrayList<>();
    private ArrayList<Integer> mMandatoryTakenImgs = new ArrayList<>();
    private ArrayList<Integer> mRecommendedTakenImgs = new ArrayList<>();
    private ArrayList<String> mCdcHeaders = new ArrayList<>();
    ArrayList<ArrayList<String>> mCdcDetails = new ArrayList<>();
    private String[][] mCdcDetailsArray;
    private FirebaseManager firebaseManager;
    private String mIncomingMessage = "";
    private String uId;
    private ExpandableListView expandableTextView;
    private ImageView mImageView;
    private String[] asia_countries;
    private String[] north_america_countries;
    private String[] south_america_countries;
    private String[] europe_countries;
    private String[] africa_countries;
    private String[] oceania_countries;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println(getTag() + " IS THE TAG");
        mIMainActivity.setToolbarTitle(getTag());
        Bundle bundle = this.getArguments();
        if (bundle != null){
            mIncomingMessage = bundle.getString((getString(R.string.intent_message)));
            System.out.println(mIncomingMessage + "IS THE INCOMING MSG");
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chosen_country, container, false);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);

        }

        Resources res = view.getResources();
        mImageView = view.findViewById(R.id.countryImg);
        mImageView.setImageResource(findImage(view, mIncomingMessage));
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        firebaseManager = new FirebaseManager();



        firebaseManager.retrieveCDCThreatLevels(new FirebaseManager.MyCallBackCdcLevels() {
            @Override
            public void onCallback(ArrayList<CDCThreatLevel> levels) {
                for (CDCThreatLevel level : levels){
                    switch (level.getLevel()){
                        case 3: {
                            mCdcHeaders.add("Level 3: Warning");
                            break;
                        }
                        case 2: {
                            mCdcHeaders.add("Level 2: Alert");
                            break;
                        }
                        case 1: {
                            mCdcHeaders.add("Level 1: Watch");
                            break;
                        }
                        default: break;
                    }
                    ArrayList<String> newList = new ArrayList<>();
                    newList.add(level.getDetail());
                    mCdcDetails.add(newList);
                    mCdcDetailsArray = new String[mCdcDetails.size()][];
                    for (int i = 0; i < mCdcDetails.size(); i++) {
                        ArrayList<String> row = mCdcDetails.get(i);
                        mCdcDetailsArray[i] = row.toArray(new String[row.size()]);
                    }
                }
                System.out.println(mCdcDetailsArray);
                expandableTextView = getView().findViewById(R.id.cdcLevelList);
                ExpandableTextViewAdapter adapter = new ExpandableTextViewAdapter(mCdcHeaders.toArray(new String[0]), mCdcDetailsArray, getContext());
                expandableTextView.setAdapter(adapter);

            }
        }, mIncomingMessage);





        RecyclerView recyclerViewRecVaccines = view.findViewById(R.id.recyclerViewRecommendedVaccines);
        RecyclerViewTravelVacAdapter adapterRec = new RecyclerViewTravelVacAdapter(mRecommendedVaccines, mRecommendedTakenImgs);
        recyclerViewRecVaccines.setAdapter(adapterRec);
        recyclerViewRecVaccines.setLayoutManager(new LinearLayoutManager(getContext()));


        final Button viewClinicBtn = view.findViewById(R.id.ViewClinicsBtn);
        viewClinicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });

        firebaseManager.retrieveMandatoryVaccines(new FirebaseManager.MyCallBackVaccines() {
            @Override
            public void onCallback(ArrayList<String> vaccines) {
                mMandatoryVaccines.addAll(vaccines);
                firebaseManager.retrieveVaccineLog(new FirebaseManager.MyCallbackVaccineLog() {
                    @Override
                    public void onCallback(ArrayList<VaccineLogEntry> value) {
                        ArrayList<VaccineLogEntry> userEntries = value;
                        for (String vaccineName : mMandatoryVaccines){
                            boolean valid = false;
                            for (VaccineLogEntry entry : userEntries){
                                if (vaccineName.equals(entry.getVaccine().getName())){
                                    LocalDate dateTaken = entry.getDateTaken().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                    LocalDate dateExpired = dateTaken.plusMonths(entry.getVaccine().getNumMonths());
                                    if (LocalDate.now().compareTo(dateExpired) < 0) {
                                        valid = true;
                                        break;
                                    }
                                }
                            }
                            if (valid)
                                mMandatoryTakenImgs.add(R.drawable.ic_check_circle_green_16dp);
                            else
                                mMandatoryTakenImgs.add(R.drawable.ic_warning_yellow_16dp);

                            RecyclerView recyclerViewManVaccines = view.findViewById(R.id.recyclerViewMandatoryVaccines);
                            RecyclerViewTravelVacAdapter adapterMan = new RecyclerViewTravelVacAdapter(mMandatoryVaccines, mMandatoryTakenImgs);
                            recyclerViewManVaccines.setAdapter(adapterMan);
                            recyclerViewManVaccines.setLayoutManager(new LinearLayoutManager(getContext()));
                        }
                    }
                }, uId);

            }

        }, mIncomingMessage);

        firebaseManager.retrieveRecommendedVaccines(new FirebaseManager.MyCallBackVaccines() {
            @Override
            public void onCallback(ArrayList<String> vaccines) {
                mRecommendedVaccines.addAll(vaccines);
                firebaseManager.retrieveVaccineLog(new FirebaseManager.MyCallbackVaccineLog() {
                    @Override
                    public void onCallback(ArrayList<VaccineLogEntry> value) {
                        ArrayList<VaccineLogEntry> userEntries = value;
                        int day,year, mth;
                        for (String vaccineName : mRecommendedVaccines){
                            boolean valid = false;
                            for (VaccineLogEntry entry : userEntries){
                                if (vaccineName.equals(entry.getVaccine().getName())){
                                    Date date = entry.getDateTaken();
                                    day = date.getDay();
                                    mth = date.getMonth();
                                    year = date.getYear();


                                    System.out.println("DATE TAKEN !!! " + day+"/"+mth+"/"+year);
                                    Date nextDue;
                                    int numMths = entry.getVaccine().getNumMonths();
                                    int newMth = numMths+mth;
                                    if (newMth<=12)
                                    {
                                        newMth = numMths+mth;
                                        nextDue = new Date(year, newMth, day);
                                    }
                                    else
                                    {
                                        while (newMth>12)
                                        {
                                            newMth = newMth-12;
                                            year++;
                                        }
                                        nextDue = new Date(year, newMth, day);
                                        System.out.println("NEXT DUE DATE!!!! day"+nextDue.getDay()+"/"+nextDue.getMonth()+"/"+nextDue.getYear());
                                    }
                                    LocalDate currentDate = LocalDate.now();
                                    LocalDate expireDate = LocalDate.of(year+2000,newMth, day);

                                    //Date currentDate = new Date();
                                    /*
                                    int day1,year1, mth1;
                                    day1 = currentDate.getDayOfMonth();
                                    mth1 = currentDate.getMonthValue();
                                    year1 = currentDate.getYear();
                                    System.out.println("CURRENT DATE "+day1+"/"+mth1+"/"+year1);*/
                                    System.out.println(currentDate.toString());
                                    System.out.println(expireDate.toString());
                                    if (expireDate.compareTo(currentDate)>0)
                                    {
                                        valid = true;
                                        break;

                                    }

                                }
                            }
                            if (valid)
                                mRecommendedTakenImgs.add(R.drawable.ic_check_circle_green_16dp);
                            else
                                mRecommendedTakenImgs.add(R.drawable.ic_warning_yellow_16dp);

                            RecyclerView recyclerViewRecVaccines = view.findViewById(R.id.recyclerViewRecommendedVaccines);
                            RecyclerViewTravelVacAdapter adapterRec = new RecyclerViewTravelVacAdapter(mRecommendedVaccines, mRecommendedTakenImgs);
                            recyclerViewRecVaccines.setAdapter(adapterRec);
                            recyclerViewRecVaccines.setLayoutManager(new LinearLayoutManager(getContext()));
                        }
                    }
                }, uId);
            }

        }, mIncomingMessage);



/*
        if(isServicesOK()){
            Button viewClinicsBtn = (Button) view.findViewById(R.id.ViewClinicsBtn);
            viewClinicsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIMainActivity.inflateFragment("Nearby Clinics", "");
                }
            });
        }
        */


        return view;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mIMainActivity = (IMainActivity) getActivity();

    }

    @Override
    public void onStart(){
        super.onStart();
        //EventBus.getDefault().register(this);
    }

    /**
     * On stop, it will stop getting updates from EventBus
     */
    @Override
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(CustomMessageEvent event) {
        Log.d("HOMEFRAG EB RECEIVER", "Username :\"" + event.getCustomMessage() + "\" Successfully Received!");
        uId = event.getCustomMessage();
        //DisplayName.setText(usernameImported);

    }


    public boolean isServicesOK(){


        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and user can make map requests

            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(getContext(), "No map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private int findImage(View view, String countryName){
        Resources res = view.getResources();
        asia_countries = res.getStringArray(R.array.asia_all_countries);
        north_america_countries = res.getStringArray(R.array.north_america_all_countries);
        south_america_countries = res.getStringArray(R.array.south_america_all_countries);
        europe_countries = res.getStringArray(R.array.europe_all_countries);
        africa_countries = res.getStringArray(R.array.africa_all_countries);
        oceania_countries = res.getStringArray(R.array.oceania_popular_countries);

        if (Arrays.asList(asia_countries).contains(countryName))
            return R.drawable.asia;
        else if (Arrays.asList(north_america_countries).contains(countryName))
            return R.drawable.north_america;
        else if (Arrays.asList(south_america_countries).contains(countryName))
            return R.drawable.south_america;
        else if (Arrays.asList(europe_countries).contains(countryName))
            return R.drawable.europe;
        else if (Arrays.asList(africa_countries).contains(countryName))
            return R.drawable.africa;
        else if (Arrays.asList(oceania_countries).contains(countryName))
            return R.drawable.oceania;
        else
            return R.drawable.antarctica;

    }

}