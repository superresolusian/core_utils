package utils;

import ij.IJ;
import ij.gui.Plot;
import ij.measure.CurveFitter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.util.Collections.shuffle;
import static org.apache.commons.lang3.ArrayUtils.toObject;

public class RANSAC {

    public static final int NITERATIONS = 1000;
    public static final double FRACTIONMAYBEINLIERS = 0.4;
    public static final double THRESHOLD = 0.05;
    public static final double FRACTIONCLOSEDATAPOINTS = 0.1;

    public int nIterations = NITERATIONS;
    public double fractionMaybeInliers = FRACTIONMAYBEINLIERS;
    public double threshold = THRESHOLD;
    public double fractionCloseDataPoints = FRACTIONCLOSEDATAPOINTS;
    public final double[] x, y, weights;
    public double xMin, xMax;
    public final int nPoints;

    public double[] bestModel = null, vanillaModel, weightedModel;
    public double bestError = Double.MAX_VALUE;

    Random random = new Random();

    public RANSAC(double[] x, double[] y){
        this.x = x;
        this.y = y;
        this.nPoints = x.length;
        this.weights = new double[nPoints];
        for(int i=0; i<nPoints; i++) weights[i] = 1.0;
    }

    public RANSAC(double[] x, double[] y, double[] weights){
        this.x = x;
        this.y = y;
        this.nPoints = x.length;
        this.weights = weights;
    }

    public void setRANSACParameters(int nIterations, double fractionMaybeInliers, double threshold, double fractionCloseDataPoints){
        this.nIterations = nIterations;
        this.fractionMaybeInliers = fractionMaybeInliers;
        this.threshold = threshold;
        this.fractionCloseDataPoints = fractionCloseDataPoints;
    }

    public void doNonRANSACFits(){
        xMin = Collections.min(Arrays.asList(toObject(x)));
        xMax = Collections.max(Arrays.asList(toObject(x)));
        double diff = xMax-xMin;

        double[] xSample = new double[1000];
        for(int i=0; i<1000; i++) xSample[i] = xMin + i*(diff/1000);

        CurveFitter cf = new CurveFitter(x, y);
        cf.doFit(CurveFitter.STRAIGHT_LINE);
        vanillaModel = cf.getParams();

        WeightedLeastSquaresFitter wlsf = new WeightedLeastSquaresFitter(x, y, weights);
        weightedModel = wlsf.getFitParamsAndError();

    }

