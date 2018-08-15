package com.logixcess.smarttaxiapplication.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.SmartTaxiApp;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.FirebaseHelper;
import com.logixcess.smarttaxiapplication.Utils.PermissionHandler;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.IOException;

import static android.content.ContentValues.TAG;
import static com.logixcess.smarttaxiapplication.Utils.Constants.FilePathUri;
import static com.logixcess.smarttaxiapplication.Utils.Constants.Storage_Path;
import static com.logixcess.smarttaxiapplication.Utils.Constants.user_image_path;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfile.
     */

    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    Firebase firebase_instance;
    CircularImageView profile_image;
    FirebaseAuth auth;
    EditText ed_first_name,ed_email,ed_phone,ed_password,et_address;
    Button btn_update;
    String user_id;
    Spinner sp_user_types;
    ProgressBar pb_image;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_user_profile, container, false);
        firebase_instance = SmartTaxiApp.getInstance().getFirebaseInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        profile_image = (CircularImageView) view.findViewById(R.id.profile_image);
        ed_first_name = view.findViewById(R.id.ed_first_name);
        sp_user_types = view.findViewById(R.id.sp_user_types);
        pb_image = view.findViewById(R.id.pb_image);
        ed_email = view.findViewById(R.id.ed_email);
        ed_phone = view.findViewById(R.id.ed_phone);
        ed_password = view.findViewById(R.id.ed_password);
        et_address = view.findViewById(R.id.et_address);
        btn_update = view.findViewById(R.id.update);
        //String email = auth.getCurrentUser().getEmail();
        user_id = auth.getCurrentUser().getUid();
        getUserData(user_id);
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                progressBar.setVisibility(View.VISIBLE);
                SaveUser();
                //((MainActivity) getActivity()).openDrawer();
            }
        });
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        return view;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }

    public void SaveUser()
    {


        if (TextUtils.isEmpty(ed_first_name.getText().toString().trim())) {
            ed_first_name.setError("Enter First Name!");
            Toast.makeText(getContext(), "Enter First Name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(ed_email.getText().toString().trim())) {
            ed_email.setError("Enter Email!");
            Toast.makeText(getContext(), "Enter Email!", Toast.LENGTH_SHORT).show();
            return;
        }
//
//        if (password.length() < 6) {
//            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if(TextUtils.isEmpty(ed_password.getText().toString().trim()))
        {
            ed_password.setError("Enter Password!");
            Toast.makeText(getContext(), "Enter Password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(ed_phone.getText().toString().trim())) {
            ed_phone.setError("Enter Phone!");
            Toast.makeText(getContext(), "Enter Phone!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(et_address.getText().toString().trim())) {
            et_address.setError("Enter Address!");
            Toast.makeText(getContext(), "Enter Address!", Toast.LENGTH_SHORT).show();
            return;
        }

//        if(progressBar!=null && progressBar.getVisibility() != View.VISIBLE)
//            progressBar.setVisibility(View.VISIBLE);

//create user
        FirebaseUser user = auth.getCurrentUser();
        if(user != null)
        {
            if(!user.getEmail().equals(ed_email.getText().toString()))
            {
                user.updateEmail(ed_email.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Email address is updated.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to update email!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        }

        // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(FilePathUri!=null)
        {
            UploadImageFileToFirebaseStorage(FilePathUri);
        }
        else
        {
            saveUserToFirebaseDatabase();
        }
        //saveUserToFirebaseDatabase();


    }

    private void saveUserToFirebaseDatabase()
    {
        User user = new User();
        user.setEmail(ed_email.getText().toString());
        user.setName(ed_first_name.getText().toString());
        user.setPassword(ed_password.getText().toString());
        user.setPhone(ed_phone.getText().toString());
        user.setAddress(et_address.getText().toString());
        //user.setState(password_edittext.getText().toString());
        if(FilePathUri!=null)
        {
            user.setUser_image_url(FilePathUri.toString());
        }
        else
        {
            user.setUser_image_url(profile_image_link);
        }
        user.setUser_id(user_id);
        user.setUser_type(sp_user_types.getSelectedItem().toString());
        FirebaseHelper firebaseHelper = new FirebaseHelper(getActivity());
        firebaseHelper.updateUser(user);
//        if(progressBar!=null && progressBar.getVisibility() == View.VISIBLE)
//            progressBar.setVisibility(View.GONE);
    }
    String profile_image_link="";
    String old_user_id="";
    String userChoosenTask = "";
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= PermissionHandler.checkPermission(getContext());
                if (items[item].equals("Take Photo"))
                {
                    boolean camera_permit = PermissionHandler.checkCameraPermission(getContext());
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
    public static final int GALLERY_REQUEST = 1889;
    private void cameraIntent()
    {
        Intent intent = new Intent();
        intent.setAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        //if (intent.resolveActivity(getContext().getPackageManager()) != null) {
        getActivity().startActivityForResult(intent, CAMERA_REQUEST);
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
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select File"),GALLERY_REQUEST);
    }
    // TODO: Rename method, update argument and hook method into UI event
    ValueEventListener valueEventListener;
    User old_user = null;
    //String old_user_id="";
    public void getUserData(String uid)
    {
        Log.d(TAG, "`````` In Push User...");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())//check if user exist
                {

                    old_user = dataSnapshot.getValue(User.class);
                    profile_image_link = old_user.getUser_image_url();
                    old_user_id = old_user.getUser_id();
                    ed_email.setText(old_user.getEmail());
                    ed_first_name.setText(old_user.getName());
                    ed_password.setText(old_user.getPassword());
                    ed_phone.setText(old_user.getPhone());
                    et_address.setText(old_user.getAddress());
                    if(old_user.getUser_type().equals("Driver"))
                    {
                        sp_user_types.setSelection(0);
                    }
                    else if(old_user.getUser_type().equals("Passenger"))
                    {
                        sp_user_types.setSelection(1);
                    }
                    if (!old_user.getUser_image_url().equals("")) {

                        if(pb_image.getVisibility()==View.GONE)
                            pb_image.setVisibility(View.VISIBLE);
                        Constants.user_image_path = old_user.getUser_image_url();
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        // Create a storage reference from our app
                        StorageReference storageRefer = storage.getReference();
                        // Create a reference with an initial file path and name
                        //StorageReference storageReff = storageRefer.child(Storage_Path + old_user.getUser_id() + ".jpg");
                        StorageReference storageReff = storageRefer.child(user_image_path);
//                            Glide.with(getContext())
//                                    .using(new FirebaseImageLoader())
//                                    .load(storageRef)
//                                    .into(iv_cam);
//                            iv_cam.setVisibility(View.VISIBLE);
//                            Glide.with(getContext())
//                                    .using(new FirebaseImageLoader())
//                                    .load(storageReff)
//                                    .into(iv_cam);
//                            iv_cam.setVisibility(View.VISIBLE);
                        String path = storageReff.toString();
                        StorageReference storageRef = storage.getReferenceFromUrl(path);//old_user.getProfile_picture());//.child("android.jpg");

                        try {
                            final File localFile = File.createTempFile("images", "jpg");
                            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    //.setImageBitmap(bitmap);
                                    if(pb_image.getVisibility()== View.VISIBLE)
                                        pb_image.setVisibility(View.GONE);
                                    BitmapDrawable background = new BitmapDrawable(bitmap);
                                    profile_image.setBackground(background);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception)
                                {
                                    if(pb_image.getVisibility()== View.VISIBLE)
                                        pb_image.setVisibility(View.GONE);
                                }
                            });
                        } catch (IOException e) {
                        }

                        //Picasso.with(getContext()).load(old_user.getProfile_picture()).into(iv_cam);
                    }
                    //old_user_id = old_user.getUser_id();
                    //old_user_name = old_user.getName();
                    //activeUsers.add(snapshot.getValue(User.class));
                }
//                    if(user.getPassword()=="")
//                    {
//                        user.setPassword(old_user.getPassword());
//                    }
//                    firebase_instance.child("User").child(user.getUser_id()).setValue(user);
                //  Toast.makeText(getContext(),"Profile Updated Successfully",Toast.LENGTH_SHORT).show();

                else {

                    //  firebase_instance.child("User").child(user.getUser_id()).setValue(user);
                    //   Toast.makeText(getContext(),"You are Registered Successfully",Toast.LENGTH_SHORT).show();
                }


            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
        firebase_instance.child("User")
                .child(uid).addListenerForSingleValueEvent(valueEventListener);//call onDataChange   executes OnDataChange method immediately and after executing that method once it stops listening to the reference location it is attached to.

    }

    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContext().getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

    ProgressDialog progressDialog;
    //Uri FilePathUri;
    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    //DatabaseReference databaseReference;
    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    public void UploadImageFileToFirebaseStorage(Uri FilePathUrii) {

        progressDialog = new ProgressDialog(getActivity());
        // Checking whether FilePathUri Is empty or not.
        if (FilePathUrii != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("Image is Uploading...");

            // Showing progressDialog.
            progressDialog.show();

            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child(Storage_Path + old_user_id + "." + GetFileExtension(FilePathUri));
            //StorageReference storageReference2nd = storageReference.child(Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUrii)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Getting image name from EditText and store into string variable.
                            String TempImageName = "Test";//ImageName.getText().toString().trim();
                            FilePathUri = taskSnapshot.getUploadSessionUri();//.getMetadata().getPath();
                            // Hiding the progressDialog after done uploading.
                            progressDialog.dismiss();

                            // Showing toast message after done uploading.
                            Toast.makeText(getContext(), "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();
                            saveUserToFirebaseDatabase();
                            //// @SuppressWarnings("VisibleForTests")
                            /////      ImageUploadInfo imageUploadInfo = new ImageUploadInfo(TempImageName, taskSnapshot.getDownloadUrl().toString());

                            // Getting image upload ID.
                            ////  String ImageUploadId = databaseReference.push().getKey();

                            // Adding image upload id s child element into databaseReference.
                            //// databaseReference.child(ImageUploadId).setValue(imageUploadInfo);
                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // Hiding the progressDialog.
                            progressDialog.dismiss();

                            // Showing exception erro message.
                            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
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

            Toast.makeText(getContext(), "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();

        }
    }
}
