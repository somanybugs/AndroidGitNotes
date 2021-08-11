package lhg.gitnotes.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import lhg.drawerlayout.DrawerLayout;
import lhg.gitnotes.app.App;
import lhg.gitnotes.app.AppBaseActivity;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.git.GitContext;
import lhg.gitnotes.git.GitService;
import lhg.gitnotes.git.action.GitSync;
import lhg.gitnotes.git.ui.GitRepoAddActivity;
import lhg.gitnotes.note.ui.NodesListFragment;

import lhg.common.OnBackPressedCallback;
import lhg.common.utils.Utils;

public class HomeActivity extends AppBaseActivity implements HomeLeftDrawerFragment.CloseCallback {
    private NodesListFragment fragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawerLayout;
    private TextView tvRepoInfo;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Make toolbar show navigation button (i.e back button with arrow icon)

        toolbar.setNavigationIcon(R.drawable.outline_menu_white_24); // Replace arrow icon with our custom icon
        toolbar.setContentInsetStartWithNavigation(0);


        tvRepoInfo = findViewById(R.id.tvRepoInfo);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> syncRepo());

        tvEmpty = findViewById(R.id.tvEmpty);
        tvEmpty.setOnClickListener(v -> startActivity(new Intent(getActivity(), GitRepoAddActivity.class)));
        tvEmpty.setVisibility(View.VISIBLE);

        drawerLayout = findViewById(R.id.drawerLayout);
        App.instance().getGitContext().getGitConfig().observe(this, gitConfig -> switchToRepo(gitConfig));
        onInitRepo();
    }

    private void syncRepo() {
        swipeRefreshLayout.setRefreshing(false);
        GitConfig gitConfig = GitContext.instance().getGitConfig().getValue();
        if (gitConfig == null) {
            return;
        }
        GitService.instance().submit(new GitSync(gitConfig, null));
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onInitRepo();
    }

    private void onInitRepo() {
        switchToRepo(GitContext.instance().getGitConfig().getValue());
    }

    private void switchToRepo(GitConfig gc) {
        Log.i("switchToRepo", gc + "");
        drawerLayout.close();
        if (gc == null) {
            setTitle(Utils.getApplicationName(this));
            swipeRefreshLayout.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        GitService.instance().submit(new GitSync(gc, null));

        tvEmpty.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        setTitle(gc.getRepoName());

        NodesListFragment nodesListFragment = new NodesListFragment();
        Bundle args = new Bundle();
        args.putSerializable("gitConfig", gc);
        nodesListFragment.setArguments(args);
        fragment = nodesListFragment;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, nodesListFragment)
                .commitAllowingStateLoss();
        tvRepoInfo.setText(gc.getUrl());
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isOpen()) {
            drawerLayout.close();
            return;
        }
        if (fragment != null && fragment instanceof OnBackPressedCallback) {
            if (((OnBackPressedCallback) fragment).onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_normal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.open(Gravity.LEFT);
            return true;
        }

        if (item.getItemId() == R.id.action_sync) {
            syncRepo();
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void closeDrawer() {
        drawerLayout.close();
    }
}