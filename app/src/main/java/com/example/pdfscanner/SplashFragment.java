package com.example.pdfscanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SplashFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_splash,container,false);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(500);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    SharedPreferences prefs=getActivity().getSharedPreferences("PDFScannerPrefs", Context.MODE_PRIVATE);
                    String token = prefs.getString("token","");
                    if (token.equals("")) {
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.initial_container, new LoginFragment(), "LOGIN_FRAGMENT")
                                .commit();
                    } else {
                        Intent i = new Intent(getActivity(), MainActivity.class);
                        startActivity(i);
                        getActivity().finish();
                    }
                }
            }

        };
        thread.start();
        return v;
    }
}
