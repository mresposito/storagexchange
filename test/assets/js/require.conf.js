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
    sinon: EnvJasmine.testDir + "sinon-1.9.0",
    squire: EnvJasmine.testDir + "Squire"
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
    sinon: {
      exports: "sinon"
    }
  }
});
