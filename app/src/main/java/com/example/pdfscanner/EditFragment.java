package com.example.pdfscanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.pdfscanner.database.ScannerFile;
import com.example.pdfscanner.database.ScannerFileLab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class EditFragment extends Fragment {
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
    private ScannerFileLab scannerFileLab;
    private boolean change_filter_check = true;


    public static EditFragment newInstance() {
        EditFragment fragment = new EditFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerFileLab = ScannerFileLab.get(getActivity());
        formSingleton = FormSingleton.get(getActivity());
        cropBitmap = formSingleton.getForm().getCropBitmap();
        originalBitmap = cropBitmap.copy(cropBitmap.getConfig(), false);
        filter = new Filter(originalBitmap);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit,container,false);
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
        checkDialog.setCancelable(false);
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

        editBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        setSelectedFilter(originalText);
        editImage.setImageBitmap(editBitmap);

        deleteDialog = new Dialog(getActivity());
        deleteDialog.setContentView(R.layout.dialog_delete);
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteDialog.setCancelable(false);
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
                Intent intent = new Intent(getActivity(),MainActivity.class);
                startActivity(intent);
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
                Objects.requireNonNull(getActivity()).onBackPressed();
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
                format_name = save_filename.getText().toString().trim();
                if (!format_name.equals("") && scannerFileLab.checkTitle(format_name)) {
                    checkDialog.dismiss();
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
                        File file = new File(getActivity().getExternalFilesDir("PDFScanner"), format_name+"."+format_type);
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            pdfDocument.writeTo(fileOutputStream);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            Toast.makeText(getActivity(), "File has been saved successfully", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        pdfDocument.close();
                    } else {
                        File file = new File(getActivity().getExternalFilesDir("PDFScanner"), format_name+"."+format_type);
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            editBitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            Toast.makeText(getActivity(), "File has been saved successfully", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                    ScannerFile scannerFile = new ScannerFile();
                    scannerFile.setTitle(format_name);
                    scannerFile.setType(format_type);
                    scannerFileLab.addScannerFile(scannerFile);
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
                else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    if (format_name.equals("")) {
                        builder1.setMessage("Error\n \nFile name must not be NULL.");
                    } else {
                        builder1.setMessage("Error\n \nFile name already exist.");
                    }
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
