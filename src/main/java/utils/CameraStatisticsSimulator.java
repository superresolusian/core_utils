package utils;

import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Binner;
import ij.process.FloatProcessor;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.ArrayList;
import java.util.Random;

import static ij.plugin.Binner.SUM;
import static java.lang.Math.max;

public class CameraStatisticsSimulator {

    public static final boolean BIN = true;
    public static final boolean RESIZE = false;
    public static final boolean USEGAMMA = true;
    public static final double defaultQE = 1.0;
    public static final double defaultClock = 0.9;
    public static final double defaultReadNoise = 0;
    public static final double defaultGain = 100;
    public static final double defaultSensitivity = 12.6;
    public static final double defaultBase = 100;



    public static FloatProcessor particlesToFrame(int w, int h, int frame,
                                                  int[] xPos, int[] yPos, ArrayList<float[]> switchingLists){
        FloatProcessor fp = new FloatProcessor(w, h);
        int nParticles = xPos.length;
        for(int n=0; n<nParticles; n++){
            int x = xPos[n];
            int y = yPos[n];
            float v = switchingLists.get(n)[frame];
            fp.setf(x, y, v);
        }
        return fp;
    }

    public static double wavelengthToSigmaPixels(double wavelength, double NA, double pixelSize){
        double fwhm = wavelength/(2*NA);
        double sigmaNm = fwhm/2.35482;
        double sigmaPx = sigmaNm/pixelSize;
        return sigmaPx;
    }

    public static FloatProcessor gaussianBlur(FloatProcessor fp, double sigma){
        fp = fp.duplicate().convertToFloatProcessor();
        fp.blurGaussian(sigma);
        return fp;
    }

    public static FloatProcessor photonsPerDetectorElement(FloatProcessor fp, int targetW, int targetH, boolean method){
        fp = fp.duplicate().convertToFloatProcessor();
        Binner binner = new Binner();
        if(method==RESIZE) return fp.resize(targetW, targetH).convertToFloatProcessor();
        else{
            double shrinkW = (double) fp.getWidth()/targetW;
            double shrinkH = (double) fp.getHeight()/targetH;
            if(shrinkH!=shrinkW) System.out.println("warning: resizing appears to be non-isotropic");
            return binner.shrink(fp, (int) shrinkW, (int) shrinkH, SUM).convertToFloatProcessor();
        }
    }

    public static FloatProcessor quantumEfficiencyAndClockCharge(FloatProcessor fp, double QE, double c){
        fp = fp.duplicate().convertToFloatProcessor();
        fp.multiply(QE);
        fp.add(c);
        return fp;
    }

    public static FloatProcessor photoelectronEmission(FloatProcessor fp){
        fp = fp.duplicate().convertToFloatProcessor();
        RandomDataGenerator rdg = new RandomDataGenerator();
        for(int p=0; p<fp.getPixelCount(); p++){
            float v = fp.getf(p);
            if(v>0) fp.setf(p, (float) rdg.nextPoisson(v));
        }
        return fp;
    }

    public static FloatProcessor applyGain(FloatProcessor fp, double gain, boolean doGamma){
        fp = fp.duplicate().convertToFloatProcessor();
        if(!USEGAMMA) fp.multiply(gain);
        else{
            for(int p=0; p<fp.getPixelCount(); p++){
                float v = fp.getf(p);
                if(v==0) continue;
                fp.setf(p, (float) new GammaDistribution(v, gain).sample());
            }
        }
        return fp;
    }

    public static FloatProcessor addReadNoise(FloatProcessor fp, double readNoise){
        fp = fp.duplicate().convertToFloatProcessor();
        Random random = new Random();
        for(int p=0; p<fp.getPixelCount(); p++){
            float v = fp.getf(p);
            fp.setf(p, (float) max(0, v+random.nextGaussian()*readNoise)); //TODO: notation in stochastic model paper is different...
        }
        return fp;
    }

    public static FloatProcessor analogToDigitalConversion(FloatProcessor fp, double sensitivity, double base){
        fp = fp.duplicate().convertToFloatProcessor();
        fp.multiply(1/sensitivity);
        fp.add(base);
        return fp;
    }



    public static void main(String[] args){
        int w = 501;
        int h = 501;
        int xc = 250;
        int yc = 250;
        int rad = 50;
        int nParticles = 4;
        int nFrames = 10;

        int[] xPos = new int[] {100, 200, 300, 400};
        int[] yPos = new int[] {100, 200, 300, 400};
        ArrayList<float[]> switchingList = new ArrayList<>();

        for(int n=0; n<nParticles; n++){
            float[] switchingArray = new float[nFrames];
            for(int f=0; f<nFrames; f++) switchingArray[f] = 500;
            switchingList.add(switchingArray);
        }

        int targetW = 50;
        int targetH = 50;
        ImageStack imsFrame = new ImageStack(w, h, nFrames);
        ImageStack imsBlur = new ImageStack(w, h, nFrames);
        ImageStack imsIncidentPhotonsBin = new ImageStack(targetW, targetH, nFrames);
        ImageStack imsPhotoelectronsBin = new ImageStack(targetW, targetH, nFrames);
        ImageStack imsGain = new ImageStack(targetW, targetH, nFrames);
        ImageStack imsReadNoise = new ImageStack(targetW, targetH, nFrames);
        ImageStack imsAnalogToDigital = new ImageStack(targetW, targetH, nFrames);

        double wavelength = 600;
        double pixelSize = 10;
        double sigma = wavelengthToSigmaPixels(wavelength, 1.4, pixelSize);

        for(int f=0; f<nFrames; f++){
            FloatProcessor fp = particlesToFrame(w, h, f, xPos, yPos, switchingList);
            imsFrame.setProcessor(fp.duplicate(), f+1);
            fp = gaussianBlur(fp, sigma);
            imsBlur.setProcessor(fp.duplicate(), f+1);
            fp = photonsPerDetectorElement(fp, targetW, targetH, BIN);
            imsIncidentPhotonsBin.setProcessor(fp.duplicate(), f+1);
            fp = photoelectronEmission(fp);
            imsPhotoelectronsBin.setProcessor(fp.duplicate(), f+1);
            fp = applyGain(fp, defaultGain, USEGAMMA);
            imsGain.setProcessor(fp.duplicate(), f+1);
            fp = addReadNoise(fp, 10);
            imsReadNoise.setProcessor(fp.duplicate(), f+1);
            fp = analogToDigitalConversion(fp, defaultSensitivity, defaultBase);
            imsAnalogToDigital.setProcessor(fp.duplicate(), f+1);
        }

        new ImageJ();

        new ImagePlus("frames", imsFrame).show();
        new ImagePlus("blurred", imsBlur).show();
        new ImagePlus("detector bin", imsIncidentPhotonsBin).show();
        new ImagePlus("photoelectrons bin", imsPhotoelectronsBin).show();
        new ImagePlus("gain", imsGain).show();
        new ImagePlus("read noise", imsReadNoise).show();
        new ImagePlus("adc", imsAnalogToDigital).show();

    }



}
