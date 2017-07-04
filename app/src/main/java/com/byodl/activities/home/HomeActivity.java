package com.byodl.activities.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.byodl.R;
import com.byodl.activities.base.BaseActivity;
import com.byodl.activities.home.fragments.AnnotateFragment;
import com.byodl.activities.home.fragments.PredictFragment;
import com.byodl.activities.home.fragments.UpdateFragment;
import com.byodl.services.SyncDataService;
import com.byodl.utils.ModelHelper;
import com.byodl.utils.NotificationHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends BaseActivity implements ModelHelper.OnInitializedListener {

    private static final int TAB_PREDICT = 0;
    private static final int TAB_ANNOTATE = 1;
    private static final int TAB_UPDATE = 2;
    private int currentPosition = -1;
    private ProgressDialog dlg;

    public static void show(Activity parent){
        if (parent==null)
            return;
        Intent intent = new Intent(parent,HomeActivity.class);
        parent.startActivity(intent);
    }

    private static final int[] TABS_ICONS = new int[]{
            R.drawable.ic_pred_icon,R.drawable.ic_edit_icon,R.drawable.ic_refresh_icon
    };

    @BindView(R.id.toolbarMenu)
    ImageButton toolbarMenu;
    @BindView(R.id.tabs)
    TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        createTabs();
        if (!ModelHelper.getInstance().initialize(this)){
            dlg = new ProgressDialog(this,R.style.AppThemeDialog);
            dlg.setIndeterminate(true);
            dlg.setMessage(getString(R.string.initing_model));
            dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dlg.setCancelable(false);
            dlg.show();

        }
        else if (savedInstanceState==null)
	        showPage(TAB_PREDICT);
        SyncDataService.startSync(this);

    }

    private void createTabs() {
        tabs.addTab(tabs.newTab().setIcon(TABS_ICONS[TAB_PREDICT]));
        tabs.addTab(tabs.newTab().setIcon(TABS_ICONS[TAB_ANNOTATE]));
        tabs.addTab(tabs.newTab().setIcon(TABS_ICONS[TAB_UPDATE]));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showPage(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
	    ColorStateList colors = ContextCompat.getColorStateList(this,R.color.tab_icon);
	    for (int i = 0; i < tabs.getTabCount(); i++) {
		    TabLayout.Tab tab = tabs.getTabAt(i);
		    if (tab!=null) {
			    Drawable icon = tab.getIcon();

			    if (icon != null) {
				    icon = DrawableCompat.wrap(icon);
				    DrawableCompat.setTintList(icon, colors);
			    }
		    }
	    }
    }

    private void showPage(int position) {
        if (currentPosition==position)
            return;
        currentPosition = position;
        switch (position){
            case TAB_PREDICT:
                switchFragment(new PredictFragment());
                break;
            case TAB_ANNOTATE:
                switchFragment(new AnnotateFragment());
                break;
            case TAB_UPDATE:
            	switchFragment(new UpdateFragment());
                break;
        }
    }
    private void switchFragment(Fragment f){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content,f);
        ft.commit();
    }
    @OnClick(R.id.toolbarMenu)
    void onMenuClick(){
        PopupMenu menu = new PopupMenu(new ContextThemeWrapper(this,R.style.AppTheme_PopupOverlay),toolbarMenu);
        menu.inflate(R.menu.home_popup);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_about:
                        showAbout();
                        return true;
                    case R.id.action_help:
                        showHelp();
                        return true;
                }
                return false;
            }
        });
        menu.show();
    }

    private void showHelp() {
        //TODO
        NotificationHelper.toast(this,R.string.not_yet_implemented);
    }

    private void showAbout() {
        //TODO
        NotificationHelper.toast(this,R.string.not_yet_implemented);
    }

    @Override
    public void onInitialized(boolean success) {
        if (dlg!=null&&dlg.isShowing())
            dlg.dismiss();

        if (!success)
            NotificationHelper.alert(this,R.string.initializing_title,R.string.initializing_msg_failed);
	    else
	        showPage(TAB_PREDICT);
    }

}
