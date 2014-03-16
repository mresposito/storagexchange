import json
import sys
sys.path.insert(0, './wiki-tools')
from wp_info import wp_info
import re

class scrape_info:
  
  def __init__(self):
    f = open("./list_of_schools.txt")
    self.list_of_schools = []
    for line in f:
      self.list_of_schools.append(line.strip())
    print self.list_of_schools
    self.jsonify_schools()

  def jsonify_schools(self):
    parsed_dict = {}
    for school in self.list_of_schools:
      parsed_dict[school] = {}
      w = wp_info(school)
      field_list = w.info.strip(" ").split("|")
      print field_list
      #TODO: Need to parse infobox stuff properly

if __name__ == "__main__":
  s = scrape_info()
