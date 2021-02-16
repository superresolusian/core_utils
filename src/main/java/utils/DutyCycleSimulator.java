package utils;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import sun.awt.image.ImageWatched;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;

import static java.lang.Math.*;

public class DutyCycleSimulator {

    public static ArrayList<Double> getStateList(double exptTime, double bleachTime, double dutyCycle, int nSwitchingCycles) {
        Random random = new Random();

        if(nSwitchingCycles==0){
            ArrayList<Double> stateList = new ArrayList<>();
            stateList.add(-bleachTime);
            return stateList;
        }

        double totalOnTime = bleachTime * dutyCycle;
        double blinkOnTime = totalOnTime / nSwitchingCycles;

        System.out.println("total on = "+totalOnTime+ ", blink on = "+blinkOnTime);

        // get blink starts
        double[] blinkStarts = new double[nSwitchingCycles];
        blinkStarts[0] = random.nextDouble()*(bleachTime-totalOnTime);

        for(int n=1; n<nSwitchingCycles; n++){
            boolean goodStartTime = false;
            double startTime = 0;
            while(!goodStartTime){
                startTime = random.nextDouble()*(bleachTime-totalOnTime);
                for(int m=n-1; m<n; m++){
                    if(abs(startTime-blinkStarts[m])<blinkOnTime){
                        goodStartTime = false;
                        break;
                    }
                    goodStartTime = true;
                }
            }
            blinkStarts[n] = startTime;
        }
        // sort by ascending
        Arrays.sort(blinkStarts);

        // for bleachers, migrate last starttime to bleachtime-blinkontime
        if(bleachTime<exptTime) blinkStarts[nSwitchingCycles-1] = bleachTime-blinkOnTime;

        // create state list
        ArrayList<Double> stateList = new ArrayList<>();
        double lastBlinkOff = 0;
        for(int n=0; n<nSwitchingCycles; n++){
            stateList.add(lastBlinkOff-blinkStarts[n]);
            stateList.add(blinkOnTime);
            lastBlinkOff = blinkStarts[n] + blinkOnTime;
        }
        if(lastBlinkOff<exptTime) stateList.add(lastBlinkOff-exptTime);

        return stateList;
    }

    public static float[] convertStateListToPhotonsInFrame(ArrayList<Double> stateList, double photonsPerBlink, int nFrames, double frameDuration) {
        //nFrames is exptTime/frameDuration
        float[] photonTrace = new float[nFrames];
        int nPhotons = getNPhotons(photonsPerBlink);
        int f = 0;
        int s = 0;
        double t = 0;

        while (f < nFrames && s < stateList.size()) {
            double thisState = stateList.get(s);
            if (thisState < 0) {
                int lastFrameOff = (int) floor((t - thisState) / frameDuration);
                while (f <= lastFrameOff) f++;
                t += -thisState;
            } else {
                double timeRemainingInBlink = thisState;
                while (timeRemainingInBlink > 0) {
                    double timeInThisFrame = (f + 1) * frameDuration - t;
                    timeInThisFrame = min(timeRemainingInBlink, timeInThisFrame);
                    int nPhotonsInFrame = (int) (nPhotons * timeInThisFrame / thisState);
                    photonTrace[f] = nPhotonsInFrame;
                    System.out.println(nPhotonsInFrame + " photons in frame " + f);
                    t += timeInThisFrame;
                    timeRemainingInBlink -= timeInThisFrame;
                    f++;
                }

            }
            s++;
        }
        return photonTrace;

    }

    public static double getBleachTime(double exptTime, double survivalFraction){

        if(survivalFraction==1) return exptTime*2;

        ExponentialDistribution bleachDistribution = new ExponentialDistribution((-400*1000)/(log(survivalFraction)));
        double bleachTime = min(exptTime, bleachDistribution.sample());

        return bleachTime;
    }

    public static int getNSwitchingCycles(double meanSwitchingCycles){
        Random random = new Random();
        int nSwitchingCycles = (int) (meanSwitchingCycles + random.nextGaussian()*(meanSwitchingCycles/10));
        return max(nSwitchingCycles, 0);
    }

    public static int getNPhotons(double meanPhotons){
        Random random = new Random();
        int nPhotons = (int) (meanPhotons + random.nextGaussian()*(meanPhotons/10));
        return max(nPhotons, 1);
    }

    public static void main(String[] args){
        double survivalFraction = 0.73;
        double dutyCycle = 0.0012;
        double meanSwitchingCycles = 26;
        double exptTime = 400*1000;
        double frameDuration = 10;
        int photons = 5202;
        int nFrames = (int) (exptTime/frameDuration);

        double bleachTime = getBleachTime(exptTime, survivalFraction);
        int nSwitchingCycles = getNSwitchingCycles(meanSwitchingCycles);


        ArrayList<Double> testList = getStateList(exptTime, bleachTime, dutyCycle, nSwitchingCycles);
        for(double e:testList) System.out.println(e);

        float[] photonTrace = convertStateListToPhotonsInFrame(testList, photons, nFrames, frameDuration);
    }

}
