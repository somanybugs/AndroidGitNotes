package lhg.gitnotes.note.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lhg.gitnotes.note.FileEntity;

public class NoteListAdapter extends BaseNoteListAdapter {
    protected boolean isSelecting = false;
    protected final List<String> selectPaths = new ArrayList<>();
    private OnSelectListsner onSelectListsner;
//    private static Map<String, Integer> suffixIcons = new HashMap();
//    static {
//        suffixIcons.put(AppConstant.FileSuffix.MD, R.drawable.ic_note_md);
//        suffixIcons.put(AppConstant.FileSuffix.TODO, R.drawable.ic_note_todo);
//        suffixIcons.put(AppConstant.FileSuffix.TXT, R.drawable.ic_note_txt);
//        suffixIcons.put(AppConstant.FileSuffix.PWD, R.drawable.ic_note_pwd);
//    }

    public void setSelecting(boolean isSelecting, List<String> selectPaths) {
        this.isSelecting = isSelecting;
        this.selectPaths.clear();
        if (selectPaths != null) {
            this.selectPaths.addAll(selectPaths);
        }
        notifyDataSetChanged();
        postSelectChanged();
    }

    private void postSelectChanged() {
        if (onSelectListsner != null) {
            onSelectListsner.onChanged(this);
        }
    }

    public boolean isSelecting() {
        return isSelecting;
    }

    public void selectNone() {
        if (this.selectPaths.isEmpty()) {
            return;
        }
        this.selectPaths.clear();
        notifyDataSetChanged();
        postSelectChanged();
    }

    public void selectAll() {
        this.selectPaths.clear();
        if (datas != null) {
            for (FileEntity f : datas) {
                selectPaths.add(f.file.getAbsolutePath());
            }
        }
        notifyDataSetChanged();
        postSelectChanged();
    }

    public boolean isSelectAll() {
        return selectPaths.size() == getItemCount();
    }

    @Override
    protected void onItemClick(int position, RecyclerView.ViewHolder holder) {
        if (isSelecting) {
            String file = getItem(position).file.getAbsolutePath();
            if (!selectPaths.remove(file)) {
                selectPaths.add(file);
            }
            notifyItemChanged(position);
            postSelectChanged();
            return;
        }
        super.onItemClick(position, holder);
    }

    @Override
    protected boolean onItemLongClick(int position, RecyclerView.ViewHolder holder) {
        if (isSelecting) {
            return false;
        }
        return super.onItemLongClick(position, holder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((BaseVH) holder).setSelected(isSelecting, selectPaths.contains(getItem(position).file.getAbsolutePath()));
    }

    public void setOnSelectListsner(OnSelectListsner onSelectListsner) {
        this.onSelectListsner = onSelectListsner;
    }

    public interface OnSelectListsner {
        void onChanged(NoteListAdapter adapter);
    }
}
