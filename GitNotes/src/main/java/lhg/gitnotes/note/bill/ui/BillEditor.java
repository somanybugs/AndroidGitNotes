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
import java.util.List;
import java.util.UUID;

import lhg.common.utils.ColorUtils;
import lhg.common.utils.Utils;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.note.bill.BillEntity;
import lhg.gitnotes.ui.FileEditor;
import lhg.gitnotes.ui.view.BaseItemMoveCallback;

public class BillEditor extends FileEditor<List<BillEntity>> {

    RecyclerView recyclerView;
    BillItemAdapter adapter;
    final ArrayList<BillEntity> datas = new ArrayList<>();
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

        setTitleAndSubtitle();
        recyclerView = findViewById(R.id.recyclerView);
        tvTotal = findViewById(R.id.tvTotal);
        findViewById(R.id.ivAdd).setOnClickListener(v -> gotoEditItem(null, true));

        initListView();

        return true;
    }

    protected BillItemAdapter createAdapter() {
        return new BillItemAdapter() {
            @NonNull
            @NotNull
            @Override
            public VH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                return new VH(parent) {
                    {
                        itemView.setOnClickListener(v -> gotoEditItem(item, false));
                    }

                    @Override
                    protected void onDelete(BillEntity item) {
                        super.onDelete(item);
                        adapter.notifyItemRemoved(getBindingAdapterPosition());
                        datas.remove(item);
                        saveLocalFile();
                        updateTotal();
                    }
                };
            }
        };
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
        prefix = prefix + String.format("%d.%02d", a / 100, a % 100);
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

        new ItemTouchHelper(new BaseItemMoveCallback(getActivity()) {
            @Override
            protected void onItemMoved(int from, int to) {
                Collections.swap(datas, from, to);//更换我们数据List的位置
                adapter.notifyItemMoved(from, to);
                saveLocalFile();
            }
        }).attachToRecyclerView(recyclerView);
    }


    @Override
    protected WriteCallback<List<BillEntity>> onCreateCallback() {
        return new WriteCallback<List<BillEntity>>() {
            final Gson gson = new Gson();
            @Override
            public List<BillEntity> onRead() throws Exception {
                String text = readFileText();
                return gson.fromJson(text, new TypeToken<ArrayList<BillEntity>>(){}.getType());
            }

            @Override
            public void onReadSuccess(List<BillEntity> it) {
                if (it != null) {
                    datas.addAll(it);
                }
                adapter.setItems(datas);
                updateTotal();
            }

            @Override
            public void onWrite() throws Exception {
                String text = gson.toJson(datas);
                writeFileText(text);
            }
        };
    }
}
