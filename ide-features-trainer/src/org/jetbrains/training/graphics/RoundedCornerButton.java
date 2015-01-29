package org.jetbrains.training.graphics;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

class RoundedCornerButton extends JButton {
    private static final float ARC_WIDTH  = 16f;
    private static final float ARC_HEIGHT = 16f;
    protected static final int FOCUS_STROKE = 2;
    protected final Color fc = new Color(100, 150, 255, 200);
    protected final Color ac = new Color(230, 230, 230);
    protected final Color rc = Color.ORANGE;
    protected final Color uf = new Color(0, 0, 0, 0);
    protected Shape shape;
    protected Shape border;
    protected Shape base;
    public RoundedCornerButton() {
        super();
    }
    public RoundedCornerButton(Icon icon) {
        super(icon);
    }
    public RoundedCornerButton(String text) {
        super(text);
    }
    public RoundedCornerButton(Action a) {
        super(a);
        //setAction(a);
    }
    public RoundedCornerButton(String text, Icon icon) {
        super(text, icon);
        //setModel(new DefaultButtonModel());
        //init(text, icon);
        //setContentAreaFilled(false);
        //setBackground(new Color(250, 250, 250));
        //initShape();
    }
    @Override public void updateUI() {
        super.updateUI();
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBackground(new Color(250, 250, 250));
        initShape();
    }
    protected void initShape() {
        if (!getBounds().equals(base)) {
            base = getBounds();
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, ARC_WIDTH, ARC_HEIGHT);
            border = new RoundRectangle2D.Float(FOCUS_STROKE, FOCUS_STROKE,
                    getWidth() - 1 - FOCUS_STROKE * 2,
                    getHeight() - 1 - FOCUS_STROKE * 2,
                    ARC_WIDTH, ARC_HEIGHT);
        }
    }
    private void paintFocusAndRollover(Graphics2D g2, Color color) {
        g2.setPaint(new GradientPaint(0, 0, color, getWidth() - 1, getHeight() - 1, color.brighter(), true));
        g2.fill(shape);
        g2.setColor(getBackground());
        g2.fill(border);
    }

    @Override protected void paintComponent(Graphics g) {
        initShape();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getModel().isArmed()) {
            g2.setColor(ac);
            g2.fill(shape);
        } else if (isRolloverEnabled() && getModel().isRollover()) {
            //paintFocusAndRollover(g2, rc);
        } else if (hasFocus()) {
            //paintFocusAndRollover(g2, uf);
        } else {
//            g2.setColor(getBackground());
//            g2.fill(shape);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setColor(getBackground());
        super.paintComponent(g2);
        g2.dispose();
    }
    @Override protected void paintBorder(Graphics g) {
        initShape();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.draw(shape);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.dispose();
    }
    @Override public boolean contains(int x, int y) {
        initShape();
        return shape == null ? false : shape.contains(x, y);
    }
}

class RoundButton extends RoundedCornerButton {
    public RoundButton() {
        super();
    }
    public RoundButton(Icon icon) {
        super(icon);
    }
    public RoundButton(String text) {
        super(text);
    }
    public RoundButton(Action a) {
        super(a);
        //setAction(a);
    }
    public RoundButton(String text, Icon icon) {
        super(text, icon);
        //setModel(new DefaultButtonModel());
        //init(text, icon);
    }
    @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        int s = Math.max(d.width, d.height);
        d.setSize(s, s);
        return d;
    }
    @Override protected void initShape() {
        if (!getBounds().equals(base)) {
            base = getBounds();
            shape = new Ellipse2D.Float(0, 0, getWidth() - 1, getHeight() - 1);
            border = new Ellipse2D.Float(FOCUS_STROKE, FOCUS_STROKE,
                    getWidth() - 1 - FOCUS_STROKE * 2,
                    getHeight() - 1 - FOCUS_STROKE * 2);
        }
    }
}

class RoundedCornerButtonUI extends BasicButtonUI {
    private static final float ARC_WIDTH  = 16f;
    private static final float ARC_HEIGHT = 16f;
    protected static final int FOCUS_STROKE = 2;
    protected final Color fc = new Color(100, 150, 255);
    protected final Color ac = new Color(220, 225, 230);
    protected final Color rc = Color.ORANGE;
    protected Shape shape;
    protected Shape border;
    protected Shape base;

