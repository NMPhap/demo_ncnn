package com.example.demo_ncnn;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileNotFoundException;
import com.tencent.squeezencnn.SqueezeNcnn;
public class SqueezeNetClassification extends Activity {

        private static final int SELECT_IMAGE = 1;

        private TextView infoResult;
        private ImageView imageView;
        private Bitmap yourSelectedImage = null;

        private SqueezeNcnn squeezencnn = new SqueezeNcnn();

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);

            boolean ret_init = squeezencnn.Init(getAssets());
            if (!ret_init)
            {
                Log.e("MainActivity", "squeezencnn Init failed");
            }

            infoResult = (TextView) findViewById(R.id.infoResult);
            imageView = (ImageView) findViewById(R.id.imageView);

            Button buttonImage = (Button) findViewById(R.id.buttonImage);
            buttonImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType("image/*");
                    startActivityForResult(i, SELECT_IMAGE);
                }
            });

            Button buttonDetect = (Button) findViewById(R.id.buttonDetect);
            buttonDetect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (yourSelectedImage == null)
                        return;

                    String result = squeezencnn.Detect(yourSelectedImage, false);

                    if (result == null)
                    {
                        infoResult.setText("detect failed");
                    }
                    else
                    {
                        infoResult.setText(result);
                    }
                }
            });

            Button buttonDetectGPU = (Button) findViewById(R.id.buttonDetectGPU);
            buttonDetectGPU.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (yourSelectedImage == null)
                        return;

                    String result = squeezencnn.Detect(yourSelectedImage, true);

                    if (result == null)
                    {
                        infoResult.setText("detect failed");
                    }
                    else
                    {
                        infoResult.setText(result);
                    }
                }
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();

                try
                {
                    if (requestCode == SELECT_IMAGE) {
                        Bitmap bitmap = decodeUri(selectedImage);

                        Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                        // resize to 227x227
                        yourSelectedImage = Bitmap.createScaledBitmap(rgba, 227, 227, false);

                        rgba.recycle();

                        imageView.setImageBitmap(bitmap);
                    }
                }
                catch (FileNotFoundException e)
                {
                    Log.e("MainActivity", "FileNotFoundException");
                    return;
                }
            }
        }

        private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException
        {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 400;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
        }

}
