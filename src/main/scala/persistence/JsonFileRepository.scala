package persistence

// ai-assisted: #5
// why: first time writing a generic Try-based file-IO abstraction in
// Scala 3; wanted an idiomatic shape for loadAll/saveAll/save

import java.nio.file.{Files, Path, Paths}
import scala.util.Try

/** Shared file-IO logic for every JSON-backed repository. Concrete
  * repositories only need to say how to turn one `T` into a `ujson.Value`
  * and back. The try/catch, file creation, and directory handling live
  * here once, via one shared abstract class: the DRY point for the
  * persistence layer (S1-13).
  *
  * Every method that touches the filesystem is wrapped in Try (S1-12).
  * A missing file, a locked file, or malformed JSON all surface as a
  * Failure the UI can show as a friendly message instead of crashing.
  */
abstract class JsonFileRepository[T](fileName: String) extends Repository[T]:

  protected def toJson(item: T): ujson.Value
  protected def fromJson(value: ujson.Value): T

  private val dataDir: Path  = Paths.get("data")
  private val filePath: Path = dataDir.resolve(fileName)

  private def ensureFile(): Try[Unit] = Try {
    if !Files.exists(dataDir) then Files.createDirectories(dataDir)
    if !Files.exists(filePath) then Files.writeString(filePath, "[]")
  }

  override def loadAll(): Try[List[T]] =
    for
      _        <- ensureFile()
      raw      <- Try(Files.readString(filePath))
      parsed   <- Try(ujson.read(raw))
      items    <- Try(parsed.arr.map(fromJson).toList)
    yield items

  override def saveAll(items: List[T]): Try[Unit] =
    for
      _   <- ensureFile()
      arr = ujson.Arr(items.map(toJson)*)
      _   <- Try(Files.writeString(filePath, ujson.write(arr, indent = 2)))
    yield ()

  override def save(item: T): Try[Unit] =
    for
      existing <- loadAll()
      _        <- saveAll(existing :+ item)
    yield ()
