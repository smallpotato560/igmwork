package com.apps.igmwork.framework.ui.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.apps.igmwork.framework.android.AndroidUtils;
import com.bcfbaselibrary.internal.Logger;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import java.io.File;

/**
 * Created by Ben on 2017/11/15.
 */

public class DraweeViewHelper {
    public static  void SetImage(String address,SimpleDraweeView imgView)
    {
        Uri uri = Uri.parse(address);
        imgView.setImageURI(uri);
    }

    public static  void SetImage(String address,SimpleDraweeView imgView,ControllerListener controllerListener)
    {
        Uri uri = Uri.parse(address);

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(controllerListener)
                .setUri(uri).build();

        imgView.setController(controller);

        //imgView.setImageURI(uri);
    }


    public static void SetResourceImage(Context context, SimpleDraweeView imgView, int resID)
    {
        //Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
        //        context.getResources().getResourcePackageName(resID) + '/' +
        //        context.getResources().getResourceTypeName(resID) + '/' +
        //        context.getResources().getResourceEntryName(resID) );

        Uri uri = Uri.parse("res:///" + resID);
        imgView.setImageURI(uri);
    }

    public static void SetResizeResourceImage(Context context,int width,int height,int resID,SimpleDraweeView imgView,ControllerListener controllerListener)
    {
        Uri uri = Uri.parse("res:///"+resID);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setAutoRotateEnabled(true)
                //.setLocalThumbnailPreviewsEnabled(true)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imgView.getController())
                .setImageRequest(request)
                .setControllerListener(controllerListener)
                .build();

        imgView.setController(controller);
    }

    public static void SetResizeResourceImage(Context context,int width,int height,int resID,SimpleDraweeView imgView)
    {
        Uri uri = Uri.parse("res:///"+resID);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setAutoRotateEnabled(true)
                //.setLocalThumbnailPreviewsEnabled(true)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imgView.getController())
                .setImageRequest(request)
                .build();

        imgView.setController(controller);
    }

    public static void SetResizeImage(int width,int height,String address,SimpleDraweeView imgView,ControllerListener controllerListener
            ,Postprocessor postprocessor)
    {
        if(address.startsWith("/"))
        {
            address="file://"+address;
        }
        Uri uri = Uri.parse(address);
        ImageRequest request;
        if(postprocessor!=null) {
            request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setAutoRotateEnabled(true)
                    //.setLocalThumbnailPreviewsEnabled(true)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .setPostprocessor(postprocessor)
                    .build();
        }
        else
        {
            request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setAutoRotateEnabled(true)
                    //.setLocalThumbnailPreviewsEnabled(true)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
        }
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imgView.getController())
                .setImageRequest(request)
                .setControllerListener(controllerListener)
                .build();

