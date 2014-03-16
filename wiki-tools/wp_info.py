import os
import re
import sys
import wp_query

class wp_info:

    def __init__(self,query):
        self.info = '' 
        q = wp_query.wp_query(query)
        self.infobox(q.get().split("\n"))
        if not self.info:
          q = wp_query.wp_query(query.title())
          self.infobox(q.get().split("\n"))
          #print self.info

    def infobox(self,txt,DEBUG=False):
        infobox = False
        braces = 0
        for line in txt:
            match = re.search(r'{{Infobox',line,flags=re.IGNORECASE)
            braces += len(re.findall(r'{{',line))
            braces -= len(re.findall(r'}}',line))
            if match:
                infobox = True
                line = re.sub(r'.*{{Infobox','{{Infobox',line)
            if infobox:
                if DEBUG: print "[%d] %s" % (braces,line.lstrip())
                self.info += line.lstrip() + "\n"
                if braces == 0:
                    break

# test cases TBD
#   Aerocar
#   GitHub
#   Heroku
#   Stack Overflow 

if __name__=="__main__":
    query = "University_of_Illinois_at_Urbana_Champaign"
    w = wp_info(query)