    @Override protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setBackground(new Color(245, 250, 255));
        b.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        initShape(b);
    }
    @Override protected void installListeners(AbstractButton button) {
        BasicButtonListener listener = new BasicButtonListener(button) {
            @Override public void mousePressed(MouseEvent e) {
                AbstractButton b = (AbstractButton) e.getComponent();
                initShape(b);
                if (shape.contains(e.getX(), e.getY())) {
                    super.mousePressed(e);
                }
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (shape.contains(e.getX(), e.getY())) {
                    super.mouseEntered(e);
                }
            }
            @Override public void mouseMoved(MouseEvent e) {
                if (shape.contains(e.getX(), e.getY())) {
                    super.mouseEntered(e);
                } else {
                    super.mouseExited(e);
                }
            }
        };
        //if (listener != null)
        button.addMouseListener(listener);
        button.addMouseMotionListener(listener);
        button.addFocusListener(listener);
        button.addPropertyChangeListener(listener);
        button.addChangeListener(listener);
    }
    @Override public void paint(Graphics g, JComponent c) {
        initShape(c);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //ContentArea
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();
            if (model.isArmed()) {
                g2.setColor(ac);
                g2.fill(shape);
            } else if (b.isRolloverEnabled() && model.isRollover()) {
                //Don't paint focus frame
                //paintFocusAndRollover(g2, c, rc);
            } else if (b.hasFocus()) {
                //Don't paint focus frame
                //paintFocusAndRollover(g2, c, fc);
            } else {
                g2.setColor(c.getBackground());
                g2.fill(shape);
            }
        }

        //Border
        g2.setPaint(c.getForeground());
        g2.draw(shape);

        g2.dispose();
        //g2.setColor(c.getBackground());
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        super.paint(g, c);
    }
    private void initShape(JComponent c) {
        if (!c.getBounds().equals(base)) {
            base = c.getBounds();
            shape = new RoundRectangle2D.Float(0, 0, c.getWidth() - 1, c.getHeight() - 1, ARC_WIDTH, ARC_HEIGHT);
            border = new RoundRectangle2D.Float(FOCUS_STROKE, FOCUS_STROKE,
                    c.getWidth() - 1 - FOCUS_STROKE * 2,
                    c.getHeight() - 1 - FOCUS_STROKE * 2,
                    ARC_WIDTH, ARC_HEIGHT);
        }
    }
    private void paintFocusAndRollover(Graphics2D g2, JComponent c, Color color) {
        g2.setPaint(new GradientPaint(0, 0, color, c.getWidth() - 1, c.getHeight() - 1, color.brighter(), true));
        g2.fill(shape);
        g2.setColor(c.getBackground());
        g2.fill(border);
    }
}

class ShapeButton extends JButton {
    protected final Color fc = new Color(100, 150, 255, 200);
    protected final Color ac = new Color(230, 230, 230);
    protected final Color rc = Color.ORANGE;
    protected final Shape shape;
    public ShapeButton(Shape s) {
        super();
        shape = s;
        setModel(new DefaultButtonModel());
        init("Shape", new DummySizeIcon(s));
        setVerticalAlignment(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.CENTER);
        setHorizontalAlignment(SwingConstants.CENTER);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBackground(new Color(250, 250, 250));
    }
    private void paintFocusAndRollover(Graphics2D g2, Color color) {
        g2.setPaint(new GradientPaint(0, 0, color, getWidth() - 1, getHeight() - 1, color.brighter(), true));
        g2.fill(shape);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getModel().isArmed()) {
            g2.setColor(ac);
            g2.fill(shape);
        } else if (isRolloverEnabled() && getModel().isRollover()) {
            paintFocusAndRollover(g2, rc);
        } else if (hasFocus()) {
            paintFocusAndRollover(g2, fc);
        } else {
            g2.setColor(getBackground());
            g2.fill(shape);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setColor(getBackground());
        super.paintComponent(g2);
        g2.dispose();
    }
    @Override protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.draw(shape);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.dispose();
    }
    @Override public boolean contains(int x, int y) {
        return shape.contains(x, y);
    }
    /*/ Test
        @Override public Dimension getPreferredSize() {
            Rectangle r = shape.getBounds();
            return new Dimension(r.width, r.height);
        }
    /*/
    private static class DummySizeIcon implements Icon {
        private final Shape shape;
        public DummySizeIcon(Shape s) {
            shape = s;
        }
        @Override public int getIconWidth() {
            return shape.getBounds().width;
        }
        @Override public int getIconHeight() {
            return shape.getBounds().height;
        }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }
    }
//*/
}