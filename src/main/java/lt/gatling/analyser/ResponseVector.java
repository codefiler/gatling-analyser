package lt.gatling.analyser;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.apache.commons.math.util.MathUtils;

import java.util.ArrayList;

public class ResponseVector {
    private ArrayList<Double> values = null;
    private int numOfErrors = 0;

    private double[] simpleArray() {
        double[] arr = new double[values.size()];

        for (int i = 0; i < values.size(); i++) {
            arr[i] = values.get(i);
        }
        return arr;
    }

    public void addError() {
        numOfErrors++;
    }

    public int getErrors() {
        return numOfErrors;
    }

    public int getPass() {
        return values.size();
    }

    public void add(double v) {
        if  (values == null) {
            values = new ArrayList();
        }
        values.add(v);
    }

    public double getPercentile(int percentile) {
        Percentile p = new Percentile();
        p.setData(simpleArray());
        p.setQuantile(percentile);
        return MathUtils.round(p.evaluate(), 3);
    }

    public double getMin() {
        Min m = new Min();
        m.setData(simpleArray());
        return MathUtils.round(m.evaluate(), 3);
    }

    public double getMax() {
        Max m = new Max();
        m.setData(simpleArray());
        return MathUtils.round(m.evaluate(), 3);
    }

    public double getAvg() {
        Mean m = new Mean();
        m.setData(simpleArray());
        return MathUtils.round(m.evaluate(), 3);
    }

    public double getStdDev() {
        StandardDeviation s = new StandardDeviation();
        s.setData(simpleArray());
        return MathUtils.round(s.evaluate(), 3);
    }
}

