package utils;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static java.lang.Math.pow;

class MyFunc implements ParametricUnivariateFunction {
    public double value(double x, double... parameters) {
        return parameters[0] + x*parameters[1];
    }

    // Jacobian matrix of the above. In this case, this is just an array of
    // partial derivatives of the above function, with one element for each parameter.
    public double[] gradient(double t, double... parameters) {
        final double a = parameters[0];
        final double b = parameters[1];

        // Jacobian Matrix Edit

        // Using Derivative Structures...
        // constructor takes 4 arguments - the number of parameters in your
        // equation to be differentiated (3 in this case), the order of
        // differentiation for the DerivativeStructure, the index of the
        // parameter represented by the DS, and the value of the parameter itself
        DerivativeStructure aDev = new DerivativeStructure(2, 1, 0, a);
        DerivativeStructure bDev = new DerivativeStructure(2, 1, 1, b);

        // define the equation to be differentiated using another DerivativeStructure
        DerivativeStructure y = aDev.add(bDev.multiply(t));

        // then return the partial derivatives required
        // notice the format, 3 arguments for the method since 3 parameters were
        // specified first order derivative of the first parameter, then the second,
        // then the third
        return new double[] {
                y.getPartialDerivative(1, 0),
                y.getPartialDerivative(0, 1)
        };

    }
}

public class WeightedLeastSquaresFitter extends AbstractCurveFitter {

    double[] x, y, weights;
    ArrayList<WeightedObservedPoint> points;
    int nPoints;
    double[] initial = new double[] {1.0, 1.0};

    public WeightedLeastSquaresFitter(double[] x, double[] y, double[] weights){
        this.x = x;
        this.y = y;
        this.weights = weights;

        assert (x.length==y.length);
        assert (x.length==weights.length);

        this.nPoints = x.length;

        this.points = new ArrayList<>();
        for(int n=0; n<nPoints; n++){
            WeightedObservedPoint point = new WeightedObservedPoint(weights[n], x[n], y[n]);
            this.points.add(point);
        }
    }

    protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
        final int len = points.size();
        final double[] target  = new double[len];
        final double[] weights = new double[len];
        final double[] initialGuess = this.initial;

        int i = 0;
        for(WeightedObservedPoint point : points) {
            target[i]  = point.getY();
            weights[i] = point.getWeight();
            i += 1;
        }

        final TheoreticalValuesFunction model = new
                TheoreticalValuesFunction(new MyFunc(), points);

        return new LeastSquaresBuilder().
                maxEvaluations(Integer.MAX_VALUE).
                maxIterations(Integer.MAX_VALUE).
                start(initialGuess).
                target(target).
                weight(new DiagonalMatrix(weights)).
                model(model.getModelFunction(), model.getModelFunctionJacobian()).
                build();
    }

    public double[] getFitParamsAndError(){
        double[] coeffs = fit(points);
        double weightedSumResidualsSquared = 0;
        for(int i=0; i<nPoints; i++){
            double yFit= coeffs[0] + x[i]*coeffs[1];
            weightedSumResidualsSquared += (1/weights[i])*(yFit-y[i])*(yFit-y[i]);
        }
        return new double[]{coeffs[0], coeffs[1], weightedSumResidualsSquared};
    }

    public static void main(String[] args) {
        double[] x = new double[100];
        double[] y = new double[100];
        double[] w = new double[100];
        for(int i=0; i<100; i++){
            x[i] = i;
            y[i] = 5 + 2*pow(x[i], 1);
            w[i] = 1;
        }

        WeightedLeastSquaresFitter fitter = new WeightedLeastSquaresFitter(x, y, w);
        ArrayList<WeightedObservedPoint> points_ = fitter.points;

        //final double coeffs[] = fitter.fit(points_);
        //fitter.initialise(4, 1);

        double coeffs[] = fitter.getFitParamsAndError();
        System.out.println(Arrays.toString(coeffs));
    }
}
