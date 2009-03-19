Mike FileUtils = Origin mimic do(

  Early = Origin mimic mimic!(Mixins Comparing) do(
    asText = method("EARLY")
    <=> = method(o, -1)
  )

  lastModified = method(path, 
    if(FileSystem exists?(path),
      time = java:io:File new(path) lastModified
      "#{time}" toDecimal,
      Early)
  )

  file? = method(n, FileSystem file?(n))
  directory? = method(n, FileSystem directory?(n))
  exists? = method(n, FileSystem exists?(n))

  eachParentOf = method(entry, block,
    parentOf = fn("workaround for FileSystem getParent bug", dir, 
      if(Regexp from("/|\\\\") match(dir),
        FileSystem parentOf(entry), nil))
    while(entry = parentOf(entry), block(entry)))
  
  mkdir = method(dir, FileSystem ensureDirectory(dir))
  
  MikeMixin = Origin mimic
  [ :file?, :directory?, :mkdir,
    ] each(name, MikeMixin cell(name) = cell(name))
  Mike mimic!(MikeMixin)

)

