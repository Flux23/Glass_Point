//Ryan Merrill 2015

package com.example.ryan.gldemo;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;

//contains dat on a point, not yet used
public class Point {

    public String mName;
    public float mLat;
    public float mLng;
    public int index;

    Point(String name, float lat, float lng){
        mName = name;
        mLat = lat;
        mLng = lng;
        index = -1;
    }



    static Vector<Point> readFile(Context context){
        Vector<Point> points = new Vector<Point>();
        //InputStream inputStream = new FileInputStream(filename);
        BufferedReader reader = null;//new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder out = new StringBuilder();
        String line = "";
        String cvsSplitBy = ",";

        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.points);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {


                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                Point point = new Point(data[0], Float.parseFloat(data[1]), Float.parseFloat(data[2]));
                points.add(point);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return points;
    }
}
