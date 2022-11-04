package com.example.videoplayer;

public abstract class Utility {
    public static String convertTime(Long ms) {
        if (ms != null) {
            long duration = ms / 1000;
            long seconds = duration % 60;
            long minutes = (duration / 60) % 60;
            long hours = (duration / (60 * 60)) % 24;
            if(hours > 0) {
                return String.format("%02d:%02d:%02d",hours, minutes, seconds);
            }else{
                return String.format("%02d:%02d",minutes, seconds);
            }
        } else {
            return null;
        }


    }
}
