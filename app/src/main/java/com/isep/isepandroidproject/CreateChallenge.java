package com.isep.isepandroidproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.annotation.Nullable;

public class CreateChallenge extends AppCompatActivity {

    public static final int CAMERA_ACTION_CODE = 1;
    ImageView imageTaken;
    Button takephoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);
        imageTaken = findViewById(R.id.ivTakenPhoto);
        takephoto = findViewById(R.id.btn_take_photo);


        takephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btnokchallenge = findViewById(R.id.btn_validate_challenge);
                TextView tv =  findViewById(R.id.textView_ML_labels);
                btnokchallenge.setEnabled(false);
                tv.setText("");
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, CAMERA_ACTION_CODE);
                }
                else{
                    Toast.makeText(CreateChallenge.this, "There is no app supporting taking photos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // Multiple object detection in static images
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()  // Optional
                        .build();
        ObjectDetector objectDetector = ObjectDetection.getClient(options);

        if(requestCode == CAMERA_ACTION_CODE && resultCode == RESULT_OK && data != null){
            Bundle bundle = data.getExtras();
            Bitmap finalPhoto = (Bitmap) bundle.get("data");
            imageTaken.setImageBitmap(finalPhoto);
            Uri uri = getImageUri(getBaseContext(), finalPhoto);
            InputImage image = InputImage.fromBitmap(finalPhoto, 0);

            objectDetector.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<DetectedObject>>() {
                                @Override
                                public void onSuccess(List<DetectedObject> detectedObjects) {
                                    Log.i("mlkit", "Succeeded machine learning detection");
                                    if(detectedObjects.isEmpty()){
                                        Log.i("mlkit", "no objects detected");
                                        Toast.makeText(CreateChallenge.this, "No object were detected", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        for (DetectedObject detectedObject : detectedObjects) {

                                            Log.i("mlkit", "One object detcted");
                                            for (DetectedObject.Label label : detectedObject.getLabels()) {
                                                String text = label.getText();

                                                if (text.isEmpty()){
                                                    Log.i("mlkit", "no label");
                                                    Toast.makeText(CreateChallenge.this, "Object detected with not enough precision", Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    Log.i("mlkit", text);
                                                    Button btnokchallenge = findViewById(R.id.btn_validate_challenge);
                                                    TextView tv =  findViewById(R.id.textView_ML_labels);
                                                    tv.append(text);
                                                    btnokchallenge.setEnabled(true);
                                                    btnokchallenge.setVisibility(View.VISIBLE);
                                                    btnokchallenge.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            uploadImage(uri);
                                                            Toast.makeText(CreateChallenge.this, "Challenge created", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                            finish();
                                                        }
                                                    });

                                                }


                                            }
                                        }
                                    }

                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    //
                                    Log.e("mlkit", "Failed machine learning detection");
                                }
                            });
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    private void uploadImage(Uri uri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        storageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).putFile(uri);
    }

    public void Home(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}