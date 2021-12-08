package lhg.gitnotes.note.todo.ui;

import android.graphics.Paint;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import lhg.common.utils.DrawableUtils;
import lhg.gitnotes.R;
import lhg.gitnotes.note.todo.TodoEntity;

public class TodoItemAdapter extends RecyclerView.Adapter<TodoItemAdapter.VH> {

    List<TodoEntity> items;

    public void setItems(List<TodoEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

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
        holder.update(item);
    }


    @Override
    public long getItemId(int position) {
        return 0;
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
                update(item);
                onChecked(item);
            });
            ivClose.setOnClickListener(v -> onDelete(item));
            itemView.setBackground(itemDrawable);
        }

        protected void onChecked(TodoEntity item) {

        }

        protected void onDelete(TodoEntity item) {

        }

        public VH(@NonNull ViewGroup parent) {
            this(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false));
        }

        public void update(TodoEntity item) {
            this.item = item;
            tvContent.setText(item.content);
            ivCheck.setSelected(item.isChecked());
            if (item.isChecked()) {
                tvContent.getPaint().setFlags(tvContent.getPaint().getFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvContent.getPaint().setFlags(tvContent.getPaint().getFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
    }

}