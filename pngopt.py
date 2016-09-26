#!/usr/bin/env python3

import argparse
import multiprocessing
import os
import shutil
import subprocess
import sys
import tempfile
import uuid

from concurrent.futures import ThreadPoolExecutor


parser = argparse.ArgumentParser()
parser.add_argument('-f', '--filter', default=[0], type=lambda s: [int(f) for f in s.split(',')],
    help='png filter method(s) for pngout and pngcrush')
parser.add_argument('-t', '--threads', type=int, default=multiprocessing.cpu_count(),
    help='Number of parallel threads to use')
parser.add_argument('-r', '--run-tools', type=lambda s: s.split(','),
    help='Run only the given tools')
parser.add_argument('filenames', nargs=argparse.REMAINDER)
args = parser.parse_args()


class Task(object):
  def __init__(self, filename):
    self.filename = filename
    self.result_path = os.path.join(tempfile.gettempdir(), uuid.uuid4().hex + '.png')
    self.result_size = None
    self.done = False

  def name(self):
    return 'cp'

  def commandline(self):
    return ['cp', self.filename, self.result_path]

  def execute(self):
    print('Running %s for %s' % (self.name(), self.filename))
    try:
      with open(os.devnull, 'w') as devnull:
        subprocess.call(self.commandline(), stdout=devnull, stderr=devnull)
      self.result_size = get_size(self.result_path)
    except Exception as e:
      print('%s failed on %s: %s' % (
          self.name(), self.filename, str(e)))
    self.done = True

  def cleanup(self):
    if self.result_path:
      try:
        os.remove(self.result_path)
      except OSError:
        pass


class PngoutTask(Task):
  def __init__(self, filter, *args):
    super(PngoutTask, self).__init__(*args)
    self.filter = filter
  def name(self):
    if self.filter == 0:
      return 'pngout'
    return 'pngout(%d)' % self.filter
  def commandline(self):
    return ['pngout', '-f%d' % self.filter, self.filename, self.result_path]


class AdvpngTask(Task):
  def name(self):
    return 'advpng'
  def execute(self):
    shutil.copy(self.filename, self.result_path)
    return Task.execute(self)
  def commandline(self):
    return ['advpng', '-z', '-4', self.result_path]


class OptipngTask(Task):
  def name(self):
    return 'optipng'
  def commandline(self):
    return ['optipng', '-o7', '-out', self.result_path, self.filename]


class PngcrushTask(Task):
  def __init__(self, filter, *args):
    super(PngcrushTask, self).__init__(*args)
    self.filter = filter
  def name(self):
    if self.filter == 0:
      return 'pngcrush'
    return 'pngcrush(%d)' % self.filter
  def commandline(self):
    return ['pngcrush', '-f', '%d' % self.filter, self.filename, self.result_path]


class ZopflipngTask(Task):
  def name(self):
    return 'zopflipng'
  def commandline(self):
    return ['zopflipng', '-m', '--filters=0me', '--lossy_8bit', '--lossy_transparent', self.filename, self.result_path]


def get_size(filename):
  return os.path.getsize(filename)


class Optimization(object):
  def __init__(self, filename):
    self.filename = filename

  def prepare(self):
    print('----- Preparing ' + self.filename + ' -----')

    original_size = get_size(self.filename)
    print('Initial size: %d bytes' % original_size)

    if sys.platform == 'darwin':
      resource_fork_path = self.filename + '/..namedfork/rsrc'
      resource_fork_size = get_size(resource_fork_path)
      if resource_fork_size:
        open(resource_fork_path, 'w').close()
        print('Erased %d byte resource fork' % resource_fork_size)

    with open(os.devnull, 'w') as devnull:
      subprocess.call(['optipng', '-o0', '-strip', 'all', self.filename],
          stdout=devnull, stderr=devnull)
    new_size = get_size(self.filename)
    if new_size < original_size:
      print('Erased %d bytes of optional metadata' % (original_size - new_size))
      print('New size: %d bytes' % new_size)
    print('')

    self.tasks = []
    self.tasks.append(ZopflipngTask(self.filename))
    self.tasks.extend([PngoutTask(f, self.filename) for f in args.filter])
    self.tasks.append(AdvpngTask(self.filename))
    self.tasks.append(OptipngTask(self.filename))
    self.tasks.extend([PngcrushTask(f, self.filename) for f in args.filter])
    if args.run_tools:
      self.tasks = [t for t in self.tasks if t.name() in args.run_tools]

  def finish(self):
    print('----- Results for ' + self.filename + ' -----')
    original_size = get_size(self.filename)
    best = None
    smallest_size = original_size
    summaries = []
    for task in self.tasks:
      if task.result_size:
        if task.result_size < smallest_size:
          smallest_size = task.result_size
          best = task
        if task.result_size < original_size:
          delta = '-%d' % (original_size - task.result_size)
        else:
          delta = '=='
        summaries.append('%s: %s' % (task.name(), delta))

    print(', '.join(summaries))
    if best:
      shutil.move(best.result_path, self.filename)
      print('%s wins\nFinal size: %d bytes' % (best.name(), best.result_size))
    else:
      print('Could not reduce size any further')
    print('')


def main():
  if not args.filenames:
    parser.print_help()
    sys.exit(0)

  for filename in args.filenames:
    if not (os.access(filename, os.W_OK) and os.path.isfile(filename)):
      print('Unable to access ' + filename)
      sys.exit(2)

  optimizations = []
  tasks = []
  for filename in args.filenames:
    optimization = Optimization(filename)
    optimization.prepare()
    optimizations.append(optimization)
    tasks.extend(optimization.tasks)

  print('----- Optimizing -----')
  with ThreadPoolExecutor(max_workers=args.threads) as executor:
    list(executor.map(lambda task: task.execute(), tasks))
  print('')

  for optimization in optimizations:
    optimization.finish()
  for task in tasks:
    task.cleanup()

if __name__ == '__main__':
  main()
