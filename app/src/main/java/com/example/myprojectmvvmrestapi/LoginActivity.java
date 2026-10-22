package com.example.myprojectmvvmrestapi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myprojectmvvmrestapi.model.AuthRequest;
import com.example.myprojectmvvmrestapi.model.AuthResponse;
import com.example.myprojectmvvmrestapi.service.ApiClient;
import com.example.myprojectmvvmrestapi.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_TOKEN = "token";

    private ApiService apiService;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi SharedPreferences
        preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Auto-login check
        if (preferences.contains(KEY_TOKEN)) {
            goToMainActivity();
            return;
        }

        apiService = ApiClient.getApiService();
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(view -> attemptLogin());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString(); // Password jangan di-trim

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email atau password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Loading...");

        AuthRequest authRequest = new AuthRequest(email, password);

        apiService.login(authRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();

                    if (token != null && !token.isEmpty()) {
                        createSession(token);
                        goToMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, "Token tidak valid dari server", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Email atau password salah", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable throwable) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                Log.e(TAG, "Network error during login", throwable);
                Toast.makeText(LoginActivity.this, "Koneksi bermasalah. Coba lagi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createSession(String rawToken) {
        // Simpan token asli (tanpa "Bearer ")
        preferences.edit().putString(KEY_TOKEN, rawToken).apply();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}