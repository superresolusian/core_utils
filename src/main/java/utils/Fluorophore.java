package utils;

import java.util.LinkedHashMap;

public class Fluorophore {
    String[] fluorophores =
            {"Alexa Fluor 488", //0
                    "Alexa Fluor 568", //1
                    "Alexa Fluor 647", //2
                    "Alexa Fluor 750", //3
                    "Alexa Fluor 790", //4
                    "Atto 488",
                    "Atto 520",
                    "Atto 565",
                    "Atto 647",
                    "Atto 647N",
                    "Atto 655",
                    "Atto 680",
                    "Atto 740",
                    "Cy2",
                    "Cy3B",
                    "Cy3",
                    "Cy3.5",
                    "Cy5",
                    "Cy5.5",
                    "Cy7",
                    "DyLight 750",
                    "Dyomics 654",
                    "Fluorescein",
                    "FITC",
                    "IRDye 800 CW",
                    "TAMRA"
            };

    String[] buffers = new String[] {"MEA", "BME"};

    String[] intensities = new String[] {"Low", "Mid", "High"};

    public double wavelength = Double.NaN;
    public double nPhotons = Double.NaN;
    public double dutyCycle = Double.NaN;
    public double survivalFraction = Double.NaN;
    public double nSwitchingCycles = Double.NaN;
    public double quantumYield = Double.NaN;

    public static LinkedHashMap<String, double[]> fluorophoreDictionary;

