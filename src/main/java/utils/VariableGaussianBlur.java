package utils;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import static java.lang.Double.isNaN;
import static java.lang.Math.ceil;
import static utils.ArrayUtils.toIntegerArray;

public class VariableGaussianBlur {

    public static FloatProcessor run(FloatProcessor fp, double[] blurVals){

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
        int w = 100, h = 100;
        double[] blurVals = new double[w*h];
        for(int i=0; i<w*h; i++) blurVals[i] = ceil(i/10)/100;
        FloatProcessor fp = new FloatProcessor(w, h);
        for(int i=0; i<w; i++){
            fp.setf(i, i, 1.0f);
            fp.setf(w-i-1, w-i-1, 1.0f);
        }

        new ImageJ();
        new ImagePlus("im", fp).show();

        FloatProcessor fpBlur = run(fp, blurVals);
        new ImagePlus("blurred", fpBlur).show();

    }

}
