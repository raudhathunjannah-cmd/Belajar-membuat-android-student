package com.example.myprojectmvvmrestapi.service;

import com.example.myprojectmvvmrestapi.model.AuthRequest;
import com.example.myprojectmvvmrestapi.model.AuthResponse;
import com.example.myprojectmvvmrestapi.model.Student;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // GET semua data student
    @GET("api/students")
    Call<List<Student>> getAllStudents();

    // GET satu student berdasarkan ID
    @GET("api/students/{id}")
    Call<Student> getStudent(@Path("id") int id);

    // POST tambah student
    @POST("api/students")
    Call<Student> createStudent(@Body Student student);

    // PUT ubah student
    @PUT("api/students/{id}")
    Call<Student> updateStudent(
            @Path("id") int id,
            @Body Student student
    );

    // DELETE student
    @Headers("Accept: application/json")
    @DELETE("api/students/{id}")
    Call<Void> deleteStudent(
            @Path("id") int id,
            @Header("Authorization") String token
    );

    //Login
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("api/login")
    Call<AuthResponse> login(@Body AuthRequest authRequest);

    @Headers("Accept: application/json")
    @POST("api/logout")
    Call<Void> logout(@Header("Authorization") String token);

}