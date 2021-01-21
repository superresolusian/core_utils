package utils;

import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.process.FloatProcessor;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Random;

import static java.lang.Math.*;
import static utils.ArrayUtils.doSort;
import static utils.StackHelper.getFpMax;
import static utils.StackHelper.getNumPositiveNonZeroPixels;
import static utils.StackHelper.normaliseByMax;

public class SimulationHelper {

    public static FloatProcessor generateDotsImage(FloatProcessor fp, double coverage, boolean randomiseIntensity, int borderPad) {
        Random random = new Random();

        int w = fp.getWidth();
        int h = fp.getHeight();
        int nPixels = fp.getPixelCount();
        int target = (int) (coverage*nPixels);
        int structurePixels = 0;

        fp.setColor(1);

        while (structurePixels < target) {
            int x = random.nextInt(w-2*borderPad)+borderPad;
            int y = random.nextInt(h-2*borderPad)+borderPad;
            if (randomiseIntensity) fp.setf(x, y, random.nextFloat());
            else fp.setf(x, y, 1.0f);
            structurePixels = getNumPositiveNonZeroPixels(fp);
        }

        return fp;
    }

    public static FloatProcessor generateLinesImage(FloatProcessor fp, double coverage, boolean randomiseIntensity, int borderPad){
        Random random = new Random();

        int w = fp.getWidth();
        int h = fp.getHeight();
        int nPixels = fp.getPixelCount();
        int target = (int) (coverage*nPixels);
        int structurePixels = 0;

        fp.setColor(1);

        while(structurePixels<target) {
            int x1 = (int) Math.floor(random.nextDouble() * (w-2*borderPad))+borderPad;
            int x2 = (int) Math.floor(random.nextDouble() * (w-2*borderPad))+borderPad;
            int y1 = (int) Math.floor(random.nextDouble() * (h-2*borderPad))+borderPad;
            int y2 = (int) Math.floor(random.nextDouble() * (h-2*borderPad))+borderPad;

            if(randomiseIntensity) fp.setColor(random.nextDouble());

            fp.drawLine(x1, y1, x2, y2);

            structurePixels = getNumPositiveNonZeroPixels(fp);
        }

        return fp;

    }

    public static FloatProcessor generateOvalsImage(FloatProcessor fp, double coverage, boolean randomiseIntensity, int borderPad){

        Random random = new Random();

        int w = fp.getWidth();
        int h = fp.getHeight();
        int nPixels = fp.getPixelCount();
        int target = (int) (coverage*nPixels);
        int structurePixels = 0;
        int maxRadius = min(20, borderPad);

        fp.setColor(1);

        while(structurePixels<target) {
            if(randomiseIntensity) fp.setColor(random.nextDouble());

            OvalRoi oval = new OvalRoi(random.nextInt(w-2*borderPad)+borderPad, random.nextInt(h-2*borderPad)+borderPad,
                    random.nextInt(maxRadius)+1, random.nextInt(maxRadius)+1);
            fp.setRoi(oval);
            fp.fill(oval);

            structurePixels = getNumPositiveNonZeroPixels(fp);
        }

        return fp;
    }

    public static FloatProcessor generatePolygonsImage(FloatProcessor fp, double coverage, boolean randomiseIntensity, int borderPad){

        Random random = new Random();

        int w = fp.getWidth();
        int h = fp.getHeight();
        int nPixels = fp.getPixelCount();
        int target = (int) (coverage*nPixels);
        int structurePixels = 0;

        fp.setColor(1);

        while(structurePixels<target){
            int x0 = random.nextInt(w-2*borderPad)+borderPad;
            int y0 = random.nextInt(h-2*borderPad)+borderPad;

            int nVertices = (int) Math.floor(random.nextDouble()*9)+3;
            double maxRadius = min(random.nextDouble()*w*0.05, borderPad);

            double[] angles = new double[nVertices];
            for(int i=0; i<nVertices; i++){
                angles[i] = random.nextDouble()*2*PI;
            }
            angles = doSort(angles);
            int[] xVals = new int[nVertices];
            int[] yVals = new int[nVertices];

            for(int i=0; i<nVertices; i++){
                double thisRadius = maxRadius*random.nextDouble();
                double thisAngle = angles[i];

                int y = (int) (thisRadius*cos(thisAngle)) + y0;
                int x = (int) (thisRadius*sin(thisAngle)) + x0;

                xVals[i] = x;
                yVals[i] = y;
            }

            Polygon p = new Polygon(xVals, yVals, nVertices);
            if(randomiseIntensity) fp.setColor(random.nextFloat());
            fp.fillPolygon(p);

            structurePixels = getNumPositiveNonZeroPixels(fp);
        }

        return fp;
    }

    public static FloatProcessor generate1DStructureGT(int width, int height, double targetFraction){

        Random random = new Random();

        FloatProcessor fp = new FloatProcessor(width, height);
        int nPixels = width*height;
        int target = (int) (targetFraction*nPixels);
        int structurePixels = 0;

        fp.setColor(1);

        while(structurePixels<target) {
            int x1 = (int) Math.floor(random.nextDouble() * width);
            int x2 = (int) Math.floor(random.nextDouble() * width);
            int y1 = (int) Math.floor(random.nextDouble() * width);
            int y2 = (int) Math.floor(random.nextDouble() * width);
            fp.drawLine(x1, y1, x2, y2);

            structurePixels = getNumPositiveNonZeroPixels(fp);
        }

        return fp;
    }

