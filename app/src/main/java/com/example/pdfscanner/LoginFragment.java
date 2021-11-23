package com.example.pdfscanner;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pdfscanner.api.PDFScannerAPI;
import com.example.pdfscanner.model.JWTToken;
import com.example.pdfscanner.model.User;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    private Button loginButton;
    private EditText username,password;
    private TextView signup, error;
    private Dialog progressDialog;
    private static final String U_NAME = "username";
    private static String uname;

    public static LoginFragment newInstance(String uname) {
        Bundle args = new Bundle();
        args.putString(U_NAME,uname);
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments().getString(U_NAME)!=null) {
                uname = getArguments().getString(U_NAME);
            }
        } catch (Exception e) {

        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login,container,false);
        loginButton = v.findViewById(R.id.login);
        username = v.findViewById(R.id.username);
        password = v.findViewById(R.id.password);
        signup = v.findViewById(R.id.toSignup);
        error = v.findViewById(R.id.loginError);
        progressDialog = new Dialog(getActivity());
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.setCancelable(false);
        if (uname!=null) {
            username.setText(uname);
        }

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.initial_container, new SignupFragment(), "SIGNUP_FRAGMENT")
                        .addToBackStack(null)
                        .commit();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().trim().equals("")) {
                    error.setText("Please enter username");
                } else if (password.getText().toString().trim().equals("")) {
                    error.setText("Please enter password");
                } else {
                    progressDialog.show();
                    PDFScannerAPI.pdfScannerAPI.login(new User(username.getText().toString().trim(), password.getText().toString().trim())).enqueue(new Callback<JWTToken>() {
                        @Override
                        public void onResponse(Call<JWTToken> call, Response<JWTToken> response) {
                            progressDialog.dismiss();
                            if (response.code() < 300 && response.code() > 199) {
                                SharedPreferences prefs;
                                SharedPreferences.Editor edit;
                                prefs=getActivity().getSharedPreferences("PDFScannerPrefs", Context.MODE_PRIVATE);
                                edit=prefs.edit();
                                String saveToken=response.body().getToken();
                                edit.putString("token",saveToken);
                                edit.putString("username",username.getText().toString().trim());
                                edit.commit();
                                Intent i  = new Intent(getActivity(), MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                getActivity().finish();
                            } else {
                                error.setText("Username and password are not correct");
                            }
                        }
                        @Override
                        public void onFailure(Call<JWTToken> call, Throwable t) {
                            progressDialog.dismiss();
                            error.setText("Something went wrong. Check your internet connection.");
                        }
                    });
                }
            }
        });
        return v;
    }

}
