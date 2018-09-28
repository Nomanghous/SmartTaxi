package com.logixcess.smarttaxiapplication.Activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.logixcess.smarttaxiapplication.DriverModule.DriverMainActivity;
import com.logixcess.smarttaxiapplication.Fragments.DatePickerFragment;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.Passenger;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.MyFirebaseInstanceIDService;
import com.logixcess.smarttaxiapplication.Services.MyFirebaseMessagingService;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.FirebaseHelper;
import com.logixcess.smarttaxiapplication.Utils.PermissionHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import static com.logixcess.smarttaxiapplication.Utils.Constants.FilePathUri;

public class Register_Next_Step extends AppCompatActivity {

    FirebaseHelper firebaseHelper;
    User user_data;
    Spinner sp_intitution_name,sp_workplace_name;
    RadioGroup  radio_group;
    Passenger passenger;
    Driver driver;
    FirebaseAuth auth;
    String cal_view;
    String cal_date;
    LinearLayout layout_passenger,layout_driver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register__next__step);
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user_data = getIntent().getParcelableExtra("user_data");
        layout_passenger = findViewById(R.id.layout_passenger);
        layout_driver = findViewById(R.id.layout_driver);
        driver = new Driver();
        if(user_data != null)
        {
            if(user_data.getUser_type().equals("Passenger"))
            {
                if(layout_passenger.getVisibility()==View.GONE)
                    layout_passenger.setVisibility(View.VISIBLE);
                if(layout_driver.getVisibility()==View.VISIBLE)
                    layout_driver.setVisibility(View.GONE);
            }
            else if(user_data.getUser_type().equals("Driver"))
            {
                if(layout_passenger.getVisibility()==View.VISIBLE)
                    layout_passenger.setVisibility(View.GONE);
                if(layout_driver.getVisibility()==View.GONE)
                    layout_driver.setVisibility(View.VISIBLE);
            }
        }
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        if(user_data != null) {
            // Get token
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("TokenError", "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            user_data.setUser_token(token);
                            // Log and toast
                            //String msg = "message";
                            //Log.d("", msg);
                            //Toast.makeText(Register_Next_Step.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        //MyFirebaseMessagingService.getI
        passenger = new Passenger();
        firebaseHelper = new FirebaseHelper(Register_Next_Step.this);
        sp_intitution_name = findViewById(R.id.sp_intitution_name);
        sp_workplace_name = findViewById(R.id.sp_workplace_name);
        radio_group = findViewById(R.id.radio_group);
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId)
            {
                switch(checkedId){
                    case R.id.radio_student:
                        passenger.setIs_working_student(false);
                        if(sp_workplace_name.getVisibility() == View.VISIBLE)
                            sp_workplace_name.setVisibility(View.GONE);
                        sp_intitution_name.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radio_working:
                        passenger.setIs_working_student(true);
                        if(sp_intitution_name.getVisibility() == View.VISIBLE)
                            sp_intitution_name.setVisibility(View.GONE);
                        sp_workplace_name.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    public void onRegisterClick(View view)
    {
        if(user_data.getUser_type().equals("Passenger"))
        {
            if(passenger.getIs_working_student())
                passenger.setOrgnization_name(sp_workplace_name.getSelectedItem().toString());
            else if(!passenger.getIs_working_student())
                passenger.setOrgnization_name(sp_intitution_name.getSelectedItem().toString());
            passenger.setFk_user_id(user_data.getUser_id());
            passenger.setPriority_level(100);
            passenger.setInOnline(true);
            passenger.setLatitude(0);
            passenger.setLongitude(0);
            SaveUser(user_data.getEmail(),user_data.getPassword(),user_data.getName(),user_data.getUser_image_url());
            //firebaseHelper.pushUser(user_data,passenger);
        }
        else if(user_data.getUser_type().equals("Driver"))
        {
//            //driver = new Driver();
//            driver.setFk_user_id(user_data.getUser_id());
//            //driver.setDate();
//            //driver.setDriving_expiry_date();
//            //driver.setDriving_license_url();
//            //driver.setFk_vehicle_id();
//            //driver.setUser_nic_url();
//           // firebaseHelper.pushUser(user_data,driver);
//            uploadNIC(Constants.FilePathUri);
//            uploadLicence(Constants.FilePathUri2);
        }

    }
    private void SaveUser(String email , String password , String username,String profile_image)
    {
        progressDialog = new ProgressDialog(Register_Next_Step.this);
        progressDialog.setMessage("Wait..");
        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            if(progressDialog.isShowing())
                progressDialog.dismiss();
            return;
        }
//        if(progressBar!=null && progressBar.getVisibility() != View.VISIBLE)
//        progressBar.setVisibility(View.VISIBLE);

//create user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Register_Next_Step.this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Toast.makeText(Register_Next_Step.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
//                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                        if (!task.isSuccessful())
                        {
                            Toast.makeText(Register_Next_Step.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            FirebaseUser user = auth.getCurrentUser();
                            if(user!=null){
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username).setPhotoUri(Uri.parse(profile_image)).build();
                                user.updateProfile(profileUpdates);
                                user_data.setUser_id(user.getUid());
                                if(user_data.getUser_type().equals("Passenger"))
                                {
                                    passenger.setFk_user_id(user_data.getUser_id());
                                    firebaseHelper.pushUser(user_data,passenger);
                                    startActivity(new Intent(Register_Next_Step.this, MainActivity.class));
                                    finish();
                                }
                                else if(user_data.getUser_type().equals("Driver"))
                                {
                                    driver.setFk_user_id(user_data.getUser_id());
                                    firebaseHelper.pushUser(user_data,driver);
                                    startActivity(new Intent(Register_Next_Step.this, DriverMainActivity.class));
                                    finish();
                                }

                            }
                            //Successfull
                            finish();
                        }
                    }
                });
    }
    public void CalendarDriveIssueClick(View view)
    {
        //Intent intent = new Intent(Register_Next_Step.this,calendar_layout.class);
        //intent.putExtra("cal_view","issue");
        //startActivity(intent);
        date_bundle =new Bundle();
        DialogFragment newFragment = new DatePickerFragment();
        date_bundle.putString("fragment","issue" );
        newFragment.setArguments(date_bundle);
        newFragment.show(getSupportFragmentManager(), "datePicker");
        Button btn_issue = findViewById(R.id.btn_issue);
        btn_issue.setText("DONE!");
    }
    Bundle date_bundle;
    public void CalendarDriveExpiryClick(View view)
    {
        date_bundle =new Bundle();
        DialogFragment newFragment = new DatePickerFragment();
        date_bundle.putString("fragment","expiry" );
        newFragment.setArguments(date_bundle);
        newFragment.show(getSupportFragmentManager(), "datePicker");
        Button btn_expiry = findViewById(R.id.btn_expiry);
        btn_expiry.setText("DONE!");
        //Intent intent = new Intent(Register_Next_Step.this,calendar_layout.class);
        //intent.putExtra("cal_view","expiry");
        //startActivity(intent);
    }
    public void uploadNIC(Uri FilePathUrii)
    {
        progressDialog = new ProgressDialog(this);
        // Checking whether FilePathUri Is empty or not.
        if (FilePathUrii != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("NIC is Uploading...");

            // Showing progressDialog.
            progressDialog.show();
            String image_name = "img-"+ System.currentTimeMillis();
            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child("images").child(image_name + "." + GetFileExtension(FilePathUri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUrii)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Getting image name from EditText and store into string variable.
                            String TempImageName = "Test";//ImageName.getText().toString().trim();
                            //FilePathUri = taskSnapshot.getDownloadUrl();
                            String image_path  = taskSnapshot.getMetadata().getPath();
                            if(driver != null)
                                driver.setUser_nic_url(image_path);
                            // Hiding the progressDialog after done uploading.
                            progressDialog.dismiss();

                            // Showing toast message after done uploading.
                            Toast.makeText(Register_Next_Step.this, "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();
                            //saveUserToFirebaseDatabase();

                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // Hiding the progressDialog.
                            progressDialog.dismiss();

                            // Showing exception erro message.
                            Toast.makeText(Register_Next_Step.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
/*
                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            // Setting progressDialog Title.
                            progressDialog.setTitle("Image is Uploading...");

                        }
                    });*/
        }
        else {

            Toast.makeText(this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();

        }

    }
    public void uploadLicence(Uri FilePathUrii)
    {
        progressDialog = new ProgressDialog(this);
        // Checking whether FilePathUri Is empty or not.
        if (FilePathUrii != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("License is Uploading...");

            // Showing progressDialog.
            progressDialog.show();
            String image_name = "img-"+ System.currentTimeMillis();
            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child("images").child(image_name + "." + GetFileExtension(FilePathUri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUrii)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Getting image name from EditText and store into string variable.
                            String TempImageName = "Test";//ImageName.getText().toString().trim();
                            //FilePathUri = taskSnapshot.getDownloadUrl();
                            String image_path  = taskSnapshot.getMetadata().getPath();
                            if(driver != null)
                                driver.setDriving_license_url(image_path);
                            // Hiding the progressDialog after done uploading.
                            progressDialog.dismiss();

                            // Showing toast message after done uploading.
                            Toast.makeText(Register_Next_Step.this, "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();
                            //saveUserToFirebaseDatabase();

                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // Hiding the progressDialog.
                            progressDialog.dismiss();

                            // Showing exception erro message.
                            Toast.makeText(Register_Next_Step.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    /*
                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            // Setting progressDialog Title.
                            progressDialog.setTitle("Image is Uploading...");

                        }
                    });*/
        }
        else {

            Toast.makeText(this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();

        }

    }

    public void onRegisterDriverClick(View view)
    {
        if(user_data.getUser_type().equals("Driver"))
        {

            //driver.setFk_user_id(user_data.getUser_id());
            driver.setInOnline(true);
            //driver.setDriving_issue();
            driver.setLatitude(0);
            driver.setLongitude(0);
            //driver.setDriving_expiry_date();
            driver.setFk_vehicle_id("");
            driver.setDriving_expiry_date(String.valueOf(Constants.date_selected_expiry));
            driver.setDriving_issue(String.valueOf(Constants.date_selected_issue));
            // firebaseHelper.pushUser(user_data,driver);
            uploadNIC(Constants.FilePathUri);
            uploadLicence(Constants.FilePathUri2);
            SaveUser(user_data.getEmail(),user_data.getPassword(),user_data.getName(),user_data.getUser_image_url());
            //firebaseHelper.pushUser(user_data,driver);
        }
    }
    //image upload code
    String userChoosenTask = "";
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= PermissionHandler.checkPermission(Register_Next_Step.this);
                if (items[item].equals("Take Photo"))
                {
                    boolean camera_permit = PermissionHandler.checkCameraPermission(Register_Next_Step.this);
                    userChoosenTask="Take Photo";
                    if(result && camera_permit)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library"))
                {
                    userChoosenTask="Choose from Library";
                    if(result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent()
    {
        Intent intent = new Intent();
        intent.setAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        //if (intent.resolveActivity(getContext().getPackageManager()) != null) {
        startActivityForResult(intent, 1);
        //}
        //else
        // {
        //   Toast.makeText(getContext(),"problem",Toast.LENGTH_SHORT).show();
        // }
    }
    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 0:
                if(resultCode == RESULT_OK)
                {

                    if(isNIC)
                    {
                        FilePathUri = data.getData();
                        isNIC = false;
                        Button btn_nic = findViewById(R.id.btn_nic);
                        btn_nic.setText("DONE!");
                    }
                    else if(isLicense)
                    {
                        isLicense =  false;
                        Button btn_license = findViewById(R.id.btn_licence);
                        btn_license.setText("DONE!");
                        Constants.FilePathUri2 = data.getData();
                    }
                    onSelectFromGalleryResult(data);
                }
                break;
            case 1:
                if(resultCode == RESULT_OK){
                    if(isNIC)
                    {
                        isNIC = false;
                        Button btn_nic = findViewById(R.id.btn_nic);
                        btn_nic.setText("DONE!");
                        FilePathUri = data.getData();
                    }
                    else if(isLicense)
                    {
                        isLicense =  false;
                        Button btn_license = findViewById(R.id.btn_licence);
                        btn_license.setText("DONE!");
                        Constants.FilePathUri2 = data.getData();
                    }
                    onCaptureImageResult(data);
                }
                break;
            case 2888:
                if(resultCode == RESULT_OK)
                {
                    //license
                    //Bitmap photo = (Bitmap) data.getExtras().get("data");
                    //Constants.FilePathUri2 = getImageUri(this,photo);
                    FilePathUri = data.getData();
                    Button btn_license = findViewById(R.id.btn_licence);
                    btn_license.setText("DONE!");
                    //Constants.FilePathUri = data.getData();
                    onCaptureImageResult(data);
                }
                break;
            case 3888:
                if(resultCode == RESULT_OK)
                {
                    //nic
                    //Bitmap photo = (Bitmap) data.getExtras().get("data");
                    //FilePathUri = getImageUri(this,photo);
                    FilePathUri = data.getData();
                    Button btn_nic = findViewById(R.id.btn_nic);
                    btn_nic.setText("DONE!");
                    //Constants.FilePathUri = data.getData();
                    onCaptureImageResult(data);
                }
                break;
            //imageView.setImageBitmap(photo);
            case 45:
                if(resultCode==RESULT_OK)
                {
                    Toast.makeText(getApplicationContext(),"Hogya Sir",Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream is = new ByteArrayInputStream(bitmapdata);
                //InputStream is =
                Bitmap bmImg = BitmapFactory.decodeStream(is);
                Drawable background = new BitmapDrawable(bmImg);
                //profile_image.setBackground(background);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Imagebitmap=thumbnail;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream is = new ByteArrayInputStream(bitmapdata);
        //InputStream is =
        Bitmap bmImg = BitmapFactory.decodeStream(is);
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    StorageReference storageReference;
    ProgressDialog progressDialog;
    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = this.getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

    @Override
    protected void onResume() {
        super.onResume();
//        cal_view = getIntent().getStringExtra("cal_view");
//        cal_date = getIntent().getStringExtra("date");
//        if(!TextUtils.isEmpty(cal_date))
//        {
//            driver = new Driver();
//            if(cal_view.equals("expiry"))
//            {
//                driver.setDriving_expiry_date(cal_date);
//            }
//            else if(cal_view.equals("issue"))
//            {
//                driver.setDriving_issue(cal_date);
//            }
//        }
    }
    public static Boolean isLicense =  false ; //2888;
    public static Boolean isNIC = false ; //2888;
    public void onUploadLicenseClick(View view)
    {
        selectImage();
        isLicense = true;
    }

    public void onUploadNICClick(View view)
    {
        selectImage();
        isNIC = true;
    }
}
