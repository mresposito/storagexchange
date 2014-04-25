define ([
  "jquery",
  "typeahead"
], function($, type) {
  return function(el) {
    var universities = new Bloodhound({
      datumTokenizer: function(d) { return Bloodhound.tokenizers.whitespace(d.name); },
      queryTokenizer: Bloodhound.tokenizers.whitespace,
      limit: 10,
      prefetch: { 
        url: "/assets/data/universities.json",
        ttl: 0
      }
    });
    universities.initialize();
    $(el).typeahead(null, {
      displayKey: 'name',
      source: universities.ttAdapter()
    });
  }
});
