package lhg.gitnotes.note.bill.ui;

import android.app.AlertDialog;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lhg.common.adapter.RecyclerClickAdapter;
import lhg.common.utils.DrawableUtils;
import lhg.common.utils.Utils;
import lhg.gitnotes.R;
import lhg.gitnotes.note.bill.BillEntity;

public class BillItemAdapter extends RecyclerClickAdapter<BillEntity, BillItemAdapter.VH> {

    List<BillEntity> items;
    final List<BillEntity> selectedItems = new ArrayList<>();
    boolean isEditing = false;
    private OnSelectListsner onSelectListsner;

    public void setItems(List<BillEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setSelecting(boolean isSelecting, List<BillEntity> selectedItems) {
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
        void onChanged(BillItemAdapter adapter);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public BillEntity getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        BillEntity item = getItem(position);
        holder.update(item, isEditing, isEditing ? findItem(item, selectedItems) >= 0 : false);
    }

    @Override
    protected void onItemClick(int position, VH holder) {
        if (isEditing) {
            BillEntity item = getItem(position);
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

    private static int findItem(BillEntity p, List<BillEntity> selectedItems) {
        return selectedItems.indexOf(p);
    }


    private void postSelectChanged() {
        if (onSelectListsner != null) {
            onSelectListsner.onChanged(this);
        }
    }

    public static class VH extends RecyclerView.ViewHolder {

        BillEntity item;
        StateListDrawable itemDrawableInSelecting;
        StateListDrawable itemDrawable;
        View ivClose;
        TextView tvName;
        TextView tvMoney;

        public VH(@NonNull View itemView) {
            super(itemView);
            itemDrawable = DrawableUtils.listItemBackgroundPrimary(itemView.getContext());
            itemDrawableInSelecting = DrawableUtils.clone(itemDrawable, Arrays.asList(new int[]{android.R.attr.state_pressed}));
            tvMoney = itemView.findViewById(R.id.tvMoney);
            ivClose = itemView.findViewById(R.id.ivClose);
            tvName = itemView.findViewById(R.id.tvName);
            ivClose.setOnClickListener(v -> delete(item));
        }

        private void delete(BillEntity item) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle(R.string.confirm_delete)
                    .setMessage(item.name + "  " + Utils.fen2yuan(item.money))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> onDelete(item))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        protected void onDelete(BillEntity item) {

        }

        public VH(@NonNull ViewGroup parent) {
            this(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false));
        }

        public void update(BillEntity item, boolean isEditing, boolean isSelected) {
            this.item = item;
            itemView.setBackground(isEditing ? itemDrawableInSelecting : itemDrawable);
            itemView.setSelected(isEditing && isSelected);
            tvName.setText(item.name + "  " + Utils.nil(item.time));
            tvMoney.setText(Utils.fen2yuan(item.money));
        }
    }

}