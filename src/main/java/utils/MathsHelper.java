package utils;

import ij.gui.Plot;
import ij.process.FloatProcessor;
import org.apache.commons.math3.stat.StatUtils;

import java.util.ArrayList;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.apache.commons.math3.stat.StatUtils.mean;
import static org.apache.commons.math3.stat.StatUtils.percentile;
import static org.apache.commons.math3.stat.StatUtils.variance;
import static utils.ArrayUtils.toDoubleArray;
import static utils.StackHelper.getPixelsAsDoubleArray;

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

    public static double[] getBins(double[] array, double p1, double p2, int nBins){
        double percentile1 = percentile(array, p1);
        double percentile2 = percentile(array, p2);
        double[] bins = new double[nBins];
        for (int i = 0; i < nBins; i++) bins[i] = percentile1 + i * (percentile2 - percentile1) / nBins;
        return bins;
    }

    public static double[] getBins(FloatProcessor fpMean, double p1, double p2, int nBins){
        double[] pixelsMean = getPixelsAsDoubleArray(fpMean);
        double percentile1 = percentile(pixelsMean, p1);
        double percentile2 = percentile(pixelsMean, p2);
        double[] bins = new double[nBins];
        for (int i = 0; i < nBins; i++) bins[i] = percentile1 + i * (percentile2 - percentile1) / nBins;
        return bins;
    }

    public static ArrayList<double[]> getHistogramPercentiles(FloatProcessor fpMean, FloatProcessor fpVariance,
                                                              double p1, double p2, boolean doPlot){
        //image histogram
        double[] pixelsVar = getPixelsAsDoubleArray(fpVariance);
        double[] pixelsMean = getPixelsAsDoubleArray(fpMean);

        if(doPlot){
            Plot plot = new Plot("Mean vs Var (all pixels)", "Mean", "Var");
            plot.addPoints(pixelsMean, pixelsVar, Plot.DOT);
            plot.show();
        }

        int nBins = 100;

        //IJ.log("1st percentile = " + percentile1 + ", 99th percentile = " + percentile99);

        double[] bins = getBins(fpMean, p1, p2, nBins);
        ArrayList<double[]> meanAndStdevVarBinned = binnedStatistic(pixelsMean, pixelsVar, bins);
        double[] varBinned = meanAndStdevVarBinned.get(0);
        double[] varBinnedError = meanAndStdevVarBinned.get(1);

        double[] meanBinned = new double[nBins - 2];
        double[] nPixelsPerBin = new double[nBins - 2];
        for (int i = 0; i < nBins - 2; i++){
            meanBinned[i] = 0.5 * (bins[i] + bins[i + 1]);

            double minVal = bins[i];
            double maxVal = bins[i+1];

            for(int p=0; p<pixelsMean.length; p++){
                double imgMeanVal = pixelsMean[p];
                if(imgMeanVal>=minVal && imgMeanVal<maxVal) nPixelsPerBin[i]++;
            }

        }

        ArrayList<double[]> list = new ArrayList<>();
        list.add(meanBinned);
        list.add(varBinned);
        list.add(nPixelsPerBin);
        list.add(varBinnedError);

        return list;
    }

    public static ArrayList<double[]> binnedStatistic(double[] imgMean, double[] varEstimate, double[] bins){
        int nBins = bins.length;
        int nPixels = imgMean.length;

        double[] statistic = new double[nBins-2];
        double[] statisticError = new double[nBins-2];


        for(int i=0; i<nBins-2; i++){
            double minVal = bins[i];
            double maxVal = bins[i+1];
            ArrayList<Double> valsForBin = new ArrayList<>();

            for(int p=0; p<nPixels; p++){
                double imgMeanVal = imgMean[p];
                if(imgMeanVal>=minVal && imgMeanVal<maxVal)valsForBin.add(varEstimate[p]);
            }

            double[] varValsForBin = toDoubleArray(valsForBin);
            statistic[i] = mean(varValsForBin);
            statisticError[i] = sqrt(variance(varValsForBin));
        }

        //return statistic;
        ArrayList<double[]> statisticAndError = new ArrayList<>();
        statisticAndError.add(statistic);
        statisticAndError.add(statisticError);
        return statisticAndError;
    }

    public static ArrayList<double[]> getHistogramPercentilesMean(FloatProcessor fpX, FloatProcessor fpY,
                                                                  double p1, double p2, int nBins){
        //image histogram
        double[] pixelsX = getPixelsAsDoubleArray(fpX);
        double[] pixelsY= getPixelsAsDoubleArray(fpY);

        double[] bins = getBins(fpX, p1, p2, nBins);

        ArrayList<double[]> binnedStatistic = binnedStatistic(pixelsX, pixelsY, bins);

        double[] yBinned = binnedStatistic.get(0);
        double[] yBinnedError = binnedStatistic.get(1);

        double[] xBinned = new double[nBins - 2];
        for (int i = 0; i < nBins - 2; i++) xBinned[i] = 0.5 * (bins[i] + bins[i + 1]);

        ArrayList<double[]> list = new ArrayList<>();
        list.add(xBinned);
        list.add(yBinned);
        list.add(yBinnedError);

        return list;
    }
}
