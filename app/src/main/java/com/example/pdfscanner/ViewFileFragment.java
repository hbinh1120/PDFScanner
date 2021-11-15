package com.example.pdfscanner;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.pdfscanner.database.ScannerFile;
import com.example.pdfscanner.database.ScannerFileLab;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public class ViewFileFragment extends Fragment {
    private ScannerFile scannerFile;
    private static final String SCANNER_FILE_ID = "scanner_file_id";

    private PDFView pdfView;
    private ImageView imageView;
    private Button deleteButton, shareButton, editButton, downloadButton, backButton;
    private TextView fileNameText;

    public static ViewFileFragment newIntance(UUID scannerFileID) {
        Bundle args = new Bundle();
        args.putSerializable(SCANNER_FILE_ID, scannerFileID);

        ViewFileFragment fragment = new ViewFileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID scannerFileID = (UUID) getArguments().getSerializable(SCANNER_FILE_ID);
        scannerFile = ScannerFileLab.get(getActivity()).getScannerFile(scannerFileID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_viewfile, container, false);
        pdfView = v.findViewById(R.id.pdf_view);
        imageView = v.findViewById(R.id.image_view);
        deleteButton = v.findViewById(R.id.deleteFileButton);
        shareButton = v.findViewById(R.id.shareButton);
        editButton = v.findViewById(R.id.editNameButton);
        downloadButton = v.findViewById(R.id.downloadButton);
        backButton = v.findViewById(R.id.viewfile_back);
        fileNameText = v.findViewById(R.id.viewfile_name);
        fileNameText.setText(scannerFile.getTitle()+"."+scannerFile.getType());
        File file = new File(getActivity().getExternalFilesDir("PDFScanner"), scannerFile.getTitle()+"."+scannerFile.getType());

        if (!file.exists()) {
            ScannerFileLab.get(getActivity()).removeScannerFile(scannerFile);
            Toast.makeText(getActivity(), "File no longer exists", Toast.LENGTH_SHORT).show();
            MainFragment mainFragment = new MainFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainFragment, "MAIN_FRAGMENT")
                    .commit();
        } else {
            if (scannerFile.getType().equals("pdf")) {
                pdfView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                pdfView.fromFile(file).load();
            } else {
                pdfView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Bitmap imageBitmap = BitmapFactory.decodeFile(file.getPath());
                imageView.setImageBitmap(imageBitmap);
            }
        }

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setTitle("Delete");
                builder1.setMessage("Are you sure to delete permanently from device?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                ScannerFileLab.get(getActivity()).removeScannerFile(scannerFile);
                                Toast.makeText(getActivity(), "File deleted successfully", Toast.LENGTH_SHORT).show();
                                MainFragment mainFragment = new MainFragment();
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, mainFragment, "MAIN_FRAGMENT")
                                        .commit();
                            }
                        });

                builder1.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setTitle("Rename");
                builder1.setCancelable(true);
                final EditText editFileName = new EditText(getActivity());
                editFileName.setHint("Enter new file name");
                builder1.setView(editFileName);
                builder1.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                String newFileName = editFileName.getText().toString().trim();
                                if (!newFileName.equals("") && ScannerFileLab.get(getActivity()).checkTitle(newFileName)) {
                                    File newFile = new File(getActivity().getExternalFilesDir("PDFScanner"), newFileName + "." + scannerFile.getType());
                                    boolean isSuccess = file.renameTo(newFile);
                                    if (isSuccess) {
                                        Toast.makeText(getActivity(), "File renamed successfully", Toast.LENGTH_SHORT).show();
                                        scannerFile.setTitle(newFileName);
                                        fileNameText.setText(newFileName+"."+scannerFile.getType());
                                        ScannerFileLab.get(getActivity()).updateScannerFile(scannerFile);
                                    } else {
                                        Toast.makeText(getActivity(), "Erorr: Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else if (newFileName.equals("")) {
                                    Toast.makeText(getActivity(), "Error: File name must be not NULL", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Error: File name already exist", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                builder1.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                if (scannerFile.getType().equals("pdf")) {
                    i.setType("application/pdf");
                } else {
                    i.setType("image/jpeg");
                }
                i.putExtra(Intent.EXTRA_TEXT, "Sharing File from PDFScanner");
                Uri path = FileProvider.getUriForFile(getActivity(),"com.example.pdfscanner.fileprovider",file);
                i.putExtra(Intent.EXTRA_STREAM, path);
                i = Intent.createChooser(i, "Sharing File from PDFScanner");
                List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getActivity().grantUriPermission(packageName, path, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                startActivity(i);
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mimeType = "application/pdf";
                if (!scannerFile.getType().equals("pdf")) {
                    mimeType = "image/jpeg";
                }
                try {
                    copyFileToDownloads(getActivity(), file, scannerFile.getTitle()+"."+scannerFile.getType(),mimeType);
                    Toast.makeText(getActivity(), "File downloaded successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Error: Something went wrong", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        return v;
    }

    private void copyFileToDownloads(Context context, File downloadFile, String fileName, String mimeType) throws IOException {
        Uri downloadedUri;
        ContentResolver resolver = context.getContentResolver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            contentValues.put(MediaStore.MediaColumns.SIZE, downloadFile.length());
            downloadedUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        } else {
            File destinyFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            downloadedUri = FileProvider.getUriForFile(getActivity(), "com.example.pdfscanner.fileprovider", destinyFile);
        }
        FileOutputStream outputStream = (FileOutputStream) resolver.openOutputStream(downloadedUri);;
        byte[] brr = new byte[1024];
        FileInputStream myFile = new FileInputStream(downloadFile.getAbsoluteFile());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(myFile);
        while (true) {
            int inByteValue = bufferedInputStream.read(brr,0,brr.length);
            if (inByteValue>0) {
              outputStream.write(brr,0,inByteValue);
            }
            else {
                break;
            }
        }
        outputStream.flush();
        outputStream.close();
        bufferedInputStream.close();
    }
}
