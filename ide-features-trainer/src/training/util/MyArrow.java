package training.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class MyArrow {
    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            MyArrow arrows = new MyArrow();

            @Override
            public void run() {
                JFrame frame = new JFrame("Bevel Arrows");

                frame.add(new JPanel() {
                    public void paintComponent(Graphics g) {
                        arrows.draw((Graphics2D) g, getWidth(), getHeight());
                    }
                }
                        , BorderLayout.CENTER);

                frame.setSize(800, 400);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }

    interface Arrow {
        void draw(Graphics2D g);
    }

    Arrow[] arrows = {new LineArrow(
            new Point2D.Double(30.0d, 30.0d),
            new Point2D.Double(150.0d, 80.0d),
            new Point2D.Double(120.0d, 150.0d),
            Color.BLACK)};

    void draw(Graphics2D g, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        for (Arrow arrow : arrows) {
            g.setColor(Color.ORANGE);
            g.fillRect(350, 20, 20, 280);

            g.setStroke(new BasicStroke(20.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g.translate(0, 100);
            arrow.draw(g);

            g.translate(400, -260);
        }
    }

    public static class LineArrow implements Arrow {

        private Point2D p0;
        private Point2D pc;
        private Point2D p1;
        private Color color;
        private float scaleFactor = 0.2f;
        private float thickness = 20.0f;

        public LineArrow(Point2D p0, Point2D pc, Point2D p1, Color color) {
            this.color = color;
            this.p0 = p0;
            this.pc = pc;
            this.p1 = p1;
        }

        public void setArrow(Point2D p0, Point2D pc, Point2D p1) {
            this.p0 = p0;
            this.pc = pc;
            this.p1 = p1;
        }

        public Point2D.Double[] getPointPath() {
            Path2D path = new Path2D.Double();
            QuadCurve2D q = new QuadCurve2D.Float();
            q.setCurve(p0, pc, p1);

            PathIterator iter = new FlatteningPathIterator(q.getPathIterator(new AffineTransform()), 0.4f);
            ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
            float[] coords = new float[6];
            while (!iter.isDone()) {
                iter.currentSegment(coords);
                int x = (int) coords[0];
                int y = (int) coords[1];
                points.add(new Point2D.Double(x, y));
                iter.next();
            }

            Point2D.Double[] points2d = new Point2D.Double[points.size()];
            points2d = points.toArray(points2d);
            return points2d;
        }

        public void setScaleFactor(float scaleFactor) {
            this.scaleFactor = scaleFactor;
        }

        public void setThickness(float thickness) {
            this.thickness = thickness;
        }

        public void draw(Graphics2D g) {
            if (p0 == p1) return;

            // where the control point for the intersection of the V needs calculating
            // by projecting where the ends meet
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

            float scaleFactor = 0.1f;

            float arrowRatio = 0.5f;
            float arrowLength = 80.0f * scaleFactor;

            BasicStroke stroke = (BasicStroke) g.getStroke();

            g.setStroke(new BasicStroke(stroke.getLineWidth() * scaleFactor, stroke.getEndCap(), stroke.getLineJoin()));

            float veeX;

            switch (stroke.getLineJoin()) {
                case BasicStroke.JOIN_BEVEL:
                    // IIRC, bevel varies system to system, this is approximate
                    veeX = 0.0f - stroke.getLineWidth() * 0.25f * scaleFactor;
                    break;
                default:
                case BasicStroke.JOIN_MITER:
                    veeX = 0.0f - stroke.getLineWidth() * 0.5f / arrowRatio * scaleFactor;
                    break;
                case BasicStroke.JOIN_ROUND:
                    veeX = 0.0f - stroke.getLineWidth() * 0.5f * scaleFactor;
                    break;
            }

            // vee
            Path2D.Float path = new Path2D.Float();
            path.moveTo(veeX - arrowLength, -arrowRatio * arrowLength);
            path.lineTo(veeX, 0.0f);
            path.lineTo(veeX - arrowLength, arrowRatio * arrowLength);
            path.moveTo(veeX, 0.0f);

            AffineTransform at = new AffineTransform();
            at.translate(p1.getX(), p1.getY());
            at.rotate(getAngle(pc, p1));
            path.transform(at);

            g.setColor(color);
            QuadCurve2D q = new QuadCurve2D.Float();
            q.setCurve(p1, pc, p0);
            path.quadTo(pc.getX(), pc.getY(), p0.getX(), p0.getY());
            g.draw(path);

        }
    }

    static class CurvedArrow implements Arrow {
        // to draw a nice curved arrow, fill a V shape rather than stroking it with lines
        public void draw(Graphics2D g) {
            // as we're filling rather than stroking, control point is at the apex,

            float arrowRatio = 0.5f;
            float arrowLength = 80.0f;

            BasicStroke stroke = (BasicStroke) g.getStroke();

            float endX = 350.0f;

            float veeX = endX - stroke.getLineWidth() * 0.5f / arrowRatio;

            // vee
            Path2D.Float path = new Path2D.Float();

            float waisting = 0.5f;

            float waistX = endX - arrowLength * 0.5f;
            float waistY = arrowRatio * arrowLength * 0.5f * waisting;
            float arrowWidth = arrowRatio * arrowLength;

            path.moveTo(veeX - arrowLength, -arrowWidth);
            path.quadTo(waistX, -waistY, endX, 0.0f);
            path.quadTo(waistX, waistY, veeX - arrowLength, arrowWidth);

            // end of arrow is pinched in
            path.lineTo(veeX - arrowLength * 0.75f, 0.0f);
            path.lineTo(veeX - arrowLength, -arrowWidth);

            g.setColor(Color.BLUE);
            g.fill(path);

            // move stem back a bit
            g.setColor(Color.RED);
            g.draw(new Line2D.Float(50.0f, 0.0f, veeX - arrowLength * 0.5f, 0.0f));
        }
    }


    /**
     * getAngle calculates an angle between line (zeroPoint, target) and X-axis line;
     *
     * @param zeroPoint - start point of line
     * @param target    - end point of line
     * @return angle between line (zeroPoint, target) and X-axis line;
     */
    public static double getAngle(Point2D zeroPoint, Point2D target) {
        double angle = (double) (Math.atan2(target.getY() - zeroPoint.getY(), target.getX() - zeroPoint.getX()));

        if (zeroPoint.getY() > target.getY()) {
            angle += Math.PI;
        }

        return angle;
    }

    /**
     * getAngle calculates an angle between lines (from zeroPoint to a, and from zeroPoint to b)
     *
     * @param zeroPoint - common point of two lines
     * @param a         - end point of the first line
     * @param b         - end point of the first line
     * @return angle between lines (from zeroPoint to a, and from zeroPoint to b)
     */
    public double getAngle(Point zeroPoint, Point a, Point b) {
        return (getAngle(zeroPoint, a) - getAngle(zeroPoint, b));
    }

//    public Point2D distanceBack(Point2D zeroPoint, Point target, double distance){
//
//    }


}