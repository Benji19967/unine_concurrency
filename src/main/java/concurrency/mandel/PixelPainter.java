package mandel;

import java.awt.*;

public class PixelPainter {
    private int maxCount = 192; // maximum number of iterations
    private boolean smooth = false;
    private boolean antialias = false;

    private Color[][] colors; // palettes
    private int pal = 0; // current palette
    private static final int[][][] colpal = { // palette colors
            { { 12, 0, 10, 20 }, { 12, 50, 100, 240 }, { 12, 20, 3, 26 }, { 12, 230, 60, 20 },
                    { 12, 25, 10, 9 }, { 12, 230, 170, 0 }, { 12, 20, 40, 10 }, { 12, 0, 100, 0 },
                    { 12, 5, 10, 10 }, { 12, 210, 70, 30 }, { 12, 90, 0, 50 }, { 12, 180, 90, 120 },
                    { 12, 0, 20, 40 }, { 12, 30, 70, 200 } },
            { { 10, 70, 0, 20 }, { 10, 100, 0, 100 }, { 14, 255, 0, 0 }, { 10, 255, 200, 0 } },
            { { 8, 40, 70, 10 }, { 9, 40, 170, 10 }, { 6, 100, 255, 70 }, { 8, 255, 255, 255 } },
            { { 12, 0, 0, 64 }, { 12, 0, 0, 255 }, { 10, 0, 255, 255 }, { 12, 128, 255, 255 }, { 14, 64, 128, 255 } },
            { { 16, 0, 0, 0 }, { 32, 255, 255, 255 } },
    };

    private int width, height; // current screen width and height

    // currently visible relative window dimensions
    private double viewX = 0.0;
    private double viewY = 0.0;
    private double zoom = 1.0;

    public PixelPainter(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public void nextPal() {
        pal = (pal + 1) % colors.length;
    }

    public void updateMaxCount() {
        maxCount += maxCount / 4; // increase the number of iterations by 1/4
    }

    public void setViewsAndZoom(double viewX, double viewY, double zoom) {
        this.viewX = viewX;
        this.viewY = viewY;
        this.zoom = zoom;
    }

    public void setWidthAndHeight(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void initColorPalettes() {
        colors = new Color[colpal.length][];
        for (int p = 0; p < colpal.length; p++) { // process all palettes
            int n = 0;
            for (int i = 0; i < colpal[p].length; i++) // get the number of all colors
                n += colpal[p][i][0];
            colors[p] = new Color[n]; // allocate pallete
            n = 0;
            for (int i = 0; i < colpal[p].length; i++) { // interpolate all colors
                int[] c1 = colpal[p][i]; // first referential color
                int[] c2 = colpal[p][(i + 1) % colpal[p].length]; // second ref. color
                for (int j = 0; j < c1[0]; j++) // linear interpolation of RGB values
                    colors[p][n + j] = new Color(
                            (c1[1] * (c1[0] - 1 - j) + c2[1] * j) / (c1[0] - 1),
                            (c1[2] * (c1[0] - 1 - j) + c2[2] * j) / (c1[0] - 1),
                            (c1[3] * (c1[0] - 1 - j) + c2[3] * j) / (c1[0] - 1));
                n += c1[0];
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
