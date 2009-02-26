use("ispec")
use("mike")

describe(Mike,

  describe("task",

    it("should create a task",
      m = Mike mimic
      t = m task(:foo, "Fooing", bar, it)
      t should mimic(Mike Task Default)
      t name should == "foo"
      t documentation should == "Fooing")

    it("should create a task with prerequisites",
      m = Mike mimic
      t = m task(:foo => :bar, "Fooing", bar, it)
      t should mimic(Mike Task Default)
      t name should == "foo"
      t prerequisites should == list(:bar))

    it("should return an existing task if only name is provided",
      m = Mike mimic
      t = m task(:foo, "Fooing", bar, it)
      m task(:foo) should == t)

    it("should enhance an existing task",
      m = Mike mimic
      t = m task(:foo, "Fooing", bar, it)
      t actions length should == 1
      t prerequisites should be empty
      m task(:foo => :bar)
      t actions length should == 1
      t prerequisites should == list(:bar)
      m task(:foo, "Do something more", more)
      t actions length should == 2
      t prerequisites should == list(:bar))

  )

  describe("namespace",
    
    it("should return the current namespace if no args given",
      m = Mike mimic
      m namespace should == m)

    it("should return the current namespace if given . as name",
      m = Mike mimic
      m namespace(".") == m)

    it("should return the parent namespace if given .. as name",
      m = Mike mimic
      n = m namespace(:foo) namespace(:bar) namespace("..")
      n should == m namespace(:foo)
      n = m namespace(:bat) namespace(:man) namespace("...:foo:bar")
      n should == m namespace(:foo) namespace(:bar))

    it("should return the root namespace if given / as name",
      m = Mike mimic
      n = m namespace(:foo) namespace(:bar) namespace("/")
      n should == m
      n = m namespace(:foo) namespace(:bar) namespace("/:foo")
      n should == m namespace(:foo)
    )

    it("should return a new namespace if just given a name",
      m = Mike mimic
      m namespace(:foo) should mimic(m))

    it("should return an existing namespace",
      m = Mike mimic
      n = m namespace(:foo) namespace(:bar)
      n should == m namespace("foo:bar"))
    
    it("should execute the given code on the new namespace",
      m = Mike mimic
      v = m namespace(:foo, yo = true)
      m cell?(:yo) should be false
      m namespace(:foo) yo should be true)
    
  )

  describe("lookupTask",
    
    it("should return nil for a not defined task", 
      m = Mike mimic
      m lookupTask(:foo) should be nil)
    
    it("should search for a task on parent namespaces",
      m = Mike mimic
      t = m task(:foo)
      m lookupTask(:foo) should == t
      m lookupTask("foo:bar") should be nil
      t = m namespace(:foo) task(:bar)
      m lookupTask("foo:bar") should == t
      m namespace(:foo) lookupTask("bat:man") should be nil
      t = m namespace(:bat) task(:man)
      m namespace(:foo) lookupTask("bat:man") should == t
      t = m namespace(:foo) namespace(:bat) task(:man)
      m namespace(:foo) lookupTask("bat:man") should == t
      m namespace(:bat) lookupTask(:foo) should == m task(:foo)
      
      t = m namespace(:cool, task(:aid))
      m lookupTask("cool:aid") should == t
    )
    
  )

);Mike

describe(Mike Namespace,
  
  describe("splitName", 
    it("should split on :",
      Mike Namespace splitName("foo:bar:baz") should == ["foo", "bar", "baz"])
    
    it("should not split on windows paths",
      Mike Namespace splitName("foo:\\bar:baz") should == ["foo:\\bar", "baz"])

    it("should not split on url likes",
      Mike Namespace splitName("foo://bar:baz") should == ["foo://bar", "baz"])
  )

  

); Mike Namespace

describe(Mike Task Default,

  describe("call",
    it("should set cell('it') to the task being executed",
      r = nil
      t = Mike Task Default mimic(nil)
      t actions << fn(r = it)
      t call
      r should == t)

    it("should not be executed more than once",
      r = 0
      t = Mike Task Default mimic(nil)
      t actions << fn(r++)
      t should not be alreadyInvoked
      t call
      t should be alreadyInvoked
      r should == 1
      t call
      t should be alreadyInvoked
      r should == 1)

    it("should execute prerequisites", 
      r = 0
      t = Mike Task Default mimic(nil)
      t prerequisites = list(fn(r++), fn(r++))
      t call(9)
      r should == 2)
    
  );call

);Mike Task
