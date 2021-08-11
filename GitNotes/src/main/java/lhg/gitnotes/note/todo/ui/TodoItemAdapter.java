package lhg.gitnotes.note.todo.ui;

import android.graphics.Paint;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import lhg.gitnotes.R;
import lhg.gitnotes.note.todo.TodoEntity;
import lhg.common.adapter.RecyclerClickAdapter;
import lhg.common.utils.DrawableUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TodoItemAdapter extends RecyclerClickAdapter<TodoEntity, TodoItemAdapter.VH> {

    List<TodoEntity> items;
    final List<TodoEntity> selectedItems = new ArrayList<>();
    boolean isEditing = false;
    private OnSelectListsner onSelectListsner;

    public void setItems(List<TodoEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setSelecting(boolean isSelecting, List<TodoEntity> selectedItems) {
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
        void onChanged(TodoItemAdapter adapter);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public TodoEntity getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TodoEntity item = getItem(position);
        holder.update(item, isEditing, isEditing ? findItem(item, selectedItems) >= 0 : false);
    }

    @Override
    protected void onItemClick(int position, VH holder) {
        if (isEditing) {
            TodoEntity item = getItem(position);
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

    private static int findItem(TodoEntity p, List<TodoEntity> selectedItems) {
        return selectedItems.indexOf(p);
    }


    private void postSelectChanged() {
        if (onSelectListsner != null) {
            onSelectListsner.onChanged(this);
        }
    }

    public static class VH extends RecyclerView.ViewHolder {

        TodoEntity item;
        StateListDrawable itemDrawableInSelecting;
        StateListDrawable itemDrawable;
        View ivCheck;
        View ivClose;
        TextView tvContent;

        public VH(@NonNull View itemView) {
            super(itemView);
            itemDrawable = DrawableUtils.listItemBackgroundPrimary(itemView.getContext());
            itemDrawableInSelecting = DrawableUtils.clone(itemDrawable, Arrays.asList(new int[]{android.R.attr.state_pressed}));
            ivCheck = itemView.findViewById(R.id.ivCheck);
            ivClose = itemView.findViewById(R.id.ivClose);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvContent.getPaint().setAntiAlias(true);//抗锯齿
            ivCheck.setOnClickListener(v -> {
                if (item.checkTime > 0) {
                    item.checkTime = 0;
                } else {
                    item.checkTime = System.currentTimeMillis();
                }
                update(item, false, false);
                onChecked(item);
            });
            ivClose.setOnClickListener(v -> onDelete(item));
        }

        protected void onChecked(TodoEntity item) {

        }

        protected void onDelete(TodoEntity item) {

        }

        public VH(@NonNull ViewGroup parent) {
            this(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false));
        }

        public void update(TodoEntity item, boolean isEditing, boolean isSelected) {
            this.item = item;
            itemView.setBackground(isEditing ? itemDrawableInSelecting : itemDrawable);
            itemView.setSelected(isEditing && isSelected);
            tvContent.setText(item.content);
            ivCheck.setSelected(item.isChecked());
            if (item.isChecked()) {
                tvContent.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvContent.getPaint().setFlags(tvContent.getPaint().getFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }

}