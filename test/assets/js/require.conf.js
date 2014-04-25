require.config({
  baseUrl: EnvJasmine.rootDir,
  paths: {
    mocks:      EnvJasmine.mocksDir,
    specs:      EnvJasmine.specsDir,

    // Libraries
    jquery: EnvJasmine.libDir + "jquery-2.0.3.min",
    underscore: EnvJasmine.libDir + "underscore-min",
    backbone: EnvJasmine.libDir + "backbone-min",
    elastic: EnvJasmine.libDir + "elasticsearch.min",
    typeahead: EnvJasmine.libDir + "typeahead.bundle.min",
    sinon: EnvJasmine.testDir + "helpers/" + "sinon-1.9.0"
  },
  shim: {
    jquery: {
      exports: "$"
    },
    underscore: {
      exports: "_"
    },
    backbone: {
      deps: ["underscore"],
      exports: "Backbone"
    }, 
    typeahead: ["jquery"],
    sinon: {
      exports: "sinon"
    }
  }
});
