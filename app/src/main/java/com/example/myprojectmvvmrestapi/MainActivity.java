package com.example.myprojectmvvmrestapi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myprojectmvvmrestapi.adapter.StudentAdapter;
import com.example.myprojectmvvmrestapi.model.Student;
import com.example.myprojectmvvmrestapi.service.ApiClient;
import com.example.myprojectmvvmrestapi.service.ApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ListView listViewStudents;
    private FloatingActionButton fabAdd;
    private ApiService apiService;
    private List<Student> studentList = new ArrayList<>();
    private StudentAdapter adapter;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        listViewStudents = findViewById(R.id.listViewStudents);
        fabAdd = findViewById(R.id.fabAdd);
        Button btnLogout = findViewById(R.id.btnLogout);
        apiService = ApiClient.getApiService();

        btnLogout.setOnClickListener(v -> logout());

        adapter = new StudentAdapter(this, studentList);
        listViewStudents.setAdapter(adapter);

        loadStudents();

        fabAdd.setOnClickListener(v -> showStudentDialog(null));

        listViewStudents.setOnItemClickListener((parent, view, position, id) -> {
            Student selectedStudent = studentList.get(position);
            showOptionDialog(selectedStudent);
        });
    }

    private void loadStudents() {
        apiService.getAllStudents().enqueue(new Callback<List<Student>>() {
            @Override
            public void onResponse(Call<List<Student>> call, Response<List<Student>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    studentList.clear();
                    studentList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Student>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error Network: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOptionDialog(Student student) {
        String[] options = {"Ubah Data", "Hapus Data"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Aksi (" + student.getName() + ")");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showStudentDialog(student);
            } else if (which == 1) {
                showDeleteConfirmation(student);
            }
        });
        builder.show();
    }

    private void showStudentDialog(Student studentToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(studentToEdit == null ? "Tambah Student Baru" : "Ubah Data Student");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_student, null);
        EditText etNis = view.findViewById(R.id.etNis);
        EditText etName = view.findViewById(R.id.etName);
        EditText etAddress = view.findViewById(R.id.etAddress);

        if (studentToEdit != null) {
            etNis.setText(studentToEdit.getNis());
            etName.setText(studentToEdit.getName());
            etAddress.setText(studentToEdit.getAddress());
        }

        builder.setView(view);
        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String nis = etNis.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (nis.isEmpty() || name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            Student studentData = new Student(nis, name, address);

            if (studentToEdit == null) {
                createStudentApi(studentData);
            } else {
                updateStudentApi(studentToEdit.getId(), studentData);
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void createStudentApi(Student student) {
        apiService.createStudent(student).enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Berhasil menambah data", Toast.LENGTH_SHORT).show();
                    loadStudents();
                } else {
                    Toast.makeText(MainActivity.this, "Gagal menambah data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Gagal Tambah: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStudentApi(int id, Student student) {
        apiService.updateStudent(id, student).enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Berhasil mengupdate data", Toast.LENGTH_SHORT).show();
                    loadStudents();
                } else {
                    Toast.makeText(MainActivity.this, "Gagal mengupdate data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Gagal Update: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus " + student.getName() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteStudentApi(student.getId()))
                .setNegativeButton("Batal", null)
                .show();
    }

    // PERBAIKAN: Method ini sebelumnya error karena ada Intent/SharedPreferences di dalam parameter
    private void deleteStudentApi(int id) {
        String token = preferences.getString("token", null);

        // PERBAIKAN: Jika API delete butuh Header, pastikan format Bearer-nya benar.
        // Jika di ApiService menggunakan @Header("Authorization"), kirim "Bearer " + token
        apiService.deleteStudent(id, "Bearer " + token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                    loadStudents();
                } else if (response.code() == 401) {
                    Toast.makeText(MainActivity.this, "Sesi habis, silahkan login ulang", Toast.LENGTH_SHORT).show();
                    goToLogin();
                } else {
                    Toast.makeText(MainActivity.this, "Gagal menghapus data. Kode: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Gagal Hapus: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        String token = preferences.getString("token", null);

        apiService.logout("Bearer " + token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Apapun hasilnya dari server, kita tetap hapus sesi lokal
                preferences.edit().remove("token").apply();
                Toast.makeText(MainActivity.this, "Logout Berhasil", Toast.LENGTH_SHORT).show();
                goToLogin();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Error Network saat logout", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}