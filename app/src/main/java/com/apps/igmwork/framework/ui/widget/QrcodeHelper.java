package com.apps.igmwork.framework.ui.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by User on 2018/10/9.
 */

@SuppressLint("Registered")
public class QrcodeHelper extends Activity {

    public Activity activity;
    public static ProgressDialog loadingDialog;
    public static ImageView ImageView;
    public OnQrcodeHelperListener  onQrcodeHelperListener  ;
    private static int IMAGE_HALFWIDTH = 50;//寬度，影響中間圖片大小

    public QrcodeHelper(ImageView imageView, Context context, OnQrcodeHelperListener listener)
    {
        ImageView = imageView;
        onQrcodeHelperListener = listener;
        //設定loading Dialog
        loadingDialog = new ProgressDialog(context);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setCancelable(true);
        loadingDialog.setMessage("loading please wait...");
    }

    public interface OnQrcodeHelperListener
    {
    }

    public static class GetImage extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected void onPreExecute() {
            //執行前 設定可以在這邊設定

            loadingDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //執行中 在背景做事情
            int QRCodeWidth = 600;
            int QRCodeHeight = 600;
            //QRCode內容編碼
            Map hints = new EnumMap(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 0);

            MultiFormatWriter writer = new MultiFormatWriter();
            try {
                //ErrorCorrectionLevel容錯率分四級：L(7%) M(15%) Q(25%) H(30%)
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

                //建立QRCode的資料矩陣
                BitMatrix result = writer.encode(params[0], BarcodeFormat.QR_CODE, QRCodeWidth, QRCodeHeight, hints);

                //建立矩陣圖
                Bitmap bitmap = Bitmap.createBitmap(QRCodeWidth, QRCodeHeight, Bitmap.Config.ARGB_4444);
                for (int y = 0; y < QRCodeHeight; y++) {
                    for (int x = 0; x < QRCodeWidth; x++) {
                        bitmap.setPixel(x, y, result.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }

                return bitmap;

            } catch (WriterException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //執行中 可以在這邊告知使用者進度
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //執行後 完成背景任務
            super.onPostExecute(bitmap);
            ImageView.setImageBitmap(bitmap);
            loadingDialog.dismiss();
        }
    }


    /**
     * 生成二維碼，默認大小為500*500
     *
     * @param text 需要生成二維碼的文字、網址等
     * @return bitmap
     */
    public static void createQRCode(String text) {
        createQRCode(text, 500);
    }

    /**
     * 生成二維碼
     *
     * @param text 需要生成二維碼的文字、網址等
     * @param size 需要生成二維碼的大小（）
     * @return bitmap
     */
    public static void createQRCode(String text, int size) {
        try {
            Map hints = new EnumMap(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, size, size, hints);
            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * size + x] = 0xff000000;
                    } else {
                        pixels[y * size + x] = 0xffffffff;
                    }

                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);

            ImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();

        }
    }

    /**
     * 生成帶logo的二維碼，默認二維碼的大小為500，logo為二維碼的1/5
     *
     * @param text    需要生成二維碼的文字、網址等
     * @param mBitmap logo文件
     * @return bitmap
     */
    public static void createQRCodeWithLogo(String text, Bitmap mBitmap) {
        createQRCodeWithLogo(text, 500, mBitmap);
    }

    /**
     * 生成帶logo的二維碼，logo默認為二維碼的1/5
     *
     * @param text    需要生成二維碼的文字、網址等
     * @param size    需要生成二維碼的大小（）
     * @param mBitmap logo文件
     * @return bitmap
     */
    public static void createQRCodeWithLogo(String text, int size, Bitmap mBitmap) {
        try {
            IMAGE_HALFWIDTH = size / 10;
            Map hints = new EnumMap(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, 0);
            /*
             * 設置容錯級別，默認為ErrorCorrectionLevel.L
             * 因為中間加入logo所以建議你把容錯級別調至H,否則可能會出現識別不了
             */
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();//矩陣高度
            int height = bitMatrix.getHeight();//矩陣寬度
            int halfW = width / 2;
            int halfH = height / 2;

            Matrix m = new Matrix();
            float sx = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getWidth();
            float sy = (float) 2 * IMAGE_HALFWIDTH
                    / mBitmap.getHeight();
            m.setScale(sx, sy);
            //設置縮放資訊
            //將logo圖片按martix設置的資訊縮放
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                    mBitmap.getWidth(), mBitmap.getHeight(), m, false);

            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                            && y > halfH - IMAGE_HALFWIDTH
                            && y < halfH + IMAGE_HALFWIDTH) {
                        //該位置用於存放圖片資訊
                        //記錄圖片每個圖元資訊
                        pixels[y * width + x] = mBitmap.getPixel(x - halfW
                                + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                    } else {
                        if (bitMatrix.get(x, y)) {
                            pixels[y * size + x] = 0xff000000;
                        } else {
                            pixels[y * size + x] = 0xffffffff;
                        }
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(size, size,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            ImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}

