package com.logixcess.smarttaxiapplication.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.logixcess.smarttaxiapplication.Models.Feedback;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Utils.Helper;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedbackFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedbackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedbackFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    HashMap<String,Float> feedback11 = new HashMap<>();
    HashMap<String,Float> feedback22 = new HashMap<>();
    HashMap<String,Float> feedback33 = new HashMap<>();
    public FeedbackFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedbackFragment.
     */

    public static FeedbackFragment newInstance(String param1, String param2) {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    String driver_id,driver_name,order_id,pending_dest,pending_pickup;
    RatingBar rb_review1,rb_review2,rb_review3;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            driver_id = getArguments().getString("driver_id");
            driver_name = getArguments().getString("driver_name");
            order_id = getArguments().getString("order_id");
            pending_dest = getArguments().getString("pending_dest");
            pending_pickup = getArguments().getString("pending_pickup");
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    Button btn_feedback;
    TextView tv_Destination,tv_Pickup,tv_driver_name;
    EditText et_complaint;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        //TODO check if there is any order of Passenger

        btn_feedback = view.findViewById(R.id.btn_feedback);
        rb_review1 = view.findViewById(R.id.rb_review1);
        rb_review2 = view.findViewById(R.id.rb_review2);
        rb_review3 = view.findViewById(R.id.rb_review3);
        tv_Destination = view.findViewById(R.id.tv_Destination);
        et_complaint = view.findViewById(R.id.et_complaint);
        tv_Pickup = view.findViewById(R.id.tv_Pickup);
        tv_driver_name = view.findViewById(R.id.tv_driver_name);
        if(pending_dest!=null && (!TextUtils.isEmpty(pending_dest)))
        {
            tv_Destination.setText(pending_dest);
        }
        if(pending_pickup!=null && (!TextUtils.isEmpty(pending_pickup)))
        {
            tv_Pickup.setText(pending_pickup);
        }
        if(driver_name != null && !TextUtils.isEmpty(driver_name))
        {
            tv_driver_name.setText(driver_name);
        }
        btn_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(tv_Pickup.getText().toString().equalsIgnoreCase("empty") || tv_Destination.getText().toString().equalsIgnoreCase("empty"))
                {
                    Toast.makeText(getActivity(),"No Driver Found",Toast.LENGTH_SHORT).show();
                    return;
                }
                Feedback feedback = new Feedback();
                feedback.setFk_driver_id(driver_id);
                feedback.setFk_order_id(order_id);
                if(!TextUtils.isEmpty(et_complaint.getText()))
                    feedback.setComplaint(et_complaint.getText().toString());
                feedback11.put(getString(R.string.review1),rb_review1.getRating());
                feedback22.put(getString(R.string.review2),rb_review2.getRating());
                feedback33.put(getString(R.string.review3),rb_review3.getRating());
                feedback.setFeedback1(feedback11);
                feedback.setFeedback2(feedback22);
                feedback.setFeedback3(feedback33);
                DatabaseReference db_ref_feedback = FirebaseDatabase.getInstance().getReference().child(Helper.REF_FEEBACK).child(order_id);
                DatabaseReference db_ref_order = FirebaseDatabase.getInstance().getReference().child(Helper.REF_ORDERS).child(order_id);
                db_ref_feedback.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {

                        }
                        else
                        {
                            db_ref_feedback.setValue(feedback).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(getContext(), "Thank you for your Feeback !", Toast.LENGTH_SHORT).show();
                                    db_ref_order.child("status").setValue(Order.OrderStatusCompletedReview).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                                Toast.makeText(getContext(), "Order Mark As Complete !", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Toast.makeText(getActivity(),"Published",Toast.LENGTH_SHORT).show();
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
}
