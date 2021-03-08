package utils;

import ij.ImageJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.awt.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.random;
import static java.lang.Math.sqrt;

public class ClusterSimulator {

    private static final double ROOT2 = sqrt(2);

    public static ArrayList<Point> createTriangleLattice(int w, double vertexLength){
        double x0 = vertexLength/2;
        double y0 = vertexLength/2;

        double rowSeparation = vertexLength/ROOT2;

        ArrayList<Point> vertexPoints = new ArrayList<>();

        double x, y = y0;

        while(y<w){
            x = x0;

            while(x<w){
                if(x>=0)vertexPoints.add(new Point((int)x, (int)y));
                x += vertexLength;
            }

            x0 = x0 - vertexLength/2;
            if(x0<0) x0 += vertexLength;
            y += rowSeparation;
        }
        return vertexPoints;
    }

    public static ArrayList<Point> jitterLattice(ArrayList<Point> lattice, double jitterRadius){
        ArrayList<Point> jitteredLattice = new ArrayList<>();
        Random random = new Random();
        for(Point p:lattice){
            double x_ = p.x + random.nextGaussian()*jitterRadius;
            double y_ = p.y + random.nextGaussian()*jitterRadius;
            jitteredLattice.add(new Point((int)x_, (int) y_));
        }
        return jitteredLattice;
    }

    public static FloatProcessor clusterAroundPoints(int w, ArrayList<Point> lattice, int nMolecules, double clusterRadius){
        FloatProcessor fp = new FloatProcessor(w, w);
        Random random = new Random();
        for(Point p:lattice){
            double xc = p.x;
            double yc = p.y;
            for(int n=0; n<nMolecules; n++){
                int xm = (int) (xc + random.nextGaussian()*clusterRadius);
                int ym = (int) (yc + random.nextGaussian()*clusterRadius);
                if(xm>=0 && xm<w && ym>0 && ym<w) fp.setf(xm, ym, fp.getf(xm, ym)+1);
            }
        }
        return fp;

    }


    public static void main(String[] arg){

        new ImageJ();

        ArrayList<Point> lattice = createTriangleLattice(500, 30);
        ArrayList<Point> jitteredLattice = jitterLattice(lattice, 3);
        FloatProcessor fp = clusterAroundPoints(500, jitteredLattice, 25, 2);


        new ImagePlus("clusters", fp).show();

    }

}
