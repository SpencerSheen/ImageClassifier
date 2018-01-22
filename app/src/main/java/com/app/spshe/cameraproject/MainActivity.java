package com.app.spshe.cameraproject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static android.graphics.Color.argb;

/**
 * Created by spshe on 7/2/2017.
 */

public class MainActivity extends AppCompatActivity {

    double width = 0;
    double height = 0;
    Button start;
    ImageView takenPhoto;
    TextView description;
    Button change;
    Button info;
    Button returnB;
    private static final int CAM_REQUEST = 1313;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    static int TAKE_PIC = -1;
    public static int count = 0;
    CameraDevice myCamera;
    private Uri mImageUri;
    int [] colors;
    int [] redArray;
    int [] greenArray;
    int [] blueArray;
    int [] alphaArray;
    Bitmap rotatedBitmap;

    private static final int INPUT_SIZE = 300;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/optimized_graph.pb";
    private static final String LABEL_FILE =
            "file:///android_asset/retrained_labels.txt";
    private Classifier classifier;

    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    File photo;


    final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        width = d.getWidth();
        height = d.getHeight();
        File newdir = new File(dir);
        newdir.mkdirs();
        classifier = ImageClassifier.create(getAssets(), MODEL_FILE, LABEL_FILE, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME);
        onLoad();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inScaled = false;
//
//        takenPhoto.setImageBitmap(thumbnail);

        String uri = mImageUri.toString();
        Log.e("uri-:", uri);
        Toast.makeText(this, mImageUri.toString(),Toast.LENGTH_LONG).show();



        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
            Matrix matrix = new Matrix();

            matrix.postRotate(90);

            //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap ,width,height,true);

            rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);
            Drawable d = new BitmapDrawable(getResources(), rotatedBitmap);
            //getPixelValues(rotatedBitmap);


            takenPhoto.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //takenPhoto.setImageBitmap(Bitmap.createS);
        //takenPhoto.set
        //takenPhoto.setImage
    }

    public void onLoad() {
        setContentView(R.layout.mainmenu); //load xml file
        start = (Button) findViewById(R.id.picture); //load button to take picture
        change = (Button) findViewById(R.id.change);
        info = (Button) findViewById(R.id.tutorial);
        //mTextureView = (TextureView) findViewById(R.id.textureView);
        //start.setY((float) (height * 0.9));

        takenPhoto = (ImageView) findViewById(R.id.photo); //load imageview
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)(width),(int)(height*0.65)); //format size
        takenPhoto.setLayoutParams(layoutParams);

        description = (TextView) findViewById(R.id.description); //initialize text
        description.setY((int)(height*0.7)); //set text location
        checkPermissions(); //checks for permissions needed

        start.setX((int)(width*0.1)); // set positions of button
        change.setX((int)(width*0.6));
        //info.setLayoutParams (new RelativeLayout.LayoutParams((int)(width*0.075), RelativeLayout.LayoutParams.WRAP_CONTENT));
        //info.setWidth((int)(width*0.075));


        start.setOnClickListener(new View.OnClickListener() { // start button click
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        change.setOnClickListener(new View.OnClickListener() { // start button click
            @Override
            public void onClick(View view) {
                if(rotatedBitmap != null)
                    defineImage(rotatedBitmap);
                //modifyValues();
                //updateImage(rotatedBitmap);
            }
        });
        info.setOnClickListener(new View.OnClickListener() { // start button click
            @Override
            public void onClick(View view) {
                goToInfo();
            }
        });

    }

    public void checkPermissions()
    {
        // Add permission for camera and let user grant the permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION); //opens permission dialog
            return;
        }
    }

    public File getFile()
    {
        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        int day = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);
        String fileName = Integer.toString(year)
                + Integer.toString(month)
                + Integer.toString(day) + "_"
                + Integer.toString(hours)
                + Integer.toString(minutes)
                + Integer.toString(seconds);
        //fileName format: year,month,day_hours,minutes,seconds


        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/Image Classifier Pictures/" + fileName +
                ".jpg");
        return file;
    }


    public void takePicture() {

        Intent intent= new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        mImageUri = Uri.fromFile(getFile());
        //mImageUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", getFile());
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, CAM_REQUEST);

    }

    public void takePic()
    {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAM_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            // close the app
            Toast.makeText(MainActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    public void getPixelValues(Bitmap bitmap)
    {
        colors = new int [bitmap.getWidth() * bitmap.getHeight()];
        redArray = new int [colors.length];
        blueArray = new int [colors.length];
        greenArray = new int [colors.length];

        alphaArray = new int[colors.length];

//        redArray = new int [bitmap.getWidth()][bitmap.getHeight()];
//        blueArray = new int [bitmap.getWidth()][bitmap.getHeight()];
//        greenArray = new int [bitmap.getWidth()][bitmap.getHeight()];
        int i = 0;
        bitmap.getPixels(colors, 0,bitmap.getWidth(),0,0, bitmap.getWidth(), bitmap.getHeight());
        int R,G,B;
        for(int y = 0; y < bitmap.getHeight(); y++)
        {
            for(int x = 0; x < bitmap.getWidth(); x++ )
            {
                //colors[i] = bitmap.getPixel(x,y);
//                redArray[i] = red(colors[i]);
//                greenArray[i] = green(colors[i]);
//                blueArray[i] = blue(colors[i]);


                alphaArray[i] = (colors[i] >> 24) & 0xff;
                redArray[i] = (colors[i] >> 16) & 0xff;
                greenArray[i] = (colors[i] >> 8) & 0xff;
                blueArray[i] = colors[i] & 0xff;
//                colors[i] = (R << 16) | (G << 8) | B;

                i++;
            }

        }

    }

    public void modifyValues()
    {
        if(redArray != null)
        {
            for(int i = 0; i < redArray.length; i++ )
            {
                redArray[i] -= (redArray[i] % 100);
                greenArray[i] -= (greenArray[i] % 100);
                blueArray[i] -= (blueArray[i] % 100);
                colors[i] = argb(alphaArray[i], redArray[i], greenArray[i], blueArray[i]);
            }
        }


    }

    public void setBlackAndWhite()
    {
        for(int i = 0; i < redArray.length; i++ )
        {
            int setValue = (redArray[i] + greenArray[i] + blueArray[i]) / 3;
            redArray[i] = setValue;
            greenArray[i] = setValue;
            blueArray[i] = setValue;
            colors[i] = argb(alphaArray[i], redArray[i], greenArray[i], blueArray[i]);
        }
    }

    public void updateImage(Bitmap bitmap)
    {
        if(bitmap != null)
        {
            bitmap.setPixels(colors, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            takenPhoto.setImageDrawable(d);
        }


    }

    public void defineImage(Bitmap bitmap)
    {
        String labelText = "";
        final List<Classifier.Recognition> results = classifier.recognizeImage(getResizedBitmap(bitmap, 300, 300));
        for(int i = 0; i < results.size(); i++)
        {
            labelText += "Item: " + results.get(i).getTitle()+ " - " +
                    "Confidence: " + results.get(i).getConfidence()*100 + "%" + "\n";
        }
        description.setText(labelText);

    }

    public void goToInfo()
    {
        setContentView(R.layout.instructions);

        returnB = (Button) findViewById(R.id.returnMenu);
//        ImageView step1Image = (ImageView) findViewById(R.id.step1Img);
//        step1Image

        returnB.setOnClickListener(new View.OnClickListener() { // start button click
            @Override
            public void onClick(View view) {
                onLoad();
                Drawable d = new BitmapDrawable(getResources(), rotatedBitmap);
                takenPhoto.setImageDrawable(d);
            }
        });
    }

    public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
    }










}
