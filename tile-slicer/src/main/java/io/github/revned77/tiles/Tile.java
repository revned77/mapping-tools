package io.github.revned77.tiles;

import java.awt.image.BufferedImage;

/** A thin wrapper around {@link BufferedImage} which adds value semantics. */
class Tile {

  private final BufferedImage image;
  private Integer hashCode;

  public Tile(BufferedImage image) {
    this.image = image;
  }

  public BufferedImage getImage() {
    return image;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (!(other instanceof Tile)) {
      return false;
    }

    Tile otherTile = (Tile) other;

    if (otherTile.hashCode != null
        && hashCode != null
        && !otherTile.hashCode.equals(hashCode)) {
      return false;
    } else if (otherTile.image.getWidth() != image.getWidth()
        || otherTile.image.getHeight() != image.getHeight()) {
      return false;
    }

    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        if (otherTile.image.getRGB(x, y) != image.getRGB(x, y)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    if (hashCode != null) {
      return hashCode;
    }

    int hash = 31 * 17 + image.getWidth();
    hash = 31 * hash + image.getHeight();
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        hash = 31 * hash + image.getRGB(x, y);
      }
    }
    hashCode = hash;
    return hash;
  }
}
