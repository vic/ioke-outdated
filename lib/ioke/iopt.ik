IOpt = Origin mimic
IOpt do(

  documentation = "Command line processing the Ioke way.
    
  "
  
  initialize = method(
    @flags = set()
    @actions = set()
    @initialize = method())
  
  cell("[]") = method(option,
    if(@cell?(option) && @cell(option) mimics?(IOpt Action),
      @cell(option),
      actions find(a, a handles?(option))))

  cell("[]=") = method(+args,
    unless(value = args last,
      args[0..-2] each(k, if(@cell?(k) && @cell(k) mimics?(IOpt Action),
          @cell(k) flags each(f, @removeCell!(f))))
      return)
    unless(cell(:value) mimics?(IOpt Action),
      if(cell(:value) kind?("Text"),
        unless(value = @[value], error!("Option not defined #{value}")),
        value = IOpt Action mimic(cell(:value))))
    actions << value
    args[0..-2] each(k, value flags << k. @cell(k) = value)
    value)

  on = dmacro(
    [>forOption]
    self[forOption],

    [>flag, +body]
    
    if(cell(:flag) kind?("Text"),
      flag = list(flag)
      while(body first && body first last == body first &&
        body first name == :"internal:createText",
        a = body first evaluateOn(call ground, call receiver)
        if(#/^--?[\\w-]+/ match(a),
          flag << a
          body = body rest, break))
      handler = body inject('fn, m, a, m << a) evaluateOn(
        call ground, call receiver)
      return(self[*flag] = handler))

    receiver = cell(:flag)

    settingCell = fn(name,
      handler = lecrox(
        receiver cell(name) = call argAt(0))
      docs = "Set #{name}"
      args = unless(isQ?(name), "value")
      IOpt Action mimic(handler, docs, args))

    callingMethod = fn(name,
      handler = lecrox(call resendToValue(receiver cell(name), receiver))
      docs = if(receiver cell?(name), 
        receiver cell(name) documentation)
      args = unless(isQ?(name), 
        if(receiver cell?(name),
            if(receiver cell(name) cell?(:argumentsCode),
              receiver cell(name) argumentsCode)))
      IOpt Action mimic(handler, docs, args))

    applyingValue = fn(value,
      handler = lecrox(call resendToValue(cell(:value), receiver))
      docs = cell(:value) documentation
      args = if(cell(:value) cell?(:argumentsCode),
          cell(:value) argumentsCode)
      IOpt Action mimic(handler, docs, args))

    applyingMessage = fn(msg,
      gnd = call ground mimic
      handler = lecrox(
        args = call evaluatedArguments
        gnd it = args first
        msg evaluateOn(gnd, receiver))
      IOpt Action mimic(handler))

    cellMsg? = fn(msg, msg name == :"@" &&
      msg next && msg next next nil?) 
    
    isQ? = fn(name, name asText[-1] == "?"[0])

    flagName = fn(name, 
      name = name asText replaceAll("_", "-")
      #/[^A-Za-z0-9-]/ allMatches(name) each(m, 
        name = name replace(m, ""))
      #/[A-Z]+[a-z]*/ allMatches(name) each(m,
        name = name replace(m, "-#{m lower}"))
      name)

    action = nil
    body each(handler,
      flag = nil

      if(handler name == :"internal:createText", ;; explicit flag
        flag = handler deepCopy
        flag -> nil
        flag = flag evaluateOn(call ground)

        what = handler next
        if(cellMsg?(what),
          action = settingCell(what next name),
          what = what evaluateOn(call ground)

          if(cell(:what) kind?("Symbol"),
            action = callingMethod(what),
            if(cell(:what) kind?("Message"),
              action = applyingMessage(what),
              action = applyingValue(cell(:what))))
          ),

        ;; dont have explicit flag, built it
        if (cellMsg?(handler),
          what = handler next name
          name = flagName(what)
          flag = if(isQ?(what), "--[no-]#{name}", "--#{name}")
          action = settingCell(what), 
          what = handler evaluateOn(call ground)
          if(cell(:what) kind?("Symbol"),
            name = flagName(what)
            flag = if(isQ?(what), "--[no-]#{name}", "--#{name}")
            action = callingMethod(what),
            error!("Invalid argument")))
      )
      ;; set the flag
      unless(action, error!("Invalid arguments"))

      self[flag] = action)
    
    action) ; on


  parse = method(argv,
    @argv = argv
    ; an array to store not handled input
    @programArguments = list()
    ; first convert the strings to actions.
    ary = argv
    callValues = list()
    callDatas = dict()
    until(ary empty?,
      if(action = self[ary first],
        callValues << action
        res = action consume(ary)
        callDatas[action] = res
        ary = res remnant
        res removeCell!(:remnant),
        programArguments << ary first
        ary = ary rest))

    ;; sort them by priority to be executed
    callValues sort each(action,
      res = callDatas[action]
      action callReceiver ||= self
      action send(:call, *(res named_args), *(res keyed_args))))

  Action = Origin mimic do(
    
    initialize = method(action, docs nil, args nil, plevel 0, receiver nil,
      @flags = set()
      @default = true
      @priority = plevel
      @callReceiver = cell(:receiver)
      @documentation = docs || cell(:action) documentation
      @argumentsCode = args || if(cell(:action) cell?(:argumentsCode), cell(:action) argumentsCode)
      @body = cell(:action))

    cell(:<=>) = method(other, priority <=> other priority)

    cell(:call) = macro(call activateValue(@cell(:body), callReceiver))

    cell("priority=") = method("Set the option priority. 
      Default priority level is 0.
      Negative values are higher priority for options that
      must be processed before those having priority(0).
      Positive ones are executed just after all priority(0)",
      value,
      @cell(:priority) = value
      self)
    
    handles? = method(option,
      flags any?(flag, handleData(flag, option)))

    handleData = method(flag, option,
      d = dict()
      m = #/=/ match(option)
      if (m, option = m beforeMatch. d[:value] = m afterMatch)
      cond(
        flag == option, d[:value] ||= @cell(:default),
        d empty? && #/^-\\w$/ match(flag) && 
          m = #/^#{flag}/ match(option),
        d[:value] = m afterMatch,
        m = #/\\[(.*?)\\]/ match(flag),
        case(option,
          m beforeMatch + m afterMatch,
          d[:value] ||= @cell(:default),
          m beforeMatch + m[1] + m afterMatch,
          d[:value] ||= if(@cell?(:alternative), 
            @cell(:alternative), @cell(:default) not())))
      d key?(:value) and(d))

    cell("argumentsCode=") = method(code,
      if(code == "..." || code == "", code = nil)
      cell(:argumentsCode) = code
      i = Origin with(names: [], keywords: [], rest: nil, krest: nil)
      unless(code, @argumentsInfo = i. return(self))
      dummy = Message fromText("fn(#{code}, nil)")
      dummy = dummy evaluateOn(dummy)
      i names = dummy argumentNames
      i keywords = dummy keywords
      i rest = if(match = #/\\+([^: ,]+)/ match(code), :(match[1]))
      i krest = if(match = #/\\+:([^ ,]+)/ match(code), :(match[1]))
      @argumentsInfo = i
      self)

    consume = method(argv,
      res = Origin mimic
      data = nil. flag = nil
      flags find(f, if(data = handleData(f, argv first), flag = f))
      unless(flag, error!("I cant handle #{argv first}"))

      info = argumentsInfo
      remnant = argv rest
      currentKey = nil
      args = list()
      klist = list()
      kmap = dict()

      shouldContinue = fn(arg, 
        cond(
          #/^--?/ match(arg), false, ;; found next flag
          currentKey, true, ;; expecting value for a key arg
          !(info names empty?) && !(info keywords empty?), 
          (klist length + args length) < 
          (info keywords length + info names length),
          info names empty? && info keywords empty?, 
          info krest || info rest,
          info names empty?,
          info krest || klist length < info keywords length,
          info keywords empty?, 
          info rest || args length < info names length,
          false))

      isKeyword = fn(arg,
        if(m = #/^([\\w+]):/ match(arg),
          :(m[1]) => if(m afterMatch empty?, nil, m afterMatch)))

      idx = remnant findIndex(arg,
        cond(
          !shouldContinue(arg), true,
          
          (p = isKeyword(arg)) && ;; process keyword arg
          (info krest || info keywords include?(p key)),
          if(kmap key?(p key),
            error!("Keyword #{p key} already specified"),
            kmap[p key] = p value
            if(p value, klist << p key, currentKey = p key))
          nil, ;; continue processing
          
          currentKey, ;; set last keyword if missing value
          klist << currentKey
          kmap[currentKey] = arg
          currentKey = nil,
            
          args << arg
          nil)) || -1

      if(args empty? && kmap empty? && 
        !(@cell(:body) argumentsCode empty?), 
        args = list(data[:value]))
      
      res flag = flag
      res remnant = remnant[idx..-1]
      res named_args = args
      res keyed_args = kmap

      res)

    handle = method(+argv,
      res = consume(argv)
      res result = send(:call, *(res named_args), *(res keyed_args))
      res)

  ); Action
)
  