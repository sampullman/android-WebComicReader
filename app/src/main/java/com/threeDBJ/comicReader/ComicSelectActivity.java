package com.threeDBJ.comicReader;

import android.Manifest.permission;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.threeDBJ.comicReader.ComicReaderApp.ComicState;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ComicSelectActivity extends AppCompatActivity {
    ComicState state;
    @BindView(R.id.toolbar) Toolbar toolBar;
    @BindView(R.id.tabs) TabLayout tabs;
    @BindView(R.id.view_pager) ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolBar);

        setupViewPager(viewPager);
        tabs.setupWithViewPager(viewPager);

        state = ((ComicReaderApp) getApplicationContext()).getComicState();

        MultiplePermissionsListener permissionsListener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                Timber.d("PERMISSIONS CHECKED");
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                Timber.d("PERMISSIONS RATIONALE");
            }
        };

        Dexter.withActivity(this)
                .withPermissions(permission.ACCESS_NETWORK_STATE, permission.INTERNET, permission.WRITE_EXTERNAL_STORAGE)
                .withListener(permissionsListener)
                .check();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ComicSelectListFragment.newInstance(ComicSelectListFragment.EXTERNAL), "COMICS");
        adapter.addFragment(ComicSelectListFragment.newInstance(ComicSelectListFragment.SAVED), "SAVED");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_settings:
            Timber.d("Show settings");
            break;
        case R.id.menu_help:
            Timber.d("Show help");
            break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 1) {
            showDialog("Error", "No saved comics to show.");
        } else if(resultCode == 2) {
            showDialog("Warning", "SD card not readable or could not be found.");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        state.clearComics();
    }

    public void showDialog(String title, String text) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.text_alt);
        dialog.setTitle(title);
        TextView t = dialog.findViewById(R.id.alt_text);
        t.setText(text);
        dialog.setCancelable(true);
        Button done = dialog.findViewById(R.id.alt_done);
        done.setOnClickListener(makeDialogCancelListener(dialog));
        dialog.show();
    }

    public OnClickListener makeDialogCancelListener(final Dialog d) {
        return new OnClickListener() {
            public void onClick(View v) {
                d.dismiss();
            }
        };
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
