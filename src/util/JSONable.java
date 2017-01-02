package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 * This file gives the type JSONable, which is something that can be converted
 * to a JSONObject. This file also defines utilities for reading and writing
 * JSON data.
 * 
 * @author James Morrow
 *
 */
public interface JSONable {
    public JSONObject toJSON();

    public class Util {
        public static int[] toIntArray(JSONArray j) {
            int[] array = new int[j.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = j.getInt(i);
            }
            return array;
        }

        public static float[] toFloatArray(JSONArray j) {
            float[] array = new float[j.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = j.getFloat(i);
            }
            return array;
        }

        public static double[] toDoubleArray(JSONArray j) {
            double[] array = new double[j.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = j.getDouble(i);
            }
            return array;
        }

        public static boolean[] toBooleanArray(JSONArray j) {
            boolean[] array = new boolean[j.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = j.getBoolean(i);
            }
            return array;
        }

        public static String[] toStringArray(JSONArray j) {
            String[] array = new String[j.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = j.getString(i);
            }
            return array;
        }

        public static ArrayList<Integer> toIntArrayList(JSONArray j) {
            ArrayList<Integer> alist = new ArrayList<Integer>(j.size());
            for (int i = 0; i < j.size(); i++) {
                alist.add(j.getInt(i));
            }
            return alist;
        }

        public static ArrayList<Float> toFloatArrayList(JSONArray j) {
            ArrayList<Float> alist = new ArrayList<Float>(j.size());
            for (int i = 0; i < j.size(); i++) {
                alist.add(j.getFloat(i));
            }
            return alist;
        }

        public static ArrayList<Double> toDoubleArrayList(JSONArray j) {
            ArrayList<Double> alist = new ArrayList<Double>(j.size());
            for (int i = 0; i < j.size(); i++) {
                alist.add(j.getDouble(i));
            }
            return alist;
        }

        public static ArrayList<Boolean> toBooleanArrayList(JSONArray j) {
            ArrayList<Boolean> alist = new ArrayList<Boolean>(j.size());
            for (int i = 0; i < j.size(); i++) {
                alist.add(j.getBoolean(i));
            }
            return alist;
        }

        public static ArrayList<String> toStringArrayList(JSONArray j) {
            ArrayList<String> alist = new ArrayList<String>(j.size());
            for (int i = 0; i < j.size(); i++) {
                alist.add(j.getString(i));
            }
            return alist;
        }

        public static JSONArray jsonify(List<? extends JSONable> data) {
            JSONArray j = new JSONArray();
            for (int i = 0; i < data.size(); i++) {
                JSONable datum = data.get(i);
                j.setJSONObject(i, datum.toJSON());
            }
            return j;
        }

        public static JSONArray jsonify(JSONable[] data) {
            JSONArray j = new JSONArray();
            for (int i = 0; i < data.length; i++) {
                JSONable datum = data[i];
                j.setJSONObject(i, datum.toJSON());
            }
            return j;
        }

        public static JSONArray jsonify(int[] data) {
            JSONArray j = new JSONArray();
            for (int i = 0; i < data.length; i++) {
                j.setInt(i, data[i]);
            }
            return j;
        }

        public static JSONArray jsonify(float[] data) {
            JSONArray j = new JSONArray();
            for (int i = 0; i < data.length; i++) {
                j.setFloat(i, data[i]);
            }
            return j;
        }

        public static JSONArray jsonify(boolean[] data) {
            JSONArray j = new JSONArray();
            for (int i = 0; i < data.length; i++) {
                j.setBoolean(i, data[i]);
            }
            return j;
        }

        public static JSONArray jsonify(double[] data) {
            JSONArray j = new JSONArray();
            for (int i = 0; i < data.length; i++) {
                j.setDouble(i, data[i]);
            }
            return j;
        }

        public static JSONArray jsonify(long[] data) {
            JSONArray j = new JSONArray();
            for (int i = 0; i < data.length; i++) {
                j.setLong(i, data[i]);
            }
            return j;
        }

        public static JSONArray jsonify(String[] data) {
            JSONArray j = new JSONArray();
            for (int i = 0; i < data.length; i++) {
                j.setString(i, data[i]);
            }
            return j;
        }
    }
}