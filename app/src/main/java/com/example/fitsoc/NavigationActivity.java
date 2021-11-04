package com.example.fitsoc;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.fitsoc.data.model.RegisteredUser;
import com.example.fitsoc.databinding.ActivityNavigationBinding;
import com.example.fitsoc.ui.profile.ProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import static android.content.ContentValues.TAG;

public class NavigationActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityNavigationBinding binding;
    /*
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;
    private DatabaseReference reference;
    private String imageUrl;
    private static final String DEFAULT_IMAGE_URL="@drawable/profile_icon";
    private TextView username_nav;
    private ImageView image_nav;


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            //String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error loading picture", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);//binding.appBarNavigation.toolbar

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
      
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_run, R.id.nav_community, R.id.nav_task, R.id.nav_history, R.id.nav_profile, R.id.nav_settings, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        /*
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        String userID = user.getEmail().replace('.', ',');

        //username_nav = (TextView) findViewById(R.id.username_nav);
        image_nav = (ImageView) findViewById(R.id.imageView);

        //username_nav.setText(user.getEmail());

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RegisteredUser userProfile = snapshot.getValue(RegisteredUser.class);
                if (userProfile != null) {
                    imageUrl = userProfile.getImageUrl();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.w(TAG, "getImageUrl:failure");
            }
        });

        // load image from url if not default picture
        if (!imageUrl.equals(DEFAULT_IMAGE_URL)){
            new NavigationActivity.DownloadImageTask(image_nav)
                    .execute(imageUrl);
        } else {
            image_nav.setImageResource(R.mipmap.ic_launcher_round);
        }
     */
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
