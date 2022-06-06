//package com.egrobots.grassanalysis.managers;
//
//import android.media.MediaPlayer;
//import android.util.Log;
//import android.widget.VideoView;
//
//public class VideoManager {
//
//    private VideoView videoView;
//    private VideoManagerCallback videoManagerCallback;
//
//    public VideoManager(VideoView videoView, VideoManagerCallback videoManagerCallback) {
//        this.videoView = videoView;
//        this.videoManagerCallback = videoManagerCallback;
//    }
//
//    public void setVideoData(String videoUri) {
//        videoView.setVideoPath(videoUri);
//        videoView.setOnPreparedListener(new OnPrepareListenerImpl());
//        videoView.setOnCompletionListener(mp -> mp.start());
//        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//            @Override
//            public boolean onError(MediaPlayer mp, int what, int extra) {
////                videoManagerCallback.onError("Error while loading the video..");
//                return true;
//            }
//        });
//    }
//
//    class OnPrepareListenerImpl implements MediaPlayer.OnPreparedListener {
//
//        @Override
//        public void onPrepared(MediaPlayer mp) {
//            videoManagerCallback.onPrepare();
//            mp.setVolume(0f, 0f);
//            mp.start();
//
//            float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
//            float screenRatio = videoView.getWidth() / (float) videoView.getHeight();
//            float scale = videoRatio / screenRatio;
//            if (scale >= 1f) {
//                videoView.setScaleX(scale);
//            } else {
//                videoView.setScaleY(1f / scale);
//            }
//        }
//
//    }
//}
