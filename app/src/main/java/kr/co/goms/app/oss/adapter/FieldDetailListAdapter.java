package kr.co.goms.app.oss.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;

import java.util.ArrayList;
import java.util.List;

import kr.co.goms.app.oss.R;
import kr.co.goms.app.oss.model.FieldDetailBeanS;
import kr.co.goms.module.common.util.GomsUtils;
import kr.co.goms.module.common.util.StringUtil;

public class FieldDetailListAdapter extends RecyclerView.Adapter<FieldDetailListAdapter.FieldDetailHolder> implements Filterable {

    private final String TAG = FieldDetailListAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<FieldDetailBeanS> mFieldDetailList;
    private ArrayList<FieldDetailBeanS> mFieldDetailListFull;
    private FieldDetailClickListener mFieldDetailClickListener;

    public enum SEARCH_TYPE{
        TITLE,
    }
    public SEARCH_TYPE mSearchType = SEARCH_TYPE.TITLE;

    public interface FieldDetailClickListener {
        void onFieldDetailClick(int position, FieldDetailBeanS fieldDetailBeanS );
        void onFieldDetailLongClick(int position, FieldDetailBeanS fieldDetailBeanS);
    }

    public class FieldDetailHolder extends RecyclerView.ViewHolder {

        private CardView cvFieldDetail;
        private LinearLayout lltPhotoDetail;
        private TextView tvMhNum;
        private TextView tvMhDate;
        private TextView tvMhCoorinate, tvMhCoorinateGeo;
        private ImageView ivPhotoAround, ivPhotoOuter, ivPhotoInner, ivPhotoEtc, ivCad;
        private LottieAnimationView mNoData;
        public FieldDetailHolder(View itemView, Context context) {
            super(itemView);
            cvFieldDetail = itemView.findViewById(R.id.cv_field_detail);
            lltPhotoDetail = itemView.findViewById(R.id.llt_photo_detail);

            tvMhNum = itemView.findViewById(R.id.tv_mh_num);
            tvMhDate = itemView.findViewById(R.id.tv_mh_date);
            tvMhCoorinate =  itemView.findViewById(R.id.tv_mh_coordinate);
            tvMhCoorinateGeo =  itemView.findViewById(R.id.tv_mh_coordinate_geo);
            ivPhotoAround =  itemView.findViewById(R.id.iv_mh_photo_around);
            ivPhotoOuter =  itemView.findViewById(R.id.iv_mh_photo_outer);
            ivPhotoInner =  itemView.findViewById(R.id.iv_mh_photo_inner);
            ivPhotoEtc =  itemView.findViewById(R.id.iv_mh_photo_etc);
            ivCad =  itemView.findViewById(R.id.iv_cad);

            mNoData = itemView.findViewById(R.id.lav_no_photo);

            setIsRecyclable(false);
        }
    }

    public FieldDetailListAdapter(Context context, ArrayList<FieldDetailBeanS> _fieldDetailList, FieldDetailClickListener fieldDetailClickListener) {
        this.mContext = context;
        this.mFieldDetailList = _fieldDetailList;
        this.mFieldDetailClickListener = fieldDetailClickListener;
        this.mFieldDetailListFull = new ArrayList<>(_fieldDetailList);
    }

