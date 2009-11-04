use("callable")
use("ispec")

describe("Callable",


  describe("activate",

    it("should not invoke mexee's call unless given an explicit argument list",
      o = Origin mimic
      o mimic!(Callable)
      invoked = list
      o call = fnx(+a, invoked << true. a)
      o should == o
      invoked should be empty
    )

    it("should invoke mixee's call cell",
      o = Origin mimic
      o call = method(+a, a)
      o mimic!(Callable)
      o(1, 2) should == [1,2]
      o() should be empty
    )
    
  )


  describe("activatable",

    it("should be true by default",
      cell(:Callable) cell(:activatable) should be true
    )

  )
  
)
