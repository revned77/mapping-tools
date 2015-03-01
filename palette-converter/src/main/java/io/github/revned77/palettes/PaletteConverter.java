package io.github.revned77.palettes;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class PaletteConverter {

  @Option(name = "-s", usage = "Source file", required = true)
  private String sourceFileName;

  @Option(name = "-t", usage = "Target file", required = true)
  private String targetFileName;

  @Option(name = "-f", usage = "File to convert", required = true)
  private String fileName;

  @Option(name = "-o", usage = "Output file", required = true)
  private String outputFileName;

  @Option(name = "-x", usage = "Ignore unknown mappings and pass those pixels through as-is")
  private boolean ignoreUnknownMappings = false;

  @Argument
  private final List<String> arguments = new ArrayList<>();

  private void realMain(String[] args) throws Exception {
    CmdLineParser parser = new CmdLineParser(this);
    parser.setUsageWidth(80);

    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.err.println();
      return;
    }

    BufferedImage source = ImageIO.read(new File(sourceFileName));
    BufferedImage target = ImageIO.read(new File(targetFileName));
    Map<Integer, Integer> translation =
        new PaletteTranslationCalculator().getPaletteTranslation(source, target);
    BufferedImage file = ImageIO.read(new File(fileName));
    BufferedImage output =
        new PaletteTranslationApplier(ignoreUnknownMappings).apply(file, translation);
    ImageIO.write(output, "png", new File(outputFileName));
  }

  public static void main(String[] args) throws Exception {
    new PaletteConverter().realMain(args);
  }
}
