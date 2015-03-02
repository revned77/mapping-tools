#!/usr/bin/python

from datetime import datetime
import PyRSS2Gen
import re
import urllib


def generate():
  update_regex = '<table(?:(?!</table>).)*Icon-NewMaps\.png.*?</table>.*?(?=&nbsp;)'
  date_regex = '\w+ \d\d?, 20\d\d'

  content = urllib.urlopen('http://vgmaps.com/NewsArchives/CurrentNews.htm').read()

  # hack: fix Feb 21 2015 typo
  content = content.replace('Feburary', 'February')

  items = []
  for update in re.findall(update_regex, content):
    update = update.decode('iso-8859-1')
    update = update.replace('../', 'http://vgmaps.com/')
    items.append(PyRSS2Gen.RSSItem(
        title='New Maps',
        link='http://vgmaps.com/',
        description=update,
        guid=str(hash(update)),
        pubDate=datetime.strptime(
            re.search(date_regex, update).group(0) + ' 19:00',
            '%B %d, %Y %H:%M')))

  rss = PyRSS2Gen.RSS2(
      title='VGMaps.com',
      link='http://vgmaps.com/',
      description='The Video Game Atlas',
      lastBuildDate=datetime.utcnow(),
      items=items)

  return rss.to_xml()


if __name__ == '__main__':
  print generate()
