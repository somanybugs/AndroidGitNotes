package lhg.gitnotes.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import lhg.common.utils.ColorUtils;

public abstract class BaseItemMoveCallback extends ItemTouchHelper.SimpleCallback {

    Paint mPaint = new Paint();

    public BaseItemMoveCallback(Context context) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        mPaint.setColor(ColorUtils.getPrimaryColor(context));
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public boolean isItemViewSwipeEnabled() { //是否启用左右滑动
        return false;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int from = viewHolder.getBindingAdapterPosition();
        int to = target.getBindingAdapterPosition();
        onItemMoved(from, to);
        return true;
    }

    protected abstract void onItemMoved(int from, int to);

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        if (dX != 0 && dY != 0 || isCurrentlyActive) {
            //长按拖拽时底部绘制一个虚线矩形
//            c.drawRect(viewHolder.itemView.getLeft(),viewHolder.itemView.getTop(),viewHolder.itemView.getRight(),viewHolder.itemView.getBottom(),mPaint);
        }
    }
}