    @Override
    public FieldDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field_detail, parent, false);
        FieldDetailHolder holder = new FieldDetailHolder(itemView, mContext);
        return holder;

    }

    @Override
    public void onBindViewHolder(final FieldDetailHolder holder, @SuppressLint("RecyclerView") final int position) {

        FieldDetailBeanS fieldDetailBeanS = mFieldDetailList.get(position);
        holder.tvMhNum.setText(fieldDetailBeanS.getRes_mh_num());
        holder.tvMhDate.setText(fieldDetailBeanS.getRes_mh_date());
        holder.tvMhCoorinate.setText(fieldDetailBeanS.getRes_mh_coordinate());

        String coordinate = fieldDetailBeanS.getRes_mh_coordinate();
        String lat = "";
        String lng = "";
        if (!StringUtil.isEmpty(coordinate)) {
            String[] latlng = coordinate.split(",");
            lat = latlng[0];
            lng = latlng[1];
            holder.tvMhCoorinateGeo.setText(GomsUtils.getAddressFromGeo(mContext,StringUtil.stringToDouble(lat),StringUtil.stringToDouble(lng)));
        }

        int total = 0;
        //20240404 이미지 캐시 처리 .diskCacheStrategy(DiskCacheStrategy.ALL)
        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_photo_around())) {
            Glide.with(mContext).load(fieldDetailBeanS.getRes_mh_photo_around())
                    .placeholder(R.drawable.ic_launcher)
                    .override(80,60)
                    .encodeQuality(70)
                    .format(DecodeFormat.PREFER_RGB_565)
                    .dontAnimate()
                    .into(holder.ivPhotoAround);
            total++;
        }
        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_photo_outer())) {
            Glide.with(mContext).load(fieldDetailBeanS.getRes_mh_photo_outer())
                    .placeholder(R.drawable.ic_launcher)
                    .override(80,60)
                    .encodeQuality(70)
                    .format(DecodeFormat.PREFER_RGB_565)
                    .dontAnimate()
                    .into(holder.ivPhotoOuter);
            total++;
        }
        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_photo_inner())) {
            Glide.with(mContext).load(fieldDetailBeanS.getRes_mh_photo_inner())
                    .placeholder(R.drawable.ic_launcher)
                    .override(80,60)
                    .encodeQuality(70)
                    .format(DecodeFormat.PREFER_RGB_565)
                    .dontAnimate()
                    .into(holder.ivPhotoInner);
            total++;
        }
        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_photo_etc())) {
            Glide.with(mContext).load(fieldDetailBeanS.getRes_mh_photo_etc())
                    .placeholder(R.drawable.ic_launcher)
                    .override(80,60)
                    .encodeQuality(70)
                    .format(DecodeFormat.PREFER_RGB_565)
                    .dontAnimate()
                    .into(holder.ivPhotoEtc);
            total++;
        }
        if(!StringUtil.isEmpty(fieldDetailBeanS.getRes_mh_cad())) {
            Glide.with(mContext).load(fieldDetailBeanS.getRes_mh_cad())
                    .placeholder(R.drawable.ic_launcher)
                    .override(80,60)
                    .encodeQuality(70)
                    .format(DecodeFormat.PREFER_RGB_565)
                    .dontAnimate()
                    .into(holder.ivCad);
            total++;
        }
        if(total > 0){
            holder.mNoData.setVisibility(View.GONE);
            holder.lltPhotoDetail.setVisibility(View.VISIBLE);
        }else{
            holder.mNoData.setVisibility(View.VISIBLE);
            holder.lltPhotoDetail.setVisibility(View.GONE);
        }

        holder.cvFieldDetail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mFieldDetailClickListener.onFieldDetailClick(position, mFieldDetailList.get(position));
            }
        });
        holder.cvFieldDetail.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mFieldDetailClickListener.onFieldDetailLongClick(position, mFieldDetailList.get(position));
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mFieldDetailList == null) {
            return 0;
        }
        return mFieldDetailList.size();
    }

    public void setData(ArrayList<FieldDetailBeanS> data) {
        mFieldDetailList = data;
    }

    public FieldDetailBeanS getItem(final int position) {
        return mFieldDetailList.get(position);
    }

    /*-------------------------------------------------------
    //검색 처리 부분입니다.
     -------------------------------------------------------*/
    @Override
    public Filter getFilter() {
        return myFilter;
    }

    public Filter myFilter = new Filter() {

        //Automatic on background thread
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<FieldDetailBeanS> filteredList = new ArrayList<FieldDetailBeanS>();
            if (charSequence == null || charSequence.length() == 0) {
                //검색값이 없으면 전체를 다 보여준다. 음..
                filteredList.addAll(mFieldDetailListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (FieldDetailBeanS fieldDetailBeanS : mFieldDetailListFull) {

                    if(SEARCH_TYPE.TITLE.name().equalsIgnoreCase(mSearchType.name())) {
                        if (fieldDetailBeanS.getRes_mh_num().toLowerCase().contains(filterPattern)) {
                            filteredList.add(fieldDetailBeanS);
                        }
                    }else{

                    }

                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        //Automatic on UI thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mFieldDetailList.clear();
            mFieldDetailList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public SEARCH_TYPE getSearchType() {
        return mSearchType;
    }

    public void setSearchType(SEARCH_TYPE mSearchType) {
        this.mSearchType = mSearchType;
    }

    public int calculateSumForDataItem1() {
        int sum = 0;
        for (FieldDetailBeanS fieldDetailBeanS : mFieldDetailListFull) {
            if("Y".equals(fieldDetailBeanS.getRes_mh_ladder_yn())){
                sum++;
            }
        }
        return sum;
    }
}