    void populateFluorophoreDictionary(){
        fluorophoreDictionary = new LinkedHashMap<String, double[]>();
        fluorophoreDictionary.put(fluorophores[0]+buffers[0],  new double[]{519, 1193, 0.00055, 0.94,  16, 0.92});
        fluorophoreDictionary.put(fluorophores[0]+buffers[1],  new double[]{519,  427,  0.0017,    1, 139, 0.92});
        fluorophoreDictionary.put(fluorophores[1]+buffers[0],  new double[]{603, 2826, 0.00058, 0.58,   7, 0.69});
        fluorophoreDictionary.put(fluorophores[1]+buffers[1],  new double[]{603, 1686,  0.0027, 0.99,  52, 0.69});
        fluorophoreDictionary.put(fluorophores[2]+buffers[0],  new double[]{665, 3823,  0.0005, 0.83,  14, 0.33});
        fluorophoreDictionary.put(fluorophores[2]+buffers[1],  new double[]{665, 5202,  0.0012, 0.73,  26, 0.33});
        fluorophoreDictionary.put(fluorophores[3]+buffers[0],  new double[]{775,  437, 0.00006, 0.36, 1.5, 0.12});
        fluorophoreDictionary.put(fluorophores[3]+buffers[1],  new double[]{775,  703,  0.0001, 0.68,   6, 0.12});
        fluorophoreDictionary.put(fluorophores[4]+buffers[0],  new double[]{810,  591, 0.00049, 0.54,   5, 0.12});
        fluorophoreDictionary.put(fluorophores[4]+buffers[1],  new double[]{810,  740,  0.0014, 0.62, 2.7, 0.12});
        fluorophoreDictionary.put(fluorophores[5]+buffers[0],  new double[]{523, 1341, 0.00065, 0.98,  11, 0.80});
        fluorophoreDictionary.put(fluorophores[5]+buffers[1],  new double[]{523, 1110,  0.0022, 0.99,  49, 0.80});
        fluorophoreDictionary.put(fluorophores[6]+buffers[0],  new double[]{538, 1231,  0.0015, 0.92,   9, 0.90});
        fluorophoreDictionary.put(fluorophores[6]+buffers[1],  new double[]{538,  868, 0.00062, 0.86,  17, 0.90});
        fluorophoreDictionary.put(fluorophores[7]+buffers[0],  new double[]{592,19714, 0.00058, 0.17,   4, 0.90});
        fluorophoreDictionary.put(fluorophores[7]+buffers[1],  new double[]{592,13294, 0.00037, 0.55,   5, 0.90});
        fluorophoreDictionary.put(fluorophores[8]+buffers[0],  new double[]{669, 1526,  0.0021, 0.46,  10, 0.20});
        fluorophoreDictionary.put(fluorophores[8]+buffers[1],  new double[]{669,  944,  0.0016, 0.84,  24, 0.20});
        fluorophoreDictionary.put(fluorophores[9]+buffers[0],  new double[]{669, 3254,  0.0012, 0.24,  9,  0.65});
        fluorophoreDictionary.put(fluorophores[9]+buffers[1],  new double[]{669, 4433,  0.0035, 0.65,  39, 0.65});
        fluorophoreDictionary.put(fluorophores[10]+buffers[0], new double[]{684, 1105,  0.0006, 0.65,  17, 0.30});
        fluorophoreDictionary.put(fluorophores[10]+buffers[1], new double[]{684,  657,  0.0011, 0.78,  22, 0.30});
        fluorophoreDictionary.put(fluorophores[11]+buffers[0], new double[]{700, 1656,  0.0019, 0.65,   8, 0.30});
        fluorophoreDictionary.put(fluorophores[11]+buffers[1], new double[]{700,  987,  0.0024, 0.91,  27, 0.30});
        fluorophoreDictionary.put(fluorophores[12]+buffers[0], new double[]{764,  779, 0.00047, 0.31,   3, 0.10});
        fluorophoreDictionary.put(fluorophores[12]+buffers[1], new double[]{764,  463,  0.0014, 0.96,  14, 0.10});
        fluorophoreDictionary.put(fluorophores[13]+buffers[0], new double[]{506, 6241, 0.00012, 0.12, 0.4, 0.12});
        fluorophoreDictionary.put(fluorophores[13]+buffers[1], new double[]{506, 4583, 0.00045, 0.19, 0.7, 0.12});
        fluorophoreDictionary.put(fluorophores[14]+buffers[0], new double[]{570, 1365,  0.0003,    1,   8, 0.67});
        fluorophoreDictionary.put(fluorophores[14]+buffers[1], new double[]{570, 2057,  0.0004, 0.89,   5, 0.67});
        fluorophoreDictionary.put(fluorophores[15]+buffers[0], new double[]{570,11022,  0.0001, 0.17, 0.5, 0.15});
        fluorophoreDictionary.put(fluorophores[15]+buffers[1], new double[]{570, 8158,  0.0003, 0.55, 1.6, 0.15});
        fluorophoreDictionary.put(fluorophores[16]+buffers[0], new double[]{596, 4968,  0.0017, 0.89, 5.7, 0.15});
        fluorophoreDictionary.put(fluorophores[16]+buffers[1], new double[]{596, 8028,  0.0005, 0.61, 3.3, 0.15});
        fluorophoreDictionary.put(fluorophores[17]+buffers[0], new double[]{670, 4254,  0.0004, 0.75,  10, 0.28});
        fluorophoreDictionary.put(fluorophores[17]+buffers[1], new double[]{670, 5873,  0.0007, 0.83,  17, 0.28});
        fluorophoreDictionary.put(fluorophores[18]+buffers[0], new double[]{694, 5831,  0.0069, 0.87,  16, 0.28});
        fluorophoreDictionary.put(fluorophores[18]+buffers[1], new double[]{694, 6337,  0.0073, 0.85,  25, 0.28});
        fluorophoreDictionary.put(fluorophores[19]+buffers[0], new double[]{776,  852,  0.0003, 0.48,   5, 0.28});
        fluorophoreDictionary.put(fluorophores[19]+buffers[1], new double[]{776,  997,  0.0004, 0.49, 2.6, 0.28});
        fluorophoreDictionary.put(fluorophores[20]+buffers[0], new double[]{778,  712,  0.0006, 0.55,   5, 0.30});
        fluorophoreDictionary.put(fluorophores[20]+buffers[1], new double[]{778,  749,  0.0002, 0.58,   6, 0.30});
        fluorophoreDictionary.put(fluorophores[21]+buffers[0], new double[]{675, 3653,  0.0011, 0.79,  20, 0.30});
        fluorophoreDictionary.put(fluorophores[21]+buffers[1], new double[]{675, 3014,  0.0018, 0.64,  19, 0.30});
        fluorophoreDictionary.put(fluorophores[22]+buffers[0], new double[]{518, 1493, 0.00032, 0.51,   4, 0.79});
        fluorophoreDictionary.put(fluorophores[22]+buffers[1], new double[]{518,  776, 0.00034, 0.83,  15, 0.79});
        fluorophoreDictionary.put(fluorophores[23]+buffers[0], new double[]{518,  639, 0.00041, 0.75,  17, 0.80});
        fluorophoreDictionary.put(fluorophores[23]+buffers[1], new double[]{518, 1086, 0.00031, 0.90,  16, 0.80});
        fluorophoreDictionary.put(fluorophores[24]+buffers[0], new double[]{794, 2753,  0.0018, 0.60,   3, 0.30});
        fluorophoreDictionary.put(fluorophores[24]+buffers[1], new double[]{794, 2540,   0.038,    1, 127, 0.30});
        fluorophoreDictionary.put(fluorophores[25]+buffers[0], new double[]{575, 4884,  0.0017, 0.85,  10, 0.20});
        fluorophoreDictionary.put(fluorophores[25]+buffers[1], new double[]{575, 2025,  0.0049, 0.99,  59, 0.20});

        // intensity-dependence for AF467[2], Atto655[10], Cy3B [14]
        fluorophoreDictionary.put(fluorophores[2]+intensities[0],  new double[]{665, 4208,  0.0041, 0.98,  19, 0.33});
        fluorophoreDictionary.put(fluorophores[2]+intensities[1],  new double[]{665, 6399,  0.0012, 0.78,  17, 0.33});
        fluorophoreDictionary.put(fluorophores[2]+intensities[2],  new double[]{665, 6321, 0.00057, 0.70,  14, 0.33});
        fluorophoreDictionary.put(fluorophores[10]+intensities[0], new double[]{684,  793,  0.0043, 0.96,  27, 0.30});
        fluorophoreDictionary.put(fluorophores[10]+intensities[1], new double[]{684,  696,  0.0012, 0.98,  21, 0.30});
        fluorophoreDictionary.put(fluorophores[10]+intensities[2], new double[]{684,  905,  0.0016, 0.56,  13, 0.30});
        fluorophoreDictionary.put(fluorophores[14]+intensities[0], new double[]{570, 1194, 0.00041, 0.94, 5.3, 0.67});
        fluorophoreDictionary.put(fluorophores[14]+intensities[1], new double[]{570, 1883, 0.00036, 0.87, 7.1, 0.67});
        fluorophoreDictionary.put(fluorophores[14]+intensities[2], new double[]{570, 2276, 0.00095, 0.82, 5.8, 0.67});

        // dummy fluorophores for theoretical behaviour
        fluorophoreDictionary.put("dummyBase",              new double[]{600, 1000, 0.0001, 1.0, 10, 1.0});
        fluorophoreDictionary.put("dummyLowBlinks",         new double[]{600, 1000, 0.0001, 1.0,  2, 1.0});
        fluorophoreDictionary.put("dummyVariableBlinks",    new double[]{600, 1000, 0.0001, 0.1, 10, 1.0});
        fluorophoreDictionary.put("dummyDimBlinks",         new double[]{600,  100, 0.0001, 1.0, 10, 1.0});
        fluorophoreDictionary.put("dummyOverlapBlinks1",    new double[]{600,10000, 0.0050, 1.0, 10, 1.0});
        fluorophoreDictionary.put("dummyOverlapBlinks2",    new double[]{600,10000, 0.0100, 1.0, 10, 1.0});
        fluorophoreDictionary.put("dummyOverlapBlinks3",    new double[]{600,10000, 0.0150, 1.0, 10, 1.0});
    }

    public Fluorophore(){
        populateFluorophoreDictionary();
    }

    public void initialise(String fluorophore, String condition){
        double[] fluorophoreInfo = fluorophoreDictionary.get(fluorophore+condition);
        this.wavelength = fluorophoreInfo[0];
        this.nPhotons = fluorophoreInfo[1];
        this.dutyCycle = fluorophoreInfo[2];
        this.survivalFraction = fluorophoreInfo[3];
        this.nSwitchingCycles = fluorophoreInfo[4];
        this.quantumYield = fluorophoreInfo[5];
    }

    public static void main(String args[]){
        Fluorophore fluorophore = new Fluorophore();
        fluorophore.initialise("Cy5", "MEA");
        System.out.println("wavelength: "+fluorophore.wavelength);
        System.out.println("nPhotons: "+fluorophore.nPhotons);
        System.out.println("dutyCycle: "+fluorophore.dutyCycle);
        System.out.println("survivalFraction: "+fluorophore.survivalFraction);
        System.out.println("nSwitchingCycles: "+fluorophore.nSwitchingCycles);
    }
}
