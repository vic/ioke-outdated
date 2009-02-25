use("mike")

Mike Runner = Origin mimic
Mike Runner do(
  run = method(argv,
    mike = Mike mimic
    app = mike application
    app loadMikeFile(mike)
    app optionParser parse(argv)
  )
);Mike Runner
