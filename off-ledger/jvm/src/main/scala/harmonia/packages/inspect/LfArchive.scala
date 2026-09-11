package harmonia.packages.inspect

import cats.effect.IO
import com.digitalasset.daml.lf.archive.{DamlLf, DamlLf2 as LF}
import java.nio.file.{Files, Path}
import java.security.MessageDigest
import java.util.jar.Manifest
import java.util.zip.ZipFile
import scala.jdk.CollectionConverters.*
import scala.util.Using

final case class LfPackage(id: String, version: String, proto: LF.Package):
  def name(index: Int): String = proto
    .getInternedDottedNames(index)
    .getSegmentsInternedStrList
    .asScala
    .map(i => proto.getInternedStrings(i))
    .mkString(".")
  def string(index: Int): String = proto.getInternedStrings(index)
  def resolve(tpe: LF.Type, depth: Int = 0): LF.Type =
    require(depth < 64, "LF type reference exceeds the supported depth (or contains a cycle)")
    if tpe.hasInternedType then resolve(proto.getInternedTypes(tpe.getInternedType), depth + 1)
    else if tpe.hasTapp then
      val function = resolve(tpe.getTapp.getLhs, depth + 1)
      val argument = tpe.getTapp.getRhs
      if function.hasBuiltin then
        LF.Type.newBuilder().setBuiltin(function.getBuiltin.toBuilder.addArgs(argument)).build()
      else if function.hasCon then
        LF.Type.newBuilder().setCon(function.getCon.toBuilder.addArgs(argument)).build()
      else throw IllegalArgumentException("Unsupported LF type application")
    else tpe

/** Reads the public LF protobuf schema. ZIP and protobuf resources are bounded before
  * interpretation.
  */
object LfArchive:
  private val expandedLimit = 32 * 1024 * 1024
  def read(dar: Path): IO[LfPackage] = IO.blocking {
    require(Files.size(dar) <= InspectDar.maximumBytes, "DAR exceeds the 8 MiB input limit")
    Using.resource(new ZipFile(dar.toFile)) { zip =>
      val entries = zip.entries().asScala.toVector
      require(
        entries.size <= 2048 && entries.map(_.getName).distinct.size == entries.size,
        "DAR has too many entries or duplicate names"
      )
      require(
        entries.forall(_.getSize >= 0) && entries.map(_.getSize).sum <= expandedLimit,
        "DAR exceeds the 32 MiB expanded size limit"
      )
      // Validate actual expansion, including unused dependencies, without trusting ZIP declarations.
      var expanded = 0L
      val buffer = new Array[Byte](8192)
      entries.filterNot(_.isDirectory).foreach { entry =>
        Using.resource(zip.getInputStream(entry)) { stream =>
          var count = stream.read(buffer)
          while count != -1 do
            expanded += count
            require(expanded <= expandedLimit, "DAR exceeds the 32 MiB expanded size limit")
            count = stream.read(buffer)
        }
      }
      def bytes(name: String): Array[Byte] =
        val entry = Option(zip.getEntry(name))
          .getOrElse(throw IllegalArgumentException(s"DAR is missing $name"))
        Using.resource(zip.getInputStream(entry)) { stream =>
          val value = stream.readNBytes(expandedLimit + 1)
          require(value.length <= expandedLimit, "DAR entry exceeds the expanded size limit")
          value
        }
      val manifest = new Manifest(new java.io.ByteArrayInputStream(bytes("META-INF/MANIFEST.MF")))
      val main = Option(manifest.getMainAttributes.getValue("Main-Dalf"))
        .getOrElse(throw IllegalArgumentException("DAR manifest is missing Main-Dalf"))
      decode(bytes(main))
    }
  }

  def decode(bytes: Array[Byte]): LfPackage =
    require(bytes.length <= expandedLimit, "DALF exceeds the expanded size limit")
    val archive = DamlLf.Archive.parseFrom(bytes)
    require(archive.getHashFunction == DamlLf.HashFunction.SHA256, "Unsupported DALF hash function")
    val digest = MessageDigest
      .getInstance("SHA-256")
      .digest(archive.getPayload.toByteArray)
      .map(b => f"${b & 0xff}%02x")
      .mkString
    require(archive.getHash == digest, "DALF payload does not match its package identity")
    val payload = DamlLf.ArchivePayload.parseFrom(archive.getPayload)
    require(
      payload.hasDamlLf2 && Set("1", "2").contains(payload.getMinor) && payload.getPatch == 0,
      "Supported package format is Daml-LF 2.1 or 2.2"
    )
    LfPackage(digest, s"2.${payload.getMinor}", LF.Package.parseFrom(payload.getDamlLf2))
