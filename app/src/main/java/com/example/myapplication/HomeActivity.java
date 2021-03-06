package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.DateFormat;


import java.text.SimpleDateFormat;

import java.util.Date;

public class HomeActivity extends AppCompatActivity  {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserID;

    Button cancle,save;


    private ProgressDialog loader;

    private String key = "";
    private String task;
    private String desc;

    private float rating;

    RatingBar ratingBar;
    private Button btSubmit;

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
        mUser = mAuth.getCurrentUser();
        onlineUserID = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("task").child(onlineUserID);


        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });


    }



    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        //za instanciranje izgleda xml datoteke
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_file, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText task = myView.findViewById(R.id.task);
        final EditText description = myView.findViewById(R.id.description);
        cancle = myView.findViewById(R.id.cancleButton);
        save = myView.findViewById(R.id.saveButton);

        ratingBar = myView.findViewById(R.id.rating_bar);

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            Toast.makeText(this, rating + " vrednost", Toast.LENGTH_LONG).show();
        });

        Button uploadPic = myView.findViewById(R.id.uploadPicture);
        uploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, UploadPictureActivity.class);
                startActivity(i);
            }
        });
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
                float rating = ratingBar.getRating();

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

                    Model model = new Model(mTask,mDescription,id,date, rating);
                    reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                System.out.println("Task je uspesan");
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

        //veze upit za reciycle view
        FirebaseRecyclerAdapter<Model, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Model model) {
                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDescription(model.getDescription());
                holder.setRating(model.getRating());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(position).getKey();
                        task = model.getTask();
                        desc = model.getDescription();
                        rating = model.getRating();
                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                //instanciranje xml datoteke
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrived_layout,parent,false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

                                                    //opisuje prikaz stavke i mp o njenom mestu u rv
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

        public void setRating(float rating){
            RatingBar rb = mView.findViewById(R.id.rating_bar);
            rb.setEnabled(false);
            rb.setRating(rating);
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
        RatingBar rB = view.findViewById(R.id.rating_bar);

        mTask.setText(task);
        mTask.setSelection(task.length());

        mDesc.setText(desc);
        mDesc.setSelection(desc.length());

        rB.setRating(rating);
        rB.setSelected(true);


        //2 button-a

        Button delButton = view.findViewById(R.id.btnDelete);
        Button update = view.findViewById(R.id.btnUpdate);

        //update data
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task = mTask.getText().toString().trim();
                desc = mDesc.getText().toString().trim();
                rating = ratingBar.getRating();
                String date = DateFormat.getDateInstance().format(new Date());

                //insert date to database
                //key za update posebnog task-a

                Model model = new Model(task, desc, key , date, rating);
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
                break;
            case R.id.maps:
                Intent i = new Intent(HomeActivity.this, CurrentLocation.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}