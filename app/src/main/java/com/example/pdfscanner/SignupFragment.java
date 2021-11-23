package com.example.pdfscanner;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pdfscanner.api.PDFScannerAPI;
import com.example.pdfscanner.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupFragment extends Fragment {
    private Button signupButton;
    private EditText username,password,repassword;
    private Dialog progressDialog;
    private TextView login, error;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register,container,false);
        signupButton = v.findViewById(R.id.signup);
        username = v.findViewById(R.id.register_username);
        password = v.findViewById(R.id.register_password);
        repassword = v.findViewById(R.id.repassword);
        login = v.findViewById(R.id.toLogin);
        error = v.findViewById(R.id.signupError);
        progressDialog = new Dialog(getActivity());
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.setCancelable(false);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.initial_container, new LoginFragment(), "LOGIN_FRAGMENT")
                        .addToBackStack(null)
                        .commit();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String u = username.getText().toString().trim();
                String p = password.getText().toString().trim();
                if (u.length()<2) {
                    error.setText("Username must have length greater than 1");
                } else if (p.length()<8 || p.length()>65) {
                    error.setText("Password must have length between 8 and 65");
                } else if (!p.equals(repassword.getText().toString().trim())) {
                    error.setText("Confirm password incorrect");
                } else {
                    progressDialog.show();
                    PDFScannerAPI.pdfScannerAPI.signup(new User(u, p)).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            progressDialog.dismiss();
                            if (response.code() < 300 && response.code() > 199) {
                                Toast.makeText(getActivity(), "Signup successful", Toast.LENGTH_SHORT).show();
                                LoginFragment loginFragment = LoginFragment.newInstance(u);
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.initial_container, loginFragment, "LOGIN_FRAGMENT")
                                        .addToBackStack(null)
                                        .commit();
                            } else {
                                error.setText("Something went wrong");
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
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
