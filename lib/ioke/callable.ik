Mixins Callable = Origin mimic
Mixins Callable do(
  
  activatable = true
  activate = method(context, message, receiver,
    if(message activation?,
      msg = '(call())
      msg arguments = message arguments
      msg evaluateOn(context, cell(:self)),
      cell(:self))
  )

)
