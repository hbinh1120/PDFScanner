package com.example.pdfscanner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class EditActivity extends AppCompatActivity {
    private static final String EXTRA_SOURCE =
            "com.example.pdfscanner.source";

    private String sourcePath;

    public static Intent newIntent(Context packageContext, String imgPath) {
        Intent intent = new Intent(packageContext, EditActivity.class);
        intent.putExtra(EXTRA_SOURCE, imgPath);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sourcePath = getIntent().getStringExtra(EXTRA_SOURCE);
        setContentView(R.layout.activity_edit);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.edit_container);
        if (fragment == null) {
            fragment = CropFragment.newInstance(sourcePath);
            fm.beginTransaction().add(R.id.edit_container,fragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.edit_container);
        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.remove("android:support:fragments");
    }
}
