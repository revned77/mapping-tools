package io.github.revned77.tiles;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class Slicer {

  @Option(name = "-w", usage = "Tile width")
  private int tileWidth = 16;

  @Option(name = "-h", usage = "Tile height")
  private int tileHeight = 16;

  @Argument(metaVar = "FILENAME", index = 0, required = true, usage = "Filename")
  private String filename;

  @Argument(metaVar = "OUTPUT_DIR", index = 1, required = true, usage = "Output directory")
  private String outputDir;

  public void run() throws IOException {
    long start = System.currentTimeMillis();

    BufferedImage image = ImageIO.read(new File(filename));
    System.err.println("Loaded image in " + (System.currentTimeMillis() - start) + " ms");
    start = System.currentTimeMillis();

    String[][] grid = new String[image.getWidth() / tileWidth][image.getHeight() / tileHeight];
    Map<Tile, String> tileMap = new HashMap<Tile, String>();
    int currentTile = 0;
    
    for (int x = 0; x < grid.length; x++) {
      for (int y = 0; y < grid[0].length; y++) {
        Tile tile = new Tile(
            image.getSubimage(x * tileWidth, y * tileHeight, tileWidth, tileHeight));
        if (tileMap.containsKey(tile)) {
          grid[x][y] = tileMap.get(tile);
        } else {
          String tileId = String.format("%-4d", currentTile);
          tileMap.put(tile, tileId);
          grid[x][y] = tileId;
          currentTile++;
        }
      }
    }

    System.err.println("Sliced in " + (System.currentTimeMillis() - start) + " ms. Found "
        + currentTile + " unique tiles");
    start = System.currentTimeMillis();

    for (Map.Entry<Tile, String> entry : tileMap.entrySet()) {
      ImageIO.write(
          entry.getKey().getImage(),
          "PNG",
          new File(outputDir + File.separator + entry.getValue().trim() + ".png"));
    }

    BufferedWriter out =
        new BufferedWriter(new FileWriter(new File(outputDir + File.separator + "tiles.txt")));
    for (int y = 0; y < grid[0].length; y++) {
      for (int x = 0; x < grid.length; x++) {
        out.write(grid[x][y]);
        out.write(' ');
      }
      out.newLine();
    }
    out.close();
    System.err.println("Wrote tiles in " + (System.currentTimeMillis() - start) + " ms");
  }
}
