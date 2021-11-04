package com.example.fitsoc.ui.profile;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.CAMERA_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fitsoc.R;
import com.example.fitsoc.data.model.RegisteredUser;
import com.example.fitsoc.ui.Global;
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

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    private TextView profileUsername, profileGender, profileAge, profileHeight, profileWeight;
    private ImageView profileAvatar;
    private ImageView camera;
    public Bitmap cameraImg;
    public Uri imageUri;
    public Uri cameraUri;
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
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 103;
    public static final int CODE_RESULT_REQUEST1 = 104;
    public static final int CODE_RESULT_REQUEST2 = 105;
    /*headShot size*/
    private static int head_output_x = 150;
    private static int head_output_y = 150;


    @SuppressLint("StaticFieldLeak")
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
        String userID;
        mAuth = FirebaseAuth.getInstance();
        /*
        if (Global.getUserID() == null) {
            user = mAuth.getCurrentUser();
            userID = user.getEmail().replace('.', ',');
        } else {
            userID = Global.getUserID().replace('.', ',');
        }*/
        userID = Global.getUserID().replace('.', ',');

        db = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("users");

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

        camera = profileView.findViewById(R.id.profile_camera);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
            }
        });



        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
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
                        new DownloadImageTask(profileAvatar).execute(imageUrl);
                    } else {
                        profileAvatar.setImageDrawable(getResources().getDrawable(R.drawable.profile_icon));
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
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
//        Intent data = null;
//        profileAvatar.setImageURI(data.getData());
//        imageUri = data.getData();
    }


    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_REQUEST_CODE);
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
//        if (requestCode == CAMERA_REQUEST_CODE){
//            imageUri = data.getData();
//            profileAvatar.setImageURI(imageUri);
//        }
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getActivity(), "Camera permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            cropGalleryPhoto(imageUri, 1, 1, head_output_x, head_output_y);
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getExtras() != null && data.getExtras().get("data") != null) {
            cameraImg = (Bitmap) data.getExtras().get("data");
            cameraUri = Uri.parse(MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), cameraImg, null,null));
            cropCameraPhoto(cameraUri, 1, 1, head_output_x, head_output_y);
        }
        if (requestCode == CODE_RESULT_REQUEST1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileAvatar.setImageURI(imageUri);
            uploadGalleryPicture();
        }
        if (requestCode == CODE_RESULT_REQUEST2 && resultCode == RESULT_OK && data != null && data.getExtras() != null && data.getExtras().get("data") != null) {
            cameraImg = (Bitmap) data.getExtras().get("data");
            cameraUri = Uri.parse(MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), cameraImg, null,null));
            profileAvatar.setImageURI(cameraUri);
            uploadCameraPicture();
        }
    }

    private void uploadGalleryPicture() {
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
                        // Log.d(TAG, "Get download url");
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

    private void uploadCameraPicture(){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Uploading");
        pd.show();

        //final String randomKey = UUID.randomUUID().toString();

        StorageReference riversRef = storageReference.child("images/" + username);
        UploadTask uploadTask = riversRef.putFile(cameraUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();

                final StorageReference ref = storage.getReference().child("images/"+username);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Log.d(TAG, "Get download url");
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

    /*crop original photo*/
    public void cropGalleryPhoto(Uri uri, int aspect_x, int aspect_y, int output_x, int output_y){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // setting crop
        intent.putExtra("crop", "true");
        // image proportion
        intent.putExtra("aspect_x", aspect_x);
        intent.putExtra("aspect_y", aspect_y);
        // image width & height
        intent.putExtra("output_x", output_x);
        intent.putExtra("output_y", output_y);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CODE_RESULT_REQUEST1);
    }

    /*crop original photo*/
    public void cropCameraPhoto(Uri uri, int aspect_x, int aspect_y, int output_x, int output_y){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // setting crop
        intent.putExtra("crop", "true");
        // image proportion
        intent.putExtra("aspect_x", aspect_x);
        intent.putExtra("aspect_y", aspect_y);
        // image width & height
        intent.putExtra("output_x", output_x);
        intent.putExtra("output_y", output_y);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CODE_RESULT_REQUEST2);
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data){
//        if (requestCode == 10 && resultCode == RESULT_OK){
//            profileAvatar.setImageURI(data.getData());
//            imageUri = data.getData();
//        }
//    }
}