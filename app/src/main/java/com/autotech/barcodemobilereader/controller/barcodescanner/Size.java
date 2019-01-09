package com.autotech.barcodemobilereader.controller.barcodescanner;

public class Size implements Comparable<Size> {
    public final int width;
    public final int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Size rotate() {
        return new Size(height, width);
    }

    public Size scale(int n, int d) {
        return new Size(width * n / d, height * n / d);
    }

    public Size scaleFit(Size into) {
        if (width * into.height >= into.width * height) {
            return new Size(into.width, height * into.width / width);
        } else {
            return new Size(width * into.height / height, into.height);
        }
    }

    public Size scaleCrop(Size into) {
        if (width * into.height <= into.width * height) {
            return new Size(into.width, height * into.width / width);
        } else {
            return new Size(width * into.height / height, into.height);
        }
    }

    public boolean fitsIn(Size other) {
        return width <= other.width && height <= other.height;
    }

    @Override
    public int compareTo(Size other) {
        int aPixels = this.height * this.width;
        int bPixels = other.height * other.width;
        if (bPixels < aPixels) {
            return 1;
        }
        if (bPixels > aPixels) {
            return -1;
        }
        return 0;
    }

    public String toString() {
        return width + "x" + height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Size size = (Size) o;

        return width == size.width && height == size.height;

    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }
}
