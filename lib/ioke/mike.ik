Mike = Origin mimic

use("mike/namespace")
use("mike/coreTasks")
use("mike/application")

Mike do(

  initialize = method(application Application mimic(self), namespace Namespace mimic,
    @application = application
    namespace extend(self)
  )

)
