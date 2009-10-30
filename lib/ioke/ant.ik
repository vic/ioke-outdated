;; Part of this program is based on
;;  Antwrap by Caleb Powell. http://rubyforge.org/projects/antwrap
Ant = Origin mimic 
Ant do (

  cell(:mimic) = macro("Create an Ant project witht the arguments given."
    call resendToValue(Project cell(:mimic), Project)
  )

  mixin! = macro("Ant Mixin methods expect the receiver to have an :ant cell holding an Ant Project,
    this macro just creates the project, stores it on the :ant cell in call ground
    and minxins Ant Mixin into the ground.
    "
    ant = call resendToValue(Project cell(:mimic), Project)
    call ground ant = ant
    call ground mimic!(Ant Mixin)
  )

  antHome = method(
    java:lang:System getenv("ANT_HOME")
  )
  
  javaHome = method(
    java:lang:System getenv("JAVA_HOME") || java:lang:System getProperty("java.home")
  )
  
  lib = Origin mimic do (
    loadJars! = method(antHome, javaHome,
      tools = FileSystem["#{javaHome}/{lib,../lib}/tools.jar"]
      unless(tools empty? use(tools first))
      FileSystem["#{antHome}/lib/*.jar"] each(jar, use(jar))
    )
    
    loaded? = method(@cell?("org:apache:tools:ant:Project"))
    
    importClasses! = method(
      import(:org:apache:tools:ant, :DefaultLogger, :Main, :Project, :RuntimeConfigurable, :Target, :UnknownElement)
      import org:xml:sax:helpers:AttributeListImpl
    )
  )

  applyBody = method(call, project, body,
    unless(body empty?,
      if(body length > 2, error!("Expected at most 2 positional arguments"))
      if(body length == 1, body = list('it, body first))
      gnd = call ground
      unless(gnd mimics?(Ant Mixin),
        gnd = call ground mimic
        gnd ant = project ant
        gnd prependMimic!(Ant Mixin))
      lexicalCode = LexicalBlock createFrom(body, gnd)
      lexicalCode call(project ant)
    )
  )

  defineTask = method(call, project, name, attributes, body,
    ensure(
      tag = Ant Tag mimic(name, project, attributes)
      parentTag = project tagStack last
      project tagStack << tag
      Ant applyBody(call, project, body)
      if(parentTag, parentTag add(tag))
      if(project tagStack length == 1, if(project declarative?, tag execute, tag))
      ,
      project tagStack = project tagStack butLast ; pop
    )
  )

  tagArguments = method(call,
    first = call arguments first
    name = first name asText
    arguments = first arguments
    if(#/^(`|cachedResult|:.*|internal.*Text)$/ match(name),
      name = first evaluateOn(call ground)
      arguments = call arguments rest,
      if(first arguments empty? && first next && first next name == :"-",
        next = first next
        while(next && next name == :"-",
          name = "#{name}-#{next arguments first name}"
          arguments = next arguments first arguments
          next = next next
        )
      )
    )
    name = name asText
    attributes = dict
    body = list
    arguments each(arg, 
      cond(
        ; keyword
        arg keyword?,
        attributes[:(arg name asText[0...-1])] = arg next evaluateOn(call ground),
        ; splatted
        arg name == :"*" && arg arguments length == 1, ;; splat
        arg = arg arguments first evaluateOn(call ground)
        case(arg, 
          Dict, arg each(pair, attributes[:(pair key asText)] = pair value),
          Pair, attributes[:(pair key asText)] = pair value,
          error!("Not splatable arguments: #{arg inspect}")),
        ; pair
        arg next && arg next name == :"=>" && arg next arguments length == 1,
        arg = arg evaluateOn(call ground)
        attributes[:(pair key asText)] = pair value,
        ;; body code
        body << arg)
    )
    Origin with(name: name, attributes: attributes, body: body)
  )

  Tag = Origin mimic do(
    initialize = method(tagName, antProject, attributes,
      @tagName = tagName
      @projectWrapper = antProject
      @project = antProject project
      @unknownElement = createUnknownElement(project, tagName)
      addAttributes(attributes)
    )

    createUnknownElement = method(project, tagName,
      element = Ant lib UnknownElement new(tagName)
      element setProject(project)
      element setOwningTarget(Ant lib Target new)
      element setTaskName(tagName)
      element getRuntimeConfigurableWrapper
      if(projectWrapper antVersion >= 1.6,
        element setTaskType(tagName)
        element setNamespace("")
        element setQName(tagName))
      element
    )

    addAttributes = method(attributes,
      unless(attributes, return)
      wrapper = Ant lib RuntimeConfigurable new(unknownElement, unknownElement getTaskName)
      if(projectWrapper antVersion >= 1.6,
        attributes each(pair, 
          applyToWrapper(wrapper, pair, fn(k, v, wrapper setAttribute(k, v)))),
        unknownElement setRuntimeConfigurableWrapper(wrapper)
        attributeList = Ant lib AttributeListImpl new
        attributes each(pair, 
          applyToWrapper(wrapper, pair, fn(k, v, attributeList addAttribute(k, "CDATA", v))))
        wrapper setAttributes(attributeList))
    )

    applyToWrapper = method(wrapper, pair, block,
      if(pair value mimics?(List), 
        error!("You cannot give a list as argument."))
      key = pair key asText
      value = pair value asText
      if(key == "pcdata", 
        wrapper addText(value),
        block(key, value))
    )

    add = method(child, 
      unknownElement addChild(child unknownElement)
      unknownElement getRuntimeConfigurableWrapper addChild(
        child unknownElement getRuntimeConfigurableWrapper
      )
    )

    executed? = false

    execute = method(
      unknownElement maybeConfigure
      unknownElement execute
      @executed? = true
      return
    )
  )
  
  Project = Origin mimic do(
    initialize = method(+:options,
      self prependMimic!(Ant Mixin)
      @ant = self
      antHome = options[:ant_home] || Ant antHome
      if(!Ant lib loaded? && antHome,
        javaHome = options[:java_home] || Ant javaHome
        Ant lib loadJars!(antHome, javaHome))
      unless(Ant lib loaded?, Ant lib importClasses!)
      antVersion = #/({version}\d+\.\d+)/ match(Ant lib Main getAntVersion()) version
      @antVersion = antVersion toDecimal
      @tagStack = list

      @project = Ant lib Project new
      project setName(options[:name] || "")
      project setDefault("")
      basedir = options[:basedir] || java:lang:System getProperty("user.dir")
      project setBasedir(basedir)
      project init
      if(options[:declarative] nil?, 
        @declarative? = true, 
        @declarative? = options[:declarative])
      default_logger = Ant lib DefaultLogger new
      default_logger setMessageOutputLevel(2)
      outstr = options[:outputstr] || java:lang:System field:out
      default_logger setOutputPrintStream(outstr)
      errorstr = options[:errorstr] || java:lang:System field:err
      default_logger setErrorPrintStream(errorstr)
      default_logger setEmacsMode(false)
      project addBuildListener(default_logger)
    )

    name = method(project getName)
    basedir = method(project getBaseDir getAbsolutePath)
    do! = macro(Ant applyBody(call, self, call arguments))
  )

  Mixin = Origin mimic do(

    cell("%") = method("Obtain value for ant property or reference", property,
      if(ant project getProperty(property), "#{it}", ant project getReference(property))
    )

    cell("$") = method("Obtain text value with ${} ant properties replaced.", value,
      "#{ant project replaceProperties(value)}" ; be sure to return an ioke text
    )
    
    cell("<") = macro("Define an Ant task",
      tag = Ant tagArguments(call)
      Ant defineTask(call, ant, tag name, tag attributes, tag body)
    )
    
  )

)
