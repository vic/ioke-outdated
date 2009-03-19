use("mike/task")
use("mike/fileUtils")

Mike Task do(

  Default = Mike Task mimic
  
  File = Mike Task mimic
  File do(
    needed? = method(
      !FileSystem exists?(name) || out_of_date?(timestamp)
    )
    
    timestamp = method(
      Mike FileUtils lastModified(name)
    )

    out_of_date? = method(stamp,
      prerequisites any?(p,
        cell(:p) cell?(:timestamp) && p timestamp > stamp
      )
    )
  )

  FileCreate = Mike Task mimic(File)
  FileCreate do(
    needed? = method( ! Mike FileUtils exists?(name) )
    timestamp = Mike FileUtils Early
  )

  Directory = Mike Task mimic(FileCreate)
  Directory do(
  
    task:def = method("Directory tasks are always created on the root namespace.",
      name, prerequisite, action, mike,
      root = mike mike:namespace root
      if(task = root task(name),
        ;; already exists, just enhance the task
        unless(task mimics?(self), error!("Expected #{name} to be a #{kind} task"))
        task addPrerequisite(cell(:prerequisite))
        task addAction(cell(:action)),
        ;; create the task
        task = self mimic(mike)
        task addPrerequisite(cell(:prerequisite))
        task addAction(fn(+r, +:k, FileSystem ensureDirectory(name)))
        task addAction(cell(:action))
        root task(name) = task
        ;; and a task for each parent directory.
        sub = task
        Mike FileUtils eachParentOf(name, fn(dir,
            unless(parent = root task(dir),
               parent = self mimic(mike)
               root task(dir) = parent
               parent addAction(fn(+r, +:k, FileSystem ensureDirectory(dir))))
            unless(parent mimics?(self), error!("Expected #{dir} to be a #{kind} task"))
            sub addPrerequisite(parent name)
            sub = parent
      )))
      task)
  )

  MikeMixin = Origin mimic do(

    task = Mike Task Default mike:def
    cell("task=") = Mike Task Default mike:set
    file = Mike Task File mike:def
    fileCreate = Mike Task FileCreate mike:def
    directory = Mike Task Directory mike:def
    defineTask = Mike Task mike:defTask
    
  )
  Mike mimic!(MikeMixin)
  
); Mike Tasks