package lhg.gitnotes.note.bill;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lhg.common.utils.ColorUtils;
import lhg.common.utils.DateFormater;
import lhg.common.utils.Utils;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.ui.FileEditor;

public class BillEditor extends FileEditor {

    RecyclerView recyclerView;
    BillItemAdapter adapter;
    Gson gson;
    ArrayList<BillEntity> datas;
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

        initListView();

        if (!isNewFile) {
            Single.fromCallable(() -> {
                String text = readFileText();
                ArrayList<BillEntity> datas = gson.fromJson(text, new TypeToken<ArrayList<BillEntity>>(){}.getType());
                return datas;
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(datas -> {
                        this.datas = datas;
                        adapter.setItems(datas);
                        updateTotal();
                    }, throwable -> {
                        this.datas = new ArrayList<>();
                        throwable.printStackTrace();
                    });
        } else {
            this.datas = new ArrayList<>();
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
                    @Override
                    protected void onDelete(BillEntity item) {
                        super.onDelete(item);
                        hasChanged = true;
                        datas.remove(item);
                        adapter.notifyDataSetChanged();
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

        if (p.money > 0) {
            tilMoney.getEditText().setText(Utils.fen2yuan(p.money));
        }
        if (TextUtils.isEmpty(p.time)) {
            p.time = showyyyyMMddHHmm.format(new Date());
        }
        tilName.getEditText().setText(p.name);
        tilTime.getEditText().setText(p.time);

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
                    finalP.money = Utils.yuan2fen(tilMoney.getEditText().getText().toString());

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
    }

    private void updateTotal() {
        tvTotal.setText(R.string.total);
        long money = 0;
        if (datas != null) {
            for (BillEntity b : datas) {
                money += b.money;
            }
        }
        tvTotal.append("   ");
        tvTotal.append(Utils.fen2yuan(money));
    }

    private void initListView() {
        adapter = createAdapter();
        adapter.setOnItemClickListener((adapter, data, holder) -> gotoEditItem(data, false));
        ConcatAdapter concatAdapter = new ConcatAdapter(new HeaderAdapter(true), adapter, new HeaderAdapter(false));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Drawable divider = getResources().getDrawable(R.drawable.divider_password_recycler);
        DividerItemDecoration dividerItem = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        dividerItem.setDrawable(divider);
        recyclerView.addItemDecoration(dividerItem);
        recyclerView.setAdapter(concatAdapter);
    }

    private class HeaderAdapter extends RecyclerView.Adapter {

        boolean top;

        public HeaderAdapter(boolean top) {
            this.top = top;
        }

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh = new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_add, parent, false)) {
            };
            vh.itemView.setOnClickListener(v -> gotoEditItem(null, top));
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }


}
