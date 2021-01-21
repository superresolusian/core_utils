package utils;

import ij.IJ;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Helper2D {

    // lifted from local-NEB codebase
    public static short[] connectedComponents(ImageProcessor ip){
        int w = ip.getWidth();
        int h = ip.getHeight();
        int nPixels = w*h;

        short[] data = new short[nPixels];
        for(int i=0; i<nPixels; i++) data[i] = (short) ip.get(i);

        short[] components = new short[data.length];
        short id = 1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = ArrayUtils.ravel(x, y, w);
                // foreground pixel that hasn't been labeled yet
                if (data[p] > 0 && components[p] == 0) {
                    labelComponentAt(id, x, y, components, data, w, h);
                    id += 1;
                }
            }
        }
        return components;
    }

    // lifted from local-NEB codebase
    private static void labelComponentAt(short id, int x, int y, short[] components, short[] data, int w, int h) {
        components[ArrayUtils.ravel(x, y, w)] = id;
        LinkedList<Point> queue = new LinkedList<Point>();
        queue.add(new Point(x, y));
        while (!queue.isEmpty()) {
            Point p = queue.removeFirst();
            List<Point> neighbors = unlabeledNeighbors(p.x, p.y, components, data, w, h);
            for (Point n : neighbors)
                components[ArrayUtils.ravel(n.x, n.y, w)] = id;
            queue.addAll(neighbors);
        }
    }

    // lifted from local-NEB codebase
    private static List<Point> unlabeledNeighbors(int x, int y, short[] components, short[] data, int w, int h) {
        assert components[ArrayUtils.ravel(x,y,w)] > 0;
        List<Point> neighbors = new LinkedList<Point>();
        for (int j = max(y - 1, 0); j < min(y + 2, h); j++) {
            for (int i = max(x - 1, 0); i < min(x + 2, w); i++) {
                int p = ArrayUtils.ravel(i, j, w);
                if (data[p] > 0 && components[p] == 0) {
                    neighbors.add(new Point(i, j));
                }
            }
        }
        return neighbors;
    }

    public static class regionProps {
        public int area;
        public Rectangle boundingBox;
        public double[] centroid;
        public List<Point> relativeCoordinates;
        public float averageIntensity;

        public regionProps(int area, Rectangle boundingBox, double[] centroid, List relativeCoordinates, float averageIntensity){
            this.area = area;
            this.boundingBox = boundingBox;
            this.centroid = centroid;
            this.relativeCoordinates = relativeCoordinates;
            this.averageIntensity = averageIntensity;
        }

    }

    public static regionProps getProps(int n, ImageProcessor ip, FloatProcessor fp){

        int w = ip.getWidth();
        int h = ip.getHeight();

        int area = 0;
        int x0 = w-1, y0 = h-1;
        int x1 = 0, y1 = 0;
        int sumX = 0, sumY = 0;
        LinkedHashMap<int[], Integer> neighbourCount = new LinkedHashMap<int[], Integer>();
        float averageIntensity = 0;

        List<Point> coordinates = new ArrayList<>();

        int minNeighbours = 8;

        for(int y=0; y<h; y++){
            for(int x=0; x<w; x++){
                if(ip.getf(x, y)==n){
                    coordinates.add(new Point(x, y));
                    if(fp!=null) {
                        averageIntensity += fp.getf(x, y);
                    }
                    area++;
                    x0 = min(x, x0);
                    y0 = min(y, y0);
                    x1 = max(x, x1);
                    y1 = max(y, y1);
                    sumX += x;
                    sumY += y;

                    int nNeighbours = 0;
                    for(int y_=max(0, y-1); y_<=min(h-1,y+1); y_++){
                        for(int x_=max(0,x-1); x_<=min(w-1, x+1); x_++){
                            if(y_==y && x_==x) continue;
                            if(ip.getf(x_,y_)>0) nNeighbours++;
                        }
                    }
                    neighbourCount.put(new int[] {x, y}, nNeighbours);
                    minNeighbours = min(minNeighbours, nNeighbours);
                }
            }
        }

        Rectangle bbox = new Rectangle(x0, y0, x1-x0, y1-y0);

        double xc = sumX/area;
        double yc = sumY/area;
        double nList = coordinates.size();
        averageIntensity/=area;

        for(int i=0; i<nList; i++){
            Point p = coordinates.get(i);
            Point p_ = new Point((int) (p.x-xc), (int)(p.y-yc));
            coordinates.set(i, p_);
        }

        return new regionProps(area, bbox, new double[]{sumX/area, sumY/area}, coordinates, averageIntensity);
    }


}
