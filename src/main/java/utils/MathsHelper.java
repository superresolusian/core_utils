package utils;

import ij.process.FloatProcessor;
import org.apache.commons.math3.stat.StatUtils;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class MathsHelper {
    
    // StatUtils has lots of handy functions
    
    public static double getMean(double[] values){
        return StatUtils.mean(values);
//        int n = values.length;
//        double mean = 0;
//        for(int i=0; i<n; i++) mean += values[i]/n;
//        return mean;
    }

    public static float getMean(float[] values){
        // make this faster if it starts to become a bottleneck
        return (float) getMean(ArrayUtils.toDoubleArray(values));        
//        int n = values.length;
//        float mean = 0;
//        for(int i=0; i<n; i++) mean += values[i]/n;
//        return mean;
    }
    
    public static double getStd(double[] values){
        return sqrt(StatUtils.populationVariance(values));        
        // return sqrt(StatUtils.variance(values)); // bias-corrected sample variance
    }

    public static double getStd(double[] values, double mean){
        return sqrt(StatUtils.populationVariance(values, mean));
        // return sqrt(StatUtils.variance(values, mean)); // bias-corrected sample variance        
//        int n = values.length;
//        double std = 0;
//        for(int i=0; i<n; i++) std += pow((values[i]-mean),2)/n;
//        return sqrt(std);
    }

    public static float getMean(FloatProcessor fp){
        float[] values = (float[]) fp.getPixels();
        return getMean(values);
    }

    public static double getDistance(int[] xy1, int[] xy2){
        return sqrt(pow(xy1[0]-xy2[0], 2) + pow(xy1[1]-xy2[1], 2));
    }
}
