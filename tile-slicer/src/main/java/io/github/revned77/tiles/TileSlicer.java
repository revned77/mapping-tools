package io.github.revned77.tiles;

import java.util.Arrays;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/** A common entry point to both the slice and splice actions. */
public class TileSlicer {

  public static void main(String[] args) throws Exception {
    if (args.length > 0 && args[0].equals("slice")) {
      Slicer slicer = new Slicer();
      if (tryInjectArgs(slicer, args)) {
        slicer.run();
      }
    } else if (args.length > 0 && args[0].equals("splice")) {
      Splicer splicer = new Splicer();
      if (tryInjectArgs(splicer, args)) {
        splicer.run();
      }
    } else {
      System.err.println("Usage:");
      System.err.println("java TileSlicer slice ...");
      System.err.println("java TileSlicer splice ...");
    }
  }

  private static boolean tryInjectArgs(Object o, String[] args) {
    CmdLineParser parser = new CmdLineParser(o);
    parser.setUsageWidth(80);
    try {
      parser.parseArgument(Arrays.copyOfRange(args, 1, args.length));
      return true;
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.err.println();
      return false;
    }
  }
}
