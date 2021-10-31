package com.example.fitsoc.ui.profile;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitsoc.R;
import com.example.fitsoc.data.model.RegisteredUser;
import com.example.fitsoc.ui.login.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class ProfileFragment extends Fragment {

    private TextView profileUsername, profileGender, profileAge, profileHeight, profileWeight;
    private ImageView profileAvatar;
    public Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;
    private DatabaseReference reference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View profileView = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        String userID = user.getEmail().replace('.', ',');

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        profileUsername = profileView.findViewById(R.id.text_profile_username);
        profileGender = profileView.findViewById(R.id.text_profile_gender);
        profileAge = profileView.findViewById(R.id.text_profile_age);
        profileHeight = profileView.findViewById(R.id.text_profile_height);
        profileWeight = profileView.findViewById(R.id.text_profile_weight);
        profileAvatar = profileView.findViewById(R.id.profile_avatar);

        profileAvatar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RegisteredUser userProfile = snapshot.getValue(RegisteredUser.class);
                if (userProfile != null){
//                    String avatar = userProfile.getImageUrl();
                    String username = userProfile.getUserId();
                    String gender = userProfile.getGender();
                    int age = userProfile.getAge();
                    int height = userProfile.getHeight();
                    int weight = userProfile.getWeight();

//                    profileAvatar.setImageURI();
                    profileUsername.setText("Username: " + username);
                    profileGender.setText("Gender: " + gender);
                    profileAge.setText("Age: " + age);
                    profileHeight.setText("Height: " + height + " cm");
                    profileWeight.setText("Weight: " + weight + " kg");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Something wrong happened:(", Toast.LENGTH_SHORT).show();
            }
        });

        return profileView;
    }

    public void choosePicture(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
//        Intent data = null;
//        profileAvatar.setImageURI(data.getData());
//        imageUri = data.getData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            profileAvatar.setImageURI(imageUri);
            uploadPicture();
//            String uid = user.getEmail().replace(".", ",");
//            StorageReference picRef = FirebaseStorage.getInstance().getReference().child("users").child(uid);
//            picRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        picRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Uri> task) {
//                                RegisteredUser newUser = new RegisteredUser();
//                                newUser.setImageurl(task.toString());
//                                db.getReference().child("users").child(newUser.getUserId().replace(".", ",")).setValue(newUser);
//                            }
//                        });
//                    } else {
//                        Toast.makeText(getContext(), "Something wrong happened.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
        }
    }

    private void uploadPicture() {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Uploading");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Toast.makeText(getContext(), "Image uploaded.", Toast.LENGTH_SHORT).show();

                RegisteredUser updatedUser = new RegisteredUser(user.getEmail());
                String gender = updatedUser.getGender();
                int age = updatedUser.getAge();
                int height = updatedUser.getHeight();
                int weight = updatedUser.getWeight();
                updatedUser.setImageUrl(taskSnapshot.toString());
                updatedUser.setGender(gender);
                updatedUser.setAge(age);
                updatedUser.setHeight(height);
                updatedUser.setWeight(weight);
                db.getReference().child("users").child(updatedUser.getUserId().replace(".", ",")).setValue(updatedUser);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getContext(), "Failed to upload.", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progressPercent = (100.00* taskSnapshot.getBytesTransferred() /taskSnapshot.getTotalByteCount());
                pd.setMessage("progress: " + (int)progressPercent + "%");
            }
        });
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data){
//        if (requestCode == 10 && resultCode == RESULT_OK){
//            profileAvatar.setImageURI(data.getData());
//            imageUri = data.getData();
//        }
//    }
}