package io.github.revned77.tiles;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.kohsuke.args4j.Argument;

public class Splicer {

  @Argument(metaVar = "TILES_DIR", index = 0, required = true, usage = "Tiles directory")
  private String tilesDir;

  @Argument(metaVar = "OUTPUT_FILENAME", index = 1, required = true, usage = "Output filename")
  private String outputFilename;

  public void run() throws IOException {
    long start = System.currentTimeMillis();

    BufferedReader reader =
        new BufferedReader(new FileReader(tilesDir + File.separator + "tiles.txt"));
    List<String> lines = new ArrayList<>();
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      lines.add(line);
    }
    reader.close();

    int[][] grid = new int[lines.size()][new StringTokenizer(lines.get(0), " ").countTokens()];
    int row = 0, col = 0;
    for (String line : lines) {
      StringTokenizer tokenizer = new StringTokenizer(line, " ");
      while (tokenizer.hasMoreTokens()) {
        grid[row][col] = Integer.parseInt(tokenizer.nextToken());
        col++;
      }
      col = 0;
      row++;
    }

    System.err.println("Loaded tiles.txt in " + (System.currentTimeMillis() - start) + " ms");
    start = System.currentTimeMillis();

    Map<Integer, Tile> tileMap = new HashMap<Integer, Tile>();
    File dir = new File(tilesDir);
    for (File file : dir.listFiles()) {
      if (file.getName().endsWith(".png")) {
        tileMap.put(
            Integer.parseInt(file.getName().substring(0, file.getName().indexOf('.'))),
            new Tile(ImageIO.read(file)));
      }
    }

    System.err.println("Loaded tiles in " + (System.currentTimeMillis() - start) + " ms");
    start = System.currentTimeMillis();
    
    int tileWidth = tileMap.values().iterator().next().getImage().getWidth();
    int tileHeight = tileMap.values().iterator().next().getImage().getHeight();

    BufferedImage image = new BufferedImage(
        grid[0].length * tileWidth, grid.length * tileHeight, BufferedImage.TYPE_INT_ARGB);
    for (int x = 0; x < grid[0].length; x++) {
      for (int y = 0; y < grid.length; y++) {
        Tile tile = tileMap.get(grid[y][x]);
        image.setRGB(
            x * tileWidth,
            y * tileHeight,
            tileWidth,
            tileHeight,
            tile.getImage().getRGB(0, 0, tileWidth, tileHeight, null, 0, 4 * tileWidth),
            0,
            4 * tileWidth);
      }
    }

    System.err.println("Assembled image in " + (System.currentTimeMillis() - start) + " ms");
    start = System.currentTimeMillis();

    ImageIO.write(image, "PNG", new File(outputFilename));
    System.err.println("Wrote image in " + (System.currentTimeMillis() - start) + " ms");
  }
}
