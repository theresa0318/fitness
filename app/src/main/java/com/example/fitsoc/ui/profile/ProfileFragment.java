package com.example.fitsoc.ui.profile;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import com.google.android.gms.tasks.Continuation;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
    private String username;
    private String gender;
    private int age;
    private int height;
    private int weight;
    private String imageUrl;
    private static final String DEFAULT_IMAGE_URL="@drawable/profile_icon";


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
                if (userProfile != null) {

                    username = userProfile.getUserId();
                    gender = userProfile.getGender();
                    age = userProfile.getAge();
                    height = userProfile.getHeight();
                    weight = userProfile.getWeight();
                    imageUrl = userProfile.getImageUrl();


                    profileUsername.setText("Username: " + username);
                    profileGender.setText("Gender: " + gender);
                    profileAge.setText("Age: " + age);
                    profileHeight.setText("Height: " + height + " cm");
                    profileWeight.setText("Weight: " + weight + " kg");
                    //int imageResource = getResources().getIdentifier(imageUrl, "drawable", getContext().getPackageName());
                    //Drawable res = getResources().getDrawable(imageResource);
                    //profileAvatar.setImageDrawable(res);
                    //profileAvatar.setImageURI(Uri.parse(imageUrl));

                    // load image from url if not default picture
                    if (!imageUrl.equals(DEFAULT_IMAGE_URL)){
                        new DownloadImageTask(profileAvatar)
                                .execute(imageUrl);
                    }
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

        //final String randomKey = UUID.randomUUID().toString();

        StorageReference riversRef = storageReference.child("images/" + username);
        UploadTask uploadTask = riversRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                //Toast.makeText(getContext(), "Image uploaded.", Toast.LENGTH_SHORT).show();

                /*
                RegisteredUser updatedUser = new RegisteredUser(user.getEmail());
                //updatedUser.setImageUrl(taskSnapshot.toString());
                updatedUser.setGender(gender);
                updatedUser.setAge(age);
                updatedUser.setHeight(height);
                updatedUser.setWeight(weight);
                */

                final StorageReference ref = storage.getReference().child("images/"+username);
                //uploadTask = ref.putFile(file);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        //Log.d(TAG, "Get download url");
                        // Continue with the task to get the download URL
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            imageUrl = String.valueOf(downloadUri);
                            //updatedUser.setImageUrl(imageUrl);
                            Log.d(TAG, imageUrl);

                            DatabaseReference userRef = db.getReference().child("users").child(username.replace(".", ","));
                            Map<String, Object> userUpdate = new HashMap<>();
                            userUpdate.put("imageUrl", imageUrl);
                            userRef.updateChildren(userUpdate,new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        Log.w(TAG, "uploadImageUrl:failure");
                                        Toast.makeText(getActivity(), "Fail to upload profile image! Please try again.",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "uploadImageUrl:success");
                                        Toast.makeText(getActivity(), "You have uploaded your profile image successfully.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            //db.getReference().child("users").child(updatedUser.getUserId().replace(".", ",")).setValue(updatedUser);
                        } else {
                            Log.w(TAG, "getImageUrl:failure");
                            Toast.makeText(getActivity(), "Fail to upload profile image! Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getContext(), "Fail to upload profile image! Please try again.", Toast.LENGTH_SHORT).show();
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