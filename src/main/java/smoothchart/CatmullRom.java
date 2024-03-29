package smoothchart;

import javafx.geometry.Point2D;


/**
 * User: hansolo
 * Date: 03.11.17
 * Time: 04:47
 */
public class CatmullRom {
    private CatmullRomSpline splineXValues;
    private CatmullRomSpline splineYValues;


    // ******************** Constructors **************************************
    public CatmullRom(final Point2D P0, final Point2D P1, final Point2D P2, final Point2D P3) {
        assert P0 != null : "p0 cannot be null";
        assert P1 != null : "p1 cannot be null";
        assert P2 != null : "p2 cannot be null";
        assert P3 != null : "p3 cannot be null";

        splineXValues = new CatmullRomSpline(P0.getX(), P1.getX(), P2.getX(), P3.getX());
        splineYValues = new CatmullRomSpline(P0.getY(), P1.getY(), P2.getY(), P3.getY());
    }


    // ******************** Methods *******************************************
    public Point2D q(final double T) { return new Point2D(splineXValues.q(T), splineYValues.q(T)); }


    // ******************** Inner Classes *************************************
    class CatmullRomSpline {
        private double p0;
        private double p1;
        private double p2;
        private double p3;


        // ******************** Constructors **************************************
        protected CatmullRomSpline(final double P0, final double P1, final double P2, final double P3) {
            p0 = P0;
            p1 = P1;
            p2 = P2;
            p3 = P3;
        }


        // ******************** Methods *******************************************
        protected double q(final double T) {
            return 0.5 * ((2 * p1) + (p2 - p0) * T + (2 * p0 - 5 * p1 + 4 * p2 - p3) * T * T + (3 * p1 - p0 - 3 * p2 + p3) * T * T * T);
        }
    }
}