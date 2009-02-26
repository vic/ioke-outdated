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
    ;; TODO: seek on parent directories
    if(FileSystem file?("Mikefile"),
      "Mikefile"))
  
)

Mike Application OptionParser = IOpt mimic do (

  initialize = method(mike,
    @mike = mike
  )

  mike:ioption = cell("iopt:ion")
  mike:at = cell("[]")
  
  iopt:ion = method(arg,
    if(o = mike:ioption(arg), return(o))
    if(task = @mike lookupTask(arg),
      action = IOpt Action mimic mimic!(task) do(init)
      action iopt = self
      action receiver = task
      action flags << task name
      ;action documentation = task documentation
      ;action argumentsCode = task argumentsCode
      Origin with(flag: task name, long: true, immediate: nil, action: action)))

  cell("[]") = method(option,
    unless(o = iopt:ion(option), return nil)
    if(o cell?(:action), o action, mike:at(option)))

  printTasks = method(
    "TASKS here" println
    System exit
  )
  
  printUsage = method("Print this help",
    @println. System exit
  )

  banner = "Usage: mike [options] [task ...]"
  on("-h", "--help", :printUsage) priority = -10
  on("--tasks", :printTasks) priority = -9

)
