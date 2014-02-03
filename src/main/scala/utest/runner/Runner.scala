package utest.runner

import utest.toTestSeq
import sbt.testing._
import sbt.testing
import collection.mutable
import utest.framework.{TestTreeSeq, TestSuite, Result}
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}
import scala.concurrent.ExecutionContext.Implicits.global
import utest.util.{ArgParse, Tree}
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

class Runner(val args: Array[String],
             val remoteArgs: Array[String],
             val printer: utest.Formatter)
             extends sbt.testing.Runner{

  val results = new AtomicReference(List.empty[Tree[Result]])

  var total = 0

  var completeCounter = new AtomicInteger(0)

  def progressString = {
    s"${completeCounter.incrementAndGet()}/$total".padTo(8, ' ')
  }
  def completed = results.get.flatMap(_.toSeq)

  def tasks(taskDefs: Array[TaskDef]): Array[sbt.testing.Task] = {

    val path = args.lift(0)
                   .filter(_(0) != '-')
                   .getOrElse("")

    val ec = if (ArgParse.find("parallel", _.toBoolean, false)(args)){
      ExecutionContext.Implicits.global
    }else{
      utest.ExecutionContext.RunNow
    }
    for(taskDef <- taskDefs) yield {
      new utest.runner.Task(taskDef, path, printer, total += _, addResult, progressString, ec)
    }
  }

  @tailrec final def addResult(r: Tree[Result]): Unit = {
    val old = results.get()
    if (!results.compareAndSet(old, r :: old)) addResult(r)
  }

  def done(): String = {
    val header = "-----------------------------------Results-----------------------------------"
    val body = results.get
                      .map(printer.format)
                      .mkString("\n")

    Seq(
      header,
      body,
      s"Tests: ${completed.length}",
      s"Passed: ${completed.count(_.value.isSuccess)}",
      s"Failed: ${completed.count(_.value.isFailure)}"
    ).mkString("\n")
  }
}
