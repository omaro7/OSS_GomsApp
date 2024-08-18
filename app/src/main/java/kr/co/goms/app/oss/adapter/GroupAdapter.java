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
import kr.co.goms.module.common.model.GroupBeanS;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupHolder> implements Filterable {

    private final String TAG = GroupAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<GroupBeanS> mGroupList;
    private GroupClickListener mGroupClickListener;

    private ArrayList<GroupBeanS> mGroupListFull;
    public enum SEARCH_TYPE{
        TITLE,
    }
    public SEARCH_TYPE mSearchType = SEARCH_TYPE.TITLE;

    public interface GroupClickListener {
        void onGroupClick(int position, GroupBeanS groupBeanS );
        void onGroupLongClick(int position, GroupBeanS groupBeanS );
    }

    public class GroupHolder extends RecyclerView.ViewHolder {
        private LinearLayout lltGroup;
        private TextView tvGroupName;
        public GroupHolder(View itemView, Context context) {
            super(itemView);
            lltGroup = itemView.findViewById(R.id.llt_group);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            setIsRecyclable(false);
        }
    }

    public GroupAdapter(Context context, ArrayList<GroupBeanS> _GroupList, GroupClickListener groupClickListener) {
        this.mContext = context;
        this.mGroupList = _GroupList;
        this.mGroupClickListener = groupClickListener;
        this.mGroupListFull = new ArrayList<>(_GroupList);
    }

    @Override
    public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        GroupHolder holder = new GroupHolder(itemView, mContext);
        return holder;

    }

    @Override
    public void onBindViewHolder(final GroupHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.tvGroupName.setText(mGroupList.get(position).getRes_mh_group_name());
        holder.tvGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGroupClickListener.onGroupClick(position, mGroupList.get(position));
            }
        });

        holder.lltGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGroupClickListener.onGroupClick(position, mGroupList.get(position));
            }
        });

        holder.lltGroup.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                mGroupClickListener.onGroupLongClick(position, mGroupList.get(position));
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mGroupList == null) {
            return 0;
        }
        return mGroupList.size();
    }

    public void setData(ArrayList<GroupBeanS> data) {
        mGroupList = data;
    }

    public GroupBeanS getItem(final int position) {
        return mGroupList.get(position);
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
            List<GroupBeanS> filteredList = new ArrayList<GroupBeanS>();
            if (charSequence == null || charSequence.length() == 0) {
                //검색값이 없으면 전체를 다 보여준다. 음..
                filteredList.addAll(mGroupListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (GroupBeanS GroupBeanS : mGroupListFull) {

                    if(SEARCH_TYPE.TITLE.name().equalsIgnoreCase(mSearchType.name())) {
                        if (GroupBeanS.getRes_mh_group_name().toLowerCase().contains(filterPattern)) {
                            filteredList.add(GroupBeanS);
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
            mGroupList.clear();
            mGroupList.addAll((List) filterResults.values);
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