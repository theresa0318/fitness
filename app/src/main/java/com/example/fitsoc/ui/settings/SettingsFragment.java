package com.example.fitsoc.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fitsoc.R;
import com.example.fitsoc.data.model.RegisteredUser;
import com.example.fitsoc.databinding.ActivityLoginBinding;
import com.example.fitsoc.ui.login.LoginActivity;
import com.example.fitsoc.ui.login.RegisterActivity;
import com.example.fitsoc.ui.profile.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

public class SettingsFragment extends Fragment implements View.OnClickListener {


    private EditText newPasswordText;
    private EditText ageText;
    private EditText heightText;
    private EditText weightText;
    private String gender="";
    private FirebaseAuth mAuth;
    private FirebaseUser user;


    //只完成了radio button的UI部分，将信息录入database的部分待补充
    public SettingsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View settingView = inflater.inflate(R.layout.fragment_settings, container, false);
       settingView.findViewById(R.id.rd_male).setOnClickListener(this);
       settingView.findViewById(R.id.rd_female).setOnClickListener(this);

       newPasswordText = settingView.findViewById(R.id.password_reset);
       ageText = settingView.findViewById(R.id.age_set);
       heightText = settingView.findViewById(R.id.height_set);
       weightText = settingView.findViewById(R.id.weight_set);
       Button saveButton = (Button)settingView.findViewById(R.id.settings_save);
       saveButton.setOnClickListener(this);

       mAuth = FirebaseAuth.getInstance();
       user = mAuth.getCurrentUser();

       return settingView;
    }


    public void onClick(View view) {


        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.rd_male:
                // Is the button now checked?
                boolean checked = ((RadioButton) view).isChecked();
                if (checked)
                    gender = "male";
                    Log.d(TAG, "choose gender: male");
                    break;
            case R.id.rd_female:
                // Is the button now checked?
                boolean isChecked = ((RadioButton) view).isChecked();
                if (isChecked)
                    gender = "female";
                    Log.d(TAG, "choose gender: female");
                    break;
            case R.id.settings_save:
                Log.d(TAG, "click save button");
                saveSettings();
                break;

        }
    }



    public void saveSettings() {

        user.updatePassword(String.valueOf(newPasswordText.getText()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User password updated.");
                        }
                        else {
                            Log.d(TAG, "Fail to update user password.");
                            Toast.makeText(getActivity(), "Fail to update password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Log.d(TAG, user.getEmail());

        RegisteredUser updatedUser = new RegisteredUser(user.getEmail());
        updatedUser.setGender(gender);
        updatedUser.setAge(Integer.parseInt(String.valueOf(ageText.getText())));
        updatedUser.setHeight(Integer.parseInt(String.valueOf(heightText.getText())));
        updatedUser.setWeight(Integer.parseInt(String.valueOf(weightText.getText())));

        DatabaseReference myRef = database.getReference();
        myRef.child("users").child(user.getEmail().replace(".", ",")).setValue(updatedUser).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                // Register success, display a message to the user, then redirect to login page.
                Log.d(TAG, "updateUserSettings:success");
                Toast.makeText(this.getActivity(), "You have updated your settings successfully.", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(this.getActivity(), this.getActivity().getClass());
                //startActivity(intent);
            }
            else {
                // If register fails, display a message to the user.
                Log.w(TAG, "updateUserSettings:failure", task1.getException());
                Toast.makeText(this.getActivity(), "Fail to update settings! Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

}
