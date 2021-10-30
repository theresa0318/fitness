package com.example.fitsoc.ui.logout;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.fitsoc.FrontPageActivity;
import com.example.fitsoc.NavigationActivity;
import com.example.fitsoc.R;
import com.example.fitsoc.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutFragment extends Fragment{

    private Button logoutCancel, logoutOk;
    private FirebaseAuth mAuth;
    private ProgressBar loadingProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View logoutView = inflater.inflate(R.layout.fragment_logout, container, false);

        mAuth = FirebaseAuth.getInstance();
        loadingProgressBar = logoutView.findViewById(R.id.loading);
        logoutCancel = logoutView.findViewById(R.id.logout_cancel);
        logoutOk = logoutView.findViewById(R.id.logout_ok);
        logoutCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NavigationActivity.class));
            }
        });
        logoutOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        return logoutView;
    }

    private void logout() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        mAuth.signOut();
        Toast.makeText(getActivity(), "You have logged out successfully!",
                Toast.LENGTH_SHORT).show();
        loadingProgressBar.setVisibility(GONE);
        startActivity(new Intent(getActivity(), FrontPageActivity.class));
    }


}
