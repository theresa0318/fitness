package com.example.fitsoc.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.fragment.app.Fragment;

import com.example.fitsoc.R;

public class SettingsFragment extends Fragment implements View.OnClickListener{

    //只完成了radio button的UI部分，将信息录入database的部分待补充
    public SettingsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View settingView = inflater.inflate(R.layout.fragment_settings, container, false);
       settingView.findViewById(R.id.rd_male).setOnClickListener(this);
        settingView.findViewById(R.id.rd_female).setOnClickListener(this);
       return settingView;
    }

    @Override
    public void onClick(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.rd_male:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.rd_female:
                if (checked)
                    // Ninjas rule
                    break;
        }
    }
}