    public void plotResults(boolean plotCF, boolean plotWLSF, boolean plotRANSAC, boolean plotErrorBars){
        if(bestModel==null) doRANSAC();

        xMin = Collections.min(Arrays.asList(toObject(x)));
        xMax = Collections.max(Arrays.asList(toObject(x)));
        double[] errorbars = new double[weights.length];
        for(int i=0; i<weights.length; i++) errorbars[i] = 1.0/sqrt(weights[i]);

        double diff = xMax-xMin;

        double[] xSample = new double[1000];
        for(int i=0; i<1000; i++) xSample[i] = xMin + i*(diff/1000);

        CurveFitter cf = new CurveFitter(x, y);
        cf.doFit(CurveFitter.STRAIGHT_LINE);
        double[] vanillaModel = cf.getParams();

        double[] yVanilla = new double[1000];
        for(int i=0; i<1000; i++){
            yVanilla[i] = xSample[i]*vanillaModel[1] + vanillaModel[0];
        }


//        WeightedLeastSquaresFitter wlsf = new WeightedLeastSquaresFitter(x, y, weights);
//        double[] weightedModel = wlsf.getFitParamsAndError();
//        double[] yWeighted = new double[1000];
//        for(int i=0; i<1000; i++){
//            yWeighted[i] = xSample[i]*weightedModel[1] + weightedModel[0];
//        }
        double[] yWeighted = new double[1];


        double[] yRANSAC = new double[1000];
        for(int i=0; i<1000; i++){
            yRANSAC[i] = xSample[i]*bestModel[1] + bestModel[0];
        }

        Plot plot = new Plot("RANSAC fitting", "x", "y");
        plot.addPoints(x, y, Plot.CIRCLE);
        if(plotErrorBars) plot.addErrorBars(errorbars);

        if(plotCF) {
            plot.setColor(Color.red);
            plot.addPoints(xSample, yVanilla, Plot.LINE);
            //IJ.log("vanilla params = "+vanillaModel[1]+", "+vanillaModel[0]);
        }
        if(plotWLSF) {
            plot.setColor(Color.green);
            plot.addPoints(xSample, yWeighted, Plot.LINE);
            //IJ.log("weighted params = "+weightedModel[1]+", "+weightedModel[0]);
        }
        if(plotRANSAC) {
            plot.setColor(Color.blue);
            plot.addPoints(xSample, yRANSAC, Plot.LINE);
        }
        plot.setColor(Color.black);

        if(plotCF && plotWLSF && plotRANSAC) plot.addLegend("Data\tLeast-squares\tWeighted least-squares\tRANSAC");
        if(plotCF && plotWLSF && !plotRANSAC) plot.addLegend("Data\tLeast-squares\tWeighted least-squares");
        if(plotCF && !plotWLSF && plotRANSAC) plot.addLegend("Data\tLeast-squares\tRANSAC");
        if(!plotCF && plotWLSF && plotRANSAC) plot.addLegend("Data\tWeighted least-squares\tRANSAC");
        if(plotCF && !plotWLSF && !plotRANSAC) plot.addLegend("Data\tLeast-squares");
        if(!plotCF && plotWLSF && !plotRANSAC) plot.addLegend("Data\tWeighted least-squares");
        if(!plotCF && !plotWLSF && plotRANSAC) plot.addLegend("Data\tRANSAC");

        plot.show();

        //IJ.log("Gain is "+bestModel[1]);
    }

    public void doRANSAC(){
        int iterations = 0;
        while (iterations<nIterations){
            IJ.showProgress(iterations, nIterations);
            IJ.showStatus("RANSAC iteration "+iterations);
            doFit();
            iterations++;
        }
        //doNonRANSACFits();
        if(bestModel==null){
            IJ.log("RANSAC failed - reverting to weighted linear regression");
            CurveFitter cf = new CurveFitter(x, y);
            cf.doFit(CurveFitter.STRAIGHT_LINE);
            bestModel = cf.getParams();
            //bestModel = weightedModel;
        }
    }

