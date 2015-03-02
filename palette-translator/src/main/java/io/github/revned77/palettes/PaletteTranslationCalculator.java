package io.github.revned77.palettes;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

class PaletteTranslationCalculator {

  public Map<Integer, Integer> getPaletteTranslation(
      BufferedImage sourceImage, BufferedImage targetImage) {
    if (sourceImage.getHeight() != targetImage.getHeight()) {
      throw new IllegalArgumentException("Images have unequal heights");
    } else if (sourceImage.getWidth() != targetImage.getWidth()) {
      throw new IllegalArgumentException("Images have unequal widths");
    }

    Map<Integer, Integer> translation = new HashMap<>();
    for (int x = 0; x < sourceImage.getWidth(); x++) {
      for (int y = 0; y < sourceImage.getHeight(); y++) {
        int sourceRgb = sourceImage.getRGB(x, y);
        int targetRgb = targetImage.getRGB(x, y);

        if (!translation.containsKey(sourceRgb)) {
          if (translation.values().contains(targetRgb)) {
            System.err.println(String.format(
                "Warning: Multiple source colors map to a single target color at (%d, %d)", x, y));
          }
          translation.put(sourceRgb, targetRgb);
        } else {
          if (translation.get(sourceRgb) != targetRgb) {
            throw new IllegalArgumentException(String.format(
                "A single source color maps to multiple target colors at (%d, %d): %s -> %s and %s",
                x,
                y,
                Rgb.formatRgb(sourceRgb),
                Rgb.formatRgb(translation.get(sourceRgb)),
                Rgb.formatRgb(targetRgb)));
          }
        }
      }
    }
    return translation;
  }
}
