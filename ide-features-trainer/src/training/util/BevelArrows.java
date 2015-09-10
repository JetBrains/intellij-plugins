package training.util;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class BevelArrows
{
    public static void main ( String...args )
    {
        SwingUtilities.invokeLater ( new Runnable () {
            BevelArrows arrows = new BevelArrows();

            @Override
            public void run () {
                JFrame frame = new JFrame ( "Bevel Arrows" );

                frame.add ( new JPanel() {
                    public void paintComponent ( Graphics g ) {
                        arrows.draw ( ( Graphics2D ) g, getWidth(), getHeight() );
                    }
                }
                        , BorderLayout.CENTER );

                frame.setSize ( 800, 400 );
                frame.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
                frame.setVisible ( true );
            }
        } );
    }

    interface Arrow {
        void draw ( Graphics2D g );
    }

    Arrow[] arrows = { new LineArrow(), new CurvedArrow() };

    void draw ( Graphics2D g, int width, int height )
    {
        g.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        g.setColor ( Color.WHITE );
        g.fillRect ( 0, 0, width, height );

        for ( Arrow arrow : arrows ) {
            g.setColor ( Color.ORANGE );
            g.fillRect ( 350, 20, 20, 280 );

            g.setStroke ( new BasicStroke ( 20.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL ) );
            g.translate ( 0, 60 );
            arrow.draw ( g );

            g.setStroke ( new BasicStroke ( 20.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER ) );
            g.translate ( 0, 100 );
            arrow.draw ( g );

            g.setStroke ( new BasicStroke ( 20.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND ) );
            g.translate ( 0, 100 );
            arrow.draw ( g );

            g.translate ( 400, -260 );
        }
    }

    static class LineArrow  implements Arrow
    {
        public void draw ( Graphics2D g )
        {
            // where the control point for the intersection of the V needs calculating
            // by projecting where the ends meet

            float scaleFactor = 0.2f;

            float arrowRatio = 0.5f;
            float arrowLength = 80.0f*scaleFactor;

            BasicStroke stroke = ( BasicStroke ) g.getStroke();

            g.setStroke(new BasicStroke(stroke.getLineWidth() * scaleFactor, stroke.getEndCap(), stroke.getLineJoin()));

            float endX = 350.0f;

            float veeX;

            switch ( stroke.getLineJoin() ) {
                case BasicStroke.JOIN_BEVEL:
                    // IIRC, bevel varies system to system, this is approximate
                    veeX = endX - stroke.getLineWidth() * 0.25f*scaleFactor;
                    break;
                default:
                case BasicStroke.JOIN_MITER:
                    veeX = endX - stroke.getLineWidth() * 0.5f / arrowRatio * scaleFactor;
                    break;
                case BasicStroke.JOIN_ROUND:
                    veeX = endX - stroke.getLineWidth() * 0.5f * scaleFactor;
                    break;
            }

            // vee
            Path2D.Float path = new Path2D.Float();

            path.moveTo ( veeX - arrowLength, -arrowRatio * arrowLength );
            path.lineTo ( veeX, 0.0f );
            path.lineTo ( veeX - arrowLength, arrowRatio * arrowLength );

            g.setColor ( Color.BLUE );
            g.draw ( path );

            // stem for exposition only
            g.setColor ( Color.YELLOW );
            g.draw ( new Line2D.Float ( 50.0f, 0.0f, veeX, 0.0f ) );

            // in practice, move stem back a bit as rounding errors
            // can make it poke through the sides of the Vee
            g.setColor ( Color.RED );
            g.draw ( new Line2D.Float ( 50.0f, 0.0f, veeX - stroke.getLineWidth() * 0.25f, 0.0f ) );
        }
    }

    static class CurvedArrow  implements Arrow
    {
        // to draw a nice curved arrow, fill a V shape rather than stroking it with lines
        public void draw ( Graphics2D g )
        {
            // as we're filling rather than stroking, control point is at the apex,

            float arrowRatio = 0.5f;
            float arrowLength = 80.0f;

            BasicStroke stroke = ( BasicStroke ) g.getStroke();

            float endX = 350.0f;

            float veeX = endX - stroke.getLineWidth() * 0.5f / arrowRatio;

            // vee
            Path2D.Float path = new Path2D.Float();

            float waisting = 0.5f;

            float waistX = endX - arrowLength * 0.5f;
            float waistY = arrowRatio * arrowLength * 0.5f * waisting;
            float arrowWidth = arrowRatio * arrowLength;

            path.moveTo ( veeX - arrowLength, -arrowWidth );
            path.quadTo ( waistX, -waistY, endX, 0.0f );
            path.quadTo ( waistX, waistY, veeX - arrowLength, arrowWidth );

            // end of arrow is pinched in
            path.lineTo ( veeX - arrowLength * 0.75f, 0.0f );
            path.lineTo ( veeX - arrowLength, -arrowWidth );

            g.setColor ( Color.BLUE );
            g.fill ( path );

            // move stem back a bit
            g.setColor ( Color.RED );
            g.draw ( new Line2D.Float ( 50.0f, 0.0f, veeX - arrowLength * 0.5f, 0.0f ) );
        }
    }
}