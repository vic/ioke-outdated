use("ispec")
use("xmlBuilder")

describe(XmlBuilder,

  describe("$", 
    it("should construct an xml", 
      pets = list(
        Origin with(name: "polo", kind: "Dog", about: "Nice white guy"),
        Origin with(name: "nemo", kind: "Fish", about: "Very famous")
      )
      xml = XmlBuilder mimic
      xml $pets(
        pets each(i, pet,
          $pet(id: i, 
            kind: pet kind,
            $name = pet name
            $ << pet about
          )
        )
      )
      xml asText should == "<pets>"+
      "<pet id=\"0\" kind=\"Dog\"><name>polo</name>Nice white guy</pet>"+
      "<pet id=\"1\" kind=\"Fish\"><name>nemo</name>Very famous</pet>"+
      "</pets>"
    )
  )

  describe("/", 
    it("should construct an xml", 
      pets = list(
        Origin with(name: "polo", kind: "Dog", about: "Nice white guy"),
        Origin with(name: "nemo", kind: "Fish", about: "Very famous")
      )
      xml = XmlBuilder mimic
      xml/pets(
        pets each(i, pet,
          xml/pet(id: i, 
            kind: pet kind,
            xml/name = pet name
            xml << pet about
          )
        )
      )
      xml asText should == "<pets>"+
      "<pet id=\"0\" kind=\"Dog\"><name>polo</name>Nice white guy</pet>"+
      "<pet id=\"1\" kind=\"Fish\"><name>nemo</name>Very famous</pet>"+
      "</pets>"
    )
  )


  describe("tag:arguments",
  
    it("should parse xml attributes",
      tag = XmlBuilder tag:arguments(ns:foo-bar => "bar", "foo.bar" => 2, :foo => 3, bar: 1, "bat.man": 22)
      tag body should be empty
      tag attr keys should == set("ns:foo-bar", "foo.bar", "foo", "bar","bat.man"))
    
    it("should evaluate pair on call context",
      foo = "moo"
      bar = "man"
      tag = XmlBuilder tag:arguments(foo => bar)
      tag attr should == dict(foo => bar))

    it("should take splat arguments and merge them in attributes",
      baz = "BAZ"
      more = dict("one" => 1)
      more["hello-there"] = "xml"
      tag = XmlBuilder tag:arguments(*more, bla)
      tag attr keys should == set("one", "hello-there")
      tag body first should mimic(Message)
      tag body first name should == :bla)

    it("should evaluate message for attributes when it ends in a terminator message",
      obtainDict = fn(n, d = dict(). n times(i, d["called#{i}"] = i). d)
      tag = XmlBuilder tag:arguments(obtainDict(3).)
      tag attr keys should == set("called0", "called1", "called2"))
    
  )
  
); XmlBuilder
