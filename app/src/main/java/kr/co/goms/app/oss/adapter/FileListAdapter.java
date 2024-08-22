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
import kr.co.goms.app.oss.model.FileBeanS;
import kr.co.goms.module.common.util.StringUtil;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileHolder> implements Filterable {

    private final String TAG = FileListAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<FileBeanS> mFileList;
    private FileClickListener mFileClickListener;

    private ArrayList<FileBeanS> mFileListFull;
    public enum SEARCH_TYPE{
        TITLE,
    }
    public SEARCH_TYPE mSearchType = SEARCH_TYPE.TITLE;

    public interface FileClickListener {
        void onFileClick(int position, FileBeanS groupBeanS );
    }

    public class FileHolder extends RecyclerView.ViewHolder {
        private LinearLayout lltFile;
        private TextView tvFileName, tvFileSize;
        public FileHolder(View itemView, Context context) {
            super(itemView);
            lltFile = itemView.findViewById(R.id.llt_file);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFileSize = itemView.findViewById(R.id.tv_file_size);
            setIsRecyclable(false);
        }
    }

    public FileListAdapter(Context context, ArrayList<FileBeanS> _FileList, FileClickListener groupClickListener) {
        this.mContext = context;
        this.mFileList = _FileList;
        this.mFileClickListener = groupClickListener;
        this.mFileListFull = new ArrayList<>(_FileList);
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_list, parent, false);
        FileHolder holder = new FileHolder(itemView, mContext);
        return holder;

    }

    @Override
    public void onBindViewHolder(final FileHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.tvFileName.setText(mFileList.get(position).getFile_name());
        holder.tvFileSize.setText(mFileList.get(position).getFile_size() + " bytes");


        holder.tvFileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFileClickListener.onFileClick(position, mFileList.get(position));
            }
        });

        holder.lltFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFileClickListener.onFileClick(position, mFileList.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mFileList == null) {
            return 0;
        }
        return mFileList.size();
    }

    public void setData(ArrayList<FileBeanS> data) {
        mFileList = data;
    }

    public FileBeanS getItem(final int position) {
        return mFileList.get(position);
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
            List<FileBeanS> filteredList = new ArrayList<FileBeanS>();
            if (charSequence == null || charSequence.length() == 0) {
                //검색값이 없으면 전체를 다 보여준다. 음..
                filteredList.addAll(mFileListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (FileBeanS FileBeanS : mFileListFull) {

                    if(SEARCH_TYPE.TITLE.name().equalsIgnoreCase(mSearchType.name())) {
                        if (FileBeanS.getFile_name().toLowerCase().contains(filterPattern)) {
                            filteredList.add(FileBeanS);
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
            mFileList.clear();
            mFileList.addAll((List) filterResults.values);
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