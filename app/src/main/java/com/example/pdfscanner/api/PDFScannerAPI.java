package com.example.pdfscanner.api;


import com.example.pdfscanner.model.JWTToken;
import com.example.pdfscanner.model.ScannerFile;
import com.example.pdfscanner.model.User;
import com.example.pdfscanner.model.UserChange;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PDFScannerAPI {
    Gson gson = new GsonBuilder().setDateFormat("HH:mm:ss dd-MM-yyy").create();
    PDFScannerAPI pdfScannerAPI = new Retrofit.Builder()
            .baseUrl("https://pdf-scanner-api.herokuapp.com/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PDFScannerAPI.class);
    @POST("auth/login")
    Call<JWTToken> login(@Body User user);

    @POST("auth/register")
    Call<User> signup(@Body User user);

    @POST("auth/changepassword")
    Call<User> changepassword(@Body UserChange user);

    @GET("files/")
    Call<List<ScannerFile>> getFiles(@Header("Authorization") String jwtToken);

    @POST("files/")
    Call<ScannerFile> putFile(@Header("Authorization") String jwtToken, @Body ScannerFile scannerFile);

    @PUT("files/{id}")
    Call<ScannerFile> updateFile(@Header("Authorization") String jwtToken, @Path("id") int id, @Body ScannerFile scannerFile);

    @DELETE("files/{id}")
    Call<ScannerFile> deleteFile(@Header("Authorization") String jwtToken, @Path("id") int id);
}
