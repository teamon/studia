package pea

object Algorithms {
    // Simulated Annealing implementation + parameters
    val SA = (td: Double) => new SimulatedAnnealing[TaskList, Int] with Alg {
        def Tmin = 0.01
        def Tmax = 100.0
        def Td = td

        def F(tasks: TaskList) = tasks.cost
        def S(tasks: TaskList) = TaskList(randomPermutation(tasks.list))
        def P(a: TaskList, b: TaskList, t: Double) = math.pow(math.E, -( F(b) - F(a) ) / t)

        def randomPermutation[A](a: Array[A]) = {
            val rand = new scala.util.Random
            val i1 = rand.nextInt(a.length)
            var i2 = rand.nextInt(a.length)
            while(i1 == i2){ i2 = rand.nextInt(a.length) }

            val b = a.clone
            val tmp = b(i1)
            b(i1) = b(i2)
            b(i2) = tmp
            b
        }
    }

    // Tabu Search implementation + parameters
    val TS = (n: Int) => new TabuSearch[TaskList, (Int, Int), Int] with Alg with Common {
        def N = n
        def Tsize = 7
        def F(tasks: TaskList) = tasks.cost
        def S(tasks: TaskList) = (0 until tasks.list.length).combinations(2).map { idx =>
          (TaskList(tasks.list.swapped(idx(0), idx(1))), (idx(0), idx(1)))
        }

        def P(tasks: TaskList, tabu: (Int, Int)) = {
          // println("P of " + tasks.list.toList + " and " + tabu._1 + " : " + tabu._2)
          // println("compare: " + (tasks.list.toList.indexWhere(e => e.index == tabu._1), tasks.list.indexWhere(e => e.index == tabu._2)))
          tasks.list.toList.indexWhere(e => e.index == tabu._1) < tasks.list.toList.indexWhere(e => e.index == tabu._2)
        }
    }
}

object App {
    import Algorithms._


    def readInstances(n: Int) = {
        def splitted[A](n: Int, list: List[A]): List[List[A]] = list match {
            case Nil => Nil
            case _ =>
                val (x, xs) = list.splitAt(n)
                x :: splitted(n, xs)
        }

        val source = io.Source.fromURL(getClass.getResource("/wt%d.txt" format n))
        val nums = source.getLines.flatMap(_.split(" +")).collect {
            case s if s.trim != "" => s.trim.toInt
        }.toList

        splitted(3*n, nums).map { group =>
            val parts = splitted(n, group)
            TaskList(parts(0).zip(parts(1)).zip(parts(2)).zipWithIndex.map {
                case (((x,y), z), i) => Task(i, x, z, y)
            }.toArray)
        }
    }

    def readOptimal(n: Int) = {
        val source = io.Source.fromURL(getClass.getResource("/wtopt%d.txt" format n))
        source.getLines.collect {
            case s if s.trim != "" => s.trim.toInt
        }.toList
    }

    def main(args: Array[String]){
        if(args.length < 2) sys.exit(-1)
        else {
            val n = args(0).toInt
            val k = args(1).toInt
            val instances = readInstances(n).zip(readOptimal(n)).take(k)

            // val tds = 0.99 :: 0.999 :: 0.9999 :: Nil
            val tds = 10 :: 100 :: 1000 :: Nil


            val results = tds.map { td =>
                print("%7s | " format td.toString)
                // val sa = SA(td)
                val sa = TS(td)

                val r = instances.zipWithIndex.collect { case ((tasks, optimal), inst) if optimal > 0 =>
                    println(" == Instance %s | optimal: %d | td: %d == " format (inst+1, optimal, td))

                    val x = (1 to 1)/*.par*/ map { i =>
                        val (time, res) = bench(sa(tasks))
                        val diff = (res.cost - optimal) * 100.0 / optimal

                        val c = if(diff == 0) Console.GREEN else Console.RED
                        println("%s%2d) %-120s [%5.2f%%] | %10d%s" format (c, i, res, diff, time, Console.RESET))
                        // print("%s.%s" format (c, Console.RESET))

                        (time, diff)
                    }

                    x
                }
                println()

                (td, r)
            }


            results.foreach { case (td, instances) =>
                val (time, diff) = ((0.0, 0.0) /: instances){
                    case ((t,d), it) =>
                        val (time, diff) = ((0.0,0.0) /: it){ case ((a,b),(c,d)) => (a+c, b+d) }
                        (t + time / it.length, d + diff / it.length)
                }

                val avgTime = time / instances.length
                val avgDiff = diff / instances.length

                println("td: %5f | time: %5.2f | diff: %5.2f" format (td.toFloat, avgTime, avgDiff))
            }
        }
    }

    def bench[T](f: => T): (Long, T) = {
        val start = System.currentTimeMillis
        val res = f
        val end = System.currentTimeMillis
        (end-start, res)
    }
}
