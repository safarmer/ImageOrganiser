package au.com.nullpointer.images.organise

import com.drew.imaging.FileType
import com.drew.imaging.FileTypeDetector
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.util.stream.Collectors.toList

fun main(args: Array<String>) {
  check(args.isNotEmpty()) { "Missing directories" }
  println("Optimising images in ${args[0]}")

  val roots = args.map { root ->
    if (root.startsWith("~")) {
      root.replace("~", System.getProperty("user.home"))
    } else root
  }

  for (root in roots) {
    val details = Files.walk(Paths.get(root))
        .filter { !Files.isDirectory(it) }
        .map(::getFileDetails)
        .collect(toList())

    println("Images:")
    details.filter { it.type != FileType.Unknown }.forEach(::println)
    println("\nOther:")
    details.filter { it.type == FileType.Unknown }.forEach(::println)
  }
}

fun getFileDetails(path: Path): FileDetails {
  val details = FileDetails.forPath(path)

  val type = Files.newInputStream(path).use { ins ->
    if (ins.available() < 8) {
      return@use FileType.Unknown
    }
    FileTypeDetector.detectFileType(BufferedInputStream(ins))
  }

  if (type == FileType.Unknown) {
    return details
  }


  val metadata = ImageMetadataReader.readMetadata(path.toFile())
  val exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)

  return details.copy(type = type, exifCreated = exif?.dateOriginal?.toInstant())
}

data class FileDetails(
    val name: String,
    val size: Long,
    val created: Instant,
    val updated: Instant,
    val type: FileType = FileType.Unknown,
    val exifCreated: Instant? = null
) {
  companion object {
    fun forPath(path: Path): FileDetails {
      val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
      return FileDetails(
          name = path.toString(),
          size = attrs.size(),
          created = attrs.creationTime().toInstant(),
          updated = attrs.lastModifiedTime().toInstant()
      )
    }
  }
}