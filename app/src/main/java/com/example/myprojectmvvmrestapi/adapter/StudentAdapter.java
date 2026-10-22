package com.example.myprojectmvvmrestapi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myprojectmvvmrestapi.R;
import com.example.myprojectmvvmrestapi.model.Student;

import java.util.List;

public class StudentAdapter extends BaseAdapter {

    private Context context;
    private List<Student> studentList;

    public StudentAdapter(Context context, List<Student> studentList) {
        this.context = context;
        this.studentList = studentList;
    }

    @Override
    public int getCount() {
        return studentList.size();
    }

    @Override
    public Object getItem(int position) {
        return studentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return studentList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_student, parent, false);
        }

        TextView tvNis = convertView.findViewById(R.id.tvNis);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvAddress = convertView.findViewById(R.id.tvAddress);

        Student student = studentList.get(position);

        tvNis.setText("NIS : " + student.getNis());
        tvName.setText("Nama : " + student.getName());
        tvAddress.setText("Alamat : " + student.getAddress());

        return convertView;
    }
}