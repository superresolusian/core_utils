package utils;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clij2.plugins.GaussianBlur2D;

import java.util.*;

import static java.lang.Double.isNaN;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static utils.ArrayUtils.toDoubleArray;
import static utils.ArrayUtils.toIntegerArray;

public class UniqueValueSplitter {

    public static ImageStack splitByValues(FloatProcessor fp, double[] values){
        int w = fp.getWidth();
        int h = fp.getHeight();

        LinkedHashMap<Double, int[]> valueMap = mapPixelsToUniqueValues(values);
        Set<Double> keys = valueMap.keySet();
        List<Double> sortedKeys = new ArrayList<>(keys);
        Collections.sort(sortedKeys);

        int nKeys = keys.size();

        ImageStack imsSplit = new ImageStack(w, h);
        int i=0;
        for(Double key:sortedKeys){
            IJ.showStatus("Splitting image...");
            IJ.showProgress(i+1, nKeys);
//            if(key==0){i++; continue;}
            int[] pixels = valueMap.get(key);
            FloatProcessor fpSplit = new FloatProcessor(w, h);
            for(int p:pixels) fpSplit.setf(p, fp.getf(p));
            imsSplit.addSlice(fpSplit);
            i++;
        }
        return imsSplit;

    }

    public static FloatProcessor splitAndBlur(FloatProcessor fp, double[] blurVals){

        int w = fp.getWidth();
        int h = fp.getHeight();

        LinkedHashMap<Double, int[]> valueMap = mapPixelsToUniqueValues(blurVals);
        Set<Double> keys = valueMap.keySet();

        FloatProcessor fpVariableBlur = new FloatProcessor(w, h);

        int nKeys = valueMap.size();
        int i=0;
        for(Double key:keys){
            IJ.showStatus("Performing convolution...");
            IJ.showProgress(i+1, nKeys);
            int[] pixels = valueMap.get(key);
            FloatProcessor fp_ = fp.duplicate().convertToFloatProcessor();
            fp_.blurGaussian(key);
            for(int p:pixels) fpVariableBlur.setf(p, fp_.getf(p));
            i++;
        }
        return fpVariableBlur;

    }

    public static FloatProcessor splitAndBlurClij(FloatProcessor fp, double[] blurVals){
        int w = fp.getWidth();
        int h = fp.getHeight();

        LinkedHashMap<Double, int[]> valueMap = mapPixelsToUniqueValues(blurVals);
        Set<Double> keys = valueMap.keySet();

        FloatProcessor fpVariableBlur = new FloatProcessor(w, h);

        CLIJ2 clij2 = CLIJ2.getInstance();
        ClearCLBuffer input = clij2.push(new ImagePlus("", fp));
        ClearCLBuffer output = clij2.create(input);

        int nKeys = valueMap.size();
        int i=0;
        for(Double key:keys){
            IJ.showStatus("Performing convolution...");
            IJ.showProgress(i+1, nKeys);
            int[] pixels = valueMap.get(key);
            clij2.gaussianBlur2D(input, output, key, key);
            FloatProcessor fp_ = clij2.pull(output).getProcessor().convertToFloatProcessor();
            for(int p:pixels) fpVariableBlur.setf(p, fp_.getf(p));
            i++;
        }

        input.close();
        output.close();

        return fpVariableBlur;
    }

    public static LinkedHashMap mapPixelsToUniqueValues(double[] values){
        double[] values_ = values.clone();
        int nValues = values.length;

        LinkedHashMap<Double, int[]> valueMap = new LinkedHashMap<>();

        for(int n=0; n<nValues; n++){
            double v = values_[n];
            if(isNaN(v)) continue;

            ArrayList<Integer> indices = new ArrayList<>();
            indices.add(n);

            for(int m=n+1; m<nValues; m++){
                double vv = values_[m];
                if(isNaN(vv)) continue;
                if(vv==v){
                    indices.add(m);
                    values_[m] = Double.NaN;
                }
            }
            valueMap.put(v, toIntegerArray(indices));
        }

        return valueMap;
    }

    public static void main(String[] args){
        int w = 100;
        int h = 100;
        FloatProcessor map = new FloatProcessor(w, h);
        for(int i=0; i<100; i++){
            for(int j=0; j<100; j++){
                map.setf(i, j, (float) ceil(i/25));
            }
        }
        double[] blurVals = toDoubleArray(map);
        FloatProcessor fp = new FloatProcessor(w, h);
        for(int i=0; i<10; i++){
            fp.setValue(i+1);
            fp.drawLine((i+1)*10, 0, (i+1)*10, 100);
        }

        new ImageJ();
        new ImagePlus("im", fp).show();

        new ImagePlus("split", splitByValues(fp, blurVals)).show();
        FloatProcessor fpBlur = splitAndBlur(fp, blurVals);
        new ImagePlus("blurred", fpBlur).show();

        FloatProcessor fpBlurClij = splitAndBlurClij(fp, blurVals);
        new ImagePlus("blurred clij", fpBlurClij).show();

//        int w = 100, h = 100;
//        FloatProcessor fp = new FloatProcessor(w, h);
//
//        for(int i=0; i<10; i++){
//            fp.setValue(i+1);
//            fp.drawLine((i+1)*10, 0, (i+1)*10, 100);
//        }
//
//        CLIJ2 clij2 = CLIJ2.getInstance();
//        ClearCLBuffer input = clij2.push(new ImagePlus("", fp));
//        ClearCLBuffer output = clij2.create(input);
//
//        new ImageJ();
//        new ImagePlus("raw", fp).show();
//        for(int i=0; i<10; i++){
//            FloatProcessor fp_ = fp.duplicate().convertToFloatProcessor();
//            fp_.blurGaussian(i+1);
//            new ImagePlus("internal ij blur "+(i+1), fp_).show();
//        }
//
//        for(int i=0; i<10; i++){
//            clij2.gaussianBlur2D(input, output, i+1, i+1);
//            ImagePlus impClij = clij2.pull(output);
//            impClij.setTitle("clij blur "+(i+1));
//            impClij.show();
//        }
//        input.close();
//        output.close();
    }

}