        imgView.setController(controller);
    }

    public static void SetResizeImage(int width,int height,String address,SimpleDraweeView imgView)
    {
        if(address.startsWith("/"))
        {
            address="file://"+address;
        }
        Uri uri = Uri.parse(address);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setAutoRotateEnabled(true)
                //.setLocalThumbnailPreviewsEnabled(true)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imgView.getController())
                .setImageRequest(request)
                .build();

        imgView.setController(controller);
    }

    public static void SetGif(final Context context,SimpleDraweeView draweeView, int resourceid){
        SetGif( context, draweeView, true, resourceid);
    }
    public static void SetGif(final Context context,SimpleDraweeView draweeView,boolean autoPlay,int resourceid){
        SetGif( context,draweeView, autoPlay, "res://"+context.getPackageName()+"/"+resourceid);
    }

    public static Bitmap GetBitmap(final Context context,String uriString)
    {
        ImageRequest imageRequest=ImageRequest.fromUri(uriString);
        if(imageRequest!=null) {
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(imageRequest, context);
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                File file = ((FileBinaryResource) resource).getFile();

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                return bitmap;
            }
        }
        return null;
    }
    public static void SetGif(final Context context,final SimpleDraweeView draweeView,boolean autoPlay,String uri){
       /* ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(uri))
                .setProgressiveRenderingEnabled(true)
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest,context);
        dataSource.subscribe(new BaseBitmapDataSubscriber() {

            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                // You can use the bitmap in only limited ways
                // No need to do any cleanup.
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                // No cleanup required here.
            }
        }, CallerThreadExecutor.getInstance());
*/

        final ViewGroup.LayoutParams layoutParams = draweeView.getLayoutParams();
        if(ScreenWidth==0) {
            ScreenWidth = AndroidUtils.GetScreenWidth(context);
        }
        PipelineDraweeController controller =(PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                //.setOldController(draweeView.getController())
                .setAutoPlayAnimations(autoPlay) //自动播放gif动画
                .setControllerListener(new ControllerListener<ImageInfo>() {
                    @Override
                    public void onSubmit(String id, Object callerContext) {

                    }

                    @Override
                    public void onFinalImageSet(String id,  ImageInfo imageInfo,  Animatable animatable) {

                        if( layoutParams.width!=(ScreenWidth/4)) {
                            layoutParams.width = ScreenWidth / 4;
                            layoutParams.height = ScreenWidth / 4;
                            draweeView.setLayoutParams(layoutParams);
                        }
                     }

                    @Override
                    public void onIntermediateImageSet(String id,  ImageInfo imageInfo) {

                    }

                    @Override
                    public void onIntermediateImageFailed(String id, Throwable throwable) {

                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {

                    }

                    @Override
                    public void onRelease(String id) {

                    }
                })
                .build();
        draweeView.setController(controller);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(0f);

        draweeView.getHierarchy().setRoundingParams(roundingParams);
    }

    protected static int ScreenWidth=0;
    public static void SetChatImage(Context context,final SimpleDraweeView simpleDraweeView, String imagePath) {
        final ViewGroup.LayoutParams layoutParams = simpleDraweeView.getLayoutParams();

        if(ScreenWidth==0) {
            ScreenWidth = AndroidUtils.GetScreenWidth(context);
            Logger.E("SetChatImage","ScreenWidth:"+ScreenWidth);
        }
        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                if (imageInfo == null) {
                    return;
                }
                int height = imageInfo.getHeight();
                int width = imageInfo.getWidth();

                if(width>=height)
                {
                    layoutParams.width = ScreenWidth/2;
                    layoutParams.height = (int) ((float) (layoutParams.width * height) / (float) width);
                }
                else
                {
                    layoutParams.width =(int)( (float)ScreenWidth*((float)4/(float)9));
                    layoutParams.height =(int) ((float) (layoutParams.width * height) / (float) width);
                }

                simpleDraweeView.setLayoutParams(layoutParams);
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {

            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                throwable.printStackTrace();
            }
        };

        if(imagePath.startsWith("/"))
        {
            imagePath="file://"+imagePath;
        }
        DraweeController controller = Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener).setUri(Uri.parse(imagePath)).build();
        simpleDraweeView.setController(controller);

        RoundingParams roundingParams = RoundingParams.fromCornersRadius(15f);
        //roundingParams.setBorder(R.color.colorAccent, 1.0);
        //roundingParams.setRoundAsCircle(true);
        //roundingParams.setCornersRadii(15,15,15,15);
        simpleDraweeView.getHierarchy().setRoundingParams(roundingParams);
    }

    public static void SetFullScreenImage(Context context,final SimpleDraweeView simpleDraweeView, String imagePath) {
        final ViewGroup.LayoutParams layoutParams = simpleDraweeView.getLayoutParams();

        if(ScreenWidth==0) {
            ScreenWidth = AndroidUtils.GetScreenWidth(context);
            Logger.E("SetChatImage","ScreenWidth:"+ScreenWidth);
        }
        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                Logger.E("onFinalImageSet","ID:"+id);
                if (imageInfo == null) {
                    return;
                }
                int height = imageInfo.getHeight();
                int width = imageInfo.getWidth();

                if(width>=height)
                {
                    layoutParams.width = ScreenWidth;
                }
                else
                {
                    layoutParams.width =ScreenWidth;

                }
                layoutParams.height =(int) ((float) (layoutParams.width * height) / (float) width);
                simpleDraweeView.setLayoutParams(layoutParams);
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {

            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        DraweeController controller = Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener).setUri(Uri.parse(imagePath)).build();

        simpleDraweeView.setController(controller);

        // RoundingParams roundingParams = RoundingParams.fromCornersRadius(15f);
        //roundingParams.setBorder(R.color.colorAccent, 1.0);
        //roundingParams.setRoundAsCircle(true);
        //roundingParams.setCornersRadii(15,15,15,15);
        //simpleDraweeView.getHierarchy().setRoundingParams(roundingParams);
    }

    public static void SetImageWithDownloadListener(Context context,final SimpleDraweeView simpleDraweeView, String imagePath,
                                                    final boolean isFullScreenImage, final boolean isSupportRetry,final boolean isSupportProgressBar,
                                                    final OnImageDownloadListener downloadListener) {
        final ViewGroup.LayoutParams layoutParams = simpleDraweeView.getLayoutParams();


        if(ScreenWidth==0) {
            ScreenWidth = AndroidUtils.GetScreenWidth(context);

        }
        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                if(downloadListener!=null)
                {
                    try {
                        downloadListener.OnImageFinal(id,true);
                    }
                    catch (Exception e)
                    {

                    }
                }
                if (imageInfo == null) {
                    return;
                }

                if(isFullScreenImage) {
                    int height = imageInfo.getHeight();
                    int width = imageInfo.getWidth();

                    if (width >= height) {
                        layoutParams.width = ScreenWidth;
                    } else {
                        layoutParams.width = ScreenWidth;

                    }
                    layoutParams.height = (int) ((float) (layoutParams.width * height) / (float) width);
                    simpleDraweeView.setLayoutParams(layoutParams);
                }
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {

            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                if(downloadListener!=null)
                {
                    try {
                        downloadListener.OnImageFinal(id,false);
                    }
                    catch (Exception e)
                    {

                    }
                }
                //throwable.printStackTrace();
            }
        };
        DraweeController controller = Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener).setUri(Uri.parse(imagePath)).setTapToRetryEnabled(isSupportRetry).build();

        simpleDraweeView.setController(controller);

        // RoundingParams roundingParams = RoundingParams.fromCornersRadius(15f);
        //roundingParams.setBorder(R.color.colorAccent, 1.0);
        //roundingParams.setRoundAsCircle(true);
        //roundingParams.setCornersRadii(15,15,15,15);
        //simpleDraweeView.getHierarchy().setRoundingParams(roundingParams);

        if(isSupportProgressBar)
            simpleDraweeView.getHierarchy().setProgressBarImage(new ProgressBarDrawable());
    }

    //定义外部类别
    public interface OnImageDownloadListener
    {
        public void OnImageFinal(String id, boolean bSucceed);
    }
}