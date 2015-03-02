# Mapping tools

This is a collection of the few reusable tools I've written to help with constructing maps for
[VGMaps.com](http://vgmaps.com). Probably not particularly helpful to anyone other than me, but nevertheless.

### palette-converter
This tool transforms images based on source and target palette inputs. I've used it to give my NES maps consistent
palettes, and also for many of the Inverted Castle maps for SotN. It's very rigid by necessity; inputs have to be
essentially identical and have no ambiguity in color translation.

### assemble-layers
I used this script for SotN mapping because I found that Photoshop's PSD files were horribly bloated. Some of the
PSDs were >100MB when the layers saved as PNGs were only 1-2MB. So I went ahead and saved them as PNGs and wrote
this helper script to put them back together using ImageMagick.

This also gives me peace of mind for the future since I'm not locked into proprietary software and I know that the
specifics of the layering algorithms won't change underneath me (when upgrading Photoshop awhile back I found that
my images ended up rendering very slightly differently).

### tile-slicer
This is a tool I wrote almost 10 years ago and rewrote a couple of times afterward. It slices up an image into
unique tiles and then reassembles them. It's marginally useful for finding mistakes in tile-based maps or for
making sweeping changes.
