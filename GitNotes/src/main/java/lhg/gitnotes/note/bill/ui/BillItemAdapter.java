package lhg.gitnotes.note.bill.ui;

import android.app.AlertDialog;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lhg.common.utils.DrawableUtils;
import lhg.gitnotes.R;
import lhg.gitnotes.note.bill.BillEntity;

public class BillItemAdapter extends RecyclerView.Adapter<BillItemAdapter.VH> {

    List<BillEntity> items;

    public void setItems(List<BillEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

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
        holder.update(item);
    }



    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static int findItem(BillEntity p, List<BillEntity> selectedItems) {
        return selectedItems.indexOf(p);
    }

    public static class VH extends RecyclerView.ViewHolder {

        BillEntity item;
        StateListDrawable itemDrawable;
        View ivClose;
        TextView tvName;
        TextView tvTime;
        TextView tvMoney;

        public VH(@NonNull View itemView) {
            super(itemView);
            itemDrawable = DrawableUtils.listItemBackgroundPrimary(itemView.getContext());
            tvMoney = itemView.findViewById(R.id.tvMoney);
            ivClose = itemView.findViewById(R.id.ivClose);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivClose.setOnClickListener(v -> delete(item));
            itemView.setBackground(itemDrawable);
        }

        private void delete(BillEntity item) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle(R.string.confirm_delete)
                    .setMessage(item.name + "  " + item.money)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> onDelete(item))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        protected void onDelete(BillEntity item) {

        }

        public VH(@NonNull ViewGroup parent) {
            this(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false));
        }

        public void update(BillEntity item) {
            this.item = item;
            tvName.setText(item.name);
            tvTime.setText(item.time);
            tvMoney.setText(item.money);
        }

        public BillEntity getItem() {
            return item;
        }
    }

}