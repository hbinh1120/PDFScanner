package com.example.pdfscanner;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdfscanner.database.ScannerFile;
import com.example.pdfscanner.database.ScannerFileLab;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.List;

public class MainFragment extends Fragment implements IOnBackPressed{

    private static final int REQUEST_IMAGE_SELECT = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 0;

    private static final String fileName = "output.jpg";


    private static final String TAG = "MainActivity";

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG,"OpenCV installed successfully");
        }else{
            Log.d(TAG,"Failed");
        }
    }

    private File mFile;
    private RecyclerView filesView;
    private ConstraintLayout emptyView;
    private FileAdapter fileAdapter;
    private SearchView searchView;
    private TextView titleView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main,container,false);

        Button galleryButton = v.findViewById(R.id.galleryButton);
        Button cameraButton = v.findViewById(R.id.cameraButton);
        searchView = v.findViewById(R.id.searchView);
        titleView = v.findViewById(R.id.toolbar_title);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                titleView.setVisibility(View.VISIBLE);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleView.setVisibility(View.GONE);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fileAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fileAdapter.getFilter().filter(newText);
                return false;
            }
        });

        filesView = v.findViewById(R.id.files_view);
        emptyView = v.findViewById(R.id.empty_view);
        filesView.setLayoutManager(new LinearLayoutManager(getActivity()));


        mFile = getPhotoFile();

        final Intent selectImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        galleryButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(selectImage, REQUEST_IMAGE_SELECT);
            }
        });

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        cameraButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.example.pdfscanner.fileprovider",
                        mFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_IMAGE_CAPTURE);
            }
        });

        updateUI();

        return v;
    }

    private void updateUI(){
        ScannerFileLab scannerFileLab = ScannerFileLab.get(getActivity());
        List<ScannerFile> scannerFiles = scannerFileLab.getScannerFiles();
        if (scannerFiles.size()==0) {
            filesView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            filesView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        fileAdapter = new FileAdapter(scannerFiles);
        filesView.setAdapter(fileAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_IMAGE_SELECT) && resultCode == Activity.RESULT_OK) {

            String imgPath;
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imgPath = mFile.getPath();
            } else {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
            }

            if (imgPath !=null) {
                Intent intent = EditActivity.newIntent(getActivity(), imgPath);
                startActivity(intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchView.isIconified()) {
            titleView.setVisibility(View.VISIBLE);

        } else {
            titleView.setVisibility(View.GONE);

        }
    }

    private class FileHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView titleText, dateText;
        private ImageView iconImage;

        private ScannerFile scannerFile;

        public FileHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_files, parent, false));
            itemView.setOnClickListener(this);
            titleText = itemView.findViewById(R.id.file_title);
            dateText = itemView.findViewById(R.id.file_date);
            iconImage = itemView.findViewById(R.id.file_icon);
        }

        public void bind(ScannerFile s) {
            scannerFile = s;
            titleText.setText(scannerFile.getTitle());
            dateText.setText(scannerFile.getDate().toString());
            if (scannerFile.getType().equals("pdf")) {
                iconImage.setImageResource(R.mipmap.ic_pdf_foreground);
            } else {
                iconImage.setImageResource(R.mipmap.ic_image_foreground);
            }
        }

        @Override
        public void onClick(View v) {
            ViewFileFragment viewFileFragment = ViewFileFragment.newIntance(scannerFile.getId());
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, viewFileFragment, "VIEW_FILE_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
        }
    }

    private class FileAdapter extends RecyclerView.Adapter<FileHolder> implements Filterable {
        private List<ScannerFile> mScannerFiles;
        private List<ScannerFile> scannerFiles;


        public FileAdapter(List<ScannerFile> scannerFiles) {
            mScannerFiles = scannerFiles;
            this.scannerFiles = scannerFiles;
        }

        @NonNull
        @Override
        public FileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new FileHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull FileHolder holder, int position) {
            ScannerFile scannerFile = mScannerFiles.get(position);
            holder.bind(scannerFile);
        }

        @Override
        public int getItemCount() {
            return mScannerFiles.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String search = constraint.toString();
                    if (search.isEmpty()) {
                        mScannerFiles = scannerFiles;
                    } else {
                        mScannerFiles = ScannerFileLab.get(getActivity()).searchScannerFiles(search);
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = mScannerFiles;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    mScannerFiles = (List<ScannerFile>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

    public File getPhotoFile() {
        File filesDir = getActivity().getFilesDir();
        return new File(filesDir, fileName);
    }

}
