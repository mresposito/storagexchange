from scrapy.contrib.spiders import CrawlSpider, Rule
from scrapy.contrib.linkextractors.sgml import SgmlLinkExtractor
from scrapy.selector import Selector
from uniscraper.items import UniItem
from pygeocoder import Geocoder

class UniSpider(CrawlSpider):
    name = "uni"
    allowed_domains = ["en.wikipedia.org"]
    start_urls = [
        "http://en.wikipedia.org/wiki/Lists_of_American_institutions_of_higher_\
        education"
    ]
    rules = [
        Rule(SgmlLinkExtractor(allow=(r'List_of_colleges_and_universities_in'), 
            deny=(r'(_University|University_)')), follow=True),
        Rule(SgmlLinkExtractor(allow=(r'(University_|_University)')), 
            callback='parse_item')
    ]

    def parse_item(self, response):
        sel = Selector(response)
        sites = sel.xpath('//body/div')
        item = UniItem()
        item['name'] = self._parse_name(sel)
        item['website'] = self._parse_website(sel)
        item['logo'] = self._parse_logo(sel)
        item['colors'] = 'None'

        # Geographic fields
        gdict = self._parse_geodata(item['name'])
        item['lat'] = gdict['lat']
        item['lng'] = gdict['lng']
        item['city'] = gdict['city']
        item['state'] = gdict['state']
        item['zip'] = gdict['zip']
        item['address'] = gdict['address']
        if self._all_fields_populated(item):
            return item

    def _parse_name(self, sel):
        title = sel.xpath('//*[@id="firstHeading"]/span/text()').extract()
        uni_name = title[0]
        # Sometimes the Wikipedia title has "(State)" or "(Country)" after
        # the university name. This checks for and removes that.
        if uni_name.find('(') > 0:
            uni_name = uni_name[:uni_name.find('(')].strip()
        return uni_name

    def _parse_website(self, sel):
        website = sel.xpath('//table[@class="infobox vcard"]/\
            tr/th[contains(text(),"Website")]/following-sibling::node()/\
            a/text()').extract()
        if len(website) > 0 and ".edu" in website[0]:
            return website[0]
        else:
            return 'None'

    def _parse_logo(self, sel):
        # Logo is always second row in infobox.
        logo = sel.xpath('//table[@class="infobox vcard"]/tr[2]\
                /td/a/img/@src').extract()
        if len(logo) > 0:
            return logo[0][2:]
        else:
            return 'None'

    def _parse_geodata(self, uni_name):
        gdict = dict()
        results = Geocoder.geocode(uni_name)
        result = results[0]
        # Round lat and lng to 4 decimal places
        gdict['lat'] = round(result.coordinates[0], 4)
        gdict['lng'] = round(result.coordinates[1], 4)
        gdict['zip'] = str(result.postal_code)
        gdict['address'] = str(result.formatted_address)
        gdict['city'] = str(result.city)
        gdict['state'] = str(result.state)
        return gdict

    def _parse_colors(self, sel):
        colors = sel.xpath('//table[@class="infobox vcard"]/tr/\
                th[descendant::a[contains(@href,"/wiki/School_colors")]]/\
                following-sibling::node()/a/text()').extract()
        if len(colors) == 0:
            colors = sel.xpath('//table[@class="infobox vcard"]/tr/\
                th[descendant::a[contains(@href,"/wiki/School_colors")]]/\
                following-sibling::node()/p/text()').extract()
        if len(colors) == 0:
            colors = sel.xpath('//table[@class="infobox vcard"]/tr/ \
                th[descendant::a[contains(@href,"/wiki/School_colors")]]/\
                following-sibling::node()/text()').extract()
        if len(colors) > 0:
            return colors[0]
        else:
            return 'None'

    def _all_fields_populated(self, item):
        print item.items()
        for field, val in item.items():
            if field != 'colors' and val == 'None':
                return False
        return True
