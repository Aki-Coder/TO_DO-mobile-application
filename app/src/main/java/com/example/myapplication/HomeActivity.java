package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;

import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.DateFormat;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity  {

    private static int RESULT_CODE_LOCATION = 101;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserID;

    private Button seeMap;
    private EditText mapLocation;

    private ProgressDialog loader;

    private String key = "";
    private String task;
    private String desc;
    private String loc;

    TextView pickDate;
    EditText edDate;
    DatePickerDialog.OnDateSetListener setListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        toolbar = findViewById(R.id.homeToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Todo - My task");

        recyclerView = findViewById(R.id.recycleView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        //zbog ovog nece home , cuur use je null
        mUser = mAuth.getCurrentUser();
        onlineUserID = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("task").child(onlineUserID);


        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(action -> {
            addTask();
        });


    }

    private void openMap(View action){

        if(!mapLocation.getText().toString().equals("")){
            String location = mapLocation.getText().toString();

            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + location));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please enter map location", Toast.LENGTH_SHORT).show();
        }

    }


    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_file, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText task = myView.findViewById(R.id.task);
        final EditText description = myView.findViewById(R.id.description);
        Button cancle = myView.findViewById(R.id.cancleButton);
        Button save = myView.findViewById(R.id.saveButton);

        mapLocation = myView.findViewById(R.id.placeMap);

        seeMap = myView.findViewById(R.id.seeMap);
        seeMap.setOnClickListener(this::openMap);

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mTask = task.getText().toString().trim();
                String mDescription = description.getText().toString().trim();
                String id = reference.push().getKey();
                String date = SimpleDateFormat.getDateInstance().format(new Date());
                String location = mapLocation.getText().toString();

                if(TextUtils.isEmpty(mTask)){
                    task.setError("Task required");
                    return;
                }if(TextUtils.isEmpty(mDescription)){
                    description.setError("Description required");
                    return;
                }
                else {
                    loader.setMessage("Adding your data");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    Model model = new Model(mTask,mDescription,id,date, location);
                    reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(HomeActivity.this, "Task has been inserted successfully", Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(HomeActivity.this, "Failed: "+error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();

                            }
                        }
                    });

                        }


                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(reference,Model.class)
                .build();

        FirebaseRecyclerAdapter<Model, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Model model) {
                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDescription(model.getDescription());
                holder.setLocation(model.getLocation());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(position).getKey();
                        task = model.getTask();
                        desc = model.getDescription();
                        loc = model.getLocation();

                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrived_layout,parent,false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setTask(String task){
            TextView taskTextView = mView.findViewById(R.id.taskT);
            taskTextView.setText(task);
        }

        public void setDescription(String desc){
            TextView descTextView = mView.findViewById(R.id.descriptionT);
            descTextView.setText(desc);
        }

        public void setDate(String date){
            TextView dateTextView = mView.findViewById(R.id.textViewDate);
            dateTextView.setText(date);
        }

        public void setLocation(String loc){
            TextView locTextView = mView.findViewById(R.id.location);
            locTextView.setText(loc);
        }

    }

    private void updateTask(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_data,null);
        myDialog.setView(view);

        final AlertDialog dialog = myDialog.create();

        //text koji se menja za task

        final EditText mTask = view.findViewById(R.id.mEditTextTask);
        final EditText mDesc = view.findViewById(R.id.mEditTextDescription);
        final EditText mLoc = view.findViewById(R.id.mEditTextLocation);

        mTask.setText(task);
        mTask.setSelection(task.length());

        mDesc.setText(desc);
        mDesc.setSelection(desc.length());

        mLoc.setText(loc);
        mLoc.setSelection(loc.length());


        //2 button-a

        Button delButton = view.findViewById(R.id.btnDelete);
        Button update = view.findViewById(R.id.btnUpdate);

        //update data
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task = mTask.getText().toString().trim();
                desc = mDesc.getText().toString().trim();
                loc = mLoc.getText().toString().trim();

                String date = DateFormat.getDateInstance().format(new Date());

                //insert date to database
                //key za update posebnog task-a

                Model model = new Model(task, desc, key , date,loc);
                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Task has been updated succesfully", Toast.LENGTH_SHORT).show();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Update failed "+error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //if succesfulll
                dialog.dismiss();
            }
        });

        //if click on delete
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Task deleted sucessfully", Toast.LENGTH_SHORT).show();
                        }else{
                            String err = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Failed to delete task "+err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //za meni

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                Intent intent  = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}