    public static FloatProcessor generateImageFromGT(FloatProcessor fpGT, double sigma, int downsample, double maxSignal, double meanBG, double stdBG){
        FloatProcessor fp = (FloatProcessor) fpGT.duplicate();
        fp.blurGaussian(sigma*downsample);
        fp = normaliseByMax(fp);
        fp.multiply(maxSignal-meanBG);
        fp.add(meanBG);

        int w_ = fp.getWidth()/downsample;
        int h_ = fp.getHeight()/downsample;

        fp.noise(stdBG);
        return (FloatProcessor) fp.resize(w_, h_);
    }

    public static FloatProcessor sharpenImage(FloatProcessor fpGT, double sigma, double exponent, double maxIntensity){
        FloatProcessor fp = fpGT.duplicate().convertToFloatProcessor();
        fp.blurGaussian(sigma);
        for(int p=0; p<fp.getPixelCount(); p++){
            float v = fp.getf(p);
            float v_ = (float) pow(v, exponent);
            fp.setf(p, v_);
        }

        //double fpMax = fp.getMax(); //TODO: search all classes for instances of ip.getMax() - this is a bullshit method!!!!!!
        double fpMax = getFpMax(fp);
        double scaleFactor = maxIntensity/fpMax;

        fp.multiply(scaleFactor);

        return fp;
    }

    public static FloatProcessor shadowImage(FloatProcessor fpGT, double sigma, double intensity){
        FloatProcessor fp = fpGT.duplicate().convertToFloatProcessor();
        fp.blurGaussian(sigma);
        double max = getFpMax(fp);
        fp.multiply(1.0/max);
        fp.multiply(-intensity);
        return fp;
    }

    public static FloatProcessor createSharpenedHairpin(int w, int h, int widest, double psfSigma, double mergeSigma, float mergeFactor, int nRepeats){

        Random random = new Random();
        FloatProcessor fpRaw = createHairpin(w, h, widest, psfSigma);

        LinkedHashMap<Integer, int[]> yPairs = getYPairValuesForX(fpRaw);
        Object[] xCoords = yPairs.keySet().toArray();
        int nPairs = xCoords.length;

        FloatProcessor fp = new FloatProcessor(w, h);

        for(int n=1; n<=nRepeats; n++){
            for(int p=0; p<nPairs; p++){
                int x = (int) xCoords[p];
                int[] yPair = yPairs.get(x);

                float dist = max(1,yPair[1]-yPair[0]);
                float weight = mergeFactor/(float) sqrt(dist);

                int c = (yPair[0] + yPair[1])/2;
                int pos = get1dGaussPosition(c, mergeSigma, h);
                float fpVal = fp.getf(x, pos);
                fp.setf(x, pos, fpVal + weight);

                for(int i=0; i<2; i++) {
                    pos = get1dGaussPosition(yPair[i], psfSigma, h);
                    fpVal = fp.getf(x, pos);
                    fp.setf(x, pos, fpVal + max(0, 1-weight));
                }
            }
        }
        return fp;
    }

    public static FloatProcessor createHairpin(int w, int h, int widest, double psfSigma){
        int xStart = 0 + (int)(5*psfSigma);
        int xEnd = (w-1) - (int)(5*psfSigma);
        int yTopStart = h/2 - widest/2;
        int yBottomStart = h/2 + widest/2;
        int yEnd = h/2;

        FloatProcessor fp = new FloatProcessor(w, h);
        fp.drawLine(xStart, yTopStart, xEnd, yEnd);
        fp.drawLine(xStart, yBottomStart, xEnd, yEnd);
        return fp;
    }

    public static LinkedHashMap<Integer, int[]> getYPairValuesForX(FloatProcessor fp){
        int w = fp.getWidth();
        int h = fp.getHeight();

        LinkedHashMap<Integer, int[]> map = new LinkedHashMap<>();
        for(int x=0; x<w; x++){
            boolean pairExists = false;
            int[] pair = new int[]{-1, -1};
            for(int y=0; y<h; y++){
                float val = fp.getf(x, y);
                if(val>0){
                    if(!pairExists){
                        pairExists = true;
                        pair[0] = y;
                    }
                    else pair[1] = y;
                }
            }
            if(!pairExists) continue;
            if(pair[1]==-1) pair[1] = pair[0];
            map.put(x, pair);
        }
        return map;
    }

    public static int get1dGaussPosition(int c, double sigma, int lim){
        Random random = new Random();
        double pos = random.nextGaussian()*sigma + c;
        if(pos<0 || pos>=lim-1) return c;
        return (int) pos;
    }

    public static void main(String[] args){
        ImageJ IJ = new ImageJ();

        int w=500, h=500, widest = 40;
        double psfSigma = 3.0, mergeSigma = 3.00;
        float mergeFactor = 1.5f;

        new ImagePlus("original hairpin", createHairpin(w, h, widest, psfSigma)).show();

        FloatProcessor fp = createSharpenedHairpin(w, h, widest, psfSigma, mergeSigma, mergeFactor, 100);
        new ImagePlus("sharpened hairpin", fp).show();
    }
}
