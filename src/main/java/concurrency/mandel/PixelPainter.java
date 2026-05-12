package mandel;

import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class PixelPainter implements Runnable {
    private int maxCount = 192; // maximum number of iterations
    private boolean smooth = false;
    private boolean antialias = false;

    private int pal = 0; // current palette

    private Color[][] pixels;
    private Color[][] colors;

    private int width, height; // current screen width and height

    // currently visible relative window dimensions
    private double viewX = 0.0;
    private double viewY = 0.0;
    private double zoom = 1.0;

    CountDownLatch latch;

    // rows for current thread
    int startY;
    int endY;

    public PixelPainter(int startY, int endY, int height, int width, Color[][] colors, Color[][] pixels, int maxCount, double viewX, double viewY, double zoom, CountDownLatch latch) {
        this.startY = startY;
        this.endY = endY;
        this.height = height;
        this.width = width;
        this.colors = colors;
        this.pixels = pixels;
        this.maxCount = maxCount;
        this.viewX = viewX;
        this.viewY = viewY;
        this.zoom = zoom;
        this.latch = latch;
    }

    @Override
    public void run() {
        paintPixels();
        if (latch != null) {
            latch.countDown();
        }
    }

    public void paintPixels() {
        for (int y = startY; y < endY; y++) {
            for (int x = 0; x < width; x++) {
                Color color = getColor(x, y);
                pixels[y][x] = color;
            }
        }
    }

    // gets a color for a specific pixel
    public Color getColor(int x, int y) {
        double r = zoom / Math.min(width, height);
        double dx = 2.5 * (x * r + viewX) - 2;
        double dy = 1.25 - 2.5 * (y * r + viewY);
        Color color = color(dx, dy);
        // computation of average color for antialiasing
        if (antialias) {
            Color c1 = color(dx - 0.25 * r, dy - 0.25 * r);
            Color c2 = color(dx + 0.25 * r, dy - 0.25 * r);
            Color c3 = color(dx + 0.25 * r, dy + 0.25 * r);
            Color c4 = color(dx - 0.25 * r, dy + 0.25 * r);
            int red = (color.getRed() + c1.getRed() + c2.getRed() + c3.getRed() + c4.getRed()) / 5;
            int green = (color.getGreen() + c1.getGreen() + c2.getGreen() + c3.getGreen() + c4.getGreen())
                    / 5;
            int blue = (color.getBlue() + c1.getBlue() + c2.getBlue() + c3.getBlue() + c4.getBlue()) / 5;
            color = new Color(red, green, blue);
        }
        return color;
    }

    // Computes a color for a given point
    private Color color(double x, double y) {
        int count = mandel(0.0, 0.0, x, y);
        int palSize = colors[pal].length;
        Color color = colors[pal][count / 256 % palSize];
        if (smooth) {
            Color color2 = colors[pal][(count / 256 + palSize - 1) % palSize];
            int k1 = count % 256;
            int k2 = 255 - k1;
            int red = (k1 * color.getRed() + k2 * color2.getRed()) / 255;
            int green = (k1 * color.getGreen() + k2 * color2.getGreen()) / 255;
            int blue = (k1 * color.getBlue() + k2 * color2.getBlue()) / 255;
            color = new Color(red, green, blue);
        }
        return color;
    }

    // Computes a value for a given complex number
    private int mandel(double zRe, double zIm, double pRe, double pIm) {
        double zRe2 = zRe * zRe;
        double zIm2 = zIm * zIm;
        double zM2 = 0.0;
        int count = 0;
        while (zRe2 + zIm2 < 4.0 && count < maxCount) {
            zM2 = zRe2 + zIm2;
            zIm = 2.0 * zRe * zIm + pIm;
            zRe = zRe2 - zIm2 + pRe;
            zRe2 = zRe * zRe;
            zIm2 = zIm * zIm;
            count++;
        }
        if (count == 0 || count == maxCount)
            return 0;
        // transition smoothing
        zM2 += 0.000000001;
        return 256 * count + (int) (255.0 * Math.log(4 / zM2) / Math.log((zRe2 + zIm2) / zM2));
    }
}
