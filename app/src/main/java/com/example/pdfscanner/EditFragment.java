package com.example.pdfscanner;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.pdfscanner.api.PDFScannerAPI;
import com.example.pdfscanner.filter.Filter;
import com.example.pdfscanner.model.ScannerFile;
import com.example.pdfscanner.singleton.FormSingleton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditFragment extends Fragment{
    private Button deleteButton, filterButton, backButton, checkButton, adjustButton, saveButton, cancelButton, deleteNoButton, deleteYesButton;
    private ImageView editImage, originalImage , grayImage, bwImage, smoothImage, magicImage;;
    private Bitmap originalBitmap,cropBitmap, grayBitmap, bwBitmap, smoothBitmap, magicBitmap;
    private Bitmap editBitmap;
    private Bitmap adjustBitmap;
    private TextView originalText, grayText, magicText, bwText, smoothText, filterText, adjustText, brightText, contrastText;
    private HorizontalScrollView filterView;
    private ConstraintLayout adjustView;
    private SeekBar contrastBar, brightBar;
    private Dialog checkDialog;
    private FormSingleton formSingleton;
    private Dialog deleteDialog;
    private EditText save_filename;
    private RadioButton save_pdf, save_jpeg;
    private RadioGroup save_format;
    private Filter filter;
    private int contrastValue, brightValue;
    private String format_type = "pdf";
    private String format_name;
    private boolean change_filter_check = true;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String token;
    private Dialog progressDialog;

    public static EditFragment newInstance() {
        EditFragment fragment = new EditFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        formSingleton = FormSingleton.get(getActivity());
        cropBitmap = formSingleton.getForm().getCropBitmap();
        originalBitmap = cropBitmap.copy(cropBitmap.getConfig(), false);
        filter = new Filter(originalBitmap);
        token = getActivity().getSharedPreferences("PDFScannerPrefs", Context.MODE_PRIVATE).getString("token","");
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit,container,false);
        progressDialog = new Dialog(getActivity());
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        progressDialog.setCancelable(false);
        deleteButton = v.findViewById(R.id.deleteButton);
        filterButton = v.findViewById(R.id.filterButton);
        backButton = v.findViewById(R.id.edit_back);
        checkButton = v.findViewById(R.id.edit_check);
        adjustButton = v.findViewById(R.id.adjustButton);
        filterButton = v.findViewById(R.id.filterButton);
        adjustText = v.findViewById(R.id.adjustTextView);
        filterText = v.findViewById(R.id.filterTextView);
        originalImage = v.findViewById(R.id.originalImageView);
        grayImage = v.findViewById(R.id.grayImageView);
        bwImage = v.findViewById(R.id.bwImageView);
        smoothImage = v.findViewById(R.id.smoothImageView);
        magicImage = v.findViewById(R.id.magicImageView);
        editImage = v.findViewById(R.id.image_edit);
        contrastBar = v.findViewById(R.id.contrastSeekBar);
        brightBar = v.findViewById(R.id.brightnessSeekBar);
        adjustView = v.findViewById(R.id.adjustView);
        filterView = v.findViewById(R.id.filterView);
        brightText = v.findViewById(R.id.brightnessText);
        contrastText = v.findViewById(R.id.contrastText);
        checkDialog = new Dialog(getActivity());
        checkDialog.setContentView(R.layout.dialog_save);
        checkDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        checkDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        saveButton = checkDialog.findViewById(R.id.btn_save_yes);
        cancelButton = checkDialog.findViewById(R.id.btn_save_no);
        save_filename = checkDialog.findViewById(R.id.save_filename);
        save_format = checkDialog.findViewById(R.id.save_format);
        save_pdf = checkDialog.findViewById(R.id.save_pdf);
        save_jpeg = checkDialog.findViewById(R.id.save_jpeg);

        originalText = v.findViewById(R.id.originalTextView);
        grayText = v.findViewById(R.id.grayTextView);
        bwText = v.findViewById(R.id.bwTextView);
        smoothText = v.findViewById(R.id.smoothTextView);
        magicText = v.findViewById(R.id.magicTextView);

        originalImage.setImageBitmap(originalBitmap);
        grayBitmap = filter.getGrayBitmap();
        grayImage.setImageBitmap(grayBitmap);
        bwBitmap = filter.getBWBitmap();
        bwImage.setImageBitmap(bwBitmap);
        smoothBitmap = filter.getSmoothBitmap();
        smoothImage.setImageBitmap(smoothBitmap);
        magicBitmap = filter.getMagicColorBitmap();
        magicImage.setImageBitmap(magicBitmap);

        editBitmap = magicBitmap.copy(magicBitmap.getConfig(), true);
        setSelectedFilter(magicText);
        editImage.setImageBitmap(editBitmap);

        deleteDialog = new Dialog(getActivity());
        deleteDialog.setContentView(R.layout.dialog_delete);
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteNoButton = deleteDialog.findViewById(R.id.btn_delete_no);
        deleteYesButton = deleteDialog.findViewById(R.id.btn_delete_yes);
        change_filter_check = true;
        brightValue = 50;
        contrastValue = 50;


        deleteNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        deleteYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        save_format.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioId = group.getCheckedRadioButtonId();
                // Check which radio button was clicked
                switch(radioId) {
                    case R.id.save_jpeg:
                        format_type = "jpeg";
                        save_pdf.setTextColor(getResources().getColor(R.color.black));
                        save_jpeg.setTextColor(getResources().getColor(R.color.primary));
                        break;
                    case R.id.save_pdf:
                        format_type = "pdf";
                        save_jpeg.setTextColor(getResources().getColor(R.color.black));
                        save_pdf.setTextColor(getResources().getColor(R.color.primary));
                        break;
                }
            }
        });

        originalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                setSelectedFilter(originalText);
                editImage.setImageBitmap(editBitmap);
            }
        });

        grayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = grayBitmap.copy(grayBitmap.getConfig(), true);
                setSelectedFilter(grayText);
                editImage.setImageBitmap(editBitmap);
            }
        });

        magicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = magicBitmap.copy(magicBitmap.getConfig(), true);
                setSelectedFilter(magicText);
                editImage.setImageBitmap(editBitmap);
            }
        });

        bwImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = bwBitmap.copy(bwBitmap.getConfig(), true);
                setSelectedFilter(bwText);
                editImage.setImageBitmap(editBitmap);
            }
        });
        smoothImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBitmap = smoothBitmap.copy(smoothBitmap.getConfig(), true);
                setSelectedFilter(smoothText);
                editImage.setImageBitmap(editBitmap);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                format_name = save_filename.getText().toString().trim();
                String pattern = "HH:mm:ss dd-MM-yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

                String date_created = simpleDateFormat.format(new Date());

                if (!format_name.equals("")) {
                    checkDialog.dismiss();
                    String username = getActivity().getSharedPreferences("PDFScannerPrefs",Context.MODE_PRIVATE).getString("username","");
                    storageReference = storage.getReference().child("ScannerFiles/"+username+"/"+date_created+"."+format_type);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (format_type.equals("pdf")) {
                        PdfDocument pdfDocument = new PdfDocument();
                        PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(editBitmap.getWidth(), editBitmap.getHeight(), 1).create();
                        PdfDocument.Page page = pdfDocument.startPage(pi);
                        Canvas canvas = page.getCanvas();
                        Paint paint = new Paint();
                        paint.setColor(getResources().getColor(R.color.white));
                        canvas.drawPaint(paint);
                        canvas.drawBitmap(editBitmap, 0, 0, null);
                        pdfDocument.finishPage(page);
                        try {
                            pdfDocument.writeTo(baos);
                        } catch (IOException e) {
                            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        pdfDocument.close();
                    } else {
                        editBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    }
                    byte[] data = baos.toByteArray();
                    UploadTask uploadTask = storageReference.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Log.i("Save","Fail");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i("Save","Success");
                        }
                    })
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                ScannerFile scannerFile = new ScannerFile(format_name,format_type,downloadUri.toString(),date_created);
                                PDFScannerAPI.pdfScannerAPI.putFile(token,scannerFile).enqueue(new Callback<ScannerFile>() {
                                    @Override
                                    public void onResponse(Call<ScannerFile> call, Response<ScannerFile> response) {
                                        if (response.code() < 300 && response.code() > 199) {
                                            Toast.makeText(getActivity(), "File has been saved successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                    @Override
                                    public void onFailure(Call<ScannerFile> call, Throwable t) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                Log.i("Save","Fail");
                            }
                        }
                    });
                }
                else {
                    progressDialog.dismiss();
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setMessage("Error\n \nPlease enter File name.");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.show();
            }
        });

        adjustButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (change_filter_check) {
                    adjustBitmap = editBitmap.copy(editBitmap.getConfig(), true);
                    contrastBar.setProgress(50);
                    brightBar.setProgress(50);
                    brightValue = 50;
                    contrastValue = 50;
                    contrastText.setText(String.valueOf(50));
                    brightText.setText(String.valueOf(50));
                    change_filter_check = false;
                }
                adjustText.setTextColor(getResources().getColor(R.color.primary));
                filterText.setTextColor(getResources().getColor(R.color.white));
                filterView.setVisibility(View.GONE);
                adjustView.setVisibility(View.VISIBLE);
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterText.setTextColor(getResources().getColor(R.color.primary));
                adjustText.setTextColor(getResources().getColor(R.color.white));
                adjustView.setVisibility(View.GONE);
                filterView.setVisibility(View.VISIBLE);
            }
        });

        contrastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                contrastText.setText(String.valueOf(progress));
                contrastValue = progress;
                editBitmap = filter.getAdjustBitmap(adjustBitmap,contrastValue,brightValue);
                editImage.setImageBitmap(editBitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        brightBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightText.setText(String.valueOf(progress));
                brightValue = progress;
                editBitmap = filter.getAdjustBitmap(adjustBitmap,contrastValue,brightValue);
                editImage.setImageBitmap(editBitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        return v;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (editBitmap != null) {
            editBitmap.recycle();
            editBitmap = null;
        }

        if (grayBitmap != null) {
            grayBitmap.recycle();
            grayBitmap = null;
        }

        if (bwBitmap != null) {
            bwBitmap.recycle();
            bwBitmap = null;
        }

        if (magicBitmap != null) {
            magicBitmap.recycle();
            magicBitmap = null;
        }

        if (smoothBitmap != null) {
            smoothBitmap.recycle();
            smoothBitmap = null;
        }

        if (adjustBitmap != null) {
            adjustBitmap.recycle();
            adjustBitmap = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (originalBitmap != null) {
            originalBitmap.recycle();
            originalBitmap = null;
        }
        if (filter != null) {
            filter.Recycle();
        }
    }

    private void setSelectedFilter(TextView selectedText) {
        change_filter_check = true;
        if (adjustBitmap!=null) {
            adjustBitmap.recycle();
            adjustBitmap = null;
        }
        originalText.setBackgroundColor(getResources().getColor(R.color.filter));
        grayText.setBackgroundColor(getResources().getColor(R.color.filter));
        magicText.setBackgroundColor(getResources().getColor(R.color.filter));
        bwText.setBackgroundColor(getResources().getColor(R.color.filter));
        smoothText.setBackgroundColor(getResources().getColor(R.color.filter));
        selectedText.setBackgroundColor(getResources().getColor(R.color.primary));
    }

}
