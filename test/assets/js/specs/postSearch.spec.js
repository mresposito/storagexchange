require([
  "jquery",
  "squire",
  "sinon"
], function($, Squire, sinon) {

  var $el = $("<html></html>");
  var inj = new Squire();

  describe("A PostSearch view should", function() {
    var callback = sinon.stub().returns([]);
    inj
    .mock("search/elastic", {getPosts: callback})

    .require(["views/post"], function(PostSearch) {
      it("should get posts at beginning", function() {
        var Post = new PostSearch ({ el: $el });
        expect(callback.calledOnce).toBe(true);
      });
    });
  });
});
