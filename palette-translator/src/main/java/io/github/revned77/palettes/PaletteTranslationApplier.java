package io.github.revned77.palettes;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class PaletteTranslationApplier {

  private final boolean ignoreUnkownMappings;

  public PaletteTranslationApplier(boolean ignoreUnknownMappings) {
    this.ignoreUnkownMappings = ignoreUnknownMappings;
  }

  public BufferedImage apply(BufferedImage image, Map<Integer, Integer> translation) {
    BufferedImage translated =
        new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    Set<String> ignoredColors = new HashSet<>();
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        int sourceRgb = image.getRGB(x, y);
        Integer targetRgb = translation.get(sourceRgb);
        if (targetRgb == null) {
          if (!ignoreUnkownMappings) {
            throw new IllegalArgumentException(String.format(
                "Missing translation for pixel at (%d, %d): %s",
                x,
                y,
                Rgb.formatRgb(sourceRgb)));
          } else {
            ignoredColors.add(Rgb.formatRgb(sourceRgb));
          }
          targetRgb = sourceRgb;
        }
        translated.setRGB(x, y, targetRgb);
      }
    }

    if (ignoreUnkownMappings && !ignoredColors.isEmpty()) {
      System.err.println("Warning: Ignored missing translations for colors: " + ignoredColors);
    }

    return translated;
  }
}
