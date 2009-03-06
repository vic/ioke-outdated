
XmlBuilder = Origin mimic do(

  tag:arguments = macro(
    body = []
    attributes = dict()
    call arguments each(a,
      cond(
        ; a keyword message
        a keyword?,
        name = a name asText[0...-1]
        value = a next evaluateOn(call ground)
        attributes[name asText] = value,
        
        ; a pair or object keyword
        #/^(=>|:)$/ match(a last name asText) && a last arguments length == 1,
        value = a last arguments first evaluateOn(call ground)
        name = []
        if(a last prev prev,
          ; a sequence of messages
          msg = a do(last prev prev -> nil)
          msg walk(m, name << m name)
          name = name join
          ,
          ; a single message
          msg = a do(last prev -> nil)
          name = msg evaluateOn(call ground))
        attributes[name asText] = value,
        
        ; *keys, message.
        a last terminator? || (a name == :"*" && a next nil? && a arguments length == 1),
        value = if(a last terminator?, a, a arguments first) evaluateOn(call ground)
        case(value,
          Dict,
          value each(pair, attributes[pair key asText] = pair value),
          Pair, 
          attributes[value key asText] = value value,
          error!("Invalid attribute: #{value}")),

        ; else it is part of the body
        body << a
    ))
    (attributes => body) do( attr = cell(:key). body = cell(:value) )
  )
  
); XmlBuilder
