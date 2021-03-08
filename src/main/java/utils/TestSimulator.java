package utils;

import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import java.util.ArrayList;

import static utils.CameraStatisticsSimulator.*;
import static utils.DutyCycleSimulator.*;

public class TestSimulator {

    public static ArrayList<int[]> simulateRing(int nParticles, int xc, int yc, int rad){
        // grab molecule coordinates
        double thetaInc = 2 * Math.PI / nParticles;
        int[] xPos = new int[nParticles];
        int[] yPos = new int[nParticles];

        for (int n = 0; n < nParticles; n++) {
            xPos[n] = (int) (xc + rad * Math.cos(thetaInc * n));
            yPos[n] = (int) (yc + rad * Math.sin(thetaInc * n));
        }

        ArrayList<int[]> positionsList = new ArrayList<>();
        positionsList.add(xPos);
        positionsList.add(yPos);
        return positionsList;
    }

    public static void main(String[] args){

        Fluorophore fluorophore = new Fluorophore();
        fluorophore.initialise("Alexa Fluor 647", "BME");
        double survivalFraction = fluorophore.survivalFraction;
        double dutyCycle = fluorophore.dutyCycle;
        double meanSwitchingCycles = fluorophore.nSwitchingCycles;
        double photons = fluorophore.nPhotons;
        double wavelength = fluorophore.wavelength;

        int w = 301;
        int h = 301;
        int xc = 150;
        int yc = 150;
        int rad = 10;
        int nParticles = 10;

        double exptTime = 60*1000;
        double frameDuration = 17;
        int nFrames = (int) (exptTime/frameDuration);

        double bleachTime = getBleachTime(exptTime, survivalFraction);
        int nSwitchingCycles = getNSwitchingCycles(meanSwitchingCycles, exptTime, bleachTime);
        System.out.println("meanSwitchingCycles = "+meanSwitchingCycles+", nSwitchingCycles = "+nSwitchingCycles);

        long start = System.currentTimeMillis();
        ArrayList<float[]> photonTraces = new ArrayList<>();
        for(int n=0; n<nParticles; n++) {
            ArrayList<Double> stateList = getStateList(exptTime, bleachTime, dutyCycle, nSwitchingCycles);
            float[] photonTrace = convertStateListToPhotonsInFrame(stateList, photons, nFrames, frameDuration);
            photonTraces.add(photonTrace);
        }

        long stop = System.currentTimeMillis();

        System.out.println("time to make "+nParticles+" traces = "+(stop-start)+"ms");
        int targetW = 30;
        int targetH = 30;
        ImageStack imsAnalogToDigital = new ImageStack(targetW, targetH, nFrames);

        double pixelSize = 10;
        double sigma = wavelengthToSigmaPixels(wavelength, 1.4, pixelSize);

        ArrayList<int[]> moleculeLocations = simulateRing(nParticles, xc, yc, rad);
        int[] xPos = moleculeLocations.get(0);
        int[] yPos = moleculeLocations.get(1);

        start = System.currentTimeMillis();

        for(int f=0; f<nFrames; f++){
            FloatProcessor fp = particlesToFrame(w, h, f, xPos, yPos, photonTraces);
            fp.add(photons*0.0001);
            fp = gaussianBlur(fp, sigma);
            fp = photonsPerDetectorElement(fp, targetW, targetH, BIN);
            fp = photoelectronEmission(fp);
            fp = applyGain(fp, defaultGain, USEGAMMA);
            fp = addReadNoise(fp, 10);
            fp = analogToDigitalConversion(fp, defaultSensitivity, defaultBase);
            imsAnalogToDigital.setProcessor(fp.duplicate(), f+1);
        }

        stop = System.currentTimeMillis();

        System.out.println("time to make "+nFrames+" frames = "+(stop-start)+"ms");

        new ImageJ();

        new ImagePlus("adc", imsAnalogToDigital).show();




    }
}