    public void doFit(){ //TODO: currently (as of 02 jan) uses error-weighted fitting in RANSAC. don't know if this is legit or not

        int nMaybeInliers = (int)(nPoints*fractionMaybeInliers);
        ArrayList<int[]> indicesLists = getIndicesMaybeInliers(nMaybeInliers);
        int[] indicesMaybeInliers = indicesLists.get(0);
        int[] indicesMaybeOutliers = indicesLists.get(1);

        int nInliers = indicesMaybeInliers.length;
        int nOutliers = indicesMaybeOutliers.length;

        double[] xMaybeInliers = new double[nInliers];
        double[] yMaybeInliers = new double[nInliers];
        double[] wMaybeInliers = new double[nInliers];
        double[] xMaybeOutliers = new double[nOutliers];
        double[] yMaybeOutliers = new double[nOutliers];
        double[] wMaybeOutliers = new double[nOutliers];

        for(int i=0; i<nInliers; i++){
            xMaybeInliers[i] = x[indicesMaybeInliers[i]];
            yMaybeInliers[i] = y[indicesMaybeInliers[i]];
            wMaybeInliers[i] = weights[indicesMaybeInliers[i]];
        }

        for(int i=0; i<nOutliers; i++){
            xMaybeOutliers[i] = x[indicesMaybeOutliers[i]];
            yMaybeOutliers[i] = y[indicesMaybeOutliers[i]];
            wMaybeOutliers[i] = weights[indicesMaybeOutliers[i]];
        }

//        WeightedLeastSquaresFitter wlsf = new WeightedLeastSquaresFitter(xMaybeInliers, yMaybeInliers, wMaybeInliers);
//        double[] coeffsAndError = wlsf.getFitParamsAndError();
//
        CurveFitter cf = new CurveFitter(xMaybeInliers, yMaybeInliers);
        cf.doFit(CurveFitter.STRAIGHT_LINE);
        double[] maybeModel = cf.getParams();
        //IJ.log("RANSAC maybe: "+cf.getResultString());

        ArrayList<Double> alsoInliersX = new ArrayList<>();
        ArrayList<Double> alsoInliersY = new ArrayList<>();
        ArrayList<Double> alsoInliersWeight = new ArrayList<>();
        for(int i=0; i<nOutliers; i++){
//            double fittedVal = xMaybeOutliers[i]*coeffsAndError[1] + coeffsAndError[0];
            double fittedVal = xMaybeOutliers[i]*maybeModel[1] + maybeModel[0];
            double realVal = yMaybeOutliers[i];
            if(abs(realVal-fittedVal)<threshold*realVal){
                alsoInliersX.add(xMaybeOutliers[i]);
                alsoInliersY.add(yMaybeOutliers[i]);
                alsoInliersWeight.add(wMaybeOutliers[i]);
            }
        }

        int nCloseDataPoints = (int) (fractionCloseDataPoints*nPoints);

        if(alsoInliersX.size()>nCloseDataPoints){
            double[] combinedXInliers = combineListAndArray(alsoInliersX, xMaybeInliers);
            double[] combinedYInliers = combineListAndArray(alsoInliersY, yMaybeInliers);
            double[] combinedWeightInliers = combineListAndArray(alsoInliersWeight, wMaybeInliers);

//            wlsf = new WeightedLeastSquaresFitter(combinedXInliers, combinedYInliers, combinedWeightInliers);
//            double[] betterModelCoeffsAndError = wlsf.getFitParamsAndError();
//            double[] betterModel = new double[]{betterModelCoeffsAndError[0], betterModelCoeffsAndError[1]};
//            double thisError = betterModelCoeffsAndError[2];

            cf = new CurveFitter(combinedXInliers, combinedYInliers);
            cf.doFit(CurveFitter.STRAIGHT_LINE);
            double[] betterModel = cf.getParams();
            double thisError = cf.getSumResidualsSqr();

            if(thisError<bestError){
                bestModel = betterModel;
                bestError = thisError;
            }
        }

    }

    ArrayList<int[]> getIndicesMaybeInliers(int nMaybeInliers){

        ArrayList<Integer> indices = new ArrayList<>();
        for(int n=0; n<nPoints; n++) indices.add(n);
        shuffle(indices);
        int[] indicesMaybeInliers = new int[nMaybeInliers];
        int[] indicesMaybeOutliers = new int[nPoints-nMaybeInliers];
        for(int i=0; i<nMaybeInliers; i++) indicesMaybeInliers[i] = indices.get(i);
        for(int i=nMaybeInliers; i<nPoints; i++) indicesMaybeOutliers[i-nMaybeInliers] = indices.get(i);

        ArrayList<int[]> indicesLists = new ArrayList<>();
        indicesLists.add(indicesMaybeInliers);
        indicesLists.add(indicesMaybeOutliers);

        return indicesLists;
    }

    double[] combineListAndArray(ArrayList<Double> list, double[] array){
        int nList = list.size();
        int nArray = array.length;
        double[] combined = new double[nList+nArray];
        for(int n=0; n<nList; n++) combined[n] = list.get(n);
        for(int n=0; n<nArray; n++) combined[n+nList] = array[n];
        return combined;
    }
}
