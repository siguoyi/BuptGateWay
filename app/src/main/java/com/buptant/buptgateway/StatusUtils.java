package com.buptant.buptgateway;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenlei on 2016/10/14.
 */
public class StatusUtils {
    public static String getTitle(final String s){
        String regex;
        String title = "";
        final List<String> list = new ArrayList<String>();
        regex = "<title>.*?</title>";
        final Pattern pa = Pattern.compile(regex, Pattern.CANON_EQ);
        final Matcher ma = pa.matcher(s);
        while (ma.find()){
            list.add(ma.group());
        }
        for (int i = 0; i < list.size(); i++){
            title = title + list.get(i);
        }
        return outTag(title);
    }

    public static String outTag(final String s){
        return s.replaceAll("<.*?>", "");
    }
}
