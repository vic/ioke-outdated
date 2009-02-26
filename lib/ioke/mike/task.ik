Mike Task = Origin mimic
Mike Task do(

  mike:processArgs = '(
    prerequisite = nil
    if(name kind?("Pair"),
      prerequisite = name value
      name = name key)
    unless(name kind?("List"), name = mike:namespace splitName(name))
    mike = namespace(name[0..-2])
    unless(mike, error!("No such namespace: #{name[0..-2]}"))
    name = name last
    action ||= nil
    unless(body nil? || body empty?,
      action = body inject(Message fromText("fn"), m, a, m << a)
      action = action evaluateOn(call ground, call receiver)))
  
  mike:set = syntax(''(dmacro(
    [>name, >action]
    body = nil
    'mike:processArgs
    `self task:def(name, cell(:prerequisite), cell(:action), mike))))

  mike:def = syntax(''(dmacro(
    [>name, +body]
    'mike:processArgs
    if(task = mike mike:namespace task(name),
      task addPrerequisite(cell(:prerequisite))
      task addAction(cell(:action)),
      task = `self task:def(name, cell(:prerequisite), cell(:action), mike))
    task)))

  mike:defTask = syntax(''(dmacro(
    [>taskKind, >name, +body]
    'mike:processArgs
    taskKind task:def(name, cell(:prerequisite), cell(:action), mike)
  )))
  
  task:def = method(name, prerequisite, action, mike,
    task = self mimic(mike)
    mike mike:namespace task(name) = task
    task prependMimic!(self) kind = self kind
    task addPrerequisite(cell(:prerequisite))
    task addAction(cell(:action))
    task)

  initialize = method(
    @initialize = method(mike,
      @mike = mike
      @alreadyInvoked? = false
      @actions = list()
      @prerequisites = list())
  )
  
  addPrerequisite = method(prerequisite,
    unless(cell(:prerequisite), return(self))
    addP = fn(p, 
      case(cell(:p) kind,
        "List", p each(p, addP(p)),
        prerequisites << cell(:p)))
    addP(cell(:prerequisite))
    self)
  
  addAction = method(action,
    unless(cell(:action), return(self))
    if(cell(:action) documentation,
      if(documentation, 
        @documentation = "%s\n%s" format(documentation, cell(:action) documentation),
        @documentation = cell(:action) documentation))
    if(cell(:action) cell?(:argumentsCode),
      @argumentsCode = cell(:action) argumentsCode)
    actions << cell(:action)
    self)

  call = macro(
    if(alreadyInvoked?, return(self), @alreadyInvoked? = true)
    prerequisites each(p, 
      case(cell(:p) kind,
        or("Symbol", "Text"),
        task = mike mike:namespace lookup(p)
        unless(task, error!("Don't know how to build task: #{p}"))
        task call,
        cell(:p) call))
    actions each(action,
      call activateValue(cell(:action), it: self))
    self)

  needed? = true
)
