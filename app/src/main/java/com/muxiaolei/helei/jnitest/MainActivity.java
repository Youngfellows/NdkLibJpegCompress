package com.muxiaolei.helei.jnitest;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_size_before;
    private ImageView image_compress_before;
    private TextView tv_size_after;
    private ImageView image_compress_after;
    private Button btn_compress;
    private Button btn_select_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();
    }

    private void initView() {
        tv_size_before = findViewById(R.id.tv_size_before);
        image_compress_before = findViewById(R.id.image_compress_before);
        tv_size_after = findViewById(R.id.tv_size_after);
        image_compress_after = findViewById(R.id.image_compress_after);
        btn_compress = findViewById(R.id.btn_compress);
        btn_select_image = findViewById(R.id.btn_select_image);
    }

    private void setListener() {
        btn_compress.setOnClickListener(this);
        btn_select_image.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_compress :
                compress();
                break;
            case R.id.btn_select_image:
                initPermission();
                break;
            default:
                break;
        }
    }

    private void compress() {
        createFileByDeleteOldFile(dstPath);
        boolean result = CompressUtils.compressBitmap(bitmap, dstPath, 30, true);
        if(result) {
            Bitmap bitmapCompress = BitmapFactory.decodeFile(dstPath);
            image_compress_after.setImageBitmap(bitmapCompress);
            tv_size_after.setText("size=" + getFileLength(new File(dstPath)) + "kb");
        }
    }

    public static final int REQUEST_CODE = 1;
    public static final int REQUEST_CODE_PICK_IMAGE = 10;
    public static final String dstPath = Environment.getExternalStorageDirectory() + File.separator + "jniTest" + File.separator + "dstImage.jpeg";

    private void initPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            choosePhoto();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto();
            } else {
                Toast.makeText(MainActivity.this,"????????????????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void choosePhoto() {
        /**
         * ???????????????????????????
         */
        Intent intent = new Intent(Intent.ACTION_PICK);
        //????????????
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private Bitmap bitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                image_compress_before.setImageBitmap(bitmap);
                tv_size_before.setText("size=" + getFileLength(getFileByUri(uri, MainActivity.this)) + "kb");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param filePath ????????????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean createFileByDeleteOldFile(String filePath) {
        return createFileByDeleteOldFile(getFileByPath(filePath));
    }

    /**
     * ??????????????????????????????
     *
     * @param filePath ????????????
     * @return ??????
     */
    public static File getFileByPath(String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * ????????????????????????null???????????????
     *
     * @param s ??????????????????
     * @return {@code true}: null????????????<br> {@code false}: ??????null???????????????
     */
    public static boolean isSpace(String s) {
        return (s == null || s.trim().length() == 0);
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param file ??????
     * @return {@code true}: ????????????<br>{@code false}: ????????????
     */
    public static boolean createFileByDeleteOldFile(File file) {
        if (file == null) return false;
        // ????????????????????????????????????false
        if (file.exists() && file.isFile() && !file.delete()) return false;
        // ????????????????????????false
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param file ??????
     * @return {@code true}: ?????????????????????<br>{@code false}: ????????????????????????
     */
    public static boolean createOrExistsDir(File file) {
        // ?????????????????????????????????true?????????????????????false???????????????????????????????????????
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * ??????????????????
     *
     * @param file ??????
     * @return ????????????
     */
    public static long getFileLength(File file) {
        if (!isFile(file)) return -1;
        return file.length()/1024;
    }


    /**
     * ?????????????????????
     *
     * @param file ??????
     * @return {@code true}: ???<br>{@code false}: ???
     */
    public static boolean isFile(File file) {
        return isFileExists(file) && file.isFile();
    }

    /**
     * ????????????????????????
     *
     * @param file ??????
     * @return {@code true}: ??????<br>{@code false}: ?????????
     */
    public static boolean isFileExists(File file) {
        return file != null && file.exists();
    }

    public static File getFileByUri(Uri uri,Context context) {
        String path = null;
        if ("file".equals(uri.getScheme())) {
            path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA }, buff.toString(), null, null);
                int index = 0;
                int dataIdx = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                    dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    path = cur.getString(dataIdx);
                }
                cur.close();
                if (index == 0) {
                } else {
                    Uri u = Uri.parse("content://media/external/images/media/" + index);
                    System.out.println("temp uri is :" + u);
                }
            }
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(uri.getScheme())) {
            // 4.2.2??????
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
            }
            cursor.close();

            return new File(path);
        } else {
            //Log.i(TAG, "Uri Scheme:" + uri.getScheme());
        }
        return null;
    }
}
