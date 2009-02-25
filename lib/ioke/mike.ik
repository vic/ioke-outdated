Mike = Origin mimic

use("mike/namespace")
use("mike/coreTasks")
use("mike/application")

Mike do (

  initialize = method(application Application mimic, namespace Namespace mimic,
    @application = application
    namespace extend(self)
  )

  mimic!(CoreTasks)
  
)
