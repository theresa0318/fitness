package com.example.fitsoc.ui.task;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.fitsoc.R;

public class TaskFragment extends Fragment {
    private CheckBox easyButton;
    private CheckBox mediumButton;
    private CheckBox hardButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_task, container, false);

        CardView cardView1 = (CardView) view.findViewById(R.id.taskCard1);
        cardView1.setRadius(20);
        cardView1.setCardElevation(20);
        cardView1.setContentPadding(10,10,10,10);

        CardView cardView2 = (CardView) view.findViewById(R.id.taskCard2);
        cardView2.setRadius(20);
        cardView2.setCardElevation(20);
        cardView2.setContentPadding(10,10,10,10);

        CardView cardView3 = (CardView) view.findViewById(R.id.taskCard3);
        cardView3.setRadius(20);
        cardView3.setCardElevation(20);
        cardView3.setContentPadding(10,10,10,10);

        easyButton = (CheckBox) view.findViewById(R.id.easyButton);
        mediumButton = (CheckBox) view.findViewById(R.id.mediumButton);
        hardButton = (CheckBox) view.findViewById(R.id.hardButton);

        // Inflate the layout for this fragment
        return view;
    }

}
