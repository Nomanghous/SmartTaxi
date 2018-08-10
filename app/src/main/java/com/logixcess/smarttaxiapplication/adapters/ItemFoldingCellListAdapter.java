package com.logixcess.smarttaxiapplication.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//import com.bumptech.glide.Glide;

public class ItemFoldingCellListAdapter extends RecyclerView.Adapter<ItemFoldingCellListAdapter.ViewHolder>
{
    private HashSet<Integer> unfoldedIndexes = new HashSet<>();
    private View.OnClickListener defaultRequestBtnClickListener;
    public HashMap<String,ImageView> user_profile_record2 = new HashMap<>();
    public  HashMap<String,ArrayList<TextView>> item_owner_name = new HashMap<>();
    Context my_context;
    List<Order> driverDataList;
    public ItemFoldingCellListAdapter(Context context, List<Order> objects)
    {
        my_context = context;
        driverDataList = objects;
        //super(context, 0, objects);
    }

    public void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    // simple methods for register cell state changes
    public void registerToggle(int position) {
        if (unfoldedIndexes.contains(position))
            registerFold(position);
        else
            registerUnfold(position);
    }

    public void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }

    public View.OnClickListener getDefaultRequestBtnClickListener() {
        return defaultRequestBtnClickListener;
    }

    public void setDefaultRequestBtnClickListener(View.OnClickListener defaultRequestBtnClickListener) {
        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cell, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

//        final User driverData = driverDataList.get(position);
        final Order order = driverDataList.get(position);
        //final Item item = driverData.getItem();
        //viewHolder.tv_car_info.setText("LZ 6140");
        viewHolder.tv_car_info.setText(order.getVehicle_id());
        //viewHolder.tv_cost.setText("$320");
        viewHolder.tv_cost.setText(order.getEstimated_cost());
        viewHolder.tv_destination.setText(order.getDropoff());
        viewHolder.tv_destination2.setText(order.getDropoff());
        viewHolder.tv_cost2.setText(order.getEstimated_cost());
        viewHolder.tv_rider_name.setText(order.getDriver_name());
        viewHolder.tv_rider_name2.setText(order.getDriver_name());
        Typeface typeFace1=Typeface.createFromAsset(my_context.getAssets(),"MavenPro-Bold.ttf");
        Typeface typeFace=Typeface.createFromAsset(my_context.getAssets(),"MavenPro-Bold.ttf");
        Typeface typeFace2=Typeface.createFromAsset(my_context.getAssets(),"MavenPro-Regular.ttf");
        viewHolder.tv_rider_phone.setTypeface(typeFace);
        viewHolder.tv_rider_name2.setTypeface(typeFace2);
        viewHolder.tv_rider_name.setTypeface(typeFace1);
        viewHolder.tv_rider_phone.setText("03124002951");
//        viewHolder.tv_destination.setText("Lahore");
//        viewHolder.tv_destination2.setText("Lahore");
//        viewHolder.tv_cost2.setText("$320");
//        viewHolder.tv_rider_name.setText("Ahmad Irfan");
//        viewHolder.tv_rider_name2.setText("Ahmad Irfan");
//        Typeface typeFace1=Typeface.createFromAsset(my_context.getAssets(),"MavenPro-Bold.ttf");
//        Typeface typeFace=Typeface.createFromAsset(my_context.getAssets(),"MavenPro-Bold.ttf");
//        Typeface typeFace2=Typeface.createFromAsset(my_context.getAssets(),"MavenPro-Regular.ttf");
//        viewHolder.tv_rider_phone.setTypeface(typeFace);
//        viewHolder.tv_rider_name2.setTypeface(typeFace2);
//        viewHolder.tv_rider_name.setTypeface(typeFace1);
//        viewHolder.tv_rider_phone.setText("03124002951");

        viewHolder.iv_rider_pic.setImageDrawable(my_context.getResources().getDrawable(R.drawable.user_placeholder));
        viewHolder.iv_rider_pic2.setImageDrawable(my_context.getResources().getDrawable(R.drawable.user_placeholder));
    //    viewHolder.btn_item_request.setTag(position);
//        viewHolder.foldingCell.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Toast.makeText(my_context,"Foldin",Toast.LENGTH_SHORT).show();
//                // do whatever
//                ((FoldingCell) view).toggle(false);
//                // register in adapter that state for selected cell is toggled
//                registerToggle(position);
//            }
//        });
        ViewGroup.LayoutParams lp = viewHolder.frameLayout2.getLayoutParams();
        int height = 2 * lp.height ;
        int width = lp.width ;
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width,height);
        ViewGroup.LayoutParams lpp = viewHolder.frameLayout.getLayoutParams();
        // lpp.height = 456;
        //viewHolder.frameLayout.requestLayout();
        // /viewHolder.frameLayout.setLayoutParams(layoutParams);
        viewHolder.foldingCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(my_context,"Foldin",Toast.LENGTH_SHORT).show();
                // do whatever
                ((FoldingCell) view).toggle(false);
                // register in adapter that state for selected cell is toggled
                registerToggle(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return driverDataList.size();
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
       /* TextView price;
        TextView tv_origin;
        TextView tv_destination;
        TextView tv_item_name;

        TextView label_arrival;

        TextView tv_arrival_range;
        TextView label_weight;
        TextView tv_weight;

*/

        //change
        TextView tv_rider_name,tv_cost,
                tv_destination,tv_destination2,tv_rider_name2,tv_rider_phone,
                tv_car_info,tv_cost2;
        ImageView iv_rider_pic,iv_rider_pic2;
        //Button btn_item_request;
        FoldingCell foldingCell;
        RelativeLayout frameLayout;
        RelativeLayout frameLayout2;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_rider_pic = itemView.findViewById(R.id.iv_rider_pic);
            frameLayout = itemView.findViewById(R.id.fl_test);
            frameLayout2 = itemView.findViewById(R.id.fm_item_title);

            foldingCell = itemView.findViewById(R.id.folding_cell);
            iv_rider_pic2 = itemView.findViewById(R.id.iv_rider_pic2);
            tv_cost = itemView.findViewById(R.id.tv_cost);
            tv_rider_name = itemView.findViewById(R.id.tv_rider_name);
            tv_destination = itemView.findViewById(R.id.tv_destination);
            //second
            tv_destination2 = itemView.findViewById(R.id.tv_destination2);
            tv_rider_name2 = itemView.findViewById(R.id.tv_rider_name2);
            tv_rider_phone = itemView.findViewById(R.id.tv_rider_phone);
            tv_car_info = itemView.findViewById(R.id.tv_car_info);
            tv_cost2 = itemView.findViewById(R.id.tv_cost2);

            //btn_item_request.requestFocus();
            //btn_item_request.setFocusableInTouchMode(true);

        }
        /*      ImageView item_icon,item_owner_icon;
        TextView title_time_label;      ImageView item_icon,item_owner_icon;
        TextView title_date_label;      ImageView item_icon,item_owner_icon;

        TextView fromAddress;
        TextView toAddress;

        TextView tv_carryon;
        TextView tv_checked_bag;
        TextView label_space_available;

        TextView label_space_price;
        TextView arrival_date;
        TextView space_price;*/
        //        TextView requestsCount;
        ////TextView date;
        ////TextView time;
    }
}

