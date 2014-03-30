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
    }
  }
});
