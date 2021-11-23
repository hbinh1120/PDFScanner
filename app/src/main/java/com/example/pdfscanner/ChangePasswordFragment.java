package com.example.pdfscanner;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pdfscanner.api.PDFScannerAPI;
import com.example.pdfscanner.model.ScannerFile;
import com.example.pdfscanner.model.User;
import com.example.pdfscanner.model.UserChange;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment implements IOnBackPressed{
    private Button saveButton;
    private EditText oldpassword,newpassword,repassword;
    private Dialog progressDialog;
    private TextView cancel, error;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.isMainFragment = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_changepassword,container,false);
        saveButton = v.findViewById(R.id.save_change_password);
        oldpassword = v.findViewById(R.id.old_password);
        newpassword = v.findViewById(R.id.new_password);
        repassword = v.findViewById(R.id.confirm_new_password);
        cancel = v.findViewById(R.id.cancel_changepassword);
        error = v.findViewById(R.id.changePasswordError);
        progressDialog = new Dialog(getActivity());
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.setCancelable(false);
        MainFragment.haveNewFile=false;


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String u = getActivity().getSharedPreferences("PDFScannerPrefs", Context.MODE_PRIVATE).getString("username","");
                String o_p = oldpassword.getText().toString().trim();
                String n_p = newpassword.getText().toString().trim();
                if (n_p.length()<8 || n_p.length()>65) {
                    error.setText("New password must have length between 8 and 65");
                } else if (!n_p.equals(repassword.getText().toString().trim())) {
                    error.setText("Confirm password incorrect");
                } else {
                    progressDialog.show();
                    PDFScannerAPI.pdfScannerAPI.changepassword(new UserChange(u, o_p, n_p)).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            progressDialog.dismiss();
                            if (response.code() < 300 && response.code() > 199) {
                                Toast.makeText(getActivity(), "Change password successful", Toast.LENGTH_SHORT).show();
                                MainFragment.haveNewFile=false;
                                MainFragment mainFragment = MainFragment.newInstance(false);
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, mainFragment, "MAIN_FRAGMENT")
                                        .commit();
                            } else {
                                error.setText("Old password is not correct");
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

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
