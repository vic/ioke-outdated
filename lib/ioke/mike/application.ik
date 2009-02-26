use("iopt")

Mike Application = Origin mimic
Mike Application do (

  initialize = method(mike Mike mimic(self),
    @executionDir = System currentDirectory
    @mike = mike
    @optionParser = OptionParser mimic(mike)
  )
  
  loadMikeFile = method(
    @mikefile = findMikefile
    unless(mikefile, error!("No Mikefile found on #{executionDir}"))
    msg = Message fromText(FileSystem readFully(mikefile))
    msg evaluateOn(mike)
  )

  findMikefile = method(fromDir nil,
    ;; TODO: implement Runtime workingDirectory or the like.
    if(FileSystem file?("Mikefile"),
      "Mikefile"))
  
)

Mike Application OptionParser = IOpt mimic do (

  TaskAction = IOpt Action mimic do (
    initialize = method(task, iopt,
      init
      @receiver = task
      @iopt = iopt
      flags << task name)

    cell(:documentation) = method(receiver documentation)
    call = macro(call activateValue(receiver cell(:call), receiver))
    arity = method(arityFrom(receiver argumentsCode))
  )

  initialize = method(mike,
    @mike = mike
  )

  mike:ioption = cell("iopt:ion")
  mike:at = cell("[]")
  
  iopt:ion = method(arg,
    if(o = mike:ioption(arg), return(o))
    if(task = @mike mike:namespace task(arg),
      action = TaskAction mimic(task, self)
      Origin with(flag: task name, long: true, immediate: nil, action: action)))

  cell("[]") = method(option,
    unless(o = iopt:ion(option), return)
    if(o cell?(:action), o action, mike:at(option)))

  printTasks = method("Show tasks",
    "TODO: walk tasks and print them here" println
    System exit
  )

  printHelp = method("Show usage.", @ println. System exit)

  banner = "Usage: mike [options] [task ...]"
  on("-h", "--help", :printHelp) priority = -10
  on("--tasks", :printTasks) priority = -9

)
