package helloscala.algorithm

import org.scalatest.{MustMatchers, WordSpec}

class AlgorithmTest extends WordSpec with MustMatchers {

  "Sum" should {
    import Sums._
    "recursive" in {
      recursiveSum(Vector(1, 2, 3)) mustBe 6
      recursiveSum(Seq()) mustBe 0
      recursiveSum(Array(7, 8, 9)) mustBe 24
    }
  }

  "Search" should {
    import Searchs._
    "genericBinarySearch" in {
      genericBinarySearch(Array(1, 2, 3, 4, 5, 6, 7, 8, 9), 9) mustBe 8
    }
  }

  "Sort" should {
    import Sorts._
    "genericQuicksort" in {
      genericQuicksort(Vector(3, 7, 7, 9, 1, 8, -32, 9, 6, 4, 2)) mustBe Vector(-32, 1, 2, 3, 4, 6, 7, 7, 8, 9, 9)
      genericQuicksort(Array(3, 7, 7, 9, 1, 8, -32, 9, 6, 4, 2)) mustBe Vector(-32, 1, 2, 3, 4, 6, 7, 7, 8, 9, 9)
      genericQuicksort(IndexedSeq(1L, 2L)) mustBe Vector(1L, 2L)
    }
  }

  "Graph" should {
    import Graphs._
    "breadthFirstSearch" in {
      val graph = Map(
        "you" -> Vector("alice", "bob", "claire"),
        "bob" -> Vector("anuj", "peggy"),
        "alice" -> Vector("peggy"),
        "claire" -> Vector("thom", "jonny"),
        "anuj" -> Vector(),
        "peggy" -> Vector(),
        "thom" -> Vector(),
        "jonny" -> Vector()
      )
      val result = breadthFirstSearch(graph, "you", (person: String) => person.lastOption.contains('m'))
      println(result)
    }

    "dijkstraSearch" in {
      val graph = Map[String, Map[String, Int]](
        "start" -> Map(("a", 6), ("b", 2)),
        "a" -> Map(("c", 7), ("fin", 9)),
        "b" -> Map(("a", 3), ("c", 5)),
        "c" -> Map(("fin", 3)),
        "fin" -> Map()
      )
      val result = dijkstraSearch(graph, "start", "fin")
      println(result)
    }
  }

  "greedys" in {
    val states = Set("cq", "sc", "gz", "js", "zj", "bj", "sh", "tj", "sd", "hn")
    val stations = Map(
      "kone" -> Set("cq", "sc", "gz"),
      "ktwo" -> Set("gz", "zj", "hn", "js"),
      "kthree" -> Set("js", "tj", "sh", "sd", "bj"),
      "kfour" -> Set("tj", "bj"),
      "kfive" -> Set("sd", "js")
    )

    val results = Greedys.greedys(stations, states)
    println(results)
    results must not be empty
  }

}
