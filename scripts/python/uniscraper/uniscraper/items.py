# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

from scrapy.item import Item, Field

class UniItem(Item):
    name = Field()
    website = Field()
    colors = Field()
    logo = Field()
    locationID = Field()
    lat = Field() 
    lng = Field()
    city = Field()
    state = Field()
    address = Field()
    zip = Field()
