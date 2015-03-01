#!/usr/bin/python

"""A script to assemble an image from layers stored in individual PNG files

Note that this script depends on the ImageMagick command line tools, so make sure that 'convert'
is in your path and works.

Layers must be PNG files stored in a single directory. Ordering and layer styles are defined by
filenames. Example usage:

$ ls layers_dir/
0.png 1.png 2.50%.png 3.linear.png
$ ./assemble-layers.py layers_dir/ assembled.png

This will stack the layers and output the result to assembled.png. Layer #2 uses 50% alpha
transparency and layer #3 uses the 'linear dodge' style.

Filenames must follow the given structure separated by '.':
1) Layer number
2) (optional) Alternates - see below
3) (optional) Layer style, one of: linear, multiply, divide
4) (optional) Alpha transparency percentage
5) 'png'

The first layer does not respect style or transparency. It may be forced to fully transparent with
the -t flag.

Alternates allow for branching individual layers without requiring the whole directory to be
duplicated. Simply set the alternates to any sequence of the letters A-Z and then use the -a option
to select one of them. Layers with alternates specified are only used if matched. Layers without
are always used. The following example results in skipping layer #3:

$ ls layers_dir/
0.png 1.png 2.AB.png 3.B.png
$ ./assemble-layers.py -a A layers_dir/ assembled.png
"""


import argparse
import os
import re
import subprocess
import sys


def run_command(layers_dir, filename, alternate, transparent_bg):
  files = os.listdir(layers_dir)
  files = filter(lambda f: f.endswith('.png'), files)
  files = [f.split('.') for f in files]
  files = filter(lambda f: f[0].isdigit(), files)
  files = sorted(files, key=lambda f: int(f[0]))

  if not alternate:
    alternates = [] 
    for f in files:
      if re.match(r'^[A-Z]+$', f[1]):
        alternates.extend(list(f[1]))
    if alternates:
      alternates = sorted(set(alternates))
      print 'Must select an alternate from ' + str(alternates)
      exit(1)

  command = ['convert']
  while re.match(r'^[A-Z]+$', files[0][1]) and not alternate in files[0][1]:
    files = files[1:]
  if transparent_bg:
    command.extend([
        '(', '.'.join(files[0]), '-alpha', 'on', '-channel', 'a', '-evaluate', 'set', '0',
        '-channel', 'rgb,sync', ')'])
  else:
    command.append('.'.join(files[0]))

  for f in files[1:]:
    style = f[1:-1]
    if style and re.match(r'^[A-Z]+$', style[0]):
      if not alternate in style[0]:
        continue;
      style = style[1:]

    command.extend(['(', '.'.join(f), '-transparent', '#FF00FF'])
    if style and re.match(r'^[0-9]+%$', style[-1]):
      command.extend([
          '-channel', 'a', '-evaluate', 'multiply', '0.%s' % style[-1][:-1], '-channel',
          'rgb,sync'])
      style = style[0:-1]
    command.extend([')'])

    if not style:
      command.extend(['-compose', 'src-over'])
    elif style[0] == 'linear':
      command.extend(['-compose', 'linear-dodge'])
    elif style[0] == 'divide':
      command.extend(['-compose', 'divide_src'])
    elif style[0] == 'multiply':
      command.extend(['-compose', 'multiply'])
    else:
      print 'Unknown layer style: %s' % style
      exit(2)
    command.append('-composite')

  command.append(os.path.abspath(filename))
  # print ' '.join(command)
  subprocess.call(command, cwd=layers_dir)


def main():
  parser = argparse.ArgumentParser()
  parser.add_argument('-a', '--alternate')
  parser.add_argument('-t', '--transparent_bg', action='store_true')
  parser.add_argument('layers_dir')
  parser.add_argument('output_filename')
  args = parser.parse_args()

  if not args.layers_dir or not args.output_filename:
    parser.print_help()
    sys.exit(0)
  
  run_command(args.layers_dir, args.output_filename, args.alternate, args.transparent_bg)


if __name__ == '__main__':
  main()
