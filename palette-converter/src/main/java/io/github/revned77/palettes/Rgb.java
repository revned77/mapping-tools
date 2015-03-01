package io.github.revned77.palettes;

public class Rgb {

  public static String formatRgb(int rgb) {
    return String.format("#%02x%02x%02x%02x",
        ((0xFF000000 & rgb) >>> 24),
        ((0x00FF0000 & rgb) >> 16),
        ((0x0000FF00 & rgb) >> 8),
        (0x000000FF & rgb));
  }
}
