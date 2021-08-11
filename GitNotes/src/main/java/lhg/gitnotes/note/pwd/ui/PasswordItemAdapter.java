package lhg.gitnotes.note.pwd.ui;

import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import lhg.gitnotes.R;
import lhg.gitnotes.note.pwd.PasswordEntity;
import lhg.common.adapter.RecyclerClickAdapter;
import lhg.common.utils.DrawableUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PasswordItemAdapter extends RecyclerClickAdapter<PasswordEntity, PasswordItemAdapter.VH> {

    List<PasswordEntity> items;
    final List<PasswordEntity> selectedItems = new ArrayList<>();
    boolean isEditing = false;
    private OnSelectListsner onSelectListsner;

    public void setItems(List<PasswordEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setSelecting(boolean isSelecting, List<PasswordEntity> selectedItems) {
        this.isEditing = isSelecting;
        this.selectedItems.clear();
        if (selectedItems != null) {
            this.selectedItems.addAll(selectedItems);
        }
        notifyDataSetChanged();
        postSelectChanged();
    }

    public void selectNone() {
        if (this.selectedItems.isEmpty()) {
            return;
        }
        this.selectedItems.clear();
        notifyDataSetChanged();
        postSelectChanged();
    }

    public void selectAll() {
        this.selectedItems.clear();
        if (items != null) {
            selectedItems.addAll(items);
        }
        notifyDataSetChanged();
        postSelectChanged();
    }

    public boolean isSelectAll() {
        return selectedItems.size() == getItemCount();
    }
    public void setOnSelectListsner(OnSelectListsner onSelectListsner) {
        this.onSelectListsner = onSelectListsner;
    }

    public interface OnSelectListsner {
        void onChanged(PasswordItemAdapter adapter);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public PasswordEntity getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PasswordEntity item = getItem(position);
        holder.update(item, isEditing, isEditing ? findItem(item, selectedItems) >= 0 : false);
    }

    @Override
    protected void onItemClick(int position, VH holder) {
        if (isEditing) {
            PasswordEntity item = getItem(position);
            if (!selectedItems.remove(item)) {
                selectedItems.add(item);
            }
            notifyItemChanged(position);
            postSelectChanged();
            return;
        }
        super.onItemClick(position, holder);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static int findItem(PasswordEntity p, List<PasswordEntity> selectedItems) {
        return selectedItems.indexOf(p);
    }


    private void postSelectChanged() {
        if (onSelectListsner != null) {
            onSelectListsner.onChanged(this);
        }
    }

    static class VH extends RecyclerView.ViewHolder {

        StateListDrawable itemDrawableInSelecting;
        StateListDrawable itemDrawable;

        public VH(@NonNull View itemView) {
            super(itemView);
            itemDrawable = DrawableUtils.listItemBackgroundPrimary(itemView.getContext());
            itemDrawableInSelecting = DrawableUtils.clone(itemDrawable, Arrays.asList(new int[]{android.R.attr.state_pressed}));
        }

        public VH(@NonNull ViewGroup parent) {
            this(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_password_normal, parent, false));
        }

        public void update(PasswordEntity item, boolean isEditing, boolean isSelected) {
            itemView.setBackground(isEditing ? itemDrawableInSelecting : itemDrawable);
            itemView.setSelected(isEditing && isSelected);
            ((TextView) itemView.findViewById(R.id.tvTitle)).setText(item.name);
            ((TextView) itemView.findViewById(R.id.tvDetail)).setText(item.account);
        }
    }
}