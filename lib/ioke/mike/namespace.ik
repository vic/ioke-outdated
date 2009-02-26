Mike Namespace = Origin mimic
Mike Namespace do(

  initialize = method(parent nil, name nil,
    @childs = dict()
    @tasks = dict()
    @parent = parent
    @root = if(parent, parent root, self)
    @scope = if(parent, parent scope + [name], if(name, [name], []))
    if(parent, parent childs[name] = self)
  )

  splitName = method("Split an string into an array of scope elements",
    name, name asText split(Regexp from(":(?![:/\\\\]+)")))

  lookup = method("Search for a task on this namespace and up", name,
    ns = self
    while(ns, if(t = ns task(name), return(t), ns = ns parent)))

  task = method("Obtain a task defined on this namespace", name, 
    scope = splitName(name asText)
    name = scope last
    ns = at(scope[0..-2])
    if(ns, ns tasks[name]))
  
  cell("task=") = method("Register a task on this namespace", name, task,
    scope = splitName(name asText)
    name = scope last
    scope = scope[0..-2]
    ns = at(scope)
    ns tasks[name] = task
    task name = if(ns scope empty?, name, "%[%s:%]%s" format(ns scope, name))
    task)

  at = method("Obtain the namespace at the given scope", scope,
    if(scope nil? || scope empty?, return(self))
    namespace = nil
    searching = self
    while(namespace nil? && searching,
      namespace = searching
      scope takeWhile(n, namespace = namespace childs[n])
      searching = searching parent)
    namespace)

  extend = method("Extend a mike instance with namespace related methods
    and bind it to this namespace.", mike,
    @mike = mike
    mike mike:namespace = self
    mike mimic!(MikeMixin))

  MikeMixin = Origin mimic do(
    
    namespace = dmacro(
      [] self,
      
      [>name]
      name ||= list()
      scope = if(name mimics?(List), name, mike:namespace splitName(name))
      unless(ns = mike:namespace at(scope),
        name = scope last
        unless(ns = mike:namespace at(scope[0..-2]),
          error!("No such namespace: #{scope}"))
        ns = Namespace mimic(ns, name)
        ns parent mike mimic(ns parent mike application, ns))
      ns mike,
      
      [>name, body]
      mike = namespace(name)
      body evaluateOn(mike))
    

    lookupTask = method(name, mike:namespace lookup(name))
  );MikeMixin
  
)
