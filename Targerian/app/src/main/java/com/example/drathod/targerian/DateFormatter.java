package com.example.drathod.targerian;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bhavan on 18/2/17.
 */

public class DateFormatter {

        String dateString;
        String givenFormat;
    DateFormatter(){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.dateString =spf.format(date);
        this.givenFormat="yyyy-MM-dd HH:mm:ss";
    }
    DateFormatter(String date) {
        this.dateString =date;
        this.givenFormat="yyyy-MM-dd HH:mm:ss";
    }
    DateFormatter(String date,String givenFormat) {
        this.dateString = date;
        this.givenFormat = givenFormat;
    }
    String format(String ReqFormat){


        SimpleDateFormat parser = new SimpleDateFormat(givenFormat);
        Date date =null;
        try {
            date = parser.parse(this.dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat spf = new SimpleDateFormat(ReqFormat);
        String output = spf.format(date);
        return output;
    }

    Boolean diff_greaterthan_10min(String s1,String s2){
        SimpleDateFormat parser = new SimpleDateFormat(givenFormat);
        Date date1 =null,date2=null;
        try {
            date1 = parser.parse(s1);
            date2 = parser.parse(s2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long different = date2.getTime() - date1.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        if(elapsedDays>0 || elapsedHours >0 || elapsedMinutes > 10){
            return true;
        }else {
            return  false;
        }

    }

}
