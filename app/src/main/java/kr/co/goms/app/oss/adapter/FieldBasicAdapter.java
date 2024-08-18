package kr.co.goms.app.oss.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.co.goms.app.oss.R;
import kr.co.goms.app.oss.model.FieldBasicBeanS;
import kr.co.goms.module.common.util.StringUtil;

public class FieldBasicAdapter extends RecyclerView.Adapter<FieldBasicAdapter.FieldBasicHolder> implements Filterable {

    private final String TAG = FieldBasicAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<FieldBasicBeanS> mFieldBasicList;
    private ArrayList<FieldBasicBeanS> mFieldBasicListFull;
    private FieldBasicClickListener mFieldBasicClickListener;

    public enum SEARCH_TYPE{
        TITLE,
    }
    public SEARCH_TYPE mSearchType = SEARCH_TYPE.TITLE;

    public interface FieldBasicClickListener {
        void onFieldBasicClick(int position, FieldBasicBeanS fieldBasicBeanS );
        void onFieldBasicLongClick(int position, FieldBasicBeanS fieldBasicBeanS );
    }

    public class FieldBasicHolder extends RecyclerView.ViewHolder {
        private LinearLayout lltFieldBasic;
        private TextView tvFieldTitle;
        private TextView tvFieldRegdate;
        private TextView tvFieldLocal;
        private TextView tvFieldCom;
        private TextView tvFieldTab;
        public FieldBasicHolder(View itemView, Context context) {
            super(itemView);
            lltFieldBasic = itemView.findViewById(R.id.llt_field_basic);
            tvFieldTitle = itemView.findViewById(R.id.tv_field_title);
            tvFieldRegdate = itemView.findViewById(R.id.tv_field_regdate);
            tvFieldLocal =  itemView.findViewById(R.id.tv_field_local);
            tvFieldCom =  itemView.findViewById(R.id.tv_field_com);
            tvFieldTab =  itemView.findViewById(R.id.tv_field_tab);

            setIsRecyclable(false);
        }
    }

    public FieldBasicAdapter(Context context, ArrayList<FieldBasicBeanS> _fieldBasicList, FieldBasicClickListener fieldBasicClickListener) {
        this.mContext = context;
        this.mFieldBasicList = _fieldBasicList;
        this.mFieldBasicClickListener = fieldBasicClickListener;
        this.mFieldBasicListFull = new ArrayList<>(_fieldBasicList);
    }

    @Override
    public FieldBasicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field_basic, parent, false);
        FieldBasicHolder holder = new FieldBasicHolder(itemView, mContext);
        return holder;

    }

    @Override
    public void onBindViewHolder(final FieldBasicHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.tvFieldTitle.setText(mFieldBasicList.get(position).getRes_mh_field_title());
        holder.tvFieldRegdate.setText(mFieldBasicList.get(position).getRes_mh_field_regdate());
        holder.tvFieldLocal.setText(mFieldBasicList.get(position).getRes_mh_field_local());
        holder.tvFieldCom.setText(mFieldBasicList.get(position).getRes_mh_com_name());
        holder.tvFieldTab.setText(StringUtil.isEmpty(mFieldBasicList.get(position).getRes_mh_field_tab())?"0":mFieldBasicList.get(position).getRes_mh_field_tab());

//        holder.tvFieldTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mFieldBasicClickListener.onFieldBasicClick(position, mFieldBasicList.get(position));
//            }
//        });
//
//        holder.tvFieldRegdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mFieldBasicClickListener.onFieldBasicClick(position, mFieldBasicList.get(position));
//            }
//        });

        holder.lltFieldBasic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFieldBasicClickListener.onFieldBasicClick(position, mFieldBasicList.get(position));
            }
        });
        holder.lltFieldBasic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mFieldBasicClickListener.onFieldBasicLongClick(position, mFieldBasicList.get(position));
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mFieldBasicList == null) {
            return 0;
        }
        return mFieldBasicList.size();
    }

    public void setData(ArrayList<FieldBasicBeanS> data) {
        mFieldBasicList = data;
    }

    public FieldBasicBeanS getItem(final int position) {
        return mFieldBasicList.get(position);
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
            List<FieldBasicBeanS> filteredList = new ArrayList<FieldBasicBeanS>();
            if (charSequence == null || charSequence.length() == 0) {
                //검색값이 없으면 전체를 다 보여준다. 음..
                filteredList.addAll(mFieldBasicListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (FieldBasicBeanS FieldBasicBeanS : mFieldBasicListFull) {

                    if(SEARCH_TYPE.TITLE.name().equalsIgnoreCase(mSearchType.name())) {
                        if (FieldBasicBeanS.getRes_mh_field_title().toLowerCase().contains(filterPattern)) {
                            filteredList.add(FieldBasicBeanS);
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
            mFieldBasicList.clear();
            mFieldBasicList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public SEARCH_TYPE getSearchType() {
        return mSearchType;
    }

    public void setSearchType(SEARCH_TYPE mSearchType) {
        this.mSearchType = mSearchType;
    }
}