# Scrapy settings for uniscraper project
#
# For simplicity, this file contains only the most important settings by
# default. All the other settings are documented here:
#
#     http://doc.scrapy.org/en/latest/topics/settings.html
#

BOT_NAME = 'uniscraper'

SPIDER_MODULES = ['uniscraper.spiders']
NEWSPIDER_MODULE = 'uniscraper.spiders'

DEPTH_LIMIT = 2
