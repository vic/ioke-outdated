use("mike")

Mike Runner = Origin mimic
Mike Runner do(
  run = method(argv,
    app = Mike Application mimic
    app loadMikeFile
    app optionParser parse(argv)
  )
);Mike Runner
