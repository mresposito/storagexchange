require([
  "jquery",
  "sinon",
  "views/post"
], function($, sinon, Post) {

  describe("PostSearch view", function() {

    var post, request;
    var findPostsSpy, resetBoardSpy;

    var stubText = function(text) {
      post.getTextSearchBox = sinon.stub().returns(text);
    };
    var stubFilters = function(filters) {
      post.filterValues = sinon.stub().returns(filters);
    };

    beforeEach(function() {
      jasmine.Ajax.useMock();

      post = new Post({
        el: $('<html><span class="search">hello</span></html>')
      });

      // Respond to the server
      request = mostRecentAjaxRequest();
      request.response(TestResponses.search.success);
      // install spies
      findPostsSpy = sinon.spy(post, "findPosts");
      resetBoardSpy = sinon.spy(post, "resetBoard");
      // install stubs
      stubText("");
      stubFilters({});
    });

    it("calls right URL", function() {
      expect(request.url).toEqual("/api/search/post");
    });
    it("calls find posts on constructor", function() {
      post.updateBoard()
      expect(findPostsSpy.calledWith({})).toBe(true);
    });
    it("starts loading 0 post", function() {
      expect(post.startingPost).toBe(0);
    });
    it("doen't update starting pint after update board", function() {
      post.updateBoard()
      expect(post.startingPost).toBe(0);
    });
    describe("Search box", function() {
      it("calls with {} if query less that three char", function() {
        stubText("he")
      });

      it("calls with porpe if query more that three char", function() {
        stubText("hello")
        post.searchCallback()
        expect(findPostsSpy.calledWith({
          query: { term: "hello" }
        })).toBe(true);
      });
      it("resets the starting counter", function() {
        post.loadMorePosts()
        post.searchCallback()
        expect(post.startingPost).toBe(0);
      });
    });
    describe("Incrementing posts", function() {
      it("calls find posts", function() {
        post.loadMorePosts()
        expect(findPostsSpy.called).toBe(true);
      }); 
      it("updates the counter", function() {
        post.loadMorePosts();
        expect(post.startingPost).toEqual(post.stepIncrement);
      });
      it("updates the counter twice", function() {
        post.loadMorePosts();
        post.loadMorePosts();
        expect(post.startingPost).toEqual(post.stepIncrement * 2);
      });
      it("does not resets the board", function() {
        expect(resetBoardSpy.calledOnce).toBe(false);
      });
      it("sends the proper json", function() {
        post.loadMorePosts()
        expect(findPostsSpy.calledWith({
          offset: {
            start: 15,
            limit: 10
          }
        })).toBe(true);
      });
    });
  });
});
