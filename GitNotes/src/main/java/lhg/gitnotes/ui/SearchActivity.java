package lhg.gitnotes.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import lhg.gitnotes.R;
import lhg.gitnotes.app.AppBaseActivity;
import lhg.gitnotes.git.GitConfig;

public class SearchActivity extends AppBaseActivity  {
    private SearchView searchView;
    private GitConfig gitConfig;

    public static Intent makeIntent(Context context, GitConfig gitConfig) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("gitConfig", gitConfig);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pcm_activity_fragment_container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setContentInsetStartWithNavigation(0);
        showPrevArrowOnActionBar();
        if (getIntent() == null) {
            finish();
            return;
        }
        gitConfig = (GitConfig) getIntent().getSerializableExtra("gitConfig");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        initActionSearch(menu);
        return true;
    }

    private void initActionSearch(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
//        searchView.setIconified(false);
//        searchView.setIconifiedByDefault(false);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                doSearch(s);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

    }

    private void doSearch(String s) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}