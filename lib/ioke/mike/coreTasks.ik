use("mike/task")

Mike Task Default = Mike Task mimic
Mike Task Default do(
  
)

Mike Task File = Mike Task mimic
Mike Task File do(
  
  needed? = method(
    
  )
)

Mike Task FileCreate = Mike Task mimic
Mike Task FileCreate do(
  needed? = method(
    
  )
)


Mike Task Directory = Mike Task mimic
Mike Task Directory do(
  needed? = method(
    
  )
)

Mike CoreTasks = Origin mimic do(

  task = Mike Task Default mike:def
  cell("task=") = Mike Task Default mike:set
  file = Mike Task File mike:def
  fileCreate = Mike Task FileCreate mike:def
  directory = Mike Task Directory mike:def

)