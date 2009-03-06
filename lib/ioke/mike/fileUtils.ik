Mike FileUtils = Origin mimic do(
  
  Early = Origin mimic do(<=> = method(o, -1))

  file? = method(n, FileSystem file?(n))
  directory? = method(n, FileSystem directory?(n))
  exists? = method(n, FileSystem exists?(n))

  eachParentOf = method(entry, block,
    parentOf = fn("workaround for FileSystem getParent bug", dir, 
      if(Regexp from("/|\\\\") match(dir),
        FileSystem parentOf(entry), nil))
    while(entry = parentOf(entry), block(entry)))
  
  mkdir = method(dir, FileSystem ensureDirectory(dir))
  
  MikeMixin = Origin mimic do(
    
  )
  Mike mimic!(MikeMixin)

)

