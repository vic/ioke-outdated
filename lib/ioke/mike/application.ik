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

  topLevel = method(argv,
    loadMikeFile
    commandLine = optionParser parse(argv, 
      argUntilNextOption: false, includeUnknownOption: false)
    unless(commandLine unknownOptions empty?,
      error!(IOpt UnknownOption, text:
        "Unknown options: %[%s %]" format(commandLine unknownOptions)))
    unless(commandLine programArguments empty?, 
      error!("Don't know how to build tasks: %[%s %]" format(
          commandLine programArguments)))
    commandLine execute
  )
  
)

Mike Application OptionParser = IOpt mimic do (

  TaskAction = IOpt Action mimic do (
    initialize = method(task, iopt,
      init
      @receiver = task
      @iopt = iopt
      options << task name)

    cell(:documentation) = method(receiver documentation)
    call = macro(call activateValue(receiver cell(:call), receiver))
    arity = method(IOpt Action Arity from(receiver))
  )

  initialize = method(mike,
    @mike = mike
  )

  mike:get = cell("iopt:get")
  
  iopt:get = method(arg,
    if(o = mike:get(arg), return(o))
    if(task = @mike mike:namespace task(arg),
      action = TaskAction mimic(task, self)
      Origin with(option: task name, short: nil, immediate: nil, action: action)))

  printTasks = method("Show tasks",
    "TODO: walk tasks and print them here" println
    System exit
  )

  printHelp = method("Show usage.", @ println. System exit)

  banner = "Usage: mike [options] [task ...]"
  on("-h", "--help", :printHelp) priority = -10
  on("--tasks", :printTasks) priority = -9

)
