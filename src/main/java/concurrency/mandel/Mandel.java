package mandel;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;

public final class Mandel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private int maxCount = 192; // maximum number of iterations
    private boolean smooth = false; // smoothing state
    private boolean antialias = false; // antialias state
    private boolean toDrag = false; // dragging state
    private boolean rect = true; // zooming or moving mode for dragging
    private int pal = 0; // current palette

    // currently visible relative window dimensions
    private double viewX = 0.0;
    private double viewY = 0.0;
    private double zoom = 1.0;

    private Image image; // offscreen image for double buffering
    private Graphics graphics; // offscreen graphics for the offscreen image
    private int width, height; // current screen width and height

    private JLabel status;
    private long time;

    private int mouseX, mouseY; // mouse position when the button was pressed
    private int dragX, dragY; // current mouse position during dragging

    private Color[][] colors; // palettes
    private final int nThreads = 4;
    private final ColorManager colorManager = new ColorManager();
    private final ExecutorService pool = Executors.newFixedThreadPool(nThreads);

    public void init() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        colors = colorManager.initColorPalettes();
    }

    public void start() {
        redraw();
    }

    private void redraw() {
        this.draw();
    }

    public void updateStatus() {
        this.status.setText("Time=" + this.time + " ms");
    }

    private boolean draw() {
        Dimension size = getSize();
        // create offscreen buffer for double buffering
        if (image == null || size.width != width || size.height != height) {
            width = size.width;
            height = size.height;
            image = createImage(width, height);
            graphics = image.getGraphics();
        }

        // fractal image drawing
        this.time = System.currentTimeMillis();
        Color[][] pixels = new Color[height][width];
        if (nThreads == 1) {
            PixelPainter pixelPainter = new PixelPainter(0, height, height, width, colors, pixels, maxCount, viewX, viewY, zoom, null);
            pixelPainter.paintPixels();
        }
        else {
            CountDownLatch latch = new CountDownLatch(nThreads);
            int startY;
            int endY;
            int nRowsPerThread = height / nThreads;
            for (int i = 0; i < nThreads; i++) {
                startY = i * nRowsPerThread;
                endY = i == nThreads - 1 ? height : Math.min(startY + nRowsPerThread, height);
                pool.execute(new PixelPainter(startY, endY, height, width, colors, pixels, maxCount, viewX, viewY, zoom, latch));
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.time = System.currentTimeMillis() - this.time;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                graphics.setColor(pixels[y][x]);
                graphics.drawLine(x, y, x, y);
            }
        }

        this.repaint();
        this.updateStatus();

        return false;
    }

    // To prevent background clearing for each paint()
    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        if (image == null) // nothing to show
            return;
        Dimension size = getSize();
        if (size.width != width || size.height != height) {
            redraw();
            return;
        }
        g.drawImage(image, 0, 0, null);
        // select-rectangle or offset-line drawing
        if (toDrag) {
            g.setColor(Color.black);
            g.setXORMode(Color.white);
            if (rect) {
                int x = Math.min(mouseX, dragX);
                int y = Math.min(mouseY, dragY);
                double w = mouseX + dragX - 2 * x;
                double h = mouseY + dragY - 2 * y;
                double r = Math.max(w / width, h / height);
                g.drawRect(x, y, (int) (width * r), (int) (height * r));
            } else
                g.drawLine(mouseX, mouseY, dragX, dragY);
        }
    }

    // methods from MouseListener interface

    public void mousePressed(MouseEvent e) {
        mouseX = dragX = e.getX();
        mouseY = dragY = e.getY();
        toDrag = true;
    }

    public void mouseReleased(MouseEvent e) {
        toDrag = false;
        int x = e.getX();
        int y = e.getY();
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) { // LMB
            double r = zoom / Math.min(width, height); // actual pixel size
            if (!rect) { // moved
                viewX += (mouseX - x) * r;
                viewY += (mouseY - y) * r;
            } else if (x == mouseX && y == mouseY) { // zoom in
                viewX += 0.5 * x * r;
                viewY += 0.5 * y * r;
                zoom *= 0.5;
            } else { // zoomed
                int mx = Math.min(x, mouseX);
                int my = Math.min(y, mouseY);
                viewX += mx * r;
                viewY += my * r;
                double w = x + mouseX - 2 * mx;
                double h = y + mouseY - 2 * my;
                zoom *= Math.max(w / width, h / height);
            }
            redraw(); // recompute and repaint
        } else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) { // RMB
            maxCount += maxCount / 4; // increase the number of iterations by 1/4
            redraw(); // recompute and repaint
        }
    }

    public void mouseClicked(MouseEvent e) {
    } // not used

    public void mouseEntered(MouseEvent e) {
    } // not used

    public void mouseExited(MouseEvent e) {
    } // not used

    // methods from MouseMotionListener interface

    public void mouseDragged(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) { // LMB drag
            dragX = e.getX();
            dragY = e.getY();
            repaint(); // only repaint - no recomputation
        }
    }

    public void mouseMoved(MouseEvent e) {
    } // not used

    // methods from KeyListener interface

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { // init
            viewX = viewY = 0.0;
            zoom = 1.0;
            redraw(); // recompute and repaint
        } else if (e.getKeyCode() == KeyEvent.VK_I) { // zoom in
            viewX += 0.25 * zoom;
            viewY += 0.25 * zoom;
            zoom *= 0.5;
            redraw(); // recompute and repaint
        } else if (e.getKeyCode() == KeyEvent.VK_O) { // zoom out
            viewX -= 0.5 * zoom;
            viewY -= 0.5 * zoom;
            zoom *= 2.0;
            redraw(); // recompute and repaint
        } else if (e.getKeyCode() == KeyEvent.VK_P) { // next palette
            pal = (pal + 1) % colors.length;            redraw(); // recompute and repaint
        } else if (e.getKeyCode() == KeyEvent.VK_S) { // smoothing
            smooth = !smooth;
            redraw(); // recompute and repaint
        } else if (e.getKeyCode() == KeyEvent.VK_A) { // antialiasing
            antialias = !antialias;
            redraw(); // recompute and repaint
        } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) { // move mode
            rect = false; // offset line (not selecting rectangle)
            if (toDrag) // repaint only when dragging is performed
                repaint(); // only repaint - no recomputation
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) { // zoom mode
            rect = true; // selecting rectangle (not offset line)
            if (toDrag) // repaint only when dragging is performed
                repaint(); // only repaint - no recomputation
        }
    }

    public void keyTyped(KeyEvent e) {
    } // not used

    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame("Mandelbrot Explorer");
        frame.setLayout(new BorderLayout());

        Mandel mandel = new Mandel();
        mandel.status = new JLabel("Starting up...");
        mandel.status.setBorder(new BevelBorder(BevelBorder.LOWERED));

        // Add components to the frame
        frame.add(mandel, BorderLayout.CENTER);
        frame.add(mandel.status, BorderLayout.SOUTH);

        frame.add(mandel);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        mandel.init();
        mandel.start();
    }
}
