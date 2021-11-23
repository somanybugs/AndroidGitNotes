package lhg.gitnotes.note.ui;

import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lhg.common.adapter.RecyclerClickAdapter;
import lhg.common.utils.DrawableUtils;
import lhg.gitnotes.R;
import lhg.gitnotes.app.AppConstant;
import lhg.gitnotes.note.FileEntity;
import lhg.gitnotes.note.FolderEntity;
import lhg.gitnotes.note.NoteEntity;

public class BaseNoteListAdapter extends RecyclerClickAdapter<FileEntity, RecyclerView.ViewHolder> {
    protected List<FileEntity> datas;

    protected static Map<String, String> suffixIcons = new HashMap();
    static {
        suffixIcons.put(AppConstant.FileSuffix.MD, "M");
        suffixIcons.put(AppConstant.FileSuffix.TODO, "V");
        suffixIcons.put(AppConstant.FileSuffix.TXT, "T");
        suffixIcons.put(AppConstant.FileSuffix.PWD, "P");
        suffixIcons.put(AppConstant.FileSuffix.BILL, "B");
    }


    public void setDatas(List<FileEntity> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (datas.get(position) instanceof NoteEntity) {
            return 1;
        } else {
            return 2;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new NoteVH(parent);
        } else {
            return new FolderVH(parent);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((BaseVH) holder).update(getItem(position));
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public FileEntity getItem(int postion) {
        return datas.get(postion);
    }

    public static class BaseVH<T> extends RecyclerView.ViewHolder {
        T data;
        StateListDrawable itemDrawableInSelecting;
        StateListDrawable itemDrawable;

        public BaseVH(@NonNull View itemView) {
            super(itemView);
            itemDrawable = DrawableUtils.listItemBackgroundPrimary(itemView.getContext());
            itemDrawableInSelecting = DrawableUtils.clone(itemDrawable, Arrays.asList(new int[]{android.R.attr.state_pressed}));
        }

        public void update(T data) {
            this.data = data;
        }

        public void setSelected(boolean inSelecting, boolean isSelected) {
            itemView.setBackground(inSelecting ? itemDrawableInSelecting : itemDrawable);
            itemView.setSelected(inSelecting && isSelected);
        }
    }


    public static class NoteVH extends BaseVH<NoteEntity> {
        TextView tvTitle;
        TextView tvIcon;
        public NoteVH(@NonNull ViewGroup parent) {
            this(parent, R.layout.item_note);
        }

        public NoteVH(@NonNull ViewGroup parent, int layoutId) {
            this(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
        }

        public NoteVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvIcon = itemView.findViewById(R.id.tvIcon);
        }

        @Override
        public void update(NoteEntity data) {
            super.update(data);
            tvTitle.setText(data.file.getName());
            String icon = suffixIcons.get(data.suffix);
            if (icon == null) {
                icon = "F";
            }
            tvIcon.setText(icon);
        }
    }

    public static class FolderVH extends BaseVH<FolderEntity> {
        TextView tvTitle;
        ImageView ivLock;
        public FolderVH(@NonNull ViewGroup parent) {
            this(parent, R.layout.item_folder);
        }

        public FolderVH(@NonNull ViewGroup parent, int layoutId) {
            this(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
        }

        public FolderVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivLock = itemView.findViewById(R.id.ivLock);
        }

        @Override
        public void update(FolderEntity data) {
            super.update(data);
            tvTitle.setText(data.file.getName());
            ivLock.setVisibility(data.isEncrypted ? View.VISIBLE : View.GONE);
        }
    }

}
