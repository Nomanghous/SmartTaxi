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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.PermissionHandler;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.logixcess.smarttaxiapplication.Utils.Constants.FilePathUri;



public class RegisterActivity extends AppCompatActivity {

    Button btn_next;
    EditText et_address,et_name,et_password,et_phone,et_email;
    Spinner sp_user_types;
    CircularImageView profile_image;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        storageReference = FirebaseStorage.getInstance().getReference();
        et_address = findViewById(R.id.et_address);
        sp_user_types = findViewById(R.id.sp_user_types);
        et_email = findViewById(R.id.et_email);
        et_name = findViewById(R.id.et_name);
        et_phone = findViewById(R.id.et_phone);
        profile_image = findViewById(R.id.profile_image);
        et_password = findViewById(R.id.et_password);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        user = new User();
        btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(Constants.USER_TOKEN))
                    user.setUser_token(Constants.USER_TOKEN);
                if(!TextUtils.isEmpty(et_address.getText().toString()))
                    user.setAddress(et_address.getText().toString());
                else
                    {
                    et_address.setError("Address Field is Empty !");
                    return;
                }
                user.setJoin_date(String.valueOf(System.currentTimeMillis()));
                if(!TextUtils.isEmpty(et_name.getText().toString()))
                    user.setName(et_name.getText().toString());
                else{
                    et_name.setError("Name Field is Empty !");
                    return;
                }
                if(!TextUtils.isEmpty(et_email.getText().toString()))
                    user.setEmail(et_email.getText().toString());
                else
                {
                    et_email.setError("Email Field is Empty !");
                    return;
                }
                if(!TextUtils.isEmpty(et_password.getText().toString()))
                    user.setPassword(et_password.getText().toString());
                else
                    {
                    et_password.setError("Password Field is Empty !");
                        return;
                    }
                if(!TextUtils.isEmpty(et_phone.getText().toString()))
                    user.setPhone(et_phone.getText().toString());
                else {
                    et_phone.setError("Phone Field is Empty !");
                    return;
                }
                user.setUser_type(sp_user_types.getSelectedItem().toString());
                if(Constants.FilePathUri != null && !TextUtils.isEmpty(Constants.FilePathUri.toString()))
                    UploadImageFileToFirebaseStorage(Constants.FilePathUri);
                else
                {
                    Intent intent;
                    intent = new Intent(RegisterActivity.this,Register_Next_Step.class);
                    intent.putExtra("user_data",user);
                    startActivity(intent);
                }

//                Intent intent;
//                intent = new Intent(RegisterActivity.this,Register_Next_Step.class);
//                intent.putExtra("user_data",user);
//                startActivity(intent);
            }
        });
    }
    String userChoosenTask = "";
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= PermissionHandler.checkPermission(RegisterActivity.this);
                if (items[item].equals("Take Photo"))
                {
                    boolean camera_permit = PermissionHandler.checkCameraPermission(RegisterActivity.this);
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
    public static final int CAMERA_REQUEST = 1888;
    private void cameraIntent()
    {
        Intent intent = new Intent();
        intent.setAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        //if (intent.resolveActivity(getContext().getPackageManager()) != null) {
        startActivityForResult(intent, 1);//CAMERA_REQUEST);
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
                if(resultCode == RESULT_OK){
                    FilePathUri = data.getData();
                    onSelectFromGalleryResult(data);
                }
                break;
            case 1:
                if(resultCode == RESULT_OK){
                    FilePathUri = data.getData();
                    onCaptureImageResult(data);
                }
                break;
            case CAMERA_REQUEST:
                if(resultCode == RESULT_OK)
                {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    FilePathUri = getImageUri(this,photo);
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
                //profile_image.setBackgroundResource(null);
                profile_image.setBackground(background);
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
        Drawable background = new BitmapDrawable(bmImg);
        profile_image.setBackground(background);
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    StorageReference storageReference;
    ProgressDialog progressDialog;
    //DatabaseReference databaseReference;
    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    public void UploadImageFileToFirebaseStorage(Uri FilePathUrii) {
        progressDialog = new ProgressDialog(this);
        // Checking whether FilePathUri Is empty or not.
        if (FilePathUrii != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("Image is Uploading...");

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
                            if(user != null)
                                user.setUser_image_url(image_path);
                            // Hiding the progressDialog after done uploading.
                            progressDialog.dismiss();

                            // Showing toast message after done uploading.
                            Toast.makeText(RegisterActivity.this, "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();
                            //saveUserToFirebaseDatabase();

                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            //Uri image_pa = task.getResult().getStorage().getDownloadUrl().getResult();
                            Intent intent;
                            intent = new Intent(RegisterActivity.this,Register_Next_Step.class);
                            intent.putExtra("user_data",user);
                            startActivity(intent);
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // Hiding the progressDialog.
                            progressDialog.dismiss();

                            // Showing exception erro message.
                            Toast.makeText(RegisterActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            // Setting progressDialog Title.
                            progressDialog.setTitle("Image is Uploading...");

                        }
                    });
        }
        else {

            Toast.makeText(this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();

        }

    }
    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = this.getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

    public void onLoginClick(View view)
    {
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
    }
}
