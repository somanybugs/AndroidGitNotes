package lhg.gitnotes.note.pwd.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;

import lhg.entityui.Entity2Layout;
import lhg.gitnotes.R;
import lhg.gitnotes.note.pwd.PasswordEntity;
import lhg.gitnotes.app.AppBaseActivity;
import lhg.gitnotes.note.pwd.EditPasswordEntity;


import java.util.UUID;

public class PasswordItemEditor extends AppBaseActivity {
    public static final String IntentKey_Item = "item";

    boolean isEmptyItem = false;
    Entity2Layout entity2Layout;
    PasswordEntity item = null;
    String inItemJson;
    EditPasswordEntity entity = new EditPasswordEntity();

    Gson gson = new Gson();

    public static Intent makeIntent(Context context, PasswordEntity item) {
        Intent intent = new Intent(context, PasswordItemEditor.class);
        intent.putExtra(IntentKey_Item, item);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_item_editor);
        setSupportActionBar(findViewById(R.id.toolbar));
        showPrevArrowOnActionBar();

        item = null;
        if (getIntent() != null) {
            item = (PasswordEntity) getIntent().getSerializableExtra(IntentKey_Item);
        }
        isEmptyItem = item == null || TextUtils.isEmpty(item.uuid);

        if (item == null) {
            item = new PasswordEntity();
        }
        entity.password = item.password;
        entity.name = item.name;
        entity.note = item.note;
        entity.account = item.account;

        findViewById(R.id.tvGenPassword).setOnClickListener(v -> new GenPasswordDialog(getActivity()).show());
        inItemJson = gson.toJson(entity);

        entity2Layout = findViewById(R.id.entityLayout);
        entity2Layout.initEdit(entity);

        if (isEmptyItem) {
            setTitle("创建帐号密码");
        } else {
            setTitle("编辑帐号密码");
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_pasword, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (save()) {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (userHasEdit()) {
            if (save()) {
                finish();
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle("确定放弃保存吗?")
                        .setNegativeButton("确定", (dialog, which) -> finish())
                        .show();
            }
        } else {
            super.onBackPressed();
        }
    }


    public boolean userHasEdit() {
        return !inItemJson.equals(gson.toJson(entity));
    }

    public boolean save() {
        setResult(RESULT_CANCELED);
        if (entity2Layout.isEditValid() != null) {
            return false;
        }


        item.account = entity.account;
        item.name = entity.name;
        item.note = entity.note;
        item.password = entity.password;
        item.time = System.currentTimeMillis();
        if (TextUtils.isEmpty(item.uuid)) {
            item.uuid = UUID.randomUUID().toString();
        }
        Intent intent = new Intent();
        intent.putExtra(IntentKey_Item, this.item);
        setResult(RESULT_OK, intent);

        return true;
    }

}
