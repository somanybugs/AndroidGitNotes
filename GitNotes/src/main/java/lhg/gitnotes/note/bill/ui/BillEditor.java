package lhg.gitnotes.note.bill.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lhg.common.utils.ColorUtils;
import lhg.common.utils.Utils;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.note.bill.BillEntity;
import lhg.gitnotes.ui.FileEditor;

public class BillEditor extends FileEditor {

    RecyclerView recyclerView;
    BillItemAdapter adapter;
    Gson gson;
    final ArrayList<BillEntity> datas = new ArrayList<>();
    boolean hasChanged = false;
    int accentColor;
    TextView tvTotal;


    public static Intent makeIntent(Context context, String path, GitConfig gitConfig, String password) {
        return makeIntent(context, BillEditor.class, path, gitConfig, password);
    }

    @Override
    protected boolean initOnCreate() {
        if (!super.initOnCreate()) {
            return false;
        }
        accentColor = ColorUtils.getAccentColor(this);

        setContentView(R.layout.activity_bill_editor);
        setSupportActionBar(findViewById(R.id.toolbar));
        showPrevArrowOnActionBar();

        gson = new Gson();

        setTitleAndSubtitle();
        recyclerView = findViewById(R.id.recyclerView);
        tvTotal = findViewById(R.id.tvTotal);
        findViewById(R.id.ivAdd).setOnClickListener(v -> gotoEditItem(null, true));

        initListView();

        if (!isNewFile) {
            Single.fromCallable(() -> {
                String text = readFileText();
                ArrayList<BillEntity> datas = gson.fromJson(text, new TypeToken<ArrayList<BillEntity>>(){}.getType());
                return datas;
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(datas -> {
                        this.datas.addAll(datas);
                        adapter.setItems(this.datas);
                        updateTotal();
                    }, throwable -> {
                        throwable.printStackTrace();
                    });
        } else {
            adapter.setItems(datas);
            updateTotal();
        }
        return true;
    }

    protected BillItemAdapter createAdapter() {
        return new BillItemAdapter(){
            @NonNull
            @NotNull
            @Override
            public VH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new VH(parent){
                    {
                        itemView.setOnClickListener(v -> gotoEditItem(item, false));
                    }
                    @Override
                    protected void onDelete(BillEntity item) {
                        super.onDelete(item);
                        hasChanged = true;
                        adapter.notifyItemRemoved(getBindingAdapterPosition());
                        datas.remove(item);
                        saveLocalFile();
                        updateTotal();
                    }
                };
            }
        };
    }


    @Override
    public void onBackPressed() {
        if (hasChanged) {
            gitSync(() -> super.onBackPressed());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveLocalFile() throws Throwable {
        String text = gson.toJson(datas);
        writeFileText(text);
    }

    SimpleDateFormat showyyyyMMddHHmm = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private void gotoEditItem(BillEntity p, boolean top) {
        if (p == null) {
            p = new BillEntity();
        }
        if (TextUtils.isEmpty(p.uuid)) {
            p.uuid = UUID.randomUUID().toString();
        }
        BillEntity finalP = p;
        View view = View.inflate(this, R.layout.dialog_input_bill, null);
        TextInputLayout tilName = view.findViewById(R.id.editName);
        TextInputLayout tilTime = view.findViewById(R.id.editTime);
        TextInputLayout tilMoney = view.findViewById(R.id.editMoney);

        if (TextUtils.isEmpty(p.time)) {
            tilTime.getEditText().setText(showyyyyMMddHHmm.format(new Date()));
        } else {
            tilTime.getEditText().setText(p.time);
        }
        tilName.getEditText().setText(p.name);
        tilMoney.getEditText().setText(p.money);


        new AlertDialog.Builder(this)
                .setTitle("Edit Item Content")
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog1, which) -> {
                    int index = datas.indexOf(finalP);
                    finalP.name = tilName.getEditText().getText().toString();
                    if (TextUtils.isEmpty(finalP.name)) {
                        tilName.setError(getString(R.string.empty_input));
                    }
                    finalP.time = tilTime.getEditText().getText().toString();
                    if (TextUtils.isEmpty(finalP.time)) {
                        tilTime.setError(getString(R.string.empty_input));
                    }
                    finalP.money = tilMoney.getEditText().getText().toString();

                    if (index >= 0) {
                        datas.set(index, finalP);
                        adapter.notifyItemChanged(index);
                    } else {
                        datas.add(top ? 0 : datas.size(), finalP);
                        adapter.notifyItemInserted(top ? 0 : datas.size());
                    }
                    hasChanged = true;
                    saveLocalFile();
                    updateTotal();
                }).setNegativeButton(android.R.string.cancel, null)
                .show();
        Utils.showKeyboard(tilName.getEditText());
    }

    private void updateTotal() {
        tvTotal.setText(R.string.total);
        long money = 0;
        if (datas != null) {
            for (BillEntity b : datas) {
                money += Utils.yuan2fen(b.money);
            }
        }
        tvTotal.append("   ");
        tvTotal.append(fen2yuan(money));
    }

    private static String fen2yuan(long a) {
        String prefix = "";
        if (a < 0) {
            prefix = "-";
            a = -a;
        }
        prefix = prefix + String.format("%d.%02d", a/100, a%100);
        if (prefix.endsWith(".00")) {
            prefix = prefix.substring(0, prefix.length() - 3);
        }
        return prefix;
    }

    private void initListView() {
        adapter = createAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Drawable divider = getResources().getDrawable(R.drawable.divider_password_recycler);
        DividerItemDecoration dividerItem = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        dividerItem.setDrawable(divider);
        recyclerView.addItemDecoration(dividerItem);
        recyclerView.setAdapter(adapter);

        QuickReplyItemTouchCallback callback = new QuickReplyItemTouchCallback();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    private class QuickReplyItemTouchCallback extends ItemTouchHelper.SimpleCallback {

        public QuickReplyItemTouchCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean isItemViewSwipeEnabled() { //是否启用左右滑动
            return false;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int from = viewHolder.getBindingAdapterPosition();
            int to = target.getBindingAdapterPosition();
            Collections.swap(datas, from, to);//更换我们数据List的位置
            adapter.notifyItemMoved(from, to);
            saveLocalFile();
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    }